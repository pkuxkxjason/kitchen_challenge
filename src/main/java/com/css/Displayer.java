package com.css;

import com.css.kitchen.Kitchen;
import com.css.kitchen.Shelf;
import java.util.Collections;
import javax.inject.Inject;
import javax.inject.Singleton;

/** Displays to console. */
@Singleton
public class Displayer {

  @Inject
  public Displayer() {}

  /** Displays the state of the kitchen after the given {@code event}. */
  public void display(String event, Kitchen kitchen) {
    StringBuilder builder = new StringBuilder();

    for (Shelf shelf : kitchen.shelves) {
      builder.append("[").append(shelf.name).append(" ").append(shelf.count()).append("] ");
    }
    builder.append(event).append(String.join("", Collections.nCopies(20, " ")));

    System.out.println(builder);
  }
}
