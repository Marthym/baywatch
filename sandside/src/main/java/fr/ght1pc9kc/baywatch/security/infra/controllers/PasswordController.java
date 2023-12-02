package fr.ght1pc9kc.baywatch.security.infra.controllers;

import fr.ght1pc9kc.baywatch.security.api.PasswordService;
import fr.ght1pc9kc.baywatch.security.api.model.PasswordEvaluation;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class PasswordController {
    private final PasswordService passwordService;

    @QueryMapping
    public Mono<PasswordEvaluation> checkPasswordStrength(@Argument("password") String password) {
        return passwordService.checkPasswordStrength(password);
    }
}
