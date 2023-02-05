package fr.ght1pc9kc.baywatch.security.infra.model;

import fr.ght1pc9kc.baywatch.common.infra.model.CreateValidation;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record UserForm(
        @NotEmpty String login,
        @NotEmpty String name,
        @Email String mail,
        @NotEmpty(groups = CreateValidation.class) String password,
        @NotNull Set<String> roles) {
}
