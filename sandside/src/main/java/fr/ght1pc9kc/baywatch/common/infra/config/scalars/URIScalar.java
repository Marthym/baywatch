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
                String urlStr;
                if (input instanceof String) {
                    urlStr = String.valueOf(input);
                } else {
                    Optional<URI> url = toURI(input);
                    if (url.isEmpty()) {
                        throw new CoercingParseValueException(
                                "Expected a 'URI' like object but was '" + input.getClass().getSimpleName() + "'."
                        );
                    }
                    return url.get();
                }
                return parseURL(urlStr);
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
                URI url = serialize(input, graphQLContext, locale);
                return StringValue.newStringValue(url.toString()).build();
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
        if (input instanceof URL url) {
            return Optional.of(url).map(Exceptions.wrap().function(URL::toURI));
        } else if (input instanceof URI uri) {
            return Optional.of((uri));
        } else if (input instanceof File file) {
            return Optional.of((file).toURI());
        }
        return Optional.empty();
    }
}
