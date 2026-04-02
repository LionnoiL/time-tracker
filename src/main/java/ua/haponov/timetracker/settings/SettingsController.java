package ua.haponov.timetracker.settings;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ua.haponov.timetracker.auth.User;

@Controller
@RequestMapping("/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final UserSettingsRepository settingsRepository;

    @GetMapping
    public String showSettings(Model model, @AuthenticationPrincipal User currentUser) {
        UserSettings settings = settingsRepository.findByUser(currentUser)
                .orElseGet(() -> {
                    UserSettings newSettings = new UserSettings(currentUser);
                    return settingsRepository.save(newSettings);
                });

        model.addAttribute("settings", settings);
        return "settings";
    }

    @PostMapping
    public String updateSettings(@ModelAttribute UserSettings settingsDetails, 
                                 @AuthenticationPrincipal User currentUser) {
        UserSettings settings = settingsRepository.findByUser(currentUser)
                .orElseThrow(() -> new IllegalStateException("Налаштування не знайдено"));

        settings.setCurrency(settingsDetails.getCurrency());
        settings.setDefaultHourlyRate(settingsDetails.getDefaultHourlyRate());

        settingsRepository.save(settings);
        return "redirect:/";
    }
}
