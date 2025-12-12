package fr.ght1pc9kc.baywatch.admin.infra.model;

public record MailSmtpConfigurationForm(
        String host,
        int port,
        boolean secure,
        String username,
        String password,
        boolean requireTls,
        Ssl ssl,
        String from,
        int pollingIntervalSeconds
) {
    public record Ssl(String protocols, boolean checkserveridentity) {
    }
}
