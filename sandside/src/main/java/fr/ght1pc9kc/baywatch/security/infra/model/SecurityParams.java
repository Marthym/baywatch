package fr.ght1pc9kc.baywatch.security.infra.model;

import lombok.AllArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@AllArgsConstructor
@ConfigurationProperties(prefix = "baywatch.security")
public class SecurityParams {
    public final JwtParams jwt;
    public final CookieParams cookie;

    @AllArgsConstructor
    public static class JwtParams {
        public final Duration validity;
    }

    @AllArgsConstructor
    public static class CookieParams {
        public final String name;
        public final Duration validity;
    }
}
