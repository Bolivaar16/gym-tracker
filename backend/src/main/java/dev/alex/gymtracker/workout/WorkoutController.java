package dev.alex.gymtracker.workout;

import dev.alex.gymtracker.api.WorkoutsApi;
import dev.alex.gymtracker.api.model.CreateWorkoutRequest;
import dev.alex.gymtracker.api.model.UpdateWorkoutRequest;
import dev.alex.gymtracker.api.model.WorkoutExercise;
import dev.alex.gymtracker.api.model.WorkoutPage;
import dev.alex.gymtracker.api.model.WorkoutSummary;
import dev.alex.gymtracker.exercise.ExerciseController;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class WorkoutController implements WorkoutsApi {

    private final WorkoutService service;
    final ExerciseController exerciseController;

    public WorkoutController(WorkoutService service, ExerciseController exerciseController) {
        this.service = service;
        this.exerciseController = exerciseController;
    }

    @Override
    public ResponseEntity<WorkoutPage> listWorkouts(Integer page, Integer size) {
        Page<WorkoutSummaryRow> result = service.listSummary(page, size);
        WorkoutPage wp = new WorkoutPage();
        wp.setPage(result.getNumber());
        wp.setSize(result.getSize());
        wp.setTotalElements(result.getTotalElements());
        wp.setTotalPages(result.getTotalPages());
        wp.setContent(result.getContent().stream()
                .map(this::toSummaryDto).collect(Collectors.toList()));
        return ResponseEntity.ok(wp);
    }

    @Override
    public ResponseEntity<dev.alex.gymtracker.api.model.Workout> createWorkout(
            CreateWorkoutRequest req) {
        dev.alex.gymtracker.workout.Workout w = service.create(req);
        return ResponseEntity.status(201).body(toDetailDto(service.getDetail(w.getId())));
    }

    @Override
    public ResponseEntity<dev.alex.gymtracker.api.model.Workout> getWorkout(Long id) {
        return ResponseEntity.ok(toDetailDto(service.getDetail(id)));
    }

    @Override
    public ResponseEntity<dev.alex.gymtracker.api.model.Workout> updateWorkout(
            Long id, UpdateWorkoutRequest req) {
        service.update(id, req);
        return ResponseEntity.ok(toDetailDto(service.getDetail(id)));
    }

    @Override
    public ResponseEntity<Void> deleteWorkout(Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    public dev.alex.gymtracker.api.model.Workout toDetailDto(dev.alex.gymtracker.workout.Workout w) {
        dev.alex.gymtracker.api.model.Workout dto = new dev.alex.gymtracker.api.model.Workout();
        dto.setId(w.getId());
        dto.setStartedAt(toOffsetDateTime(w.getStartedAt()));
        dto.setFinishedAt(toOffsetDateTime(w.getFinishedAt()));
        dto.setNotes(w.getNotes());
        dto.setExercises(w.getExercises().stream()
                .map(this::toWorkoutExerciseDto).collect(Collectors.toList()));
        return dto;
    }

    private WorkoutExercise toWorkoutExerciseDto(dev.alex.gymtracker.workout.WorkoutExercise we) {
        WorkoutExercise dto = new WorkoutExercise();
        dto.setExercise(exerciseController.toDto(we.getExercise()));
        dto.setPosition(we.getPosition());
        dto.setSets(we.getSets().stream().map(SetController::toSetDto).collect(Collectors.toList()));
        return dto;
    }

    private WorkoutSummary toSummaryDto(WorkoutSummaryRow row) {
        WorkoutSummary dto = new WorkoutSummary();
        dto.setId(row.getId());
        dto.setStartedAt(toOffsetDateTime(row.getStartedAt()));
        dto.setFinishedAt(toOffsetDateTime(row.getFinishedAt()));
        dto.setNotes(row.getNotes());
        dto.setExerciseCount(row.getExerciseCount() != null ? row.getExerciseCount() : 0);
        dto.setTotalSets(row.getTotalSets() != null ? row.getTotalSets() : 0);
        dto.setTotalVolumeKg(row.getTotalVolumeKg() != null
                ? row.getTotalVolumeKg().doubleValue() : 0.0);
        return dto;
    }

    private static OffsetDateTime toOffsetDateTime(java.time.Instant instant) {
        return instant != null ? instant.atOffset(ZoneOffset.UTC) : null;
    }
}
