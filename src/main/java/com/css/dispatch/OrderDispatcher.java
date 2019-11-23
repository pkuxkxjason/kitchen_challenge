package com.css.dispatch;

import com.css.kitchen.Kitchen;
import com.css.kitchen.Order;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import javax.inject.Singleton;

/** A {@link Dispatcher} to dispatch {@link Order orders}. */
@Singleton
public class OrderDispatcher extends Dispatcher {

  private static final long ORDER_DISPATCH_DURATION = 250;

  private final Kitchen kitchen;
  private final JsonReader reader;
  private final Gson gson = new Gson();

  @Inject
  OrderDispatcher(ScheduledExecutorService executorService, Kitchen kitchen, String fileName) {
    super(executorService);

    this.kitchen = kitchen;
    try {
      this.reader = new JsonReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
      reader.beginArray();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  boolean dispatch() {
    try {
      if (!reader.hasNext()) {
        reader.endArray();
        reader.close();
        return false;
      }

      // Stream the next order and process.
      Order order = gson.fromJson(reader, Order.class);
      kitchen.processOrder(order);
    } catch (IOException e) {
      System.out.println("Unable to process order. Skipping this order.");
      return false;
    }

    return true;
  }

  @Override
  long durationUntilNextDispatch() {
    return ORDER_DISPATCH_DURATION;
  }
}
