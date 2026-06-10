package dev.alex.gymtracker.stats;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

interface StatsRepository extends Repository<StatsWorkoutRef, Long> {

    @Query(value = """
            SELECT TO_CHAR(w.started_at AT TIME ZONE 'UTC', 'IYYY-"W"IW') AS period,
                   SUM(ws.reps * ws.weight_kg)                            AS volumeKg
            FROM workout w
            JOIN workout_exercise we ON we.workout_id = w.id
            JOIN workout_set ws      ON ws.workout_exercise_id = we.id
            JOIN exercise e          ON e.id = we.exercise_id
            WHERE (CAST(:muscleGroup AS text) IS NULL
                   OR CAST(:muscleGroup AS text) = ANY (e.muscle_groups))
              AND (CAST(:fromDate AS date) IS NULL
                   OR CAST(w.started_at AT TIME ZONE 'UTC' AS date) >= CAST(:fromDate AS date))
              AND (CAST(:toDate AS date) IS NULL
                   OR CAST(w.started_at AT TIME ZONE 'UTC' AS date) <= CAST(:toDate AS date))
            GROUP BY 1
            ORDER BY 1
            """, nativeQuery = true)
    List<VolumeRow> volumeByWeek(@Param("muscleGroup") String muscleGroup,
                                 @Param("fromDate") LocalDate fromDate,
                                 @Param("toDate") LocalDate toDate);

    @Query(value = """
            SELECT TO_CHAR(w.started_at AT TIME ZONE 'UTC', 'YYYY-MM') AS period,
                   SUM(ws.reps * ws.weight_kg)                         AS volumeKg
            FROM workout w
            JOIN workout_exercise we ON we.workout_id = w.id
            JOIN workout_set ws      ON ws.workout_exercise_id = we.id
            JOIN exercise e          ON e.id = we.exercise_id
            WHERE (CAST(:muscleGroup AS text) IS NULL
                   OR CAST(:muscleGroup AS text) = ANY (e.muscle_groups))
              AND (CAST(:fromDate AS date) IS NULL
                   OR CAST(w.started_at AT TIME ZONE 'UTC' AS date) >= CAST(:fromDate AS date))
              AND (CAST(:toDate AS date) IS NULL
                   OR CAST(w.started_at AT TIME ZONE 'UTC' AS date) <= CAST(:toDate AS date))
            GROUP BY 1
            ORDER BY 1
            """, nativeQuery = true)
    List<VolumeRow> volumeByMonth(@Param("muscleGroup") String muscleGroup,
                                  @Param("fromDate") LocalDate fromDate,
                                  @Param("toDate") LocalDate toDate);

