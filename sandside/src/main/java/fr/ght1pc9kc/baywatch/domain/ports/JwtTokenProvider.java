package fr.ght1pc9kc.baywatch.domain.ports;

import fr.ght1pc9kc.baywatch.api.security.model.BaywatchAuthentication;
import fr.ght1pc9kc.baywatch.api.security.model.User;

import java.util.Collection;

public interface JwtTokenProvider {

    BaywatchAuthentication createToken(User user, boolean remember, Collection<String> authorities);

    BaywatchAuthentication getAuthentication(String token);

    boolean validateToken(String token, boolean checkExpiration);

    default boolean validateToken(String token) {
        return validateToken(token, true);
    }
}
