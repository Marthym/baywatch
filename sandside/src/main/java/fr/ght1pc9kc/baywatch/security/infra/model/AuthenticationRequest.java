package fr.ght1pc9kc.baywatch.security.infra.model;

import jakarta.validation.constraints.NotBlank;

public record AuthenticationRequest(
        @NotBlank String username,
        @NotBlank String password,
        Boolean remember
) {
    public boolean rememberMe() {
        return remember != null && remember;
    }
}
