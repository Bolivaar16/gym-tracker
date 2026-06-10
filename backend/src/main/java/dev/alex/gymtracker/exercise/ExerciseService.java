package dev.alex.gymtracker.exercise;

import dev.alex.gymtracker.api.model.CreateExerciseRequest;
import dev.alex.gymtracker.common.ConflictException;
import dev.alex.gymtracker.common.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
public class ExerciseService {

    private final ExerciseRepository repo;

    public ExerciseService(ExerciseRepository repo) {
        this.repo = repo;
    }

    public List<Exercise> list(MuscleGroup muscleGroup, boolean includeArchived) {
        String mg = muscleGroup != null ? muscleGroup.name() : null;
        return repo.findByFilter(mg, includeArchived);
    }

    public Exercise get(Long id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("Exercise " + id + " not found"));
    }

    @Transactional
    public Exercise create(CreateExerciseRequest req) {
        if (repo.existsByNameIgnoreCase(req.getName())) {
            throw new ConflictException("An exercise named '" + req.getName() + "' already exists");
        }
        Exercise e = new Exercise();
        applyRequest(e, req);
        return repo.save(e);
    }

    @Transactional
    public Exercise update(Long id, CreateExerciseRequest req) {
        Exercise e = get(id);
        applyRequest(e, req);
        return repo.save(e);
    }

    @Transactional
    public void archive(Long id) {
        Exercise e = get(id);
        e.setArchived(true);
        repo.save(e);
    }

    private void applyRequest(Exercise e, CreateExerciseRequest req) {
        e.setName(req.getName());
        e.setMuscleGroups(
            req.getMuscleGroups().stream()
                .map(mg -> mg.getValue())
                .toArray(String[]::new)
        );
        e.setDefaultRestSeconds(req.getDefaultRestSeconds() != null ? req.getDefaultRestSeconds() : 120);
        e.setNotes(req.getNotes());
    }
}
