package fr.ght1pc9kc.baywatch.security.infra.model;

import lombok.Value;

import javax.validation.constraints.NotBlank;

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
