package com.css.kitchen;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;

import com.css.Displayer;
import com.google.common.collect.Sets;
import java.time.Clock;
import java.time.Instant;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ShelfTests {

  private final Order order1 = new Order("order 1", Order.Type.HOT, 20, 0.5);
  private final Order order2 = new Order("order 2", Order.Type.HOT, 20, 0.5);
  private final Order order3 = new Order("order 3", Order.Type.HOT, 20, 0.5);
  private final Order order4 = new Order("order 4", Order.Type.HOT, 20, 0.5);
  private final Order order5 = new Order("order 5", Order.Type.HOT, 20, 0.5);
  private final Order order6 = new Order("order 6", Order.Type.HOT, 20, 0.5);

  @Mock private Displayer displayer;
  @Mock private Shelf.Evaluator evaluator;
  @Mock private Clock clock;

  private Shelf hotShelf;


  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);

    hotShelf = new Shelf("hot", Sets.newHashSet(Order.Type.HOT), 5, 1, clock, evaluator);
    
    when(evaluator.getExpiration(any(Shelf.class), any(Order.class), anyLong())).thenReturn(1L);
    when(clock.instant()).thenReturn(Instant.EPOCH);
  }

  @Test
  public void addRemoveOrder_ShouldMatchTheCount() {
    hotShelf.add(order1);
    hotShelf.add(order2);
    hotShelf.add(order3);

    assertThat(hotShelf.count()).isEqualTo(3);

    hotShelf.remove(order1);
    assertThat(hotShelf.count()).isEqualTo(2);
    hotShelf.add(order4);
    assertThat(hotShelf.count()).isEqualTo(3);
    hotShelf.remove(order2);
    assertThat(hotShelf.count()).isEqualTo(2);
    hotShelf.add(order5);
    assertThat(hotShelf.count()).isEqualTo(3);
    hotShelf.add(order6);
    assertThat(hotShelf.count()).isEqualTo(4);
  }

  @Test
  public void expiredOrder_ShouldBeRemoved() {
    hotShelf.add(order1);
    assertThat(hotShelf.count()).isEqualTo(1);

    when(clock.instant()).thenReturn(Instant.MAX);
    assertThat(hotShelf.count()).isEqualTo(0);

    when(clock.instant()).thenReturn(Instant.EPOCH);
    hotShelf.add(order2);
    hotShelf.add(order3);
    assertThat(hotShelf.count()).isEqualTo(2);
  }
}
