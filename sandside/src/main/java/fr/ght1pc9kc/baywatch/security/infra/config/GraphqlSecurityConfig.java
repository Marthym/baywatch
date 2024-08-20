package fr.ght1pc9kc.baywatch.security.infra.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.ght1pc9kc.baywatch.security.api.model.UserSettings;
import fr.ght1pc9kc.entity.graphql.EntityDataFetcher;
import fr.ght1pc9kc.entity.graphql.EntityTypeResolver;
import graphql.schema.DataFetcher;
import graphql.schema.TypeResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

@Configuration
public class GraphqlSecurityConfig {
    @Bean
    public RuntimeWiringConfigurer customRuntimeWiringConfigurer(ObjectMapper mapper) {
        DataFetcher<Object> dataFetcher = new EntityDataFetcher(mapper);
        TypeResolver typeResolver = new EntityTypeResolver();

        return wiringBuilder -> wiringBuilder
                .type(UserSettings.class.getSimpleName(), builder -> builder.defaultDataFetcher(dataFetcher).typeResolver(typeResolver));
    }
}
