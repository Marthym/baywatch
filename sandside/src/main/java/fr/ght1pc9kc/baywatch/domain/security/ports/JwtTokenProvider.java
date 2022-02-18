package fr.ght1pc9kc.baywatch.domain.security.ports;

import fr.ght1pc9kc.baywatch.api.common.model.Entity;
import fr.ght1pc9kc.baywatch.api.security.model.BaywatchAuthentication;
import fr.ght1pc9kc.baywatch.api.security.model.User;

import java.util.Collection;

public interface JwtTokenProvider {

    BaywatchAuthentication createToken(Entity<User> user, boolean remember, Collection<String> authorities);

    BaywatchAuthentication getAuthentication(String token);

    boolean validateToken(String token, boolean checkExpiration);

    default boolean validateToken(String token) {
        return validateToken(token, true);
    }
}
