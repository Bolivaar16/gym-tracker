package dev.alex.gymtracker.exercise;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    boolean existsByNameIgnoreCase(String name);

    @Query(value = """
            SELECT * FROM exercise
            WHERE (:muscleGroup::text IS NULL OR :muscleGroup = ANY(muscle_groups))
              AND (archived = false OR :includeArchived = true)
            ORDER BY name
            """, nativeQuery = true)
    List<Exercise> findByFilter(String muscleGroup, boolean includeArchived);
}
