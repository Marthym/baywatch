package fr.ght1pc9kc.baywatch.security.infra.controllers;

import fr.ght1pc9kc.baywatch.security.api.PasswordService;
import fr.ght1pc9kc.baywatch.security.api.model.PasswordEvaluation;
import fr.ght1pc9kc.baywatch.security.infra.adapters.UserMapper;
import fr.ght1pc9kc.baywatch.security.infra.model.UserForm;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class PasswordController {
    private final PasswordService passwordService;
    private final UserMapper userMapper;

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public Mono<PasswordEvaluation> passwordCheckStrength(@Argument("password") String password) {
        return passwordService.checkPasswordStrength(password);
    }

    @QueryMapping
    public Mono<PasswordEvaluation> passwordCheckAnonymous(@Argument("user") UserForm user) {
        return passwordService.checkPasswordStrength(userMapper.getUser(user));
    }

    @QueryMapping
    public Flux<String> passwordGenerate(@Argument("number") int number) {
        return passwordService.generateSecurePassword(number);
    }
}
