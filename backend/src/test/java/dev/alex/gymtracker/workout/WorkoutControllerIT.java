package dev.alex.gymtracker.workout;

import dev.alex.gymtracker.common.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class WorkoutControllerIT extends IntegrationTestBase {

    @Test
    void createWorkout_happyPath() throws Exception {
        mockMvc.perform(post("/api/workouts")
                        .header("Authorization", "Bearer " + token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.startedAt").exists())
                .andExpect(jsonPath("$.exercises").isArray());
    }

    @Test
    void listWorkouts_pagination() throws Exception {
        createWorkout();
        mockMvc.perform(get("/api/workouts?page=0&size=10")
                        .header("Authorization", "Bearer " + token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.totalElements").value(org.hamcrest.Matchers.greaterThan(0)));
    }

    @Test
    void logSet_autoAttachesExercise() throws Exception {
        long workoutId = createWorkout();
        long exerciseId = getFirstExerciseId();

        String setBody = """
                {"reps":8,"weightKg":80.0}
                """;
        mockMvc.perform(post("/api/workouts/" + workoutId + "/exercises/" + exerciseId + "/sets")
                        .header("Authorization", "Bearer " + token())
                        .contentType(MediaType.APPLICATION_JSON).content(setBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reps").value(8))
                .andExpect(jsonPath("$.weightKg").value(80.0))
                .andExpect(jsonPath("$.setNumber").value(1));

        // exercise now appears in workout detail
        mockMvc.perform(get("/api/workouts/" + workoutId)
                        .header("Authorization", "Bearer " + token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exercises.length()").value(1))
                .andExpect(jsonPath("$.exercises[0].sets.length()").value(1));
    }

    @Test
    void logSet_setNumberIncrementsPerExercise() throws Exception {
        long workoutId = createWorkout();
        long exerciseId = getFirstExerciseId();
        String setBody = """
                {"reps":5,"weightKg":100.0}
                """;
        for (int i = 1; i <= 3; i++) {
            mockMvc.perform(post("/api/workouts/" + workoutId + "/exercises/" + exerciseId + "/sets")
                            .header("Authorization", "Bearer " + token())
                            .contentType(MediaType.APPLICATION_JSON).content(setBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.setNumber").value(i));
        }
    }

    @Test
    void deleteWorkout_removesItFromList() throws Exception {
        long id = createWorkout();
        mockMvc.perform(delete("/api/workouts/" + id)
                        .header("Authorization", "Bearer " + token()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/workouts/" + id)
                        .header("Authorization", "Bearer " + token()))
                .andExpect(status().isNotFound());
    }

    @Test
    void logSet_nonExistentWorkout_returns404() throws Exception {
        long exerciseId = getFirstExerciseId();
        mockMvc.perform(post("/api/workouts/999999/exercises/" + exerciseId + "/sets")
                        .header("Authorization", "Bearer " + token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reps\":5,\"weightKg\":50.0}"))
                .andExpect(status().isNotFound());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private long createWorkout() throws Exception {
        String resp = mockMvc.perform(post("/api/workouts")
                        .header("Authorization", "Bearer " + token())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andReturn().getResponse().getContentAsString();
        return com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(resp).get("id").asLong();
    }

    private long getFirstExerciseId() throws Exception {
        String resp = mockMvc.perform(get("/api/exercises")
                        .header("Authorization", "Bearer " + token()))
                .andReturn().getResponse().getContentAsString();
        return com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(resp).get(0).get("id").asLong();
    }
}
