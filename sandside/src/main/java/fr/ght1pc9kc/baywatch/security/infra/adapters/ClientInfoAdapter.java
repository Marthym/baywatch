package fr.ght1pc9kc.baywatch.security.infra.adapters;

import fr.ght1pc9kc.baywatch.common.api.model.UserMeta;
import fr.ght1pc9kc.baywatch.common.infra.filters.ReactiveClientInfoContextHolder;
import fr.ght1pc9kc.baywatch.common.infra.filters.ReactiveLocaleContextHolder;
import fr.ght1pc9kc.baywatch.security.api.model.Permission;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.security.domain.ports.ReactiveContextPort;
import fr.ght1pc9kc.entity.api.Entity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import reactor.util.context.Context;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Locale;

@Component
public class ClientInfoAdapter implements ReactiveContextPort {

    @Override
    public Context withUserContext(Entity<User> user) {
        // Retrieve client IP to allow limit rate in mail
        InetSocketAddress inetAddress = user.meta(UserMeta.loginIP).map(ip -> {
                    URI uri = URI.create("https://" + ip.replace("/", ""));
                    return new InetSocketAddress(uri.getHost(), uri.getPort());
                })
                .orElse(new InetSocketAddress("127.0.0.1", 0));

        // Rebuild authentication with user roles
        Authentication authentication = new PreAuthenticatedAuthenticationToken(user, null,
                AuthorityUtils.createAuthorityList(user.self().roles().stream()
                        .map(Permission::toString)
                        .toArray(String[]::new)));

        Locale locale = user.meta(UserMeta.locale, Locale.class).orElse(Locale.getDefault());

        return ReactiveClientInfoContextHolder.withClientInfo(inetAddress, null, null)
                .putAll(ReactiveSecurityContextHolder.withAuthentication(authentication).readOnly())
                .putAll(ReactiveLocaleContextHolder.withLocale(locale).readOnly());
    }
}