    @Query(value = """
            SELECT CAST(w.started_at AT TIME ZONE 'UTC' AS date) AS workoutDate,
                   w.id                                          AS workoutId,
                   MAX(ws.weight_kg * (1 + ws.reps / 30.0))     AS estimatedOneRmKg
            FROM workout w
            JOIN workout_exercise we ON we.workout_id = w.id
            JOIN workout_set ws      ON ws.workout_exercise_id = we.id
            WHERE we.exercise_id = :exerciseId
              AND (CAST(:fromDate AS date) IS NULL
                   OR CAST(w.started_at AT TIME ZONE 'UTC' AS date) >= CAST(:fromDate AS date))
              AND (CAST(:toDate AS date) IS NULL
                   OR CAST(w.started_at AT TIME ZONE 'UTC' AS date) <= CAST(:toDate AS date))
            GROUP BY w.id, w.started_at
            ORDER BY w.started_at
            """, nativeQuery = true)
    List<OneRmRow> oneRmSeries(@Param("exerciseId") Long exerciseId,
                               @Param("fromDate") LocalDate fromDate,
                               @Param("toDate") LocalDate toDate);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM exercise WHERE id = :exerciseId)",
           nativeQuery = true)
    boolean exerciseExists(@Param("exerciseId") Long exerciseId);

    @Query(value = """
            WITH set_data AS (
                SELECT e.id                                          AS exercise_id,
                       e.name                                        AS exercise_name,
                       w.id                                          AS workout_id,
                       CAST(w.started_at AT TIME ZONE 'UTC' AS date) AS workout_date,
                       ws.reps,
                       ws.weight_kg,
                       ws.weight_kg * (1 + ws.reps / 30.0)          AS one_rm
                FROM workout_set ws
                JOIN workout_exercise we ON we.id = ws.workout_exercise_id
                JOIN workout w           ON w.id = we.workout_id
                JOIN exercise e          ON e.id = we.exercise_id
            ),
            ranked AS (
                SELECT *,
                       ROW_NUMBER() OVER (PARTITION BY exercise_id
                                          ORDER BY weight_kg DESC, workout_date ASC, workout_id ASC) AS rn_weight,
                       ROW_NUMBER() OVER (PARTITION BY exercise_id
                                          ORDER BY one_rm DESC, workout_date ASC, workout_id ASC)    AS rn_one_rm,
                       ROW_NUMBER() OVER (PARTITION BY exercise_id
                                          ORDER BY reps DESC, workout_date ASC, workout_id ASC)      AS rn_reps
                FROM set_data
            )
            SELECT exercise_id                                    AS exerciseId,
                   exercise_name                                  AS exerciseName,
                   MAX(weight_kg)    FILTER (WHERE rn_weight = 1) AS maxWeightKg,
                   MAX(workout_date) FILTER (WHERE rn_weight = 1) AS maxWeightDate,
                   MAX(workout_id)   FILTER (WHERE rn_weight = 1) AS maxWeightWorkoutId,
                   MAX(one_rm)       FILTER (WHERE rn_one_rm = 1) AS maxOneRmKg,
                   MAX(workout_date) FILTER (WHERE rn_one_rm = 1) AS maxOneRmDate,
                   MAX(workout_id)   FILTER (WHERE rn_one_rm = 1) AS maxOneRmWorkoutId,
                   MAX(reps)         FILTER (WHERE rn_reps = 1)   AS maxReps,
                   MAX(workout_date) FILTER (WHERE rn_reps = 1)   AS maxRepsDate,
                   MAX(workout_id)   FILTER (WHERE rn_reps = 1)   AS maxRepsWorkoutId
            FROM ranked
            GROUP BY exercise_id, exercise_name
            ORDER BY exercise_name
            """, nativeQuery = true)
    List<PrRow> personalRecords();

    @Query(value = """
            SELECT
                (SELECT COUNT(*)
                 FROM workout w
                 WHERE w.finished_at IS NOT NULL
                   AND DATE_TRUNC('week', w.started_at AT TIME ZONE 'UTC')
                       = DATE_TRUNC('week', now() AT TIME ZONE 'UTC'))         AS workoutsThisWeek,
                (SELECT COUNT(*)
                 FROM workout w
                 WHERE w.finished_at IS NOT NULL
                   AND DATE_TRUNC('month', w.started_at AT TIME ZONE 'UTC')
                       = DATE_TRUNC('month', now() AT TIME ZONE 'UTC'))        AS workoutsThisMonth,
                (SELECT COALESCE(SUM(ws.reps * ws.weight_kg), 0)
                 FROM workout w
                 JOIN workout_exercise we ON we.workout_id = w.id
                 JOIN workout_set ws      ON ws.workout_exercise_id = we.id
                 WHERE w.finished_at IS NOT NULL
                   AND DATE_TRUNC('week', w.started_at AT TIME ZONE 'UTC')
                       = DATE_TRUNC('week', now() AT TIME ZONE 'UTC'))         AS totalVolumeThisWeekKg
            """, nativeQuery = true)
    SummaryRow summaryCounts();

    @Query(value = """
            WITH days AS (
                SELECT DISTINCT CAST(w.started_at AT TIME ZONE 'UTC' AS date) AS workout_day
                FROM workout w
                WHERE w.finished_at IS NOT NULL
                  AND CAST(w.started_at AT TIME ZONE 'UTC' AS date)
                      <= CAST(now() AT TIME ZONE 'UTC' AS date)
            ),
            numbered AS (
                SELECT workout_day,
                       ROW_NUMBER() OVER (ORDER BY workout_day DESC) AS rn
                FROM days
            )
            SELECT COUNT(*)
            FROM numbered
            WHERE workout_day = CAST(now() AT TIME ZONE 'UTC' AS date) - (rn - 1)
            """, nativeQuery = true)
    long currentStreakDays();
}
