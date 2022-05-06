package fr.ght1pc9kc.baywatch.security.domain.ports;

import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.security.api.model.BaywatchAuthentication;
import fr.ght1pc9kc.baywatch.security.api.model.User;

import java.util.Collection;

public interface JwtTokenProvider {

    BaywatchAuthentication createToken(Entity<User> user, boolean remember, Collection<String> authorities);

    BaywatchAuthentication getAuthentication(String token);

    boolean validateToken(String token, boolean checkExpiration);

    default boolean validateToken(String token) {
        return validateToken(token, true);
    }
}
