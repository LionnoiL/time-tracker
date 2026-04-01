package ua.haponov.timetracker.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final TelegramAuthService telegramAuthService;

    @Value("${telegram.bot.token}")
    private String botToken;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/auth/telegram")
    public String telegramAuth(@RequestParam Map<String, String> params, HttpServletRequest request) {
        if (!telegramAuthService.validateParent(params)) {
            return "redirect:/login?error=invalid_hash";
        }

        Long telegramId = Long.valueOf(params.get("id"));
        String firstName = params.get("first_name");

        User user = userRepository.findByTelegramId(telegramId)
                .orElseGet(() -> userRepository.save(
                    new User(telegramId, firstName, params.get("last_name"), params.get("username"), params.get("photo_url"))
                ));

        List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        Authentication auth = new UsernamePasswordAuthenticationToken(user, null, authorities);

        SecurityContextHolder.getContext().setAuthentication(auth);

        request.getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

        return "redirect:/";
    }
}
