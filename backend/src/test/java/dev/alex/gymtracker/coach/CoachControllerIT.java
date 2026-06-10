package dev.alex.gymtracker.coach;

import com.fasterxml.jackson.databind.json.JsonMapper;
import dev.alex.gymtracker.common.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CoachControllerIT extends IntegrationTestBase {

    @MockBean
    CoachAiClient coachAiClient;

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Test
    void askCoach_noToken_returns401() throws Exception {
        mockMvc.perform(post("/api/coach/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"test\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void askCoach_blankMessage_returns400() throws Exception {
        when(coachAiClient.isConfigured()).thenReturn(true);
        mockMvc.perform(post("/api/coach/ask")
                        .header("Authorization", "Bearer " + token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void askCoach_notConfigured_returns503() throws Exception {
        when(coachAiClient.isConfigured()).thenReturn(false);
        mockMvc.perform(post("/api/coach/ask")
                        .header("Authorization", "Bearer " + token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"What should I do?\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("AI coach is not configured"));
    }

    @Test
    void askCoach_happyPath_returnsReply() throws Exception {
        when(coachAiClient.isConfigured()).thenReturn(true);
        when(coachAiClient.ask(any(), any())).thenReturn("Focus on legs today!");

        // Seed an exercise + finished workout
        String exBody = "{\"name\":\"Coach Test Squat\",\"muscleGroups\":[\"LEGS\"]}";
        String exResp = mockMvc.perform(post("/api/exercises")
                        .header("Authorization", "Bearer " + token())
                        .contentType(MediaType.APPLICATION_JSON).content(exBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long exId = JSON.readTree(exResp).get("id").asLong();

        String wResp = mockMvc.perform(post("/api/workouts")
                        .header("Authorization", "Bearer " + token())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long wId = JSON.readTree(wResp).get("id").asLong();

        mockMvc.perform(post("/api/workouts/" + wId + "/exercises/" + exId + "/sets")
                        .header("Authorization", "Bearer " + token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reps\":5,\"weightKg\":100.0}"))
                .andExpect(status().isCreated());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put("/api/workouts/" + wId + "/finish")
                        .header("Authorization", "Bearer " + token()))
                .andExpect(status().isOk());

        String body = "{\"message\":\"What should I do?\",\"includeRecentWorkouts\":true,\"includePersonalRecords\":true}";
        mockMvc.perform(post("/api/coach/ask")
                        .header("Authorization", "Bearer " + token())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply").value("Focus on legs today!"))
                .andExpect(jsonPath("$.contextSummary").value(org.hamcrest.Matchers.containsString("recent workout")));
    }

    @Test
    void askCoach_noContext_promptHasNoSections() throws Exception {
        when(coachAiClient.isConfigured()).thenReturn(true);
        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        when(coachAiClient.ask(promptCaptor.capture(), any())).thenReturn("General advice.");

        String body = "{\"message\":\"Advice?\",\"includeRecentWorkouts\":false,\"includePersonalRecords\":false}";
        mockMvc.perform(post("/api/coach/ask")
                        .header("Authorization", "Bearer " + token())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contextSummary").value("Used: no training context"));

        String prompt = promptCaptor.getValue();
        org.junit.jupiter.api.Assertions.assertFalse(prompt.contains("RECENT WORKOUTS"),
                "Prompt should not contain RECENT WORKOUTS when flag is false");
        org.junit.jupiter.api.Assertions.assertFalse(prompt.contains("PERSONAL RECORDS"),
                "Prompt should not contain PERSONAL RECORDS when flag is false");
    }

    @Test
    void askCoach_upstreamFailure_returns503() throws Exception {
        when(coachAiClient.isConfigured()).thenReturn(true);
        when(coachAiClient.ask(any(), any())).thenThrow(new RuntimeException("upstream down"));

        mockMvc.perform(post("/api/coach/ask")
                        .header("Authorization", "Bearer " + token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Test?\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("AI coach is temporarily unavailable"));
    }

    @Test
    void askCoach_emptyDb_returnsSummaryWithZeros() throws Exception {
        when(coachAiClient.isConfigured()).thenReturn(true);
        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        when(coachAiClient.ask(promptCaptor.capture(), any())).thenReturn("Start training!");

        mockMvc.perform(post("/api/coach/ask")
                        .header("Authorization", "Bearer " + token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Where to start?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contextSummary").value("Used: 0 recent workouts, 0 PRs"));

        String prompt = promptCaptor.getValue();
        org.junit.jupiter.api.Assertions.assertTrue(prompt.contains("None."),
                "Prompt should contain None. for empty sections");
    }
}
