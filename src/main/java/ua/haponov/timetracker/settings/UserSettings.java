package ua.haponov.timetracker.settings;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.haponov.timetracker.auth.User;

@Data
@Entity
@Table(name = "user_settings")
@NoArgsConstructor
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String currency = "грн";
    private Double defaultHourlyRate = 0.0;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    public UserSettings(User user) {
        this.user = user;
    }
}
