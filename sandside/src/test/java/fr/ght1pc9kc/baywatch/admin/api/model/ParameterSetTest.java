package fr.ght1pc9kc.baywatch.admin.api.model;

import fr.ght1pc9kc.entity.api.Entity;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

class ParameterSetTest {
    @Test
    void should_convert_map() {
        Assertions.assertThat(new ParameterSet(List.of(
                Entity.identify(Map.entry("mail.smtp.host", "smtp.jedi.net")).withId("42"),
                Entity.identify(Map.entry("mail.smtp.ssl.checkserveridentity", "true")).withId("43"),
                Entity.identify(Map.entry("mail.smtp.ssl.protocols", "TLSv1.3 TLSv1.2")).withId("44"),
                Entity.identify(Map.entry("mail.smtp.pollingIntervalSeconds", "60")).withId("45"),
                Entity.identify(Map.entry("config.mail.smtp.pollingIntervalSeconds", "60")).withId("45")
        )).toMap("mail.smtp")).isEqualTo(Map.of(
            "host", "smtp.jedi.net",
            "sslCheckserveridentity", "true",
            "sslProtocols", "TLSv1.3 TLSv1.2",
            "pollingIntervalSeconds", "60"
        ));
    }
}