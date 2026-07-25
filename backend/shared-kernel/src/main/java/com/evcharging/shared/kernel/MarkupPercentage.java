package com.evcharging.shared.kernel;

import java.io.Serializable;
import java.util.Objects;

/**
 * Value object representing a markup percentage stored as basis points. 1 basis point = 0.01%
 * (e.g., 1500 basis points = 15.00%).
 *
 * <p>Shared across modules (identity owns vendor markup; station reads it). Range: 0 to 10000 (0%
 * to 100%).
 */
public final class MarkupPercentage implements Serializable {

  private static final int MAX_BASIS_POINTS = 10000;

  private final int basisPoints;

  private MarkupPercentage(int basisPoints) {
    if (basisPoints < 0 || basisPoints > MAX_BASIS_POINTS) {
      throw new IllegalArgumentException(
          "Markup basis points must be between 0 and " + MAX_BASIS_POINTS);
    }
    this.basisPoints = basisPoints;
  }

  /** Creates a markup percentage from basis points. */
  public static MarkupPercentage ofBasisPoints(int basisPoints) {
    return new MarkupPercentage(basisPoints);
  }

  /** Creates a markup percentage from a decimal percentage (e.g., 15.0 for 15%). */
  public static MarkupPercentage ofPercentage(double percentage) {
    if (percentage < 0 || percentage > 100) {
      throw new IllegalArgumentException("Percentage must be between 0 and 100");
    }
    return new MarkupPercentage((int) Math.round(percentage * 100));
  }

  /** Creates a zero markup percentage. */
  public static MarkupPercentage zero() {
    return new MarkupPercentage(0);
  }

  /** Reconstitutes from persistence. */
  public static MarkupPercentage of(int basisPoints) {
    return new MarkupPercentage(basisPoints);
  }

  /** Returns the markup as basis points (1 BP = 0.01%). */
  public int getBasisPoints() {
    return basisPoints;
  }

  /** Returns the markup as a decimal percentage (e.g., 15.00 for 1500 BP). */
  public double getPercentage() {
    return basisPoints / 100.0;
  }

  /** Returns the markup as a decimal multiplier (e.g., 0.15 for 15%). */
  public double getMultiplier() {
    return basisPoints / 10000.0;
  }

  /** Applies the markup to a base amount (in tenths of cents). */
  public int applyTo(int baseAmountTenthCents) {
    return baseAmountTenthCents + (int) Math.round(baseAmountTenthCents * getMultiplier());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MarkupPercentage that = (MarkupPercentage) o;
    return basisPoints == that.basisPoints;
  }

  @Override
  public int hashCode() {
    return Objects.hash(basisPoints);
  }

  @Override
  public String toString() {
    return String.format("%.2f%%", getPercentage());
  }
}
