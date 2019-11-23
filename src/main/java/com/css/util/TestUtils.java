package com.css.util;

import com.css.kitchen.Order;
import com.google.gson.Gson;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Random;

/** Collection of test utils. */
public class TestUtils {

  private TestUtils() {}

  /**
   * Generates random test cases.
   *
   * @param outputFile the output file.
   * @param size the size of the test case.
   * @throws IOException if failed to generate the test case.
   */
  public static void generateTestCase(String outputFile, int size) throws IOException {
    Random random = new Random();
    Order.Type[] types = new Order.Type[] {Order.Type.HOT, Order.Type.COLD, Order.Type.FROZEN};
    OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8");
    Gson gson = new Gson();

    writer.write("[\n");
    for (int i = 0; i < size; i++) {
      String name = "order " + (i + 1);
      Order.Type type = types[random.nextInt(3)];
      int shelfLife = random.nextInt(1000);
      double decayRate = random.nextDouble();

      if (i > 0) writer.write(",\n");
      writer.write(gson.toJson(new Order(name, type, shelfLife, decayRate)));
    }
    writer.write("]");
    writer.close();
  }
}
