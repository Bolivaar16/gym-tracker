package dev.alex.gymtracker.stats;

import com.fasterxml.jackson.databind.json.JsonMapper;
import dev.alex.gymtracker.common.IntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class StatsControllerIT extends IntegrationTestBase {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    // Helper: create an exercise, return its id
    private long createExercise(String name, String muscleGroup) throws Exception {
        String body = String.format(
                "{\"name\":\"%s\",\"muscleGroups\":[\"%s\"]}", name, muscleGroup);
        String resp = mockMvc.perform(post("/api/exercises")
                        .header("Authorization", "Bearer " + token())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JSON.readTree(resp).get("id").asLong();
    }

    // Helper: start a workout, return its id
    private long startWorkout() throws Exception {
        String resp = mockMvc.perform(post("/api/workouts")
                        .header("Authorization", "Bearer " + token())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JSON.readTree(resp).get("id").asLong();
    }

    // Helper: add a set
    private void addSet(long workoutId, long exerciseId, int reps, double weightKg) throws Exception {
        String body = String.format("{\"reps\":%d,\"weightKg\":%.1f}", reps, weightKg);
        mockMvc.perform(post("/api/workouts/" + workoutId + "/exercises/" + exerciseId + "/sets")
                        .header("Authorization", "Bearer " + token())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
    }

    // Helper: finish a workout
    private void finishWorkout(long workoutId) throws Exception {
        mockMvc.perform(put("/api/workouts/" + workoutId + "/finish")
                        .header("Authorization", "Bearer " + token()))
                .andExpect(status().isOk());
    }

    // ── 401 tests ─────────────────────────────────────────────────────────────

    @Test
    void volume_noToken_returns401() throws Exception {
        mockMvc.perform(get("/api/stats/volume?groupBy=week"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void oneRm_noToken_returns401() throws Exception {
        mockMvc.perform(get("/api/stats/exercises/1/one-rm"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void prs_noToken_returns401() throws Exception {
        mockMvc.perform(get("/api/stats/prs"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void summary_noToken_returns401() throws Exception {
        mockMvc.perform(get("/api/stats/summary"))
                .andExpect(status().isUnauthorized());
    }

    // ── Volume ────────────────────────────────────────────────────────────────

    @Test
    void volume_empty_returns200EmptyArray() throws Exception {
        mockMvc.perform(get("/api/stats/volume?groupBy=week")
                        .header("Authorization", "Bearer " + token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void volume_groupByWeek_returnsPeriodAndVolume() throws Exception {
        long exId = createExercise("Squat Stats Week", "LEGS");
        long wId = startWorkout();
        addSet(wId, exId, 5, 100.0);   // 500 kg
        addSet(wId, exId, 5, 100.0);   // 500 kg  → 1000 total this week

        mockMvc.perform(get("/api/stats/volume?groupBy=week")
                        .header("Authorization", "Bearer " + token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThan(0)))
                .andExpect(jsonPath("$[0].period").value(org.hamcrest.Matchers.matchesPattern("\\d{4}-W\\d{2}")))
                .andExpect(jsonPath("$[0].volumeKg").value(org.hamcrest.Matchers.greaterThan(0.0)));
    }

    @Test
    void volume_groupByMonth_returnsPeriodWithYYYYMM() throws Exception {
        long exId = createExercise("Squat Stats Month", "LEGS");
        long wId = startWorkout();
        addSet(wId, exId, 3, 80.0);

        mockMvc.perform(get("/api/stats/volume?groupBy=month")
                        .header("Authorization", "Bearer " + token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThan(0)))
                .andExpect(jsonPath("$[0].period").value(org.hamcrest.Matchers.matchesPattern("\\d{4}-\\d{2}")));
    }

    @Test
    void volume_muscleGroupFilter_excludesOtherMuscles() throws Exception {
        long chestEx = createExercise("Bench Stats Filter", "CHEST");
        long legsEx  = createExercise("Squat Stats Filter", "LEGS");
        long wId = startWorkout();
        addSet(wId, chestEx, 10, 60.0);  // 600
        addSet(wId, legsEx, 10, 100.0);  // 1000 — should be excluded

        String resp = mockMvc.perform(get("/api/stats/volume?groupBy=week&muscleGroup=CHEST")
                        .header("Authorization", "Bearer " + token()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // volumeKg should be 600 (only chest), not 1600
        double volume = JSON.readTree(resp).get(0).get("volumeKg").asDouble();
        org.junit.jupiter.api.Assertions.assertTrue(volume < 1600,
                "Expected only chest volume but got " + volume);
    }

    // ── 1RM ───────────────────────────────────────────────────────────────────

    @Test
    void oneRm_unknownExercise_returns404() throws Exception {
        mockMvc.perform(get("/api/stats/exercises/999999/one-rm")
                        .header("Authorization", "Bearer " + token()))
                .andExpect(status().isNotFound());
    }

    @Test
    void oneRm_exerciseWithNoSets_returnsEmptyArray() throws Exception {
        long exId = createExercise("No Sets Exercise Stats", "CORE");
        mockMvc.perform(get("/api/stats/exercises/" + exId + "/one-rm")
                        .header("Authorization", "Bearer " + token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void oneRm_setsInWorkout_returnsEpleyMax() throws Exception {
        long exId = createExercise("Deadlift 1RM Stats", "BACK");
        long wId = startWorkout();
        // 100 kg × 5 reps → Epley = 100 * (1 + 5/30.0) = 116.67
        addSet(wId, exId, 5, 100.0);
        // 80 kg × 10 reps → Epley = 80 * (1 + 10/30.0) = 106.67  (lower)
        addSet(wId, exId, 10, 80.0);

        String resp = mockMvc.perform(get("/api/stats/exercises/" + exId + "/one-rm")
                        .header("Authorization", "Bearer " + token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andReturn().getResponse().getContentAsString();

        double e1rm = JSON.readTree(resp).get(0).get("estimatedOneRmKg").asDouble();
        org.junit.jupiter.api.Assertions.assertEquals(116.67, e1rm, 0.1);
    }

    // ── PRs ───────────────────────────────────────────────────────────────────

    @Test
    void prs_empty_returnsEmptyArray() throws Exception {
        mockMvc.perform(get("/api/stats/prs")
                        .header("Authorization", "Bearer " + token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void prs_differentPRsInDifferentSets() throws Exception {
        long exId = createExercise("OHP PR Stats", "SHOULDERS");
        long wId = startWorkout();
        // max weight: 120 kg × 1 rep  → 1RM = 124
        addSet(wId, exId, 1, 120.0);
        // max 1RM:   100 kg × 10 reps → 1RM = 133.3
        addSet(wId, exId, 10, 100.0);
        // max reps:   60 kg × 15 reps → 1RM = 90
        addSet(wId, exId, 15, 60.0);

        String resp = mockMvc.perform(get("/api/stats/prs")
                        .header("Authorization", "Bearer " + token()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        var arr = JSON.readTree(resp);
        // Find this exercise
        var prNode = arr.get(arr.size() - 1); // last by name or find by scanning
        for (var node : arr) {
            if (node.get("exercise").get("id").asLong() == exId) {
                prNode = node;
                break;
            }
        }
        org.junit.jupiter.api.Assertions.assertEquals(120.0, prNode.get("maxWeightKg").asDouble(), 0.01);
        org.junit.jupiter.api.Assertions.assertEquals(15, prNode.get("maxReps").asInt());
        double maxOneRm = prNode.get("maxOneRmKg").asDouble();
        org.junit.jupiter.api.Assertions.assertEquals(133.3, maxOneRm, 0.5);
    }

    // ── Summary ───────────────────────────────────────────────────────────────

    @Test
    void summary_emptyDb_returnsZeros() throws Exception {
        mockMvc.perform(get("/api/stats/summary")
                        .header("Authorization", "Bearer " + token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workoutsThisWeek").value(0))
                .andExpect(jsonPath("$.workoutsThisMonth").value(0))
                .andExpect(jsonPath("$.totalVolumeThisWeekKg").value(0.0))
                .andExpect(jsonPath("$.currentStreakDays").value(0));
    }

    @Test
    void summary_finishedWorkoutThisWeek_countedCorrectly() throws Exception {
        long exId = createExercise("Summary Test Exercise", "CHEST");
        long wId = startWorkout();
        addSet(wId, exId, 10, 50.0);  // 500 kg volume
        finishWorkout(wId);

        // Start an unfinished workout this week — should NOT count
        long unfinished = startWorkout();
        addSet(unfinished, exId, 10, 50.0);

        String resp = mockMvc.perform(get("/api/stats/summary")
                        .header("Authorization", "Bearer " + token()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        var node = JSON.readTree(resp);
        org.junit.jupiter.api.Assertions.assertTrue(node.get("workoutsThisWeek").asInt() >= 1);
        org.junit.jupiter.api.Assertions.assertTrue(node.get("totalVolumeThisWeekKg").asDouble() >= 500.0);
    }

    @Test
    void summary_streak_finishedTodayCountsAsOne() throws Exception {
        long exId = createExercise("Streak Test Exercise", "CORE");
        long wId1 = startWorkout();
        addSet(wId1, exId, 5, 20.0);
        finishWorkout(wId1);
        long wId2 = startWorkout();
        addSet(wId2, exId, 5, 20.0);
        finishWorkout(wId2);

        // Two finished workouts today → streak = at least 1
        mockMvc.perform(get("/api/stats/summary")
                        .header("Authorization", "Bearer " + token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStreakDays").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }
}
