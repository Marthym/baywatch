package fr.ght1pc9kc.baywatch.infra.security;

import fr.ght1pc9kc.baywatch.api.security.model.User;
import fr.ght1pc9kc.baywatch.domain.exceptions.SecurityException;
import fr.ght1pc9kc.baywatch.domain.ports.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.domain.ports.JwtTokenProvider;
import fr.ght1pc9kc.baywatch.infra.security.adapters.AuthenticationManagerAdapter;
import fr.ght1pc9kc.baywatch.infra.security.exceptions.BaywatchCredentialsException;
import fr.ght1pc9kc.baywatch.infra.security.exceptions.NoSessionException;
import fr.ght1pc9kc.baywatch.infra.security.model.AuthenticationRequest;
import fr.ght1pc9kc.baywatch.infra.security.model.BaywatchUserDetails;
import fr.ght1pc9kc.baywatch.infra.security.model.SecurityParams;
import fr.ght1pc9kc.baywatch.infra.security.model.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.Optional;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("${baywatch.base-route}/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManagerAdapter authenticationManager;
    private final AuthenticationFacade authFacade;
    private final SecurityParams securityParams;

    @PostMapping("/login")
    public Mono<User> login(@Valid Mono<AuthenticationRequest> authRequest, ServerWebExchange exchange) {
        return authRequest
                .flatMap(login -> authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(login.getUsername(), login.getPassword())))

                .map(auth -> {
                    BaywatchUserDetails user = (BaywatchUserDetails) auth.getPrincipal();
//                    Set<String> authorities = AuthorityUtils.authorityListToSet(auth.getAuthorities());
//                    String token = tokenProvider.createToken(user.getEntity(), authorities);
//                    exchange.getResponse().addCookie(ResponseCookie.from(securityParams.cookie.name, token)
//                            .httpOnly(true)
//                            .secure("https".equals(exchange.getRequest().getURI().getScheme()))
//                            .sameSite("Strict")
//                            .maxAge(securityParams.jwt.validity)
//                            .path("/api")
//                            .build());
                    log.debug("Login to {}.", user.getUsername());
                    return user.getEntity().withPassword(null);
                })

                .onErrorMap(BadCredentialsException.class, BaywatchCredentialsException::new);
    }

    @DeleteMapping("/logout")
    public Mono<Void> logout(ServerWebExchange exchange) {
        if (log.isDebugEnabled()) {
            String user = Optional.ofNullable(exchange.getRequest().getCookies().getFirst(securityParams.cookie.name))
                    .map(HttpCookie::getValue)
                    .map(tokenProvider::getAuthentication)
                    .map(a -> String.format("%s (%s)", a.user.login, a.user.id))
                    .orElse("Unknown User");
            log.debug("Logout for {}.", user);
        }
        return Mono.fromRunnable(() -> exchange.getResponse().addCookie(
                ResponseCookie.from(securityParams.cookie.name, "")
                        .httpOnly(true)
                        .secure("https".equals(exchange.getRequest().getURI().getScheme()))
                        .sameSite("Strict")
                        .path("/api")
                        .maxAge(0)
                        .build()));
    }

    @PutMapping("/refresh")
    public Mono<Session> refresh(ServerWebExchange exchange) {
        String token = Optional.ofNullable(exchange.getRequest().getCookies().getFirst(securityParams.cookie.name))
                .map(HttpCookie::getValue)
                .orElseThrow(() -> new NoSessionException("User not login on !"));

        return authenticationManager.refresh(token)
                .map(auth -> {
                    exchange.getResponse().addCookie(ResponseCookie.from(securityParams.cookie.name, token)
                            .httpOnly(true)
                            .secure("https".equals(exchange.getRequest().getURI().getScheme()))
                            .sameSite("Strict")
                            .maxAge(securityParams.jwt.validity)
                            .path("/api")
                            .build());
                    return Session.builder()
                            .user(auth.user.withPassword(null))
                            .maxAge(securityParams.jwt.validity.toSeconds())
                            .build();
                })
                .onErrorMap(SecurityException.class, BaywatchCredentialsException::new);
    }

    @GetMapping("/current")
    @PreAuthorize("isAuthenticated()")
    public Mono<User> currentUser() {
        return authFacade.getConnectedUser();
    }
}
