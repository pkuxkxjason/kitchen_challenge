package com.css.dispatch;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;

import com.css.kitchen.Kitchen;
import com.css.kitchen.Order;
import java.util.concurrent.ScheduledExecutorService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DriverDispatcherTests {

  private final Order order = new Order("order 1", Order.Type.HOT, 20, 0.5);

  @Mock private ScheduledExecutorService executorService;
  @Mock private Kitchen kitchen;

  private DriverDispatcher driverDispatcher;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);

    driverDispatcher = new DriverDispatcher(executorService, kitchen, meanFrequency -> 1);
  }

  @Test
  public void dispatch_shouldReturnIffKitchenStillHaveOrderForPickedUp() {
    when(kitchen.pickUpOrder(anyLong())).thenReturn(order);
    assertThat(driverDispatcher.dispatch()).isTrue();

    when(kitchen.pickUpOrder(anyLong())).thenReturn(null);
    assertThat(driverDispatcher.dispatch()).isFalse();
  }
}
