package fr.ght1pc9kc.baywatch.tests.samples;

import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.common.domain.Hasher;
import fr.ght1pc9kc.baywatch.security.api.model.Role;
import fr.ght1pc9kc.baywatch.security.api.model.User;

import java.util.List;

public final class UserSamples {
    /**
     * The {@link Role#ADMIN} role {@link User}
     */
    public static final Entity<User> YODA = Entity.identify(
            Hasher.sha3("yoda@jedi.com"),
            User.builder()
                    .login("yoda")
                    .name("Yoda Master")
                    .mail("yoda@jedi.com")
                    .role(Role.ADMIN.name())
                    .password("adoy")
                    .build());

    /**
     * The {@link Role#MANAGER} role {@link User}
     */
    public static final Entity<User> OBIWAN = Entity.identify(
            Hasher.sha3("obiwan.kenobi@jedi.com"),
            User.builder()
                    .login("okenobi")
                    .name("Obiwan Kenobi")
                    .mail("obiwan.kenobi@jedi.com")
                    .role(Role.MANAGER.name())
                    .password("nawibo")
                    .build());

    /**
     * The {@link Role#USER} role {@link User}
     */
    public static final Entity<User> LUKE = Entity.identify(
            Hasher.sha3("luke.skywalker@jedi.com"),
            User.builder()
                    .login("lskywalker")
                    .name("Luke Skywalker")
                    .mail("luke.skywalker@jedi.com")
                    .role(Role.USER.name())
                    .password("ekul")
                    .build());

    /**
     * The {@link Role#SYSTEM} role {@link User}
     */
    public static final Entity<User> THE_FORCE = Entity.identify(
            Role.SYSTEM.name(),
            User.builder()
                    .name(Role.SYSTEM.name())
                    .login(Role.SYSTEM.name().toLowerCase())
                    .role(Role.SYSTEM.name()).build());

    public static final List<Entity<User>> SAMPLES = List.of(OBIWAN, LUKE);
}
