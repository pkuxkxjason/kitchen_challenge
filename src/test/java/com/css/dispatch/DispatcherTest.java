package com.css.dispatch;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DispatcherTest {

  private final AtomicBoolean dispatcherReturn = new AtomicBoolean(true);

  @Mock private ScheduledExecutorService executorService;

  private Dispatcher dispatcher;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);

    dispatcher =
        new Dispatcher(executorService) {
          @Override
          boolean dispatch() {
            return dispatcherReturn.get();
          }

          @Override
          long durationUntilNextDispatch() {
            return 2;
          }
        };
    doAnswer(
            invocation -> {
              ((Runnable) invocation.getArguments()[0]).run();
              return null;
            })
        .when(executorService)
        .execute(any(Runnable.class));
  }

  @Test
  public void start_whenDispatchReturnTrue_shouldScheduleExecutorService() {
    dispatcher.start();
    verify(executorService).execute(any(Runnable.class));
    verify(executorService).schedule(any(Runnable.class), eq(2L), eq(TimeUnit.MILLISECONDS));
  }

  @Test
  public void start_whenDispatchReturnFalse_shouldNotScheduleExecutorService() {
    dispatcherReturn.set(false);
    dispatcher.start();
    verify(executorService).execute(any(Runnable.class));
    verify(executorService, never()).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
  }

  @Test
  public void stop_shouldShutdownExecutorService() {
    dispatcher.stop();
    verify(executorService).shutdown();
  }
}
