package fr.ght1pc9kc.baywatch.security.infra.model;

import jakarta.validation.constraints.NotEmpty;

public record ChangePasswordForm(
        @NotEmpty String oldPassword,
        @NotEmpty String newPassword
) {
}
