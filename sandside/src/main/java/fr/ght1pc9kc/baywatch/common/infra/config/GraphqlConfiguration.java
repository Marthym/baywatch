package fr.ght1pc9kc.baywatch.common.infra.config;

import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

@Configuration
public class GraphqlConfiguration {
    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {

        GraphQLScalarType voidScalar = GraphQLScalarType.newScalar()
                .name("Void")
                .coercing(new Coercing<>() {
                    @Override
                    public Object serialize(@NotNull Object dataFetcherResult) throws CoercingSerializeException {
                        return null;
                    }

                    @Override
                    public @NotNull Object parseValue(@NotNull Object input) throws CoercingParseValueException {
                        return new Object();
                    }

                    @Override
                    public @NotNull Object parseLiteral(@NotNull Object input) throws CoercingParseLiteralException {
                        return new Object();
                    }
                })
                .build();
        return wiringBuilder -> wiringBuilder
                .scalar(voidScalar);
    }
}
