package fr.ght1pc9kc.baywatch.common.infra.config.scalars;

import com.machinezoo.noexception.Exceptions;
import graphql.GraphQLContext;
import graphql.execution.CoercedVariables;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Locale;
import java.util.Optional;

@UtilityClass
public class URIScalar {

    public static final GraphQLScalarType INSTANCE;

    static {
        Coercing<URI, URI> coercing = new Coercing<>() {
            @Override
            public URI serialize(@NotNull Object input,
                                 @NotNull GraphQLContext graphQLContext,
                                 @NotNull Locale locale) throws CoercingSerializeException {
                Optional<URI> url;
                if (input instanceof String) {
                    url = Optional.of(parseURL(input.toString()));
                } else {
                    url = toURI(input);
                }
                if (url.isPresent()) {
                    return url.get();
                }
                throw new CoercingSerializeException(
                        "Expected a 'URI' like object but was '" + input.getClass().getSimpleName() + "'."
                );
            }

            @Override
            public @NotNull URI parseValue(@NotNull Object input,
                                           @NotNull GraphQLContext graphQLContext,
                                           @NotNull Locale locale) throws CoercingParseValueException {
                if (input instanceof String) {
                    return parseURL(String.valueOf(input));
                } else {
                    return toURI(input).orElseThrow(() -> new CoercingParseValueException(
                            "Expected a 'URI' like object but was '" + input.getClass().getSimpleName() + "'."));
                }
            }

            @Override
            public @NotNull URI parseLiteral(@NotNull Value<?> input,
                                             @NotNull CoercedVariables variables,
                                             @NotNull GraphQLContext graphQLContext,
                                             @NotNull Locale locale) throws CoercingParseLiteralException {
                if (!(input instanceof StringValue)) {
                    throw new CoercingParseLiteralException(
                            "Expected AST type 'StringValue' but was '" + input.getClass().getSimpleName() + "'."
                    );
                }
                return parseURL(((StringValue) input).getValue());
            }

            @Override
            public @NotNull Value<StringValue> valueToLiteral(@NotNull Object input,
                                                              @NotNull GraphQLContext graphQLContext,
                                                              @NotNull Locale locale) {
                return StringValue.newStringValue(
                                serialize(input, graphQLContext, locale)
                                        .toString())
                        .build();
            }

            private URI parseURL(String input) {
                try {
                    return URI.create(input);
                } catch (Exception e) {
                    throw new CoercingParseLiteralException("Unable to parse value " + input + " as an URI !", e);
                }
            }
        };

        INSTANCE = GraphQLScalarType.newScalar()
                .name("URI")
                .description("A Uri scalar")
                .coercing(coercing)
                .build();
    }

    private static Optional<URI> toURI(Object input) {
        return switch (input) {
            case URL url -> Optional.of(url).map(Exceptions.wrap().function(URL::toURI));
            case URI uri -> Optional.of((uri));
            case File file -> Optional.of((file).toURI());
            default -> Optional.empty();
        };
    }
}
