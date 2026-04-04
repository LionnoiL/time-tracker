package ua.haponov.timetracker.projects;

import jakarta.persistence.*;
import lombok.Data;
import ua.haponov.timetracker.auth.User;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@Entity
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private LocalDate deadline;
    private Double hourlyRate;
    private Long totalMinutes = 0L;
    private java.time.LocalDateTime startTime;

    @Column(length = 2000)
    private String description;
    private boolean isCompleted = false;

    @JsonIgnore
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectSession> sessions = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public double getEarnedAmount() {
        if (totalMinutes == null || hourlyRate == null) return 0.0;
        return (totalMinutes / 60.0) * hourlyRate;
    }
}