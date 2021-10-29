package fr.ght1pc9kc.baywatch.infra.security;

import fr.ght1pc9kc.baywatch.api.security.model.BaywatchAuthentication;
import fr.ght1pc9kc.baywatch.infra.security.model.SecurityParams;
import fr.ght1pc9kc.baywatch.infra.security.model.SecurityParams.CookieParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxProperties.SameSite;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
public final class TokenCookieManager {
    private final String baseRoute;
    private final CookieParams params;

    public TokenCookieManager(@Value("${baywatch.base-route}") String baseRoute, SecurityParams params) {
        this.baseRoute = baseRoute;
        this.params = params.cookie;
    }

    public Optional<HttpCookie> getTokenCookie(ServerHttpRequest request) {
        return Optional.ofNullable(request.getCookies().getFirst(params.name));
    }

    public ResponseCookie buildTokenCookie(String scheme, BaywatchAuthentication bwAuth) {
        Duration maxAge = (bwAuth.isRememberMe()) ? Duration.ofSeconds(-1) : params.validity;

        return ResponseCookie.from(params.name, bwAuth.getToken())
                .httpOnly(true)
                .secure("https".equals(scheme))
                .sameSite(SameSite.STRICT.attribute())
                .maxAge(maxAge)
                .path(baseRoute)
                .build();
    }

    public ResponseCookie buildTokenCookieDeletion(String scheme) {
        return ResponseCookie.from(params.name, "")
                .httpOnly(true)
                .secure("https".equals(scheme))
                .sameSite(SameSite.STRICT.attribute())
                .path(baseRoute)
                .maxAge(0)
                .build();
    }
}
