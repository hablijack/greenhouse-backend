package de.hablijack.greenhouse.ai.service;

import java.time.LocalDateTime;

public class TimeContext {

  public final int hourOfDay;
  public final int month;
  public final TimeOfDay timeOfDay;
  public final Season season;

  public enum TimeOfDay {
    NIGHT(0, 4),
    MORNING(5, 9),
    NOON(10, 16),
    EVENING(17, 20),
    LATE_NIGHT(21, 23);

    public final int startHour;
    public final int endHour;

    TimeOfDay(int start, int end) {
      this.startHour = start;
      this.endHour = end;
    }

    public static TimeOfDay fromHour(int hour) {
      for (TimeOfDay t : values()) {
        if (hour >= t.startHour && hour <= t.endHour) {
          return t;
        }
      }
      return NOON;
    }
  }

  public enum Season {
    SPRING(3, 5),
    SUMMER(6, 8),
    AUTUMN(9, 11),
    WINTER(12, 2);

    public final int startMonth;
    public final int endMonth;

    Season(int start, int end) {
      this.startMonth = start;
      this.endMonth = end;
    }

    public static Season fromMonth(int month) {
      for (Season s : values()) {
        if (s == WINTER) {
          if (month >= s.startMonth || month <= s.endMonth) {
            return s;
          }
        } else {
          if (month >= s.startMonth && month <= s.endMonth) {
            return s;
          }
        }
      }
      return SPRING;
    }
  }

  public TimeContext(int hourOfDay, int month) {
    this.hourOfDay = hourOfDay;
    this.month = month;
    this.timeOfDay = TimeOfDay.fromHour(hourOfDay);
    this.season = Season.fromMonth(month);
  }

  public static TimeContext now() {
    LocalDateTime now = LocalDateTime.now();
    return new TimeContext(now.getHour(), now.getMonthValue());
  }

  public static TimeContext from(Integer hour, Integer month) {
    if (hour == null || month == null) {
      return now();
    }
    return new TimeContext(hour, month);
  }

  public String timeOfDayLabel() {
    return switch (timeOfDay) {
      case NIGHT, LATE_NIGHT -> "Nacht";
      case MORNING -> "Morgen";
      case NOON -> "Mittag";
      case EVENING -> "Abend";
    };
  }

  public String seasonLabel() {
    return switch (season) {
      case SPRING -> "Frühling";
      case SUMMER -> "Sommer";
      case AUTUMN -> "Herbst";
      case WINTER -> "Winter";
    };
  }
}
