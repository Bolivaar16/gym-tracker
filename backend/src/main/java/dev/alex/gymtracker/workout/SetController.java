package dev.alex.gymtracker.workout;

import dev.alex.gymtracker.api.model.CreateSetRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Handles set endpoints separately to avoid the java.util.Set vs model.Set name collision
 * that would appear if we implemented the generated SetsApi interface.
 */
@RestController
@RequestMapping("/api/workouts/{workoutId}/exercises/{exerciseId}/sets")
public class SetController {

    private final WorkoutService service;

    public SetController(WorkoutService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<dev.alex.gymtracker.api.model.WorkoutSet> createSet(
            @PathVariable Long workoutId,
            @PathVariable Long exerciseId,
            @Valid @RequestBody CreateSetRequest req) {
        WorkoutSet ws = service.createSet(workoutId, exerciseId, req);
        return ResponseEntity.status(201).body(toSetDto(ws));
    }

    @PutMapping("/{setId}")
    public ResponseEntity<dev.alex.gymtracker.api.model.WorkoutSet> updateSet(
            @PathVariable Long workoutId,
            @PathVariable Long exerciseId,
            @PathVariable Long setId,
            @Valid @RequestBody CreateSetRequest req) {
        WorkoutSet ws = service.updateSet(workoutId, exerciseId, setId, req);
        return ResponseEntity.ok(toSetDto(ws));
    }

    @DeleteMapping("/{setId}")
    public ResponseEntity<Void> deleteSet(
            @PathVariable Long workoutId,
            @PathVariable Long exerciseId,
            @PathVariable Long setId) {
        service.deleteSet(workoutId, exerciseId, setId);
        return ResponseEntity.noContent().build();
    }

    static dev.alex.gymtracker.api.model.WorkoutSet toSetDto(dev.alex.gymtracker.workout.WorkoutSet ws) {
        dev.alex.gymtracker.api.model.WorkoutSet dto = new dev.alex.gymtracker.api.model.WorkoutSet();
        dto.setId(ws.getId());
        dto.setSetNumber(ws.getSetNumber());
        dto.setReps(ws.getReps());
        dto.setWeightKg(ws.getWeightKg().doubleValue());
        dto.setRpe(ws.getRpe() != null ? ws.getRpe().doubleValue() : null);
        dto.setCompletedAt(ws.getCompletedAt() != null
                ? ws.getCompletedAt().atOffset(ZoneOffset.UTC) : null);
        return dto;
    }
}
