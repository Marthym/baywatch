package fr.ght1pc9kc.baywatch.notify.domain.model;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Objects;

public record SmtpServerConfig(
        @NotNull String server,
        int port,
        String from,
        boolean secure,
        String username,
        String password,
        String sslProtocols,
        boolean checkServerIdentity,
        boolean requireTls,
        Duration pollingInterval
) {
    public SmtpServerConfig {
        Objects.requireNonNull(server, "server property must not be null !");
    }
}
