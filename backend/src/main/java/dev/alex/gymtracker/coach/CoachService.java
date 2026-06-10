package dev.alex.gymtracker.coach;

import dev.alex.gymtracker.common.ServiceUnavailableException;
import dev.alex.gymtracker.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CoachService {

    private static final Logger log = LoggerFactory.getLogger(CoachService.class);

    private final CoachContextRepository repo;
    private final CoachAiClient aiClient;
    private final AppProperties props;

    public CoachService(CoachContextRepository repo, CoachAiClient aiClient,
                        AppProperties props) {
        this.repo = repo;
        this.aiClient = aiClient;
        this.props = props;
    }

    public CoachAnswer ask(String message, boolean includeRecentWorkouts,
                           boolean includePersonalRecords) {
        if (!aiClient.isConfigured()) {
            throw new ServiceUnavailableException("AI coach is not configured");
        }

        List<RecentWorkoutLine> workoutLines = includeRecentWorkouts
                ? repo.recentWorkoutLines() : List.of();
        List<CoachPrLine> prLines = includePersonalRecords
                ? repo.personalRecords() : List.of();

        String systemPrompt = buildSystemPrompt(workoutLines, prLines,
                includeRecentWorkouts, includePersonalRecords);

        String reply;
        try {
            reply = aiClient.ask(systemPrompt, message);
        } catch (RuntimeException ex) {
            log.warn("Anthropic API call failed: {}", ex.getMessage(), ex);
            throw new ServiceUnavailableException("AI coach is temporarily unavailable");
        }

        long workoutCount = workoutLines.stream()
                .map(RecentWorkoutLine::workoutId).distinct().count();
        String contextSummary = buildContextSummary(
                includeRecentWorkouts, workoutCount,
                includePersonalRecords, prLines.size());

        return new CoachAnswer(reply, contextSummary);
    }

    private String buildSystemPrompt(List<RecentWorkoutLine> workoutLines,
                                     List<CoachPrLine> prLines,
                                     boolean includeWorkouts,
                                     boolean includePrs) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a personal gym coach assistant. Be concise and actionable (max 3-4 sentences).\n\n");
        sb.append("USER PROFILE:\n");
        String profile = props.coachProfileNotes();
        sb.append((profile == null || profile.isBlank()) ? "No profile configured." : profile);

        if (includeWorkouts) {
            sb.append("\n\nRECENT WORKOUTS (last 5 finished):\n");
            if (workoutLines.isEmpty()) {
                sb.append("None.");
            } else {
                // Group by workoutId, preserving SQL order
                Map<Long, List<RecentWorkoutLine>> byWorkout = workoutLines.stream()
                        .collect(Collectors.groupingBy(
                                RecentWorkoutLine::workoutId,
                                LinkedHashMap::new,
                                Collectors.toList()));
                for (Map.Entry<Long, List<RecentWorkoutLine>> entry : byWorkout.entrySet()) {
                    List<RecentWorkoutLine> exercises = entry.getValue();
                    String date = exercises.get(0).workoutDate().toString();
                    String exercisePart = exercises.stream().map(e -> {
                        if (e.setCount() == 0) return e.exerciseName() + " — 0 sets";
                        return String.format("%s — %d sets, max %.1f kg x %d reps",
                                e.exerciseName(), e.setCount(),
                                e.maxWeightKg() != null ? e.maxWeightKg().doubleValue() : 0.0,
                                e.maxReps() != null ? e.maxReps() : 0);
                    }).collect(Collectors.joining("; "));
                    sb.append("- ").append(date).append(": ").append(exercisePart).append("\n");
                }
            }
        }

        if (includePrs) {
            sb.append("\nPERSONAL RECORDS:\n");
            if (prLines.isEmpty()) {
                sb.append("None.");
            } else {
                for (CoachPrLine pr : prLines) {
                    sb.append(String.format("- %s: max weight %.1f kg, max reps %d, est. 1RM %.1f kg\n",
                            pr.exerciseName(),
                            pr.maxWeightKg() != null ? pr.maxWeightKg().doubleValue() : 0.0,
                            pr.maxReps() != null ? pr.maxReps() : 0,
                            pr.maxOneRmKg() != null ? pr.maxOneRmKg().doubleValue() : 0.0));
                }
            }
        }

        return sb.toString();
    }

    private String buildContextSummary(boolean includeWorkouts, long workoutCount,
                                       boolean includePrs, int prCount) {
        if (!includeWorkouts && !includePrs) return "Used: no training context";
        if (includeWorkouts && includePrs)
            return String.format("Used: %d recent workouts, %d PRs", workoutCount, prCount);
        if (includeWorkouts)
            return String.format("Used: %d recent workouts", workoutCount);
        return String.format("Used: %d PRs", prCount);
    }
}
