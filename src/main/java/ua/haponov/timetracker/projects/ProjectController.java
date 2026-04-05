package ua.haponov.timetracker.projects;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ua.haponov.timetracker.auth.User;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectRepository projectRepository;
    private final ProjectStatsService projectStatsService;
    private final ProjectSessionRepository projectSessionRepository;

    @GetMapping("/stats")
    public ProjectStats getStats(@AuthenticationPrincipal User currentUser) {
        List<Project> all = projectRepository.findAllByUser(currentUser);
        return projectStatsService.calculateStats(all);
    }

    @GetMapping("/{id}/sessions")
    public ResponseEntity<List<ProjectSession>> getProjectSessions(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        return projectRepository.findById(id)
                .filter(p -> p.getUser().getId().equals(currentUser.getId()))
                .map(p -> ResponseEntity.ok(projectSessionRepository.findAllByProjectIdOrderByStartTimeDesc(p.getId())))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<Project> getAllProjects(@AuthenticationPrincipal User currentUser) {
        return projectRepository.findByUserAndIsCompletedIsFalse(currentUser);
    }

    @PostMapping
    public Project createProject(@RequestBody Project project, @AuthenticationPrincipal User currentUser) {
        project.setUser(currentUser);
        return projectRepository.save(project);
    }

    @PatchMapping("/{id}/add-time")
    public ResponseEntity<Project> addTime(@PathVariable Long id,
                                           @RequestBody Map<String, Long> payload,
                                           @AuthenticationPrincipal User currentUser) {
        return projectRepository.findById(id)
                .filter(p -> p.getUser().getId().equals(currentUser.getId())) // Проверка владельца
                .map(project -> {
                    Long minutesToAdd = payload.getOrDefault("minutes", 0L);
                    project.setTotalMinutes(project.getTotalMinutes() + minutesToAdd);
                    return ResponseEntity.ok(projectRepository.save(project));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        return projectRepository.findById(id)
                .filter(project -> project.getUser().getId().equals(currentUser.getId()))
                .map(project -> {
                    projectRepository.delete(project);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<Project> completeProject(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        return projectRepository.findById(id)
                .filter(p -> p.getUser().getId().equals(currentUser.getId()))
                .map(project -> {
                    if (project.getStartTime() != null) {
                        Instant now = Instant.now();
                        stopProjectAndSaveSession(project, now);
                    }
                    project.setCompleted(true);
                    return ResponseEntity.ok(projectRepository.save(project));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/start")
    public ResponseEntity<Project> startTimer(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        Instant now = Instant.now();

        projectRepository.findAllByUser(currentUser).stream()
                .filter(p -> p.getStartTime() != null && !p.getId().equals(id))
                .forEach(p -> stopProjectAndSaveSession(p, now));

        Project project = projectRepository.findById(id)
                .filter(p -> p.getUser().getId().equals(currentUser.getId()))
                .orElseThrow();

        project.setStartTime(now);
        return ResponseEntity.ok(projectRepository.save(project));
    }

    @PatchMapping("/{id}/stop")
    public ResponseEntity<Project> stopTimer(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        Instant now = Instant.now();

        Project project = projectRepository.findById(id)
                .filter(p -> p.getUser().getId().equals(currentUser.getId()))
                .orElseThrow();

        if (project.getStartTime() != null) {
            stopProjectAndSaveSession(project, now);
        }
        return ResponseEntity.ok(projectRepository.save(project));
    }

    private void stopProjectAndSaveSession(Project project, Instant endTime) {
        long minutes = java.time.Duration.between(project.getStartTime(), endTime).toMinutes();

        ProjectSession session = new ProjectSession(project, project.getStartTime());
        session.setEndTime(endTime);
        session.setDurationMinutes(minutes);
        projectSessionRepository.save(session);

        project.setTotalMinutes(project.getTotalMinutes() + minutes);
        project.setStartTime(null);
    }
}
