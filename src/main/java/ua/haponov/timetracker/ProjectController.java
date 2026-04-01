package ua.haponov.timetracker;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectRepository projectRepository;
    private final ProjectStatsService projectStatsService;

    @GetMapping("/stats")
    public ProjectStats getStats() {
        List<Project> allProjects = projectRepository.findAll();
        return projectStatsService.calculateStats(allProjects);
    }

    @GetMapping
    public List<Project> getAllProjects() {
        return projectRepository.findProjectsByIsCompletedIsFalse();
    }

    @PostMapping
    public Project createProject(@RequestBody Project project) {
        return projectRepository.save(project);
    }

    @PatchMapping("/{id}/add-time")
    public ResponseEntity<Project> addTime(@PathVariable Long id, @RequestBody Map<String, Long> payload) {
        return projectRepository.findById(id)
                .map(project -> {
                    Long minutesToAdd = payload.getOrDefault("minutes", 0L);
                    project.setTotalMinutes(project.getTotalMinutes() + minutesToAdd);
                    return ResponseEntity.ok(projectRepository.save(project));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        return projectRepository.findById(id)
                .map(project -> {
                    project.setCompleted(true);
                    projectRepository.save(project);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
