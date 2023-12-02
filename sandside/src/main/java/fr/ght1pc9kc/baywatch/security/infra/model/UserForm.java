package fr.ght1pc9kc.baywatch.security.infra.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record UserForm(
        String login,
        String name,
        @Email String mail,
        String password,
        List<@NotBlank String> roles) {
}
