package ua.haponov.timetracker.projects;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ua.haponov.timetracker.auth.User;
import ua.haponov.timetracker.settings.UserSettings;
import ua.haponov.timetracker.settings.UserSettingsRepository;

import java.util.List;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class ProjectViewController {

    private final ProjectRepository projectRepository;
    private final ProjectStatsService projectStatsService;
    private final UserSettingsRepository userSettingsRepository;

    @GetMapping
    public String listProjects(Model model, @AuthenticationPrincipal User currentUser,
                               @RequestParam(value = "showAll", required = false, defaultValue = "false") boolean showAll) {
        List<Project> allProjects = projectRepository.findAllByUser(currentUser);
        List<Project> projects;

        if (!showAll) {
            projects = allProjects.stream()
                    .filter(p -> !p.isCompleted())
                    .toList();
        } else {
            projects = allProjects;
        }

        UserSettings userSettings = userSettingsRepository.findByUser(currentUser).orElse(new UserSettings());

        Project project = new Project();
        project.setHourlyRate(userSettings.getDefaultHourlyRate());

        model.addAttribute("projects", projects);
        model.addAttribute("newProject", project);
        model.addAttribute("userSettings", userSettings);
        model.addAttribute("showAll", showAll);

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

    @GetMapping("/edit/{id}")
    public String editProjectForm(@PathVariable Long id, Model model, @AuthenticationPrincipal User currentUser) {
        Project project = projectRepository.findById(id)
                .filter(p -> p.getUser().getId().equals(currentUser.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Проект не знайдено або доступ заборонено"));

        UserSettings userSettings = userSettingsRepository.findByUser(currentUser).orElse(new UserSettings());

        model.addAttribute("project", project);
        model.addAttribute("hours", project.getTotalMinutes() / 60);
        model.addAttribute("minutes", project.getTotalMinutes() % 60);
        model.addAttribute("userSettings", userSettings);

        return "edit-project";
    }

    @PostMapping("/edit/{id}")
    public String updateProject(@PathVariable Long id,
                                @ModelAttribute Project projectDetails,
                                @RequestParam("editHours") Long hours,
                                @RequestParam("editMinutes") Long minutes,
                                @AuthenticationPrincipal User currentUser) {
        Project project = projectRepository.findById(id)
                .filter(p -> p.getUser().getId().equals(currentUser.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Проект не знайдено"));

        project.setName(projectDetails.getName());
        project.setDescription(projectDetails.getDescription());
        project.setDeadline(projectDetails.getDeadline());
        project.setHourlyRate(projectDetails.getHourlyRate());

        // Конвертуємо години та хвилини назад у totalMinutes
        project.setTotalMinutes((hours * 60) + minutes);

        projectRepository.save(project);
        return "redirect:/";
    }
}
