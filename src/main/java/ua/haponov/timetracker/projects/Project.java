package ua.haponov.timetracker.projects;

import jakarta.persistence.*;
import lombok.Data;
import ua.haponov.timetracker.auth.User;

import java.time.LocalDate;

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
    private String description;
    private boolean isCompleted = false;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public double getEarnedAmount() {
        if (totalMinutes == null || hourlyRate == null) return 0.0;
        return (totalMinutes / 60.0) * hourlyRate;
    }
}