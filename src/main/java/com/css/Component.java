package com.css;

import static com.css.kitchen.Order.Type;

import com.css.dispatch.DriverDispatcher;
import com.css.dispatch.OrderDispatcher;
import com.css.kitchen.Order;
import com.css.kitchen.Shelf;
import com.google.common.collect.Sets;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import java.time.Clock;
import java.time.ZoneId;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Singleton;

@Singleton
@dagger.Component(modules = Component.Module.class)
interface Component {

  DriverDispatcher driverDispatcher();

  OrderDispatcher orderDispatcher();

  @dagger.Module
  class Module {

    private final String inputFile;

    Module(String inputFile) {
      this.inputFile = inputFile;
    }

    @Provides
    static Shelf.Evaluator provideShelfEvaluator() {
      return new Shelf.Evaluator() {
        @Override
        public long getValue(Shelf shelf, Order order, long orderAge) {
          return order.shelfLife
              - orderAge
              - (long) Math.ceil(order.decayRate * shelf.coefficient * orderAge);
        }

        @Override
        public long getExpiration(Shelf shelf, Order order, long currentTime) {
          return currentTime
              + (long) Math.ceil(order.shelfLife / (1 + shelf.coefficient * order.decayRate));
        }
      };
    }

    @Provides
    @IntoSet
    static Shelf provideHotShelf(Clock clock, Shelf.Evaluator evaluator) {
      return new Shelf("Hot", Sets.newHashSet(Type.HOT), 15, 1, clock, evaluator);
    }

    @Provides
    @IntoSet
    static Shelf provideColdShelf(Clock clock, Shelf.Evaluator evaluator) {
      return new Shelf("Cold", Sets.newHashSet(Type.COLD), 15, 1, clock, evaluator);
    }

    @Provides
    @IntoSet
    Shelf provideOverflowShelf(Clock clock, Shelf.Evaluator evaluator) {
      return new Shelf(
          "Overflow", Sets.newHashSet(Type.HOT, Type.COLD, Type.FROZEN), 20, 1, clock, evaluator);
    }

    @Provides
    @Singleton
    static Clock provideClock() {
      return Clock.tickSeconds(ZoneId.systemDefault());
    }

    @Provides
    static ScheduledExecutorService provideExecutorService() {
      return Executors.newSingleThreadScheduledExecutor();
    }

    @Provides
    String provideFileName() {
      return inputFile;
    }
  }
}
