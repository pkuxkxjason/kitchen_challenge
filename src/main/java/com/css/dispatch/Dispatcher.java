package com.css.dispatch;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** A simple dispatcher. */
abstract class Dispatcher {

  private final ScheduledExecutorService executorService;

  Dispatcher(ScheduledExecutorService executorService) {
    this.executorService = executorService;
  }

  /** Starts dispatching. */
  public final void start() {
    executorService.execute(
        new Runnable() {
          @Override
          public void run() {
            if (dispatch()) {
              executorService.schedule(this, durationUntilNextDispatch(), TimeUnit.MILLISECONDS);
            } else {
              stop();
            }
          }
        });
  }

  /** Stops dispatching. */
  public final void stop() {
    executorService.shutdown();
  }

  /** @return {@code true} iff the dispatch is successful. */
  abstract boolean dispatch();

  /** @return the duration until next dispatch (in milliseconds). */
  abstract long durationUntilNextDispatch();
}
