package fr.ght1pc9kc.baywatch.infra.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import fr.ght1pc9kc.baywatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.api.model.News;
import fr.ght1pc9kc.baywatch.infra.config.jackson.FeedMixin;
import fr.ght1pc9kc.baywatch.infra.config.jackson.NewsMixin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MappingConfiguration {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonMapperCustomizer() {
        return builder -> {
            log.debug("Configure Jackson");
            builder.findModulesViaServiceLoader(true);
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            builder.featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            builder.filters(new SimpleFilterProvider().setFailOnUnknownId(false));
            builder.serializationInclusion(JsonInclude.Include.NON_NULL);
            builder.mixIn(News.class, NewsMixin.class);
            builder.mixIn(Feed.class, FeedMixin.class);
        };
    }
}
