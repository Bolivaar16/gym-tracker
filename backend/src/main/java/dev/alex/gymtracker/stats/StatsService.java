package dev.alex.gymtracker.stats;

import dev.alex.gymtracker.common.NotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class StatsService {

    private final StatsRepository repo;

    public StatsService(StatsRepository repo) {
        this.repo = repo;
    }

    public List<VolumeRow> volume(String groupBy, String muscleGroup,
                                  LocalDate from, LocalDate to) {
        return "month".equals(groupBy)
                ? repo.volumeByMonth(muscleGroup, from, to)
                : repo.volumeByWeek(muscleGroup, from, to);
    }

    public List<OneRmRow> oneRmSeries(Long exerciseId, LocalDate from, LocalDate to) {
        if (!repo.exerciseExists(exerciseId)) {
            throw new NotFoundException("Exercise " + exerciseId + " not found");
        }
        return repo.oneRmSeries(exerciseId, from, to);
    }

    public List<PrRow> personalRecords() {
        return repo.personalRecords();
    }

    public SummaryRow summaryCounts() {
        return repo.summaryCounts();
    }

    public long currentStreakDays() {
        return repo.currentStreakDays();
    }
}
