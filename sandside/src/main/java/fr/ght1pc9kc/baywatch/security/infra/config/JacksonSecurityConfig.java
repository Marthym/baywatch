package fr.ght1pc9kc.baywatch.security.infra.config;

import fr.ght1pc9kc.baywatch.security.api.model.Permission;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class JacksonSecurityConfig {
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonMapperSecurityCustomizer() {
        return builder -> {
            log.debug("Configure Security Jackson Mapper");
            builder.mixIn(User.class, UserMixin.class);
            builder.mixIn(Permission.class, PermissionMixin.class);
        };
    }
}
