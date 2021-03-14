package fr.ght1pc9kc.baywatch.domain.security;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import fr.ght1pc9kc.baywatch.api.model.BaywatchAuthentication;
import fr.ght1pc9kc.baywatch.api.model.User;
import fr.ght1pc9kc.baywatch.domain.exceptions.SecurityException;
import fr.ght1pc9kc.baywatch.domain.ports.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;
import java.text.ParseException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

@Slf4j
public class JwtTokenProviderImpl implements JwtTokenProvider {
    private static final String AUTHORITIES_KEY = "roles";
    private final byte[] secretKey;
    private final Duration validity;

    private final Clock clock;

    public JwtTokenProviderImpl(Duration validity) {
        SecureRandom random = new SecureRandom();
        byte[] sharedSecret = new byte[32];
        random.nextBytes(sharedSecret);
        this.secretKey = sharedSecret;
        this.clock = Clock.systemUTC();
        this.validity = validity;
    }

    @Override
    public String createToken(User user, Collection<String> authorities) {
        String auths = String.join(",", authorities);

        try {
            JWSSigner signer = new MACSigner(secretKey);

            Instant now = clock.instant();
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(user.id)
                    .issuer("baywatch/sandside")
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plus(validity)))
                    .claim(AUTHORITIES_KEY, auths)
                    .claim("login", user.login)
                    .claim("name", user.name)
                    .claim("mail", user.mail)
                    .build();
            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
            signedJWT.sign(signer);
            return signedJWT.serialize();

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
            signedJWT.verify(verifier);
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            User user = User.builder()
                    .id(claims.getSubject())
                    .login(claims.getStringClaim("login"))
                    .name(claims.getStringClaim("name"))
                    .mail(claims.getStringClaim("mail"))
                    .build();

            List<String> authorities = Arrays.stream(claims.getStringClaim(AUTHORITIES_KEY).split(","))
                    .map(String::trim)
                    .filter(not(String::isBlank))
                    .collect(Collectors.toUnmodifiableList());

            return new BaywatchAuthentication(user, token, authorities);
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
