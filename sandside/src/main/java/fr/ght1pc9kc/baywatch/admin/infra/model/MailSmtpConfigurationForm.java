package fr.ght1pc9kc.baywatch.admin.infra.model;

import fr.ght1pc9kc.baywatch.common.api.constraints.NullOrNotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record MailSmtpConfigurationForm(
        @NotBlank
        String host,
        @Positive
        int port,
        boolean secure,
        @NotBlank
        String username,
        @NullOrNotBlank
        String password,
        boolean requireTls,
        Ssl ssl,
        @Email
        String from,
        @Positive @Min(2)
        int pollingIntervalSeconds
) {
    public record Ssl(@NotBlank String protocols, boolean checkserveridentity) {
    }
}
