package fr.ght1pc9kc.baywatch.security.infra;

import fr.ght1pc9kc.baywatch.security.api.model.BaywatchAuthentication;
import fr.ght1pc9kc.baywatch.security.api.model.Role;
import fr.ght1pc9kc.baywatch.security.api.model.RoleUtils;
import fr.ght1pc9kc.baywatch.security.infra.model.SecurityParams;
import fr.ght1pc9kc.baywatch.security.infra.model.SecurityParams.CookieParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.Cookie;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Component
public final class TokenCookieManager {
    private static final String HTTP_SECURE_SCHEME = "https";
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
        Duration maxAge = (bwAuth.rememberMe()) ? Duration.ofSeconds(-1) : params.validity;
        String path = (bwAuth.authorities().contains(RoleUtils.toSpringAuthority(Role.ADMIN))) ?
                "/" : baseRoute;
        return ResponseCookie.from(params.name, bwAuth.token())
                .httpOnly(true)
                .secure(HTTP_SECURE_SCHEME.equals(scheme))
                .sameSite(Cookie.SameSite.STRICT.attributeValue())
                .maxAge(maxAge)
                .path(path)
                .build();
    }

    public List<ResponseCookie> buildTokenCookieDeletion(String scheme) {
        return List.of(
                ResponseCookie.from(params.name, "")
                        .httpOnly(true)
                        .secure(HTTP_SECURE_SCHEME.equals(scheme))
                        .sameSite(Cookie.SameSite.STRICT.attributeValue())
                        .path("/")
                        .maxAge(0)
                        .build(),
                ResponseCookie.from(params.name, "")
                        .httpOnly(true)
                        .secure(HTTP_SECURE_SCHEME.equals(scheme))
                        .sameSite(Cookie.SameSite.STRICT.attributeValue())
                        .path(baseRoute)
                        .maxAge(0)
                        .build()
        );
    }
}
