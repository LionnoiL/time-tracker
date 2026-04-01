package ua.haponov.timetracker;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

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

    public double getEarnedAmount() {
        if (totalMinutes == null || hourlyRate == null) return 0.0;
        return (totalMinutes / 60.0) * hourlyRate;
    }
}