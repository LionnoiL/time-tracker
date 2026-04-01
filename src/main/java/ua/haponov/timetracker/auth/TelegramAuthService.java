package ua.haponov.timetracker.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class TelegramAuthService {

    @Value("${telegram.bot.token}")
    private String botToken;

    public boolean validateParent(Map<String, String> params) {
        String hash = params.get("hash");
        if (hash == null) return false;

        Map<String, String> sortedParams = new TreeMap<>(params);
        sortedParams.remove("hash");

        String dataCheckString = sortedParams.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("\n"));

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] secretKey = digest.digest(botToken.getBytes(StandardCharsets.UTF_8));

            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(secretKey, "HmacSHA256");
            hmac.init(keySpec);
            byte[] hmacBytes = hmac.doFinal(dataCheckString.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : hmacBytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString().equals(hash);
        } catch (Exception e) {
            return false;
        }
    }
}
