package dev.alex.gymtracker.common;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        int status,
        String error,
        String message,
        Instant timestamp,
        List<FieldError> details
) {
    public record FieldError(String field, String message) {}
}
