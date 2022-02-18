package fr.ght1pc9kc.baywatch.infra.common.config.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import fr.ght1pc9kc.baywatch.api.common.model.Entity;
import fr.ght1pc9kc.baywatch.api.techwatch.model.Feed;
import fr.ght1pc9kc.baywatch.api.techwatch.model.News;
import fr.ght1pc9kc.baywatch.api.security.model.User;
import fr.ght1pc9kc.baywatch.infra.common.mappers.EntityJacksonMixin;
import fr.ght1pc9kc.baywatch.infra.common.config.jackson.mixins.FeedMixin;
import fr.ght1pc9kc.baywatch.infra.common.config.jackson.mixins.NewsMixin;
import fr.ght1pc9kc.baywatch.infra.security.config.UserMixin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class JacksonMappingConfiguration {
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonMapperCustomizer() {
        return builder -> {
            log.debug("Configure Jackson Mapper");
            builder.findModulesViaServiceLoader(true);
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            builder.featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            builder.filters(new SimpleFilterProvider().setFailOnUnknownId(false));
            builder.serializationInclusion(JsonInclude.Include.NON_NULL);
            builder.mixIn(News.class, NewsMixin.class);
            builder.mixIn(Feed.class, FeedMixin.class);
            builder.mixIn(User.class, UserMixin.class);
            builder.mixIn(Entity.class, EntityJacksonMixin.class);
        };
    }
}
