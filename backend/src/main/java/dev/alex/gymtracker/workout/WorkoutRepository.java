package dev.alex.gymtracker.workout;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface WorkoutRepository extends JpaRepository<Workout, Long> {

    @Query("""
            SELECT w FROM Workout w
            LEFT JOIN FETCH w.exercises we
            LEFT JOIN FETCH we.exercise
            LEFT JOIN FETCH we.sets
            WHERE w.id = :id
            """)
    Optional<Workout> findByIdWithDetail(Long id);

    @Query(value = """
            SELECT w.id,
                   w.started_at      AS startedAt,
                   w.finished_at     AS finishedAt,
                   w.notes,
                   COUNT(DISTINCT we.id)              AS exerciseCount,
                   COUNT(ws.id)                       AS totalSets,
                   COALESCE(SUM(ws.reps * ws.weight_kg), 0) AS totalVolumeKg
            FROM workout w
            LEFT JOIN workout_exercise we ON we.workout_id = w.id
            LEFT JOIN workout_set ws      ON ws.workout_exercise_id = we.id
            GROUP BY w.id, w.started_at, w.finished_at, w.notes
            ORDER BY w.started_at DESC
            """,
            countQuery = "SELECT COUNT(*) FROM workout",
            nativeQuery = true)
    Page<WorkoutSummaryRow> findSummaryPage(Pageable pageable);
}
