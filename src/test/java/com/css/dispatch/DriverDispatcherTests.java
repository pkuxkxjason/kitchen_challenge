package com.css.dispatch;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import com.css.dispatch.DriverDispatcher.DiscreteGenerator;
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
  @Mock private DiscreteGenerator generator;

  private DriverDispatcher driverDispatcher;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    when(generator.generateEventsCount(anyDouble())).thenReturn(1);
    driverDispatcher = new DriverDispatcher(executorService, kitchen, generator);
  }

  @Test
  public void dispatch_shouldReturnIffKitchenStillHaveOrderForPickedUp() {
    when(kitchen.pickUpOrder(anyLong())).thenReturn(order);
    assertThat(driverDispatcher.dispatch()).isTrue();

    when(kitchen.pickUpOrder(anyLong())).thenReturn(null);
    assertThat(driverDispatcher.dispatch()).isFalse();
  }

  @Test
  public void dispatch_shouldPickUpOrderByTheGivenNumberOfDrivers() {
    when(kitchen.pickUpOrder(anyLong())).thenReturn(order);
    when(generator.generateEventsCount(anyDouble())).thenReturn(5);
    assertThat(driverDispatcher.dispatch()).isTrue();
    verify(kitchen,times(5)).pickUpOrder(anyLong());
  }

}
