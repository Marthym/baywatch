package fr.ght1pc9kc.baywatch.security.domain.ports;

import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.entity.api.Entity;
import reactor.util.context.Context;

public interface ReactiveContextPort {
    Context withUserContext(Entity<User> user);
}
