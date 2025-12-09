package fr.ght1pc9kc.baywatch.admin.infra.controllers;

import fr.ght1pc9kc.baywatch.admin.api.AppConfigurationService;
import fr.ght1pc9kc.baywatch.admin.api.model.ParameterSet;
import fr.ght1pc9kc.baywatch.admin.infra.model.MailSmtpConfigurationForm;
import fr.ght1pc9kc.entity.api.Entity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AppConfigurationController {
    private static final String EMAIL_CONFIG_PREFIX = "mail.smtp";
    private final AppConfigurationService appConfigurationService;
    private final ObjectMapper jsonMapper;

    @QueryMapping
    public Mono<Map<String, Object>> adminAppConfigurationMailSmtp() {
        return appConfigurationService.get(EMAIL_CONFIG_PREFIX)
                .map(ParameterSet::toMap);
    }

    @MutationMapping
    public Mono<Map<String, Object>> adminAppConfigurationMailSmtpUpdate(MailSmtpConfigurationForm mailSmtp) {
        ParameterSet configToPersist = new ParameterSet(jsonMapper.convertValue(mailSmtp, new TypeReference<Map<String, String>>() {
        }).entrySet().stream().map(e -> Entity.identify(e).withId(EMAIL_CONFIG_PREFIX)).toList());
        return appConfigurationService.update(configToPersist)
                .map(ParameterSet::toMap);
    }
}
