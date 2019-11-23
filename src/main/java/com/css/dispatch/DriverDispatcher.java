package com.css.dispatch;

import com.css.kitchen.Kitchen;
import com.css.util.MathUtils;
import com.google.common.annotations.VisibleForTesting;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Singleton;

/** A {@link Dispatcher} to dispatch drivers following a given distribution. */
@Singleton
public class DriverDispatcher extends Dispatcher {

  private static final double DRIVERS_PER_SECONDS = 3;
  private static final long DRIVER_TIMEOUT = 1;

  private final Kitchen kitchen;
  private final DiscreteGenerator generator;

  @Inject
  DriverDispatcher(ScheduledExecutorService executorService, Kitchen kitchen) {
    this(executorService, kitchen, MathUtils::generatePoissonNumber);
  }

  @VisibleForTesting
  DriverDispatcher(
      ScheduledExecutorService executorService, Kitchen kitchen, DiscreteGenerator generator) {
    super(executorService);

    this.kitchen = kitchen;
    this.generator = generator;
  }

  @Override
  boolean dispatch() {
    int numDriver = generator.generateEventsCount(DRIVERS_PER_SECONDS);

    for (int i = 0; i < numDriver; i++) {
      if (kitchen.pickUpOrder(DRIVER_TIMEOUT) == null) return false;
    }

    return true;
  }

  @Override
  long durationUntilNextDispatch() {
    return TimeUnit.SECONDS.toMillis(1);
  }

  /** An interface to generate discrete events. */
  interface DiscreteGenerator {

    /** @return generates the number of events (per second) based on a {@code meanFrequency}. */
    int generateEventsCount(double meanFrequency);
  }
}
