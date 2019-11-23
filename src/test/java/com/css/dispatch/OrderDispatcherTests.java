package com.css.dispatch;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.css.kitchen.Kitchen;
import com.css.kitchen.Order;
import java.util.concurrent.ScheduledExecutorService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class OrderDispatcherTests {

  @Mock private ScheduledExecutorService executorService;
  @Mock private Kitchen kitchen;

  private OrderDispatcher orderDispatcher;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);

    orderDispatcher =
        new OrderDispatcher(executorService, kitchen, "src/test/resources/input1.json");
  }

  @Test(expected = RuntimeException.class)
  public void fileDoesNotExist_shouldThrowException() {
    new OrderDispatcher(executorService, kitchen, "DoesNotExist.json");
  }

  @Test
  public void dispatch_shouldReadOrdersFromFile() {
    assertThat(orderDispatcher.dispatch()).isTrue();
    ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
    verify(kitchen).processOrder(captor.capture());
    assertThat(captor.getValue().name).isEqualTo("order 1");
  }

  @Test
  public void dispatch_whenReadToEOF_shouldReturnFalse() {
    assertThat(orderDispatcher.dispatch()).isTrue();
    assertThat(orderDispatcher.dispatch()).isTrue();
    assertThat(orderDispatcher.dispatch()).isFalse();
    verify(kitchen, times(2)).processOrder(any(Order.class));
  }

  @Test
  public void dispatch_whenFailedToReadFromFile_shouldSkipOrder() {
    OrderDispatcher orderDispatcher =
        new OrderDispatcher(executorService, kitchen, "src/test/resources/input2.json");

    assertThat(orderDispatcher.dispatch()).isTrue();
    assertThat(orderDispatcher.dispatch()).isFalse();
    verify(kitchen).processOrder(any(Order.class));
  }
}
