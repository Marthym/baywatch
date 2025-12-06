package fr.ght1pc9kc.baywatch.common.infra.config.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.security.infra.config.UserMixin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.SimpleFilterProvider;

import java.util.Locale;

@Slf4j
@Configuration
public class JacksonMappingConfiguration {
    @Bean
    public JsonMapperBuilderCustomizer jacksonMapperCustomizer() {
        return builder -> {
            log.debug("Configure Common Jackson Mapper");
            builder.findAndAddModules();
            builder.disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS);
            builder.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            builder.filterProvider(new SimpleFilterProvider().setFailOnUnknownId(false));
            builder.changeDefaultPropertyInclusion(spec -> spec.withContentInclusion(JsonInclude.Include.NON_NULL));
            builder.addModule(
                    new SimpleModule().addSerializer(Locale.class, new LocaleToLanguageTagSerializer()));
            builder.addMixIn(User.class, UserMixin.class);
        };
    }
}
