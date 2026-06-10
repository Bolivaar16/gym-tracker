package dev.alex.gymtracker.exercise;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "exercise")
public class Exercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "muscle_groups", columnDefinition = "text[]", nullable = false)
    private String[] muscleGroups;

    @Column(name = "default_rest_seconds", nullable = false)
    private int defaultRestSeconds = 120;

    private String notes;

    @Column(nullable = false)
    private boolean archived = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String[] getMuscleGroups() { return muscleGroups; }
    public void setMuscleGroups(String[] muscleGroups) { this.muscleGroups = muscleGroups; }

    public int getDefaultRestSeconds() { return defaultRestSeconds; }
    public void setDefaultRestSeconds(int defaultRestSeconds) { this.defaultRestSeconds = defaultRestSeconds; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }

    public Instant getCreatedAt() { return createdAt; }
}
