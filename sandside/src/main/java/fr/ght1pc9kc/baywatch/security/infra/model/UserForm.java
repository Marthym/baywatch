package fr.ght1pc9kc.baywatch.security.infra.model;

import fr.ght1pc9kc.baywatch.common.infra.model.CreateValidation;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.util.Set;

@Value
public class UserForm {
    public @NotEmpty String login;
    public @NotEmpty String name;
    public @Email String mail;
    public @NotEmpty(groups = CreateValidation.class) String password;
    public @NotNull Set<String> roles;
}
