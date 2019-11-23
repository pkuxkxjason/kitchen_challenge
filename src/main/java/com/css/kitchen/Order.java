package com.css.kitchen;

import com.google.gson.annotations.SerializedName;
import javax.annotation.Nullable;

/**
 * Represents an {@link Order}. If the {@link Order} has been processed, then it'll have a non-null
 * {@link #processed} property. If it has been picked up then it'll have a non-null {@link
 * #pickedUp} property.
 */
public class Order {

  public final String name;
  public final int shelfLife;
  public final double decayRate;

  @SerializedName("temp")
  final Type type;

  @Nullable Processed processed;
  @Nullable PickedUp pickedUp;

  public Order(String name, Type type, int shelfLife, double decayRate) {
    this.name = name;
    this.type = type;
    this.shelfLife = shelfLife;
    this.decayRate = decayRate;
  }

  /**
   * @return a non-null {@link Processed} but will throw an {@link Exception} if it's not available.
   */
  Processed processed() {
    assert processed != null;
    return processed;
  }

  /**
   * @return whether this {@link Order} for picked up at time {@code time}. It means that the {@link
   *     Order} has been processed, is not expired, and has not been picked up yet.
   */
  boolean canBePickedUpAtTime(long time) {
    return processed != null && processed.expiration > time && pickedUp == null;
  }

  /** Represents different {@link Order} types (hot, cold, frozen). */
  public enum Type {
    @SerializedName("hot")
    HOT,
    @SerializedName("cold")
    COLD,
    @SerializedName("frozen")
    FROZEN
  }

  /** Represents the state of the {@link Order} when picked up. */
  static class PickedUp {

    final long value;

    PickedUp(long value) {
      this.value = value;
    }
  }

  /** Represents the state of the {@link Order} when processed. */
  static class Processed {

    final Shelf shelf;
    final long expiration;

    Processed(Shelf shelf, long expiration) {
      this.shelf = shelf;
      this.expiration = expiration;
    }
  }
}
