package fr.ght1pc9kc.baywatch.security.infra.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Value;

@Value
public class AuthenticationRequest {
    @NotBlank
    public String username;

    @NotBlank
    public String password;

    public Boolean remember;

    public boolean rememberMe() {
        return remember != null && remember;
    }
}
