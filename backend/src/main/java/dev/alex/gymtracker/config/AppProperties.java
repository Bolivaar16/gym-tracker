package dev.alex.gymtracker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        String username,
        String passwordHash,
        String jwtSecret,
        long jwtTtlSeconds,
        String coachProfileNotes
) {}
