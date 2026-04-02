package ua.haponov.timetracker.projects;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ua.haponov.timetracker.auth.User;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectRepository projectRepository;
    private final ProjectStatsService projectStatsService;

    @GetMapping("/stats")
    public ProjectStats getStats(@AuthenticationPrincipal User currentUser) {
        List<Project> all = projectRepository.findAllByUser(currentUser);
        return projectStatsService.calculateStats(all);
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
                        java.time.LocalDateTime now = java.time.LocalDateTime.now(java.time.ZoneOffset.UTC);
                        long minutes = java.time.Duration.between(project.getStartTime(), now).toMinutes();
                        project.setTotalMinutes(project.getTotalMinutes() + minutes);
                        project.setStartTime(null);
                    }
                    project.setCompleted(true);
                    return ResponseEntity.ok(projectRepository.save(project));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/start")
    public ResponseEntity<Project> startTimer(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        java.time.LocalDateTime now = java.time.LocalDateTime.now(java.time.ZoneOffset.UTC);

        projectRepository.findAllByUser(currentUser).stream()
                .filter(p -> p.getStartTime() != null && !p.getId().equals(id))
                .forEach(p -> {
                    long minutes = java.time.Duration.between(p.getStartTime(), now).toMinutes();
                    p.setTotalMinutes(p.getTotalMinutes() + (int) minutes);
                    p.setStartTime(null);
                    projectRepository.save(p);
                });

        Project project = projectRepository.findById(id)
                .filter(p -> p.getUser().getId().equals(currentUser.getId()))
                .orElseThrow();
        project.setStartTime(now);
        return ResponseEntity.ok(projectRepository.save(project));
    }

    @PatchMapping("/{id}/stop")
    public ResponseEntity<Project> stopTimer(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        java.time.LocalDateTime now = java.time.LocalDateTime.now(java.time.ZoneOffset.UTC);

        Project project = projectRepository.findById(id)
                .filter(p -> p.getUser().getId().equals(currentUser.getId()))
                .orElseThrow();

        if (project.getStartTime() != null) {
            long minutes = java.time.Duration.between(project.getStartTime(), now).toMinutes();
            project.setTotalMinutes(project.getTotalMinutes() + (int) minutes);
            project.setStartTime(null);
        }
        return ResponseEntity.ok(projectRepository.save(project));
    }
}
