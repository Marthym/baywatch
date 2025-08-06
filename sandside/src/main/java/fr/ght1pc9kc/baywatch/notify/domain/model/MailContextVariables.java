package fr.ght1pc9kc.baywatch.notify.domain.model;

import lombok.Builder;

import java.util.Map;

@Builder(toBuilder = true)
public record MailContextVariables(
        String login,
        String username,
        String token
) {
    public Map<String, String> asMap() {
        return Map.of(
                "login", login,
                "username", username,
                "token", token
        );
    }
}
