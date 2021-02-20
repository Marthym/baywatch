package fr.ght1pc9kc.baywatch.infra.model;

import lombok.Value;

import javax.validation.constraints.NotBlank;

@Value
public class AuthenticationRequest {
    @NotBlank
    public String username;

    @NotBlank
    public String password;
}
