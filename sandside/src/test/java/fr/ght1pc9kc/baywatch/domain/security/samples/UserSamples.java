package fr.ght1pc9kc.baywatch.domain.security.samples;

import fr.ght1pc9kc.baywatch.api.common.model.Entity;
import fr.ght1pc9kc.baywatch.api.security.model.Role;
import fr.ght1pc9kc.baywatch.api.security.model.User;
import fr.ght1pc9kc.baywatch.domain.utils.Hasher;

import java.util.List;

public final class UserSamples {
    public static final Entity<User> YODA = Entity.identify(
            Hasher.sha3("yoda@jedi.com"),
            User.builder()
                    .login("yoda")
                    .name("Yoda Master")
                    .mail("yoda@jedi.com")
                    .role(Role.ADMIN)
                    .build());

    public static final Entity<User> OBIWAN = Entity.identify(
            Hasher.sha3("obiwan.kenobi@jedi.com"), User.builder()
                    .login("okenobi")
                    .name("Obiwan Kenobi")
                    .mail("obiwan.kenobi@jedi.com")
                    .role(Role.MANAGER)
                    .build());

    public static final Entity<User> LUKE = Entity.identify(
            Hasher.sha3("luke.skywalker@jedi.com"),
            User.builder()
                    .login("lskywalker")
                    .name("Luke Skywalker")
                    .mail("luke.skywalker@jedi.com")
                    .role(Role.USER)
                    .build());

    public static final List<Entity<User>> SAMPLES = List.of(OBIWAN, LUKE);
}
