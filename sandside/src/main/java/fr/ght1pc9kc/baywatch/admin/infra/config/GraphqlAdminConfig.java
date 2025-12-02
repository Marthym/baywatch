package fr.ght1pc9kc.baywatch.admin.infra.config;

import fr.ght1pc9kc.baywatch.admin.api.model.RawFeed;
import fr.ght1pc9kc.entity.graphql.EntityDataFetcher;
import fr.ght1pc9kc.entity.graphql.EntityTypeResolver;
import fr.ght1pc9kc.entity.json.Jackson3EntityMapper;
import graphql.schema.DataFetcher;
import graphql.schema.TypeResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class GraphqlAdminConfig {
    @Bean
    public RuntimeWiringConfigurer adminRuntimeWiringConfigurer(ObjectMapper mapper) {
        DataFetcher<Object> dataFetcher = EntityDataFetcher.builder()
                .mapper(new Jackson3EntityMapper(mapper))
                .build();
        TypeResolver typeResolver = new EntityTypeResolver();

        return wiringBuilder -> wiringBuilder
                .type(RawFeed.class.getSimpleName(), builder -> builder.defaultDataFetcher(dataFetcher).typeResolver(typeResolver));
    }
}
