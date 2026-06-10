package dev.alex.gymtracker.exercise;

import dev.alex.gymtracker.api.ExercisesApi;
import dev.alex.gymtracker.api.model.CreateExerciseRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ExerciseController implements ExercisesApi {

    private final ExerciseService service;

    public ExerciseController(ExerciseService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<List<dev.alex.gymtracker.api.model.Exercise>> listExercises(
            dev.alex.gymtracker.api.model.MuscleGroup muscleGroup,
            Boolean includeArchived) {
        MuscleGroup domainMg = muscleGroup != null
                ? MuscleGroup.valueOf(muscleGroup.getValue())
                : null;
        return ResponseEntity.ok(
                service.list(domainMg, Boolean.TRUE.equals(includeArchived))
                        .stream().map(this::toDto).collect(Collectors.toList())
        );
    }

    @Override
    public ResponseEntity<dev.alex.gymtracker.api.model.Exercise> getExercise(Long id) {
        return ResponseEntity.ok(toDto(service.get(id)));
    }

    @Override
    public ResponseEntity<dev.alex.gymtracker.api.model.Exercise> createExercise(
            CreateExerciseRequest req) {
        return ResponseEntity.status(201).body(toDto(service.create(req)));
    }

    @Override
    public ResponseEntity<dev.alex.gymtracker.api.model.Exercise> updateExercise(
            Long id, CreateExerciseRequest req) {
        return ResponseEntity.ok(toDto(service.update(id, req)));
    }

    @Override
    public ResponseEntity<Void> deleteExercise(Long id) {
        service.archive(id);
        return ResponseEntity.noContent().build();
    }

    public dev.alex.gymtracker.api.model.Exercise toDto(Exercise e) {
        dev.alex.gymtracker.api.model.Exercise dto = new dev.alex.gymtracker.api.model.Exercise();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setDefaultRestSeconds(e.getDefaultRestSeconds());
        dto.setNotes(e.getNotes());
        dto.setArchived(e.isArchived());
        if (e.getMuscleGroups() != null) {
            dto.setMuscleGroups(
                Arrays.stream(e.getMuscleGroups())
                    .map(dev.alex.gymtracker.api.model.MuscleGroup::fromValue)
                    .collect(Collectors.toList())
            );
        }
        return dto;
    }
}
