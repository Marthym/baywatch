package fr.ght1pc9kc.baywatch.infra.controllers;

import fr.ght1pc9kc.baywatch.api.UserService;
import fr.ght1pc9kc.baywatch.api.model.User;
import fr.ght1pc9kc.baywatch.api.model.request.PageRequest;
import fr.ght1pc9kc.baywatch.api.model.request.filter.Criteria;
import fr.ght1pc9kc.baywatch.domain.ports.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.domain.ports.JwtTokenProvider;
import fr.ght1pc9kc.baywatch.infra.controllers.exceptions.BadCredentialException;
import fr.ght1pc9kc.baywatch.infra.model.AuthenticationRequest;
import fr.ght1pc9kc.baywatch.infra.model.BaywatchUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final JwtTokenProvider tokenProvider;
    private final ReactiveAuthenticationManager authenticationManager;
    private final UserService userService;
    private final AuthenticationFacade authFacade;

    @PostMapping("/login")
    public Mono<User> login(@Valid Mono<AuthenticationRequest> authRequest, ServerWebExchange exchange) {
        return authRequest
                .flatMap(login -> authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(login.getUsername(), login.getPassword())))

                .map(auth -> {
                    BaywatchUserDetails user = (BaywatchUserDetails) auth.getPrincipal();
                    Set<String> authorities = AuthorityUtils.authorityListToSet(auth.getAuthorities());
                    String token = tokenProvider.createToken(user.getEntity(), authorities);
                    exchange.getResponse().addCookie(ResponseCookie.from("token", token)
                            .httpOnly(true)
                            .secure("https".equals(exchange.getRequest().getURI().getScheme()))
                            .sameSite("Strict")
                            .path("/api")
                            .build());
//                    session.getAttributes().putIfAbsent("token", token);
                    return user;
                })

                .flatMap(user -> userService.list(PageRequest.one(Criteria.property("login").eq(user.getUsername()))).next())
                .map(user -> user.withPassword(null))
                .onErrorMap(BadCredentialsException.class, BadCredentialException::new);
    }

    @GetMapping("/current")
    @PreAuthorize("isAuthenticated()")
    public Mono<User> currentUser() {
        return authFacade.getConnectedUser();
    }
}
