package fr.ght1pc9kc.baywatch.tests.samples;

import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.security.api.model.Permission;
import fr.ght1pc9kc.baywatch.security.api.model.Role;
import fr.ght1pc9kc.baywatch.security.api.model.User;

import java.util.List;

public final class UserSamples {
    /**
     * The {@link Role#ADMIN} role {@link User}
     */
    public static final Entity<User> YODA = Entity.identify(
            "US01GRQ11X1W8E6NQER7E1FNQ7HC",
            User.builder()
                    .login("yoda")
                    .name("Yoda Master")
                    .mail("yoda@jedi.com")
                    .role(Role.ADMIN)
                    .password("adoy")
                    .build());

    /**
     * The {@link Role#MANAGER} role {@link User}
     */
    public static final Entity<User> OBIWAN = Entity.identify(
            "US01GRQ11XKGHERDEBSCHBNJAY78",
            User.builder()
                    .login("okenobi")
                    .name("Obiwan Kenobi")
                    .mail("obiwan.kenobi@jedi.com")
                    .role(Role.MANAGER)
                    .password("nawibo")
                    .build());

    /**
     * The {@link User} role {@link Role#USER} and
     * {@link Role#MANAGER} for {@code TM01GP696RFPTY32WD79CVB0KDTF} (JEDI_TEAM)
     */
    public static final Entity<User> LUKE = Entity.identify(
            "US01GRQ15DCEX52JH4GWJ26G33ME",
            User.builder()
                    .login("lskywalker")
                    .name("Luke Skywalker")
                    .mail("luke.skywalker@jedi.com")
                    .role(Role.USER)
                    .role(Permission.manager("TM01GP696RFPTY32WD79CVB0KDTF"))
                    .password("ekul")
                    .build());

    /**
     * The {@link User} role {@link Role#USER} and
     * {@link Role#MANAGER} for {@code TM01GP696RFPTY32WD79CVB0KDTF} (JEDI_TEAM)
     */
    public static final Entity<User> MWINDU = Entity.identify(
            "US01GRQ15DCEX52JH4GWJ26G33ME",
            User.builder()
                    .login("mwindu")
                    .name("Mace Windu")
                    .mail("mace.windu@jedi.com")
                    .role(Role.USER)
                    .role(Permission.manager("TM01GP696RFPTY32WD79CVB0KDTF"))
                    .password("udniw")
                    .build());

    /**
     * The {@link User} role {@link Role#USER} and
     * {@link Role#MANAGER} for {@code TM01GPETWVATJ968SJ717NRHYSEZ} (SITH_TEAM)
     */
    public static final Entity<User> DSIDIOUS = Entity.identify(
            "US01GRQ15F7DNWH4Q6Q1H5F7HYT0",
            User.builder()
                    .login("dsidious")
                    .name("Darth Sidious")
                    .mail("darth.sidious@sith.com")
                    .role(Role.USER)
                    .role(Permission.manager("TM01GPETWVATJ968SJ717NRHYSEZ"))
                    .password("suoidis")
                    .build());

    /**
     * The {@link Role#SYSTEM} role {@link User}
     */
    public static final Entity<User> THE_FORCE = Entity.identify(
            Role.SYSTEM.name(),
            User.builder()
                    .name(Role.SYSTEM.name())
                    .login(Role.SYSTEM.name().toLowerCase())
                    .role(Role.SYSTEM).build());

    public static final List<Entity<User>> SAMPLES = List.of(OBIWAN, LUKE);
}
