package fr.ght1pc9kc.baywatch.infra.security.model;

import fr.ght1pc9kc.baywatch.api.security.model.Role;
import lombok.Value;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Value
public class UserForm {
    public @NotEmpty String login;
    public @NotEmpty String name;
    public @Email String mail;
    public @NotEmpty String password;
    public @NotNull Role role;
}
