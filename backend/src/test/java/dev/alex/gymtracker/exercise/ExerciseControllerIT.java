package dev.alex.gymtracker.exercise;

import dev.alex.gymtracker.common.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ExerciseControllerIT extends IntegrationTestBase {

    @Test
    void listExercises_returnsSeeded() throws Exception {
        mockMvc.perform(get("/api/exercises").header("Authorization", "Bearer " + token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThan(0)));
    }

    @Test
    void createAndGetExercise_happyPath() throws Exception {
        String body = """
                {"name":"Test Exercise","muscleGroups":["CHEST"],"defaultRestSeconds":90}
                """;
        String location = mockMvc.perform(post("/api/exercises")
                        .header("Authorization", "Bearer " + token())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Exercise"))
                .andExpect(jsonPath("$.archived").value(false))
                .andReturn().getResponse().getContentAsString();

        long id = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(location).get("id").asLong();

        mockMvc.perform(get("/api/exercises/" + id).header("Authorization", "Bearer " + token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.muscleGroups[0]").value("CHEST"));
    }

    @Test
    void createExercise_duplicateName_returns409() throws Exception {
        String body = """
                {"name":"Barbell Bench Press","muscleGroups":["CHEST"]}
                """;
        mockMvc.perform(post("/api/exercises")
                        .header("Authorization", "Bearer " + token())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteExercise_archivesIt() throws Exception {
        String body = """
                {"name":"Exercise To Archive","muscleGroups":["CORE"]}
                """;
        String resp = mockMvc.perform(post("/api/exercises")
                        .header("Authorization", "Bearer " + token())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long id = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(resp).get("id").asLong();

        mockMvc.perform(delete("/api/exercises/" + id).header("Authorization", "Bearer " + token()))
                .andExpect(status().isNoContent());

        // still retrievable but archived=true
        mockMvc.perform(get("/api/exercises/" + id).header("Authorization", "Bearer " + token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.archived").value(true));

        // not in default list
        mockMvc.perform(get("/api/exercises").header("Authorization", "Bearer " + token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id==" + id + ")]").isEmpty());
    }

    @Test
    void getExercise_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/exercises/999999").header("Authorization", "Bearer " + token()))
                .andExpect(status().isNotFound());
    }

    @Test
    void unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/exercises"))
                .andExpect(status().isUnauthorized());
    }
}
