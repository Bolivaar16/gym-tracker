package dev.alex.gymtracker.workout;

import java.math.BigDecimal;
import java.time.Instant;

public interface WorkoutSummaryRow {
    Long getId();
    Instant getStartedAt();
    Instant getFinishedAt();
    String getNotes();
    Integer getExerciseCount();
    Integer getTotalSets();
    BigDecimal getTotalVolumeKg();
}
