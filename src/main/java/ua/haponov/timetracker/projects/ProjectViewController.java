package ua.haponov.timetracker.projects;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ua.haponov.timetracker.auth.User;

import java.util.List;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class ProjectViewController {

    private final ProjectRepository projectRepository;
    private final ProjectStatsService projectStatsService;

    @GetMapping
    public String listProjects(Model model, @AuthenticationPrincipal User currentUser) {
        List<Project> allProjects = projectRepository.findAllByUser(currentUser);

        List<Project> activeProjects = allProjects.stream()
                .filter(p -> !p.isCompleted())
                .toList();

        model.addAttribute("projects", activeProjects);
        model.addAttribute("newProject", new Project());

        ProjectStats stats = projectStatsService.calculateStats(allProjects);
        model.addAttribute("stats", stats);

        return "projects";
    }

    @PostMapping("/add")
    public String addProject(@ModelAttribute Project project, @AuthenticationPrincipal User currentUser) {
        project.setUser(currentUser);
        projectRepository.save(project);
        return "redirect:/";
    }
}
