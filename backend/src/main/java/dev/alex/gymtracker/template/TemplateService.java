package dev.alex.gymtracker.template;

import dev.alex.gymtracker.api.model.CreateTemplateRequest;
import dev.alex.gymtracker.api.model.TemplateExerciseInput;
import dev.alex.gymtracker.common.BadRequestException;
import dev.alex.gymtracker.common.ConflictException;
import dev.alex.gymtracker.common.NotFoundException;
import dev.alex.gymtracker.exercise.Exercise;
import dev.alex.gymtracker.exercise.ExerciseRepository;
import dev.alex.gymtracker.workout.Workout;
import dev.alex.gymtracker.workout.WorkoutExercise;
import dev.alex.gymtracker.workout.WorkoutRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class TemplateService {

    private final TemplateRepository templateRepo;
    private final ExerciseRepository exerciseRepo;
    private final WorkoutRepository workoutRepo;

    public TemplateService(TemplateRepository templateRepo,
                           ExerciseRepository exerciseRepo,
                           WorkoutRepository workoutRepo) {
        this.templateRepo = templateRepo;
        this.exerciseRepo = exerciseRepo;
        this.workoutRepo = workoutRepo;
    }

    public List<TemplateSummaryRow> listSummaries() {
        return templateRepo.findSummaries();
    }

    public Template getDetail(Long id) {
        return templateRepo.findByIdWithDetail(id)
                .orElseThrow(() -> new NotFoundException("Template " + id + " not found"));
    }

    @Transactional
    public Template create(CreateTemplateRequest req) {
        if (templateRepo.existsByNameIgnoreCase(req.getName())) {
            throw new ConflictException("A template named '" + req.getName() + "' already exists");
        }
        Template t = new Template();
        t.setName(req.getName());
        t.setNotes(req.getNotes());
        if (req.getExercises() != null) {
            applyExercises(t, req.getExercises());
        }
        return templateRepo.save(t);
    }

    @Transactional
    public Template update(Long id, CreateTemplateRequest req) {
        Template t = getDetail(id);
        if (templateRepo.existsByNameIgnoreCaseAndIdNot(req.getName(), id)) {
            throw new ConflictException("A template named '" + req.getName() + "' already exists");
        }
        t.setName(req.getName());
        t.setNotes(req.getNotes());
        // Flush deletes first to avoid UNIQUE(template_id, exercise_id) on re-insert
        t.getExercises().clear();
        templateRepo.saveAndFlush(t);
        if (req.getExercises() != null) {
            applyExercises(t, req.getExercises());
        }
        return templateRepo.save(t);
    }

    @Transactional
    public void delete(Long id) {
        if (!templateRepo.existsById(id)) {
            throw new NotFoundException("Template " + id + " not found");
        }
        templateRepo.deleteById(id);
    }

    @Transactional
    public Workout start(Long id) {
        Template t = templateRepo.findByIdWithDetail(id)
                .orElseThrow(() -> new NotFoundException("Template " + id + " not found"));
        Workout w = new Workout();
        w.setTemplateId(t.getId());
        for (TemplateExercise te : t.getExercises()) {
            WorkoutExercise we = new WorkoutExercise();
            we.setWorkout(w);
            we.setExercise(te.getExercise());
            we.setPosition(te.getPosition());
            w.getExercises().add(we);
        }
        return workoutRepo.save(w);
    }

    private void applyExercises(Template t, List<TemplateExerciseInput> inputs) {
        Set<Long> seen = new HashSet<>();
        for (int i = 0; i < inputs.size(); i++) {
            TemplateExerciseInput input = inputs.get(i);
            if (!seen.add(input.getExerciseId())) {
                throw new BadRequestException(
                        "Duplicate exerciseId " + input.getExerciseId() + " in exercise list");
            }
            Exercise ex = exerciseRepo.findById(input.getExerciseId())
                    .orElseThrow(() -> new NotFoundException(
                            "Exercise " + input.getExerciseId() + " not found"));
            TemplateExercise te = new TemplateExercise();
            te.setTemplate(t);
            te.setExercise(ex);
            te.setPosition(i + 1);
            te.setTargetSets(input.getTargetSets() != null ? input.getTargetSets() : 3);
            te.setTargetReps(input.getTargetReps() != null ? input.getTargetReps() : 10);
            if (input.getTargetWeightKg() != null) {
                te.setTargetWeightKg(BigDecimal.valueOf(input.getTargetWeightKg().doubleValue()));
            }
            t.getExercises().add(te);
        }
    }
}
