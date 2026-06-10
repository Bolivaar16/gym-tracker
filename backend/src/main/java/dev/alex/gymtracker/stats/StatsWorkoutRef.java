package dev.alex.gymtracker.stats;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "workout")
class StatsWorkoutRef {
    @Id
    private Long id;
}
