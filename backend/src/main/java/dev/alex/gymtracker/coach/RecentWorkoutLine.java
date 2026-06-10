package dev.alex.gymtracker.coach;

import java.math.BigDecimal;
import java.time.LocalDate;

record RecentWorkoutLine(LocalDate workoutDate, Long workoutId, String exerciseName,
                         int setCount, BigDecimal maxWeightKg, Integer maxReps) {}
