package ua.haponov.timetracker.projects;

public record ProjectStats(
    long activeCount,
    long completedCount,
    String totalHours,
    long totalEarnings
) {}
