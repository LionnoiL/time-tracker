package ua.haponov.timetracker.auth;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "users")
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long telegramId;

    private String firstName;
    private String lastName;
    private String username;
    private String photoUrl;

    public User(Long telegramId, String firstName, String lastName, String username, String photoUrl) {
        this.telegramId = telegramId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.photoUrl = photoUrl;
    }
}
