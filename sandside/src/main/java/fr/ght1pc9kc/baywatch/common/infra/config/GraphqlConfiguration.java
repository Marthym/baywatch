package fr.ght1pc9kc.baywatch.common.infra.config;

import fr.ght1pc9kc.baywatch.common.infra.config.scalars.URIScalar;
import graphql.schema.Coercing;
import graphql.schema.GraphQLScalarType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

@Slf4j
@Configuration
public class GraphqlConfiguration {
    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {

        GraphQLScalarType voidScalar = GraphQLScalarType.newScalar()
                .name("Void")
                .coercing(new Coercing<>() {
                })
                .build();

        return wiringBuilder -> wiringBuilder
                .scalar(voidScalar)
                .scalar(URIScalar.INSTANCE);
    }
}
