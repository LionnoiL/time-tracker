package ua.haponov.timetracker.projects;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectStatsService {

    public ProjectStats calculateStats(List<Project> all) {

        long active = all.stream().filter(p -> !p.isCompleted()).count();
        long completed = all.stream().filter(Project::isCompleted).count();
        long totalMins = all.stream()
                .mapToLong(p -> p.getTotalMinutes() != null ? p.getTotalMinutes() : 0)
                .sum();
        double earnings = all.stream()
                .filter(p -> !p.isCompleted())
                .mapToDouble(Project::getEarnedAmount)
                .sum();

        return new ProjectStats(
                active,
                completed,
                String.format("%.1f", totalMins / 60.0),
                Math.round(earnings)
        );
    }
}
