package fr.ght1pc9kc.baywatch.notify.domain.model;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record SmtpServerConfig(
        @NotNull String server,
        int port,
        boolean secure,
        String username,
        String password,
        String cipher,
        boolean requireTls
) {
    public SmtpServerConfig {
        Objects.requireNonNull(server, "server property must not be null !");
    }
}
