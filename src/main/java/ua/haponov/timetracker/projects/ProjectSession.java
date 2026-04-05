package ua.haponov.timetracker.projects;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Entity
@NoArgsConstructor
public class ProjectSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    private Instant startTime;
    private Instant endTime;
    private Long durationMinutes;

    public ProjectSession(Project project, Instant startTime) {
        this.project = project;
        this.startTime = startTime;
    }
}
