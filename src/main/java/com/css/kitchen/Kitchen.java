package com.css.kitchen;

import com.css.Displayer;
import com.google.common.collect.Lists;
import java.time.Clock;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Represents a {@link Kitchen} which has multiple {@link Shelf shelves}. <br>
 * <br>
 * The {@link Shelf shelves} are sorted by their coefficient. This allows us to simply choose the
 * first suitable {@link Shelf} when an {@link Order} is processed. <br>
 * <br>
 * Besides each {@link Shelf} holding its {@link Order orders}, the {@link Kitchen} also maintains a
 * global queue of {@link Order orders} so that it can quickly return the {@link Order orders} in a
 * LIFO manner. We achieve amortized O(logn) for both {@link #processOrder(Order)} and {@link
 * #pickUpOrder(long)}.
 */
@Singleton
public class Kitchen {

  public final List<Shelf> shelves;

  private ReentrantLock lock = new ReentrantLock();

  private final Clock clock;
  private final Displayer displayer;

  /** 
   * A ordered queue by the expiration time help to consume the orders in the proper way.
   * The PriorityQueue in each shelf is for locating the order with O(logn).
  */ 
  private final PriorityBlockingQueue<Order> ordersQueue =
    new PriorityBlockingQueue<Order>(20, Comparator.comparingLong(order -> order.processed().expiration));

  @Inject
  Kitchen(Set<Shelf> shelves, Clock clock, Displayer displayer) {
    this.shelves = Lists.newArrayList(shelves);
    this.shelves.sort(Comparator.comparingInt(shelf -> shelf.coefficient));
    shelves.forEach(shelf -> shelf.callback = order -> displayer.display(order.name + " is spoiled.", Kitchen.this));
    this.clock = clock;
    this.displayer = displayer;
  }

  /** Processes the {@link Order}. */
  public synchronized void processOrder(Order order) {
    lock.lock();
    try {
      // Find the first available shelf.
      Optional<Shelf> shelfOptional = shelves.stream().
        filter(shelf -> shelf.checkOrder(order)).
        findFirst();

      // If there's no available shelf, the order is wasted.
      if (!shelfOptional.isPresent()) {
        display(order.name + " is wasted.");
      }
      else {
        // Add the order to the shelf.
        shelfOptional.get().add(order);
        ordersQueue.add(order);
        display(order.name + " is processed.");
      }
    } finally {
      lock.unlock();
    }
  }

  /**
   * @return the picked up {@link Order} or {@code null} if there is no order after {@code timeout}
   *     seconds.
   */
  @Nullable
  public Order pickUpOrder(long timeout) {
    long currentTime = clock.instant().getEpochSecond();
    @Nullable Order order = null;

    while (true) {
      // Remove the oldest order in the queue.
      try {
        order = ordersQueue.poll(timeout, TimeUnit.SECONDS);
      } catch (InterruptedException ignored) {
      }

      // If the order is good, then remove it from the shelf.
      if (order == null) return null;
      lock.lock();
      try {
        if (order.canBePickedUpAtTime(currentTime)) {
          order.processed().shelf.remove(order);
          display(order.name + " is picked up.");
          break;
        } else {
          //if it's the last proccessed order and wasted, 
          //it will remain on the shelf forever unless explicitly remove from here
          order.processed().shelf.count();
        }
      } finally {
        lock.unlock();
      }
    }
    return order;
  }

  private void display(String event) {
    displayer.display(event, this);
  }
}
