package ua.haponov.timetracker.projects;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long durationMinutes;

    public ProjectSession(Project project, LocalDateTime startTime) {
        this.project = project;
        this.startTime = startTime;
    }
}
