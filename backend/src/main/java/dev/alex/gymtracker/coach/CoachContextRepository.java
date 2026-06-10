package dev.alex.gymtracker.coach;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
class CoachContextRepository {

    private final JdbcTemplate jdbc;

    CoachContextRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    List<RecentWorkoutLine> recentWorkoutLines() {
        String sql = """
                WITH recent AS (
                    SELECT w.id, w.started_at
                    FROM workout w
                    WHERE w.finished_at IS NOT NULL
                    ORDER BY w.started_at DESC
                    LIMIT 5
                )
                SELECT CAST(r.started_at AT TIME ZONE 'UTC' AS date) AS workout_date,
                       r.id                                          AS workout_id,
                       e.name                                        AS exercise_name,
                       COUNT(ws.id)                                  AS set_count,
                       MAX(ws.weight_kg)                             AS max_weight_kg,
                       MAX(ws.reps)                                  AS max_reps
                FROM recent r
                JOIN workout_exercise we ON we.workout_id = r.id
                JOIN exercise e          ON e.id = we.exercise_id
                LEFT JOIN workout_set ws ON ws.workout_exercise_id = we.id
                GROUP BY r.id, r.started_at, e.name, we.position
                ORDER BY r.started_at DESC, we.position
                """;
        return jdbc.query(sql, (rs, rowNum) -> new RecentWorkoutLine(
                rs.getDate("workout_date").toLocalDate(),
                rs.getLong("workout_id"),
                rs.getString("exercise_name"),
                rs.getInt("set_count"),
                rs.getBigDecimal("max_weight_kg"),
                (Integer) rs.getObject("max_reps")
        ));
    }

    List<CoachPrLine> personalRecords() {
        String sql = """
                SELECT e.name                                   AS exercise_name,
                       MAX(ws.weight_kg)                        AS max_weight_kg,
                       MAX(ws.reps)                             AS max_reps,
                       MAX(ws.weight_kg * (1 + ws.reps / 30.0)) AS max_one_rm_kg
                FROM workout_set ws
                JOIN workout_exercise we ON we.id = ws.workout_exercise_id
                JOIN exercise e          ON e.id = we.exercise_id
                GROUP BY e.name
                ORDER BY e.name
                """;
        return jdbc.query(sql, (rs, rowNum) -> new CoachPrLine(
                rs.getString("exercise_name"),
                rs.getBigDecimal("max_weight_kg"),
                (Integer) rs.getObject("max_reps"),
                rs.getBigDecimal("max_one_rm_kg")
        ));
    }
}
