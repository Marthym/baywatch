package fr.ght1pc9kc.baywatch.admin.infra.controllers;

import fr.ght1pc9kc.baywatch.admin.api.AppConfigurationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AppConfigurationController {
    private final AppConfigurationService appConfigurationService;

    @QueryMapping
    public Mono<Map<String, Object>> adminAppConfigurationMailSmtp() {
        return appConfigurationService.get("mail.smtp")
                .map(ps -> ps.toMap("mail.smtp"));
    }
}
