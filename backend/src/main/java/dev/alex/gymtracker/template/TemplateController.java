package dev.alex.gymtracker.template;

import dev.alex.gymtracker.api.TemplatesApi;
import dev.alex.gymtracker.api.model.CreateTemplateRequest;
import dev.alex.gymtracker.api.model.TemplateExerciseItem;
import dev.alex.gymtracker.api.model.TemplateSummary;
import dev.alex.gymtracker.exercise.ExerciseController;
import dev.alex.gymtracker.workout.WorkoutController;
import dev.alex.gymtracker.workout.WorkoutService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class TemplateController implements TemplatesApi {

    private final TemplateService service;
    private final ExerciseController exerciseController;
    private final WorkoutController workoutController;
    private final WorkoutService workoutService;

    public TemplateController(TemplateService service,
                              ExerciseController exerciseController,
                              WorkoutController workoutController,
                              WorkoutService workoutService) {
        this.service = service;
        this.exerciseController = exerciseController;
        this.workoutController = workoutController;
        this.workoutService = workoutService;
    }

    @Override
    public ResponseEntity<List<TemplateSummary>> listTemplates() {
        return ResponseEntity.ok(
                service.listSummaries().stream().map(this::toSummaryDto).collect(Collectors.toList()));
    }

    @Override
    public ResponseEntity<dev.alex.gymtracker.api.model.Template> createTemplate(
            CreateTemplateRequest createTemplateRequest) {
        return ResponseEntity.status(201).body(toDto(service.create(createTemplateRequest)));
    }

    @Override
    public ResponseEntity<dev.alex.gymtracker.api.model.Template> getTemplate(Long id) {
        return ResponseEntity.ok(toDto(service.getDetail(id)));
    }

    @Override
    public ResponseEntity<dev.alex.gymtracker.api.model.Template> updateTemplate(
            Long id, CreateTemplateRequest createTemplateRequest) {
        return ResponseEntity.ok(toDto(service.update(id, createTemplateRequest)));
    }

    @Override
    public ResponseEntity<Void> deleteTemplate(Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<dev.alex.gymtracker.api.model.Workout> startWorkoutFromTemplate(Long id) {
        dev.alex.gymtracker.workout.Workout w = service.start(id);
        return ResponseEntity.status(201)
                .body(workoutController.toDetailDto(workoutService.getDetail(w.getId())));
    }

    // ── Mappers ────────────────────────────────────────────────────────────────

    private dev.alex.gymtracker.api.model.Template toDto(Template t) {
        dev.alex.gymtracker.api.model.Template dto = new dev.alex.gymtracker.api.model.Template();
        dto.setId(t.getId());
        dto.setName(t.getName());
        dto.setNotes(t.getNotes());
        dto.setCreatedAt(t.getCreatedAt().atOffset(ZoneOffset.UTC));
        dto.setExercises(t.getExercises().stream()
                .map(this::toTemplateExerciseDto).collect(Collectors.toList()));
        return dto;
    }

    private TemplateExerciseItem toTemplateExerciseDto(TemplateExercise te) {
        TemplateExerciseItem dto = new TemplateExerciseItem();
        dto.setExercise(exerciseController.toDto(te.getExercise()));
        dto.setPosition(te.getPosition());
        dto.setTargetSets(te.getTargetSets());
        dto.setTargetReps(te.getTargetReps());
        dto.setTargetWeightKg(te.getTargetWeightKg() != null
                ? te.getTargetWeightKg().doubleValue() : null);
        return dto;
    }

    private TemplateSummary toSummaryDto(TemplateSummaryRow row) {
        TemplateSummary dto = new TemplateSummary();
        dto.setId(row.getId());
        dto.setName(row.getName());
        dto.setNotes(row.getNotes());
        dto.setExerciseCount(row.getExerciseCount() != null ? row.getExerciseCount() : 0);
        return dto;
    }
}
