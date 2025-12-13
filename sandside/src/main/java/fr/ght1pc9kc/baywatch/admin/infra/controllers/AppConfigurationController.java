package fr.ght1pc9kc.baywatch.admin.infra.controllers;

import fr.ght1pc9kc.baywatch.admin.api.AppConfigurationService;
import fr.ght1pc9kc.baywatch.admin.api.model.ParameterSet;
import fr.ght1pc9kc.baywatch.admin.infra.exceptions.InvalidConfigurationException;
import fr.ght1pc9kc.baywatch.admin.infra.model.MailSmtpConfigurationForm;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static java.util.Objects.nonNull;

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
    public Mono<Map<String, Object>> adminAppConfigurationMailSmtpUpdate(@Valid @Argument("mailSmtp") MailSmtpConfigurationForm mailSmtp) {
        Map<String, Object> parameters = jsonMapper.convertValue(mailSmtp, new TypeReference<>() {
        });
        return appConfigurationService.update(flatten(parameters, EMAIL_CONFIG_PREFIX))
                .map(ParameterSet::toMap)
                .onErrorMap(ConstraintViolationException.class, e ->
                        e.getConstraintViolations().stream().findFirst().map(violation ->
                                new InvalidConfigurationException(
                                        violation.getMessage(), List.of(violation.getPropertyPath().toString()))
                        ).orElseGet(() -> new InvalidConfigurationException(e.getLocalizedMessage(), List.of())));
    }

    private static List<Entry<String, String>> flatten(Map<String, Object> current, @Nullable String prefix) {
        List<Entry<String, String>> out = new java.util.ArrayList<>();

        for (Entry<String, Object> e : current.entrySet()) {
            String key = (prefix == null || prefix.isBlank()) ? e.getKey() : prefix + "." + e.getKey();
            Object value = e.getValue();

            if (value instanceof Map<?, ?> m) {
                @SuppressWarnings("unchecked")
                Map<String, Object> sub = (Map<String, Object>) m;
                out.addAll(flatten(sub, key));
            } else {
                if (nonNull(value)) {
                    out.add(Map.entry(key, String.valueOf(value)));
                }
            }
        }

        return out;
    }
}
