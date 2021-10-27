package fr.ght1pc9kc.baywatch.infra.security.model;

import lombok.AllArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.time.Duration;

@AllArgsConstructor
@ConstructorBinding
@ConfigurationProperties(prefix = "baywatch.security")
public class SecurityParams {
    public final JwtParams jwt;
    public final CookieParams cookie;

    @AllArgsConstructor
    @ConstructorBinding
    public static class JwtParams {
        public final Duration validity;
    }

    @AllArgsConstructor
    @ConstructorBinding
    public static class CookieParams {
        public final String name;
        public final Duration validity;
    }
}
