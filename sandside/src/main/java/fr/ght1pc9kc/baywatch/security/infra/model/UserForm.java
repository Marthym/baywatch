package fr.ght1pc9kc.baywatch.security.infra.model;

import fr.ght1pc9kc.baywatch.security.api.model.Role;
import fr.ght1pc9kc.baywatch.common.infra.model.CreateValidation;
import lombok.Value;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Value
public class UserForm {
    public @NotEmpty String login;
    public @NotEmpty String name;
    public @Email String mail;
    public @NotEmpty(groups = CreateValidation.class) String password;
    public @NotNull Role role;
}