package dev.alex.gymtracker.stats;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface PrRow {
    Long getExerciseId();
    String getExerciseName();
    BigDecimal getMaxWeightKg();
    LocalDate getMaxWeightDate();
    Long getMaxWeightWorkoutId();
    BigDecimal getMaxOneRmKg();
    LocalDate getMaxOneRmDate();
    Long getMaxOneRmWorkoutId();
    Integer getMaxReps();
    LocalDate getMaxRepsDate();
    Long getMaxRepsWorkoutId();
}
