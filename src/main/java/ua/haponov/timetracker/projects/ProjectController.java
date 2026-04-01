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
                    project.setCompleted(true);
                    projectRepository.save(project);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
