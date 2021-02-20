package fr.ght1pc9kc.baywatch.domain.ports;

import fr.ght1pc9kc.baywatch.api.model.BaywatchAuthentication;

import java.util.Collection;

public interface JwtTokenProvider {

    String createToken(String userId, Collection<String> authorities);

    BaywatchAuthentication getAuthentication(String token);

    boolean validateToken(String token);
}
