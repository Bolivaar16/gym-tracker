package dev.alex.gymtracker.workout;

import dev.alex.gymtracker.api.model.CreateSetRequest;
import dev.alex.gymtracker.api.model.CreateWorkoutRequest;
import dev.alex.gymtracker.api.model.UpdateWorkoutRequest;
import dev.alex.gymtracker.common.NotFoundException;
import dev.alex.gymtracker.exercise.Exercise;
import dev.alex.gymtracker.exercise.ExerciseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Service
public class WorkoutService {

    private final WorkoutRepository workoutRepo;
    private final ExerciseRepository exerciseRepo;

    public WorkoutService(WorkoutRepository workoutRepo, ExerciseRepository exerciseRepo) {
        this.workoutRepo = workoutRepo;
        this.exerciseRepo = exerciseRepo;
    }

    public Page<WorkoutSummaryRow> listSummary(int page, int size) {
        return workoutRepo.findSummaryPage(PageRequest.of(page, size));
    }

    public Workout getDetail(Long id) {
        return workoutRepo.findByIdWithDetail(id)
                .orElseThrow(() -> new NotFoundException("Workout " + id + " not found"));
    }

    @Transactional
    public Workout create(CreateWorkoutRequest req) {
        Workout w = new Workout();
        if (req.getStartedAt() != null) {
            w.setStartedAt(req.getStartedAt().toInstant());
        }
        w.setNotes(req.getNotes());
        return workoutRepo.save(w);
    }

    @Transactional
    public Workout update(Long id, UpdateWorkoutRequest req) {
        Workout w = workoutRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Workout " + id + " not found"));
        if (req.getFinishedAt() != null) {
            w.setFinishedAt(req.getFinishedAt().toInstant());
        }
        w.setNotes(req.getNotes());
        return workoutRepo.save(w);
    }

    @Transactional
    public void delete(Long id) {
        if (!workoutRepo.existsById(id)) {
            throw new NotFoundException("Workout " + id + " not found");
        }
        workoutRepo.deleteById(id);
    }

    @Transactional
    public WorkoutSet createSet(Long workoutId, Long exerciseId, CreateSetRequest req) {
        Workout workout = workoutRepo.findByIdWithDetail(workoutId)
                .orElseThrow(() -> new NotFoundException("Workout " + workoutId + " not found"));
        Exercise exercise = exerciseRepo.findById(exerciseId)
                .orElseThrow(() -> new NotFoundException("Exercise " + exerciseId + " not found"));

        WorkoutExercise we = workout.getExercises().stream()
                .filter(x -> x.getExercise().getId().equals(exerciseId))
                .findFirst()
                .orElseGet(() -> attachExercise(workout, exercise));

        int nextNumber = we.getSets().stream()
                .mapToInt(WorkoutSet::getSetNumber).max().orElse(0) + 1;

        WorkoutSet ws = new WorkoutSet();
        ws.setWorkoutExercise(we);
        ws.setSetNumber(nextNumber);
        ws.setReps(req.getReps());
        ws.setWeightKg(BigDecimal.valueOf(req.getWeightKg()));
        if (req.getRpe() != null) ws.setRpe(BigDecimal.valueOf(req.getRpe()));
        we.getSets().add(ws);

        workoutRepo.save(workout);
        return ws;
    }

    @Transactional
    public WorkoutSet updateSet(Long workoutId, Long exerciseId, Long setId, CreateSetRequest req) {
        WorkoutSet ws = findSet(workoutId, exerciseId, setId);
        ws.setReps(req.getReps());
        ws.setWeightKg(BigDecimal.valueOf(req.getWeightKg()));
        ws.setRpe(req.getRpe() != null ? BigDecimal.valueOf(req.getRpe()) : null);
        workoutRepo.save(ws.getWorkoutExercise().getWorkout());
        return ws;
    }

    @Transactional
    public void deleteSet(Long workoutId, Long exerciseId, Long setId) {
        WorkoutSet ws = findSet(workoutId, exerciseId, setId);
        ws.getWorkoutExercise().getSets().remove(ws);
        workoutRepo.save(ws.getWorkoutExercise().getWorkout());
    }

    private WorkoutExercise attachExercise(Workout workout, Exercise exercise) {
        int nextPos = workout.getExercises().stream()
                .mapToInt(WorkoutExercise::getPosition).max().orElse(0) + 1;
        WorkoutExercise we = new WorkoutExercise();
        we.setWorkout(workout);
        we.setExercise(exercise);
        we.setPosition(nextPos);
        workout.getExercises().add(we);
        return we;
    }

    private WorkoutSet findSet(Long workoutId, Long exerciseId, Long setId) {
        Workout workout = workoutRepo.findByIdWithDetail(workoutId)
                .orElseThrow(() -> new NotFoundException("Workout " + workoutId + " not found"));
        WorkoutExercise we = workout.getExercises().stream()
                .filter(x -> x.getExercise().getId().equals(exerciseId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(
                        "Exercise " + exerciseId + " not in workout " + workoutId));
        return we.getSets().stream()
                .filter(s -> s.getId().equals(setId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Set " + setId + " not found"));
    }
}
