package fr.ght1pc9kc.baywatch.domain.security;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import fr.ght1pc9kc.baywatch.api.common.model.Entity;
import fr.ght1pc9kc.baywatch.api.security.model.BaywatchAuthentication;
import fr.ght1pc9kc.baywatch.api.security.model.Role;
import fr.ght1pc9kc.baywatch.api.security.model.User;
import fr.ght1pc9kc.baywatch.domain.security.exceptions.SecurityException;
import fr.ght1pc9kc.baywatch.domain.security.ports.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

@Slf4j
public class JwtBaywatchAuthenticationProviderImpl implements JwtTokenProvider {
    private static final String AUTHORITIES_KEY = "roles";
    private final byte[] secretKey;
    private final Duration validity;

    private final Clock clock;

    public JwtBaywatchAuthenticationProviderImpl(byte[] secretKey, Duration validity, Clock clock) {
        this.secretKey = secretKey;
        this.clock = clock;
        this.validity = validity;
    }

    @Override
    public BaywatchAuthentication createToken(Entity<User> user, boolean remember, Collection<String> authorities) {
        List<String> auth = new ArrayList<>(authorities);
        auth.add(user.entity.role.authority());
        String auths = String.join(",", auth);

        try {
            JWSSigner signer = new MACSigner(secretKey);

            Instant now = clock.instant();
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(user.id)
                    .issuer("baywatch/sandside")
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plus(validity)))
                    .claim("login", user.entity.login)
                    .claim("name", user.entity.name)
                    .claim("mail", user.entity.mail)
                    .claim("createdAt", Date.from(user.createdAt))
                    .claim("remember", remember)
                    .claim(AUTHORITIES_KEY, auths)
                    .build();
            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
            signedJWT.sign(signer);
            String token = signedJWT.serialize();
            return new BaywatchAuthentication(user, token, remember, authorities);

        } catch (JOSEException e) {
            log.debug("Username: {}, validity: {}, authorities: {}", user, validity, auths);
            throw new SecurityException("Unable to create token !", e);
        }
    }

    @Override
    public BaywatchAuthentication getAuthentication(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(secretKey);
            if (!signedJWT.verify(verifier)) {
                throw new SecurityException("Invalid Token signature !");
            }
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

            List<String> authorities = Arrays.stream(claims.getStringClaim(AUTHORITIES_KEY).split(","))
                    .map(String::trim)
                    .filter(not(String::isBlank))
                    .collect(Collectors.toList());

            Role role = Arrays.stream(Role.values())
                    .filter(r -> authorities.contains(r.authority()))
                    .findAny().orElse(Role.ANONYMOUS);

            Instant createdAt = Optional.ofNullable(claims.getDateClaim("createdAt"))
                    .map(Date::toInstant)
                    .orElse(Instant.EPOCH);

            Entity<User> user = new Entity<>(claims.getSubject(), createdAt, User.builder()
                    .login(claims.getStringClaim("login"))
                    .name(claims.getStringClaim("name"))
                    .mail(claims.getStringClaim("mail"))
                    .role(role)
                    .build());

            boolean rememberMe = Optional.ofNullable(claims.getBooleanClaim("remember")).orElse(false);
            return new BaywatchAuthentication(user, token, rememberMe, authorities);
        } catch (ParseException | JOSEException e) {
            log.debug("Token: {}", token);
            throw new SecurityException("Unable to build Authentication !", e);
        }
    }

    @Override
    public boolean validateToken(String token, boolean checkExpiration) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(secretKey);
            if (!signedJWT.verify(verifier)) {
                return false;
            }
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            return !checkExpiration
                    || !claims.getExpirationTime().toInstant().isBefore(clock.instant());

        } catch (IllegalStateException | ParseException | JOSEException e) {
            log.info("Invalid JWT token.");
            log.trace("Invalid JWT token trace.", e);
            return false;
        }
    }
}
