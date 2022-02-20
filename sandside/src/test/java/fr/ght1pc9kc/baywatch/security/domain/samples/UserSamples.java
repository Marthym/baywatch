package fr.ght1pc9kc.baywatch.security.domain.samples;

import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.security.api.model.Role;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.common.domain.Hasher;

import java.util.List;

public final class UserSamples {
    public static final Entity<User> YODA = Entity.identify(
            Hasher.sha3("yoda@jedi.com"),
            User.builder()
                    .login("yoda")
                    .name("Yoda Master")
                    .mail("yoda@jedi.com")
                    .role(Role.ADMIN)
                    .password("adoy")
                    .build());

    public static final Entity<User> OBIWAN = Entity.identify(
            Hasher.sha3("obiwan.kenobi@jedi.com"), User.builder()
                    .login("okenobi")
                    .name("Obiwan Kenobi")
                    .mail("obiwan.kenobi@jedi.com")
                    .role(Role.MANAGER)
                    .password("nawibo")
                    .build());

    public static final Entity<User> LUKE = Entity.identify(
            Hasher.sha3("luke.skywalker@jedi.com"),
            User.builder()
                    .login("lskywalker")
                    .name("Luke Skywalker")
                    .mail("luke.skywalker@jedi.com")
                    .role(Role.USER)
                    .password("ekul")
                    .build());

    public static final List<Entity<User>> SAMPLES = List.of(OBIWAN, LUKE);
}
