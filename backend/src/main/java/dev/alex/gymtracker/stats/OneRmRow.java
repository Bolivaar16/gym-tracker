package dev.alex.gymtracker.stats;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface OneRmRow {
    LocalDate getWorkoutDate();
    Long getWorkoutId();
    BigDecimal getEstimatedOneRmKg();
}
