package fr.ght1pc9kc.baywatch.infra.adapters;

import fr.ght1pc9kc.baywatch.domain.ports.JwtTokenProvider;
import fr.ght1pc9kc.baywatch.domain.security.JwtTokenProviderImpl;
import lombok.experimental.Delegate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;

@Component
public class JwtTokenProviderAdapter implements JwtTokenProvider {

    @Delegate
    private final JwtTokenProvider tokenProvider;

    public JwtTokenProviderAdapter(@Value("${baywatch.security.jwt.validity}") Duration tokenValidity) {
        SecureRandom random = new SecureRandom();
        byte[] sharedSecret = new byte[32];
        random.nextBytes(sharedSecret);
        this.tokenProvider = new JwtTokenProviderImpl(sharedSecret, tokenValidity, Clock.systemUTC());
    }
}
