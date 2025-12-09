package fr.ght1pc9kc.baywatch.admin.infra.model;

public record MailSmtpConfigurationForm(
        String host,
        int port,
        boolean secure,
        String username,
        String password,
        String sslProtocols,
        boolean requireTls,
        boolean sslCheckserveridentity,
        String from,
        int pollingIntervalSeconds
) {
}
