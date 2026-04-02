package ua.haponov.timetracker.settings;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.haponov.timetracker.auth.User;

import java.util.Optional;

public interface UserSettingsRepository extends JpaRepository<UserSettings, Long> {

    Optional<UserSettings> findByUser(User user);
}
