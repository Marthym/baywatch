package fr.ght1pc9kc.baywatch.infra.controllers;

import fr.ght1pc9kc.baywatch.api.UserService;
import fr.ght1pc9kc.baywatch.api.model.request.PageRequest;
import fr.ght1pc9kc.baywatch.api.model.request.filter.Criteria;
import fr.ght1pc9kc.baywatch.domain.ports.JwtTokenProvider;
import fr.ght1pc9kc.baywatch.infra.controllers.exceptions.BadCredentialException;
import fr.ght1pc9kc.baywatch.infra.model.AuthenticationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.Set;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthenticationController {

    private final JwtTokenProvider tokenProvider;
    private final ReactiveAuthenticationManager authenticationManager;
    private final UserService userService;

    @PostMapping("/login")
    public Mono<fr.ght1pc9kc.baywatch.api.model.User> login(@Valid Mono<AuthenticationRequest> authRequest, WebSession session) {
        return authRequest
                .flatMap(login -> authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(login.getUsername(), login.getPassword())))

                .map(auth -> {
                    User user = (User) auth.getPrincipal();
                    Set<String> authorities = AuthorityUtils.authorityListToSet(auth.getAuthorities());
                    String token = tokenProvider.createToken(user.getUsername(), authorities);
                    session.getAttributes().putIfAbsent("token", token);
                    return user;
                })

                .flatMap(user -> userService.list(PageRequest.one(Criteria.property("login").eq(user.getUsername()))).next())
                .map(user -> user.withPassword(null))
                .onErrorMap(BadCredentialsException.class, BadCredentialException::new);
    }
}
