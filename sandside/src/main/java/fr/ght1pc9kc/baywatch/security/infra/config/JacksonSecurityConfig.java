package fr.ght1pc9kc.baywatch.security.infra.config;

import fr.ght1pc9kc.baywatch.security.api.model.Permission;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class JacksonSecurityConfig {
    @Bean
    public JsonMapperBuilderCustomizer jacksonMapperSecurityCustomizer() {
        return builder -> {
            log.debug("Configure Security Jackson Mapper");
            builder.addMixIn(User.class, UserMixin.class);
            builder.addMixIn(Permission.class, PermissionMixin.class);
        };
    }
}
