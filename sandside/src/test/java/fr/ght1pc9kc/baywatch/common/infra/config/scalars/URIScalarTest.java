package fr.ght1pc9kc.baywatch.common.infra.config.scalars;

import graphql.GraphQLContext;
import graphql.execution.CoercedVariables;
import graphql.language.NullValue;
import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Locale;

class URIScalarTest {

    @Test
    void should_serialize_Scalar() throws MalformedURLException {
        Coercing<?, ?> coercing = URIScalar.INSTANCE.getCoercing();
        GraphQLContext gqlContext = GraphQLContext.newContext().build();
        Assertions.assertThat(
                        coercing.serialize("https://jedi.com/", gqlContext, Locale.US))
                .isInstanceOf(URI.class)
                .isEqualTo(URI.create("https://jedi.com/"));

        Assertions.assertThat(
                        coercing.serialize(URI.create("https://jedi.com/"), gqlContext, Locale.US))
                .isInstanceOf(URI.class)
                .isEqualTo(URI.create("https://jedi.com/"));

        Assertions.assertThat(
                        coercing.serialize(URI.create("https://jedi.com/").toURL(), gqlContext, Locale.US))
                .isInstanceOf(URI.class)
                .isEqualTo(URI.create("https://jedi.com/"));

        Assertions.assertThat(
                        coercing.serialize(new File(URI.create("file:///test.txt")), gqlContext, Locale.US))
                .isInstanceOf(URI.class)
                .isEqualTo(URI.create("file:///test.txt"));

        Object input = new Object();
        Assertions.assertThatThrownBy(() -> coercing.serialize(input, gqlContext, Locale.US))
                .isInstanceOf(CoercingSerializeException.class);
    }

    @Test
    void should_parse_scalar() {
        GraphQLContext gqlContext = GraphQLContext.newContext().build();
        Coercing<?, ?> coercing = URIScalar.INSTANCE.getCoercing();
        Assertions.assertThat(
                        coercing.parseValue("https://jedi.com/", gqlContext, Locale.US))
                .isInstanceOf(URI.class)
                .isEqualTo(URI.create("https://jedi.com/"));

        Object input = new Object();
        Assertions.assertThatThrownBy(() -> coercing.parseValue(input, gqlContext, Locale.US))
                .isInstanceOf(CoercingParseValueException.class);
    }

    @Test
    void should_parse_literal() {
        GraphQLContext gqlContext = GraphQLContext.newContext().build();
        Coercing<?, ?> coercing = URIScalar.INSTANCE.getCoercing();
        CoercedVariables variables = CoercedVariables.emptyVariables();
        Assertions.assertThat(
                        coercing.parseLiteral(new StringValue("https://jedi.com/"), variables, gqlContext, Locale.US))
                .isInstanceOf(URI.class)
                .isEqualTo(URI.create("https://jedi.com/"));

        NullValue input = NullValue.of();
        Assertions.assertThatThrownBy(() -> coercing.parseLiteral(input, variables, gqlContext, Locale.US))
                .isInstanceOf(CoercingParseLiteralException.class);

        StringValue wrongUriInput = new StringValue("/\\");
        Assertions.assertThatThrownBy(() -> coercing.parseLiteral(wrongUriInput, variables, gqlContext, Locale.US))
                .isInstanceOf(CoercingParseLiteralException.class);
    }

    @Test
    void should_value_to_literal() {
        GraphQLContext gqlContext = GraphQLContext.newContext().build();
        Coercing<?, ?> coercing = URIScalar.INSTANCE.getCoercing();
        Assertions.assertThat(
                        coercing.valueToLiteral(URI.create("https://jedi.com/"), gqlContext, Locale.US))
                .isInstanceOf(StringValue.class)
                .extracting(v -> ((StringValue) v).getValue())
                .isEqualTo("https://jedi.com/");
    }
}