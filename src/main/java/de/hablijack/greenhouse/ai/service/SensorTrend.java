package de.hablijack.greenhouse.ai.service;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
public class SensorTrend {

  public final double current;
  public final double min;
  public final double max;
  public final double average;
  public final double rateOfChange;
  public final TrendDirection direction;
  public final int dataPoints;

  public enum TrendDirection {
    RISING_FAST,
    RISING,
    STABLE,
    FALLING,
    FALLING_FAST,
    INSUFFICIENT_DATA
  }

  public SensorTrend(double current, double min, double max, double average,
      double rateOfChange, TrendDirection direction, int dataPoints) {
    this.current = current;
    this.min = min;
    this.max = max;
    this.average = average;
    this.rateOfChange = rateOfChange;
    this.direction = direction;
    this.dataPoints = dataPoints;
  }

  public static SensorTrend empty(double current) {
    return new SensorTrend(current, current, current, current, 0,
        TrendDirection.INSUFFICIENT_DATA, 1);
  }
}
