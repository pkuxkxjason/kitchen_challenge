package com.css.kitchen;

import com.google.common.annotations.VisibleForTesting;
import java.time.Clock;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * Represents a {@link Shelf}. <br>
 * <br>
 * The {@link Shelf} holds its {@link Order orders} in a priority queue based on their expiration.
 * Expired {@link Order orders} and picked up {@link Order orders} are removed lazily to achieve
 * amortized O(logn) for {@link #add(Order)} and {@link #remove(Order)}.
 */
public class Shelf {

  public final String name;
  public final int coefficient;

  private final Set<Order.Type> acceptedOrderTypes;
  private final int capacity;
  private final Clock clock;
  private final Evaluator evaluator;
  private final Queue<Order> orders =
      new PriorityQueue<>(Comparator.comparingLong(order -> order.processed().expiration));

  @Nullable Callback callback;

  /** The number of orders that have been marked as picked up but have not been removed yet. */
  private int numPickedUp;

  public Shelf(
      String name,
      Set<Order.Type> acceptedOrderTypes,
      int capacity,
      int coefficient,
      Clock clock,
      Evaluator evaluator) {
    this.name = name;
    this.acceptedOrderTypes = acceptedOrderTypes;
    this.capacity = capacity;
    this.coefficient = coefficient;
    this.clock = clock;
    this.evaluator = evaluator;
  }

  /** @return the current count of orders. */
  public synchronized long count() {
    removeStaleOrdersIfNeeded(clock.instant().getEpochSecond());

    return orders.size() - numPickedUp;
  }

  public synchronized Double getNormalizedValue(Order order) {
    long currentTime = clock.instant().getEpochSecond();
    long orderAge = currentTime - order.processed.createdAt;
    long value = evaluator.getValue(this, order, orderAge);
    return (Double) 1.0d * value/order.shelfLife;
  }

  public synchronized void info(StringBuilder infoBuilder) {
    infoBuilder.append(this.name).append("::");
    for (Order order: orders()) {
      infoBuilder.append(order.name).append(":");
      infoBuilder.append(String.format("%.2f",getNormalizedValue(order))).append(",");
    }
    infoBuilder.append("\n");
  }

  /** @return {@code true} iff the {@link Shelf} can hold this {@link Order}. */
  synchronized boolean checkOrder(Order order) {
    removeStaleOrdersIfNeeded(clock.instant().getEpochSecond());

    return acceptedOrderTypes.contains(order.type) && orders.size() - numPickedUp < capacity;
  }

  /** Adds an {@link Order}. */
  synchronized void add(Order order) {
    long currentTime = clock.instant().getEpochSecond();
    long expiration = evaluator.getExpiration(this, order, currentTime);
    order.processed = new Order.Processed(this, expiration, currentTime);
    orders.add(order);
  }

  /** Removes an {@link Order}. */
  synchronized void remove(Order order) {
    long currentTime = clock.instant().getEpochSecond();
    removeStaleOrdersIfNeeded(currentTime);
    long orderAge = currentTime - order.processed.createdAt;
    // We just mark this order as picked up but we don't need to remove it right away.
    long value = evaluator.getValue(this, order, orderAge);
    order.pickedUp = new Order.PickedUp(value);
    numPickedUp++;
  }



  /**
   * This method is inefficient and should only be used for testing.
   *
   * @return the current {@link Order orders} on this {@link Shelf}.
   */
  @VisibleForTesting
  synchronized Order[] orders() {
    return orders
        .stream()
        .filter(order -> order.canBePickedUpAtTime(clock.instant().getEpochSecond()))
        .toArray(Order[]::new);
  }

  /**
   * Removes all expired {@link Order} or {@link Order} has been picked up based on {@code
   * currentTime}.
   *
   * @param currentTime current time in seconds.
   */
  private void removeStaleOrdersIfNeeded(long currentTime) {
    do {
      @Nullable Order order = orders.peek();
      if (order == null) break;

      // Remove picked up order and update pickup count.
      if (order.pickedUp != null) {
        orders.remove(order);
        numPickedUp--;
        continue;
      }

      // Remove expired order.
      if (currentTime >= order.processed().expiration) {
        orders.remove(order);
        if (callback != null) callback.onOrderExpired(order);
        continue;
      }
      break;
    } while (true);
  }

  /**
   * Implements these methods to evaluate the expiration and value of an {@link Order} on this
   * {@link Shelf}.
   */
  public interface Evaluator {

    /**
     * @return the value of the {@link Order} {@code order} after it has been on the {@link Shelf}
     *     {@code shelf} for {@code orderAge} seconds.
     */
    long getValue(Shelf shelf, Order order, long orderAge);

    /**
     * @return the expiration of the {@link Order} {@code order} if it's to be put on {@link Shelf}
     *     {@code shelf} at time {@code currentTime} (in seconds).
     */
    long getExpiration(Shelf shelf, Order order, long currentTime);
  }

  /** Callback for events on {@link Shelf}. */
  interface Callback {

    /** Called when the {@link Order} {@code order} on the {@link Shelf} is expired. */
    void onOrderExpired(Order order);
  }
}
