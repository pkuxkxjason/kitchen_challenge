package com.css.kitchen;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import com.css.Displayer;
import com.google.common.collect.Sets;
import java.time.Clock;
import java.time.Instant;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class KitchenTests {

  private final Order order1 = new Order("order 1", Order.Type.HOT, 20, 0.5);
  private final Order order2 = new Order("order 2", Order.Type.COLD, 20, 0.5);
  private final Order order3 = new Order("order 3", Order.Type.HOT, 20, 0.5);
  private final Order order4 = new Order("order 4", Order.Type.HOT, 20, 0.5);
  private final Order order5 = new Order("order 5", Order.Type.HOT, 20, 0.5);
  private final Order order6 = new Order("order 6", Order.Type.HOT, 20, 0.5);

  @Mock private Displayer displayer;
  @Mock private Clock clock;
  @Mock private Shelf.Evaluator evaluator;

  private Shelf hotShelf;
  private Shelf coldShelf;
  private Shelf overflowShelf;

  private Kitchen kitchen;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);

    hotShelf = new Shelf("hot", Sets.newHashSet(Order.Type.HOT), 2, 1, clock, evaluator);
    coldShelf = new Shelf("cold", Sets.newHashSet(Order.Type.COLD), 2, 1, clock, evaluator);
    overflowShelf =
        new Shelf(
            "overflow", Sets.newHashSet(Order.Type.HOT, Order.Type.COLD), 2, 1, clock, evaluator);
    kitchen = new Kitchen(Sets.newHashSet(hotShelf, coldShelf, overflowShelf), clock, displayer);

    when(evaluator.getExpiration(any(Shelf.class), any(Order.class), anyLong())).thenReturn(1L);
    when(clock.instant()).thenReturn(Instant.EPOCH);
  }

  @Test
  public void processOrder_whenChooseTheCorrectShelf() {
    kitchen.processOrder(order1);
    assertThat(hotShelf.count()).isEqualTo(1);
    assertThat(hotShelf.orders()).asList().containsExactly(order1);
    assertThat(order1.processed).isNotNull();

    kitchen.processOrder(order2);
    assertThat(coldShelf.count()).isEqualTo(1);
    assertThat(coldShelf.orders()).asList().containsExactly(order2);
    assertThat(order2.processed).isNotNull();

    kitchen.processOrder(order3);
    kitchen.processOrder(order4);
    assertThat(hotShelf.count()).isEqualTo(2);
    assertThat(coldShelf.count()).isEqualTo(1);
    assertThat(overflowShelf.count()).isEqualTo(1);
    assertThat(hotShelf.orders()).asList().containsExactly(order1, order3);
    assertThat(overflowShelf.orders()).asList().containsExactly(order4);
    assertThat(order3.processed).isNotNull();
    assertThat(order4.processed).isNotNull();
  }

  @Test
  public void processOrder_whenTheShelvesAreFull_shouldThrowOrderAway() {
    kitchen.processOrder(order1);
    kitchen.processOrder(order3);
    kitchen.processOrder(order4);
    kitchen.processOrder(order5);
    kitchen.processOrder(order6);

    assertThat(hotShelf.orders()).asList().containsExactly(order1, order3);
    assertThat(overflowShelf.orders()).asList().containsExactly(order4, order5);

    assertThat(order6.processed).isNull();
  }

  @Test
  public void pickUpOrder_shouldReturnOrdersLIFO() {
    kitchen.processOrder(order1);
    kitchen.processOrder(order2);

    assertThat(kitchen.pickUpOrder(0)).isEqualTo(order1);
    assertThat(order1.pickedUp).isNotNull();

    assertThat(kitchen.pickUpOrder(0)).isEqualTo(order2);
    assertThat(order2.pickedUp).isNotNull();

    kitchen.processOrder(order3);
    kitchen.processOrder(order4);

    assertThat(kitchen.pickUpOrder(0)).isEqualTo(order3);
    assertThat(order3.pickedUp).isNotNull();

    assertThat(kitchen.pickUpOrder(0)).isEqualTo(order4);
    assertThat(order4.pickedUp).isNotNull();
  }

  @Test
  public void pickUpOrder_shouldReturnCorrectOrderValue() {
    when(evaluator.getValue(any(Shelf.class), eq(order1), anyLong())).thenReturn(5L);
    when(evaluator.getValue(any(Shelf.class), eq(order2), anyLong())).thenReturn(2L);
    when(evaluator.getValue(any(Shelf.class), eq(order3), anyLong())).thenReturn(6L);
    when(evaluator.getValue(any(Shelf.class), eq(order4), anyLong())).thenReturn(3L);

    kitchen.processOrder(order1);
    kitchen.processOrder(order2);
    kitchen.processOrder(order3);
    kitchen.processOrder(order4);

    Order order = kitchen.pickUpOrder(0);
    assertThat(order).isNotNull();
    assertThat(order.pickedUp).isNotNull();
    assertThat(order.pickedUp.value).isEqualTo(5L);

    order = kitchen.pickUpOrder(0);
    assertThat(order).isNotNull();
    assertThat(order.pickedUp).isNotNull();
    assertThat(order.pickedUp.value).isEqualTo(2L);

    order = kitchen.pickUpOrder(0);
    assertThat(order).isNotNull();
    assertThat(order.pickedUp).isNotNull();
    assertThat(order.pickedUp.value).isEqualTo(6L);

    order = kitchen.pickUpOrder(0);
    assertThat(order).isNotNull();
    assertThat(order.pickedUp).isNotNull();
    assertThat(order.pickedUp.value).isEqualTo(3L);
  }

  @Test
  public void pickUpOrder_shouldNotReturnExpiredOrders() {
    when(evaluator.getExpiration(any(Shelf.class), eq(order1), anyLong())).thenReturn(5L);
    when(evaluator.getExpiration(any(Shelf.class), eq(order2), anyLong())).thenReturn(2L);
    when(evaluator.getExpiration(any(Shelf.class), eq(order3), anyLong())).thenReturn(6L);
    when(evaluator.getExpiration(any(Shelf.class), eq(order4), anyLong())).thenReturn(3L);

    kitchen.processOrder(order1);
    kitchen.processOrder(order2);
    kitchen.processOrder(order3);
    kitchen.processOrder(order4);

    when(clock.instant()).thenReturn(Instant.ofEpochSecond(3));
    assertThat(kitchen.pickUpOrder(1000)).isEqualTo(order1);
    assertThat(kitchen.pickUpOrder(1000)).isEqualTo(order3);
  }

  @Test
  public void pickUpOrder_whenThereIsNoMoreOrder_shouldReturnNull() {
    assertThat(kitchen.pickUpOrder(0)).isEqualTo(null);
  }
}
