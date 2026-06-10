package dev.alex.gymtracker.coach;

import dev.alex.gymtracker.api.CoachApi;
import dev.alex.gymtracker.api.model.CoachRequest;
import dev.alex.gymtracker.api.model.CoachResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CoachController implements CoachApi {

    private final CoachService service;

    public CoachController(CoachService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<CoachResponse> askCoach(CoachRequest coachRequest) {
        boolean includeWorkouts = !Boolean.FALSE.equals(coachRequest.getIncludeRecentWorkouts());
        boolean includePrs = !Boolean.FALSE.equals(coachRequest.getIncludePersonalRecords());
        CoachAnswer answer = service.ask(coachRequest.getMessage(), includeWorkouts, includePrs);
        CoachResponse resp = new CoachResponse();
        resp.setReply(answer.reply());
        resp.setContextSummary(answer.contextSummary());
        return ResponseEntity.ok(resp);
    }
}
