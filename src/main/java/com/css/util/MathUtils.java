package com.css.util;

import org.apache.commons.math3.distribution.PoissonDistribution;

/** Collection of math related utils. */
public class MathUtils {
  private MathUtils() {}

  /** @return a Possion random number with mean value {@code mean}. */
  public static int generatePoissonNumber(double mean) {
    PoissonDistribution generator = new PoissonDistribution(mean);
    int value = generator.sample();
    return value;
  }
}
