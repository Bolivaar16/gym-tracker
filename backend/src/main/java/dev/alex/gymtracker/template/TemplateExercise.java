package dev.alex.gymtracker.template;

import dev.alex.gymtracker.exercise.Exercise;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "template_exercise",
       uniqueConstraints = @UniqueConstraint(columnNames = {"template_id", "exercise_id"}))
public class TemplateExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_id", nullable = false)
    private Template template;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Column(nullable = false)
    private int position;

    @Column(name = "target_sets", nullable = false)
    private int targetSets = 3;

    @Column(name = "target_reps", nullable = false)
    private int targetReps = 10;

    @Column(name = "target_weight_kg", precision = 6, scale = 2)
    private BigDecimal targetWeightKg;

    public Long getId() { return id; }
    public Template getTemplate() { return template; }
    public void setTemplate(Template template) { this.template = template; }
    public Exercise getExercise() { return exercise; }
    public void setExercise(Exercise exercise) { this.exercise = exercise; }
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
    public int getTargetSets() { return targetSets; }
    public void setTargetSets(int targetSets) { this.targetSets = targetSets; }
    public int getTargetReps() { return targetReps; }
    public void setTargetReps(int targetReps) { this.targetReps = targetReps; }
    public BigDecimal getTargetWeightKg() { return targetWeightKg; }
    public void setTargetWeightKg(BigDecimal targetWeightKg) { this.targetWeightKg = targetWeightKg; }
}
