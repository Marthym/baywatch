package fr.ght1pc9kc.baywatch.techwatch.infra.config;

import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import fr.ght1pc9kc.baywatch.techwatch.api.model.State;
import fr.ght1pc9kc.baywatch.techwatch.infra.model.NewsMixin;
import fr.ght1pc9kc.baywatch.techwatch.infra.model.StateMixin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class TechwatchJacksonMappingConfiguration {
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer techwatchJacksonMapperCustomizer() {
        return builder -> {
            log.debug("Configure Techwatch Jackson Mapper");
            builder.mixIn(News.class, NewsMixin.class);
            builder.mixIn(State.class, StateMixin.class);
        };
    }
}
