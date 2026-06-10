package dev.alex.gymtracker.stats;

import java.math.BigDecimal;

public interface SummaryRow {
    Long getWorkoutsThisWeek();
    Long getWorkoutsThisMonth();
    BigDecimal getTotalVolumeThisWeekKg();
}
