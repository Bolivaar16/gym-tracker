package dev.alex.gymtracker.stats;

import dev.alex.gymtracker.api.StatsApi;
import dev.alex.gymtracker.api.model.ExerciseRef;
import dev.alex.gymtracker.api.model.MuscleGroup;
import dev.alex.gymtracker.api.model.OneRmPoint;
import dev.alex.gymtracker.api.model.PersonalRecord;
import dev.alex.gymtracker.api.model.StatsSummary;
import dev.alex.gymtracker.api.model.VolumeGroupBy;
import dev.alex.gymtracker.api.model.VolumePoint;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class StatsController implements StatsApi {

    private final StatsService service;

    public StatsController(StatsService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<List<VolumePoint>> getVolumeStats(
            VolumeGroupBy groupBy, MuscleGroup muscleGroup, LocalDate from, LocalDate to) {
        String mgValue = muscleGroup != null ? muscleGroup.getValue() : null;
        List<VolumeRow> rows = service.volume(groupBy.getValue(), mgValue, from, to);
        List<VolumePoint> result = rows.stream().map(r -> {
            VolumePoint p = new VolumePoint();
            p.setPeriod(r.getPeriod());
            p.setVolumeKg(r.getVolumeKg() != null ? r.getVolumeKg().doubleValue() : 0.0);
            return p;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<List<OneRmPoint>> getExerciseOneRm(
            Long id, LocalDate from, LocalDate to) {
        List<OneRmRow> rows = service.oneRmSeries(id, from, to);
        List<OneRmPoint> result = rows.stream().map(r -> {
            OneRmPoint p = new OneRmPoint();
            p.setDate(r.getWorkoutDate());
            p.setWorkoutId(r.getWorkoutId());
            p.setEstimatedOneRmKg(r.getEstimatedOneRmKg() != null
                    ? r.getEstimatedOneRmKg().doubleValue() : 0.0);
            return p;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<List<PersonalRecord>> getPersonalRecords() {
        List<PrRow> rows = service.personalRecords();
        List<PersonalRecord> result = rows.stream().map(r -> {
            ExerciseRef ref = new ExerciseRef();
            ref.setId(r.getExerciseId());
            ref.setName(r.getExerciseName());

            PersonalRecord pr = new PersonalRecord();
            pr.setExercise(ref);
            pr.setMaxWeightKg(r.getMaxWeightKg() != null ? r.getMaxWeightKg().doubleValue() : 0.0);
            pr.setMaxWeightDate(r.getMaxWeightDate());
            pr.setMaxWeightWorkoutId(r.getMaxWeightWorkoutId());
            pr.setMaxOneRmKg(r.getMaxOneRmKg() != null ? r.getMaxOneRmKg().doubleValue() : 0.0);
            pr.setMaxOneRmDate(r.getMaxOneRmDate());
            pr.setMaxOneRmWorkoutId(r.getMaxOneRmWorkoutId());
            pr.setMaxReps(r.getMaxReps() != null ? r.getMaxReps() : 0);
            pr.setMaxRepsDate(r.getMaxRepsDate());
            pr.setMaxRepsWorkoutId(r.getMaxRepsWorkoutId());
            return pr;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<StatsSummary> getStatsSummary() {
        SummaryRow counts = service.summaryCounts();
        long streak = service.currentStreakDays();

        StatsSummary dto = new StatsSummary();
        dto.setWorkoutsThisWeek(counts.getWorkoutsThisWeek() != null
                ? Math.toIntExact(counts.getWorkoutsThisWeek()) : 0);
        dto.setWorkoutsThisMonth(counts.getWorkoutsThisMonth() != null
                ? Math.toIntExact(counts.getWorkoutsThisMonth()) : 0);
        dto.setTotalVolumeThisWeekKg(counts.getTotalVolumeThisWeekKg() != null
                ? counts.getTotalVolumeThisWeekKg().doubleValue() : 0.0);
        dto.setCurrentStreakDays(Math.toIntExact(streak));
        return ResponseEntity.ok(dto);
    }
}
