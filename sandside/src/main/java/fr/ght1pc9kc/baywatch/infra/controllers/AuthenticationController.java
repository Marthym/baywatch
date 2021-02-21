package fr.ght1pc9kc.baywatch.infra.controllers;

import fr.ght1pc9kc.baywatch.domain.ports.JwtTokenProvider;
import fr.ght1pc9kc.baywatch.infra.controllers.exceptions.BadCredentialException;
import fr.ght1pc9kc.baywatch.infra.model.AuthenticationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import javax.validation.Valid;
import java.util.Set;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthenticationController {

    private final JwtTokenProvider tokenProvider;
    private final ReactiveAuthenticationManager authenticationManager;

    @PostMapping("/login")
    public Mono<ResponseEntity<User>> login(@Valid Mono<AuthenticationRequest> authRequest) {
        return authRequest.flatMap(login -> authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(login.getUsername(), login.getPassword()))
                .map(auth -> {
                    User user = (User) auth.getPrincipal();
                    Set<String> authorities = AuthorityUtils.authorityListToSet(auth.getAuthorities());
                    String token = tokenProvider.createToken(user.getUsername(), authorities);
                    return Tuples.of(token, user);
                })
        ).map(jwt -> {
                    HttpHeaders httpHeaders = new HttpHeaders();
                    httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + jwt.getT1());
                    return new ResponseEntity<>(jwt.getT2(), httpHeaders, HttpStatus.OK);
                }
        ).onErrorMap(BadCredentialsException.class, BadCredentialException::new);
    }
}
