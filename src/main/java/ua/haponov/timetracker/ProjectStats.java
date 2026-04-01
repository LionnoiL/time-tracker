package ua.haponov.timetracker;

public record ProjectStats(
    long activeCount,
    long completedCount,
    String totalHours,
    long totalEarnings
) {}
