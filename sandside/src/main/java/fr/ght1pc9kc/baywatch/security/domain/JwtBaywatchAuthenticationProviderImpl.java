package fr.ght1pc9kc.baywatch.security.domain;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import fr.ght1pc9kc.baywatch.common.api.DefaultMeta;
import fr.ght1pc9kc.baywatch.security.api.model.BaywatchAuthentication;
import fr.ght1pc9kc.baywatch.security.api.model.Permission;
import fr.ght1pc9kc.baywatch.security.api.model.RoleUtils;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.security.domain.exceptions.SecurityException;
import fr.ght1pc9kc.baywatch.security.domain.ports.JwtTokenProvider;
import fr.ght1pc9kc.entity.api.Entity;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static fr.ght1pc9kc.baywatch.common.api.DefaultMeta.createdAt;
import static java.util.function.Predicate.not;

@Slf4j
public class JwtBaywatchAuthenticationProviderImpl implements JwtTokenProvider {
    private static final String ISSUER = "baywatch/sandside";
    private static final String AUTHORITIES_KEY = "roles";
    private static final String LOGIN_KEY = "login";
    private static final String NAME_KEY = "name";
    private static final String MAIL_KEY = "mail";
    private static final String CREATED_AT_KEY = "createdAt";
    private static final String REMEMBER_ME_KEY = "remember";
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
        auth.addAll(user.self().roles().stream().map(RoleUtils::toSpringAuthority).toList());
        String auths = String.join(",", auth);

        try {
            JWSSigner signer = new MACSigner(secretKey);

            Instant now = clock.instant();
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(user.id())
                    .issuer(ISSUER)
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plus(validity)))
                    .claim(LOGIN_KEY, user.self().login())
                    .claim(NAME_KEY, user.self().name())
                    .claim(MAIL_KEY, user.self().mail())
                    .claim(CREATED_AT_KEY, user.meta(createdAt, Instant.class).map(Date::from).orElse(null))
                    .claim(REMEMBER_ME_KEY, remember)
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
                    .toList();

            List<Permission> roles = authorities.stream()
                    .map(RoleUtils::fromSpringAuthority)
                    .sorted(Permission.COMPARATOR)
                    .distinct()
                    .toList();

            Instant createdAt = Optional.ofNullable(claims.getDateClaim(CREATED_AT_KEY))
                    .map(Date::toInstant)
                    .orElse(Instant.EPOCH);

            Entity<User> user = Entity.identify(User.builder()
                            .login(claims.getStringClaim(LOGIN_KEY))
                            .name(claims.getStringClaim(NAME_KEY))
                            .mail(claims.getStringClaim(MAIL_KEY))
                            .roles(roles)
                            .build())
                    .meta(DefaultMeta.createdAt, createdAt)
                    .withId(claims.getSubject());

            boolean rememberMe = Optional.ofNullable(claims.getBooleanClaim(REMEMBER_ME_KEY)).orElse(false);
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
