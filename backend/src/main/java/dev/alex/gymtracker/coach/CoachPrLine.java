package dev.alex.gymtracker.coach;

import java.math.BigDecimal;

record CoachPrLine(String exerciseName, BigDecimal maxWeightKg,
                   Integer maxReps, BigDecimal maxOneRmKg) {}
