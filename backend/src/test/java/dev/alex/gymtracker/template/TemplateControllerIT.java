package dev.alex.gymtracker.template;

import dev.alex.gymtracker.common.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TemplateControllerIT extends IntegrationTestBase {

    // ── Test Cases 1-13 ───────────────────────────────────────────────────────

    @Test
    void createTemplate_happyPath() throws Exception {
        List<Long> exerciseIds = getFirstTwoExerciseIds();
        String body = """
                {
                  "name":"Full Body Template",
                  "exercises":[
                    {"exerciseId":%d},
                    {"exerciseId":%d}
                  ]
                }
                """.formatted(exerciseIds.get(0), exerciseIds.get(1));

        mockMvc.perform(post("/api/templates")
                        .header("Authorization", "Bearer " + token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Full Body Template"))
                .andExpect(jsonPath("$.exercises.length()").value(2))
                .andExpect(jsonPath("$.exercises[0].position").value(1))
                .andExpect(jsonPath("$.exercises[1].position").value(2))
                .andExpect(jsonPath("$.exercises[0].targetSets").value(3))
                .andExpect(jsonPath("$.exercises[0].targetReps").value(10));
    }

    @Test
    void createTemplate_duplicateName_returns409() throws Exception {
        List<Long> exerciseIds = getFirstTwoExerciseIds();
        String body = """
                {
                  "name":"Unique Template Name",
                  "exercises":[{"exerciseId":%d}]
                }
                """.formatted(exerciseIds.get(0));

        // First creation succeeds
        mockMvc.perform(post("/api/templates")
                        .header("Authorization", "Bearer " + token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        // Second creation with same name fails
        mockMvc.perform(post("/api/templates")
                        .header("Authorization", "Bearer " + token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void createTemplate_unknownExerciseId_returns404() throws Exception {
        String body = """
                {
                  "name":"Template with Invalid Exercise",
                  "exercises":[{"exerciseId":999999}]
                }
                """;

        mockMvc.perform(post("/api/templates")
                        .header("Authorization", "Bearer " + token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void createTemplate_duplicateExerciseInList_returns400() throws Exception {
        long exerciseId = getFirstExerciseId();
        String body = """
                {
                  "name":"Template with Duplicate",
                  "exercises":[
                    {"exerciseId":%d},
                    {"exerciseId":%d}
                  ]
                }
                """.formatted(exerciseId, exerciseId);

        mockMvc.perform(post("/api/templates")
                        .header("Authorization", "Bearer " + token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listTemplates_returnsSummariesWithExerciseCount() throws Exception {
        List<Long> exerciseIds = getFirstTwoExerciseIds();
        long templateId = createTemplate("List Test Template", exerciseIds);

        mockMvc.perform(get("/api/templates")
                        .header("Authorization", "Bearer " + token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id==" + templateId + ")].exerciseCount").value(2));
    }

    @Test
    void getTemplate_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/templates/999999")
                        .header("Authorization", "Bearer " + token()))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTemplate_fullReplace() throws Exception {
        List<Long> exerciseIds = getFirstTwoExerciseIds();
        long templateId = createTemplate("Original Template", exerciseIds);

        // Create a different third exercise
        long thirdExerciseId = exerciseIds.size() > 2 ? exerciseIds.get(2) : getFirstExerciseId();

        String updateBody = """
                {
                  "name":"Original Template",
                  "exercises":[{"exerciseId":%d}]
                }
                """.formatted(thirdExerciseId);

        mockMvc.perform(put("/api/templates/" + templateId)
                        .header("Authorization", "Bearer " + token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk());

        // Verify the update removed the old exercises
        mockMvc.perform(get("/api/templates/" + templateId)
                        .header("Authorization", "Bearer " + token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exercises.length()").value(1))
                .andExpect(jsonPath("$.exercises[0].position").value(1));
    }

    @Test
    void deleteTemplate_removesIt() throws Exception {
        List<Long> exerciseIds = getFirstTwoExerciseIds();
        long templateId = createTemplate("Template to Delete", exerciseIds);

        mockMvc.perform(delete("/api/templates/" + templateId)
                        .header("Authorization", "Bearer " + token()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/templates/" + templateId)
                        .header("Authorization", "Bearer " + token()))
                .andExpect(status().isNotFound());
    }

    @Test
    void startWorkout_createsPopulatedWorkout() throws Exception {
        List<Long> exerciseIds = getFirstTwoExerciseIds();
        long templateId = createTemplate("Workout Template", exerciseIds);

        String resp = mockMvc.perform(post("/api/templates/" + templateId + "/start")
                        .header("Authorization", "Bearer " + token())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.exercises.length()").value(2))
                .andExpect(jsonPath("$.exercises[0].sets").isArray())
                .andExpect(jsonPath("$.exercises[1].sets").isArray())
                .andExpect(jsonPath("$.startedAt").exists())
                .andReturn().getResponse().getContentAsString();

        // Verify exercises match template order
        mockMvc.perform(get("/api/templates/" + templateId)
                        .header("Authorization", "Bearer " + token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exercises[0].position").value(1))
                .andExpect(jsonPath("$.exercises[1].position").value(2));
    }

    @Test
    void startWorkout_copySemantics() throws Exception {
        List<Long> exerciseIds = getFirstTwoExerciseIds();
        long templateId = createTemplate("Original Workout Template", exerciseIds);

        // Start a workout from this template
        String workoutResp = mockMvc.perform(post("/api/templates/" + templateId + "/start")
                        .header("Authorization", "Bearer " + token())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long workoutId = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(workoutResp).get("id").asLong();

        // Now modify the template to only have the first exercise
        String updateBody = """
                {
                  "name":"Original Workout Template",
                  "exercises":[{"exerciseId":%d}]
                }
                """.formatted(exerciseIds.get(0));

        mockMvc.perform(put("/api/templates/" + templateId)
                        .header("Authorization", "Bearer " + token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk());

        // Verify workout still has both original exercises (copy semantics)
        mockMvc.perform(get("/api/workouts/" + workoutId)
                        .header("Authorization", "Bearer " + token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exercises.length()").value(2));
    }

    @Test
    void deleteTemplate_keepsStartedWorkouts() throws Exception {
        List<Long> exerciseIds = getFirstTwoExerciseIds();
        long templateId = createTemplate("Template with Workouts", exerciseIds);

        // Start a workout
        String workoutResp = mockMvc.perform(post("/api/templates/" + templateId + "/start")
                        .header("Authorization", "Bearer " + token())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long workoutId = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(workoutResp).get("id").asLong();

        // Delete the template
        mockMvc.perform(delete("/api/templates/" + templateId)
                        .header("Authorization", "Bearer " + token()))
                .andExpect(status().isNoContent());

        // Verify the workout still exists with exercises intact
        mockMvc.perform(get("/api/workouts/" + workoutId)
                        .header("Authorization", "Bearer " + token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exercises.length()").value(2));
    }

    @Test
    void startWorkout_templateNotFound_returns404() throws Exception {
        mockMvc.perform(post("/api/templates/999999/start")
                        .header("Authorization", "Bearer " + token())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void templates_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/templates"))
                .andExpect(status().isUnauthorized());
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private long createTemplate(String name, List<Long> exerciseIds) throws Exception {
        StringBuilder exercisesJson = new StringBuilder();
        for (int i = 0; i < exerciseIds.size(); i++) {
            if (i > 0) exercisesJson.append(",");
            exercisesJson.append("""
                    {"exerciseId":%d}
                    """.formatted(exerciseIds.get(i)));
        }

        String body = """
                {
                  "name":"%s",
                  "exercises":[%s]
                }
                """.formatted(name, exercisesJson);

        String resp = mockMvc.perform(post("/api/templates")
                        .header("Authorization", "Bearer " + token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn().getResponse().getContentAsString();

        return com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(resp).get("id").asLong();
    }

    private List<Long> getFirstTwoExerciseIds() throws Exception {
        String resp = mockMvc.perform(get("/api/exercises")
                        .header("Authorization", "Bearer " + token()))
                .andReturn().getResponse().getContentAsString();

        var tree = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(resp);
        return Arrays.asList(
                tree.get(0).get("id").asLong(),
                tree.get(1).get("id").asLong()
        );
    }

    private long getFirstExerciseId() throws Exception {
        String resp = mockMvc.perform(get("/api/exercises")
                        .header("Authorization", "Bearer " + token()))
                .andReturn().getResponse().getContentAsString();
        return com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(resp).get(0).get("id").asLong();
    }
}
