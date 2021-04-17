package fr.ght1pc9kc.baywatch.domain.ports;

import fr.ght1pc9kc.baywatch.api.security.model.BaywatchAuthentication;
import fr.ght1pc9kc.baywatch.api.security.model.User;

import java.util.Collection;

public interface JwtTokenProvider {

    String createToken(User userId, Collection<String> authorities);

    BaywatchAuthentication getAuthentication(String token);

    boolean validateToken(String token, boolean checkExpiration);

    default boolean validateToken(String token) {
        return validateToken(token, true);
    }
}
