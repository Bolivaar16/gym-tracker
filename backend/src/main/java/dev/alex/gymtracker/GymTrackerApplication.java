package dev.alex.gymtracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class GymTrackerApplication {
    public static void main(String[] args) {
        SpringApplication.run(GymTrackerApplication.class, args);
    }
}
