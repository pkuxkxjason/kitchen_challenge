package com.css;

import com.css.Component.Module;
import com.css.dispatch.DriverDispatcher;
import com.css.dispatch.OrderDispatcher;

public class Main {

  private static final String INPUT_FILE = "input.json";

  public static void main(String args[]) {
    Component component = DaggerComponent.builder().module(new Module(INPUT_FILE)).build();
    OrderDispatcher orderDispatcher = component.orderDispatcher();
    DriverDispatcher driverDispatcher = component.driverDispatcher();

    // Start dispatching.
    orderDispatcher.start();
  }
}
