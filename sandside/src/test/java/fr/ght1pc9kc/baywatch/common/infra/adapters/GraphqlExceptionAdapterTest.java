package fr.ght1pc9kc.baywatch.common.infra.adapters;

import fr.ght1pc9kc.baywatch.common.api.exceptions.TranslatableException;
import graphql.GraphQLError;
import graphql.GraphqlErrorException;
import graphql.execution.ExecutionStepInfo;
import graphql.execution.ResultPath;
import graphql.language.Field;
import graphql.schema.DataFetchingEnvironment;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.graphql.execution.ErrorType;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class GraphqlExceptionAdapterTest {

    private final GraphqlExceptionAdapter tested = new GraphqlExceptionAdapter();
    private final DataFetchingEnvironment dataFetchingEnvironment = mock(DataFetchingEnvironment.class);

    @BeforeEach
    void setUp() {
        doReturn(new Field("lightsaber")).when(dataFetchingEnvironment).getField();
        ExecutionStepInfo executionStepInfo = mock(ExecutionStepInfo.class);
        doReturn(executionStepInfo).when(dataFetchingEnvironment).getExecutionStepInfo();
        doReturn(ResultPath.rootPath()).when(executionStepInfo).getPath();
    }

    @Test
    void should_resolve_constraint_violation_exception_like_vader_validates_death_star_plans() {
        // Given
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        doReturn("The Death Star exhaust port must be smaller than 2 meters").when(violation).getMessage();
        ConstraintViolationException exception = new ConstraintViolationException(
                "Death Star construction violated Imperial standards",
                Set.of(violation)
        );

        // When
        GraphQLError actual = tested.resolveToSingleError(exception, dataFetchingEnvironment);

        // Then
        Assertions.assertThat(actual).isNotNull();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(actual.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            softly.assertThat(actual.getExtensions().get("classification")).isEqualTo(ErrorType.BAD_REQUEST.name());
            softly.assertThat(actual.getMessage()).contains("Death Star construction violated Imperial standards");
        });
    }

    @Test
    void should_resolve_illegal_argument_exception_like_luke_using_wrong_lightsaber_crystal() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException(
                "You cannot use a red kyber crystal in a Jedi lightsaber, young Skywalker"
        );

        // When
        GraphQLError actual = tested.resolveToSingleError(exception, dataFetchingEnvironment);

        // Then
        Assertions.assertThat(actual).isNotNull();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(actual.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            softly.assertThat(actual.getExtensions().get("classification")).isEqualTo(ErrorType.BAD_REQUEST.name());
            softly.assertThat(actual.getMessage()).isEqualTo("You cannot use a red kyber crystal in a Jedi lightsaber, young Skywalker");
        });
    }

    @Test
    void should_resolve_translatable_exception_like_protocol_droid_malfunction() {
        // Given
        final class MyTranslatableException extends RuntimeException implements TranslatableException {
            @Override
            public String getTranslationKey() {
                return "protocol.droid.malfunction.c3po";
            }

            @Override
            public String classification() {
                return "FORBIDDEN";
            }

            @Override
            public Map<String, Object> getExtensions() {
                return Map.of(
                        "droidModel", "C-3PO",
                        "malfunctionLevel", "MINOR"
                );
            }

            @Override
            public String getLocalizedMessage() {
                return "Oh my! The odds of successfully navigating an asteroid field are 3,720 to 1!";
            }

            @Override
            public String getMessage() {
                return "Protocol droid calculation error";
            }
        }
        MyTranslatableException exception = new MyTranslatableException();

        // When
        GraphQLError actual = tested.resolveToSingleError(exception, dataFetchingEnvironment);

        // Then
        Assertions.assertThat(actual).isNotNull();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(actual.getErrorType()).isEqualTo(ErrorType.FORBIDDEN);
            softly.assertThat(actual.getMessage()).isEqualTo("Oh my! The odds of successfully navigating an asteroid field are 3,720 to 1!");
            softly.assertThat(actual.getExtensions().get("translation")).isEqualTo("protocol.droid.malfunction.c3po");
            softly.assertThat(actual.getExtensions().get("classification")).isEqualTo("FORBIDDEN");
            softly.assertThat(actual.getExtensions().get("droidModel")).isEqualTo("C-3PO");
            softly.assertThat(actual.getExtensions().get("malfunctionLevel")).isEqualTo("MINOR");
        });
    }

    @Test
    void should_resolve_translatable_exception_with_default_classification_like_jedi_council_error() {
        // Given
        final class MyTranslatableException extends RuntimeException implements TranslatableException {
            @Override
            public String getTranslationKey() {
                return "jedi.council.session.error";
            }

            @Override
            public String getLocalizedMessage() {
                return "A disturbance in the Force, I sense";
            }

            @Override
            public String getMessage() {
                return "Jedi Council session interrupted";
            }
        }
        MyTranslatableException exception = new MyTranslatableException();

        // When
        GraphQLError actual = tested.resolveToSingleError(exception, dataFetchingEnvironment);

        // Then
        Assertions.assertThat(actual).isNotNull();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(actual.getErrorType()).isEqualTo(ErrorType.INTERNAL_ERROR);
            softly.assertThat(actual.getMessage()).isEqualTo("A disturbance in the Force, I sense");
            softly.assertThat(actual.getExtensions().get("translation")).isEqualTo("jedi.council.session.error");
            softly.assertThat(actual.getExtensions().get("classification")).isEqualTo("INTERNAL_ERROR");
        });
    }

    @Test
    void should_resolve_graphql_error_exception_like_rebel_alliance_network_failure() {
        // Given
        GraphqlErrorException exception = GraphqlErrorException.newErrorException()
                .errorClassification(ErrorType.NOT_FOUND)
                .message("Unable to locate rebel base coordinates")
                .extensions(Map.of(
                        "sector", "Yavin",
                        "systemStatus", "COMPROMISED"
                ))
                .path(List.of("rebelBase", "coordinates"))
                .build();

        // When
        GraphQLError actual = tested.resolveToSingleError(exception, dataFetchingEnvironment);

        // Then
        Assertions.assertThat(actual).isNotNull();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(actual.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
            softly.assertThat(actual.getMessage()).isEqualTo("Unable to locate rebel base coordinates");
            softly.assertThat(actual.getExtensions().get("sector")).isEqualTo("Yavin");
            softly.assertThat(actual.getExtensions().get("systemStatus")).isEqualTo("COMPROMISED");
            softly.assertThat(actual.getPath()).isEqualTo(List.of("rebelBase", "coordinates"));
        });
    }

    @Test
    void should_resolve_default_case_like_unknown_sith_force_power() {
        // Given
        RuntimeException exception = new RuntimeException("Unknown Sith force power detected");

        // When
        GraphQLError actual = tested.resolveToSingleError(exception, dataFetchingEnvironment);

        // Then
        Assertions.assertThat(actual)
                .describedAs("Should return null to allow other exception resolvers to handle this unknown Sith power")
                .isNull();
    }

    @Test
    void should_resolve_nested_exception_like_empire_strikes_back() {
        // Given
        IllegalArgumentException nestedException = new IllegalArgumentException("I am your father, Luke");
        RuntimeException wrappedException = new RuntimeException("The Empire strikes back", nestedException);

        // When
        GraphQLError actual = tested.resolveToSingleError(wrappedException, dataFetchingEnvironment);

        // Then
        Assertions.assertThat(actual)
                .describedAs("Should return null as RuntimeException is not handled, even with nested IllegalArgumentException")
                .isNull();
    }

    @Test
    void should_handle_constraint_violation_with_empty_message_like_silent_stormtrooper() {
        // Given
        ConstraintViolationException exception = new ConstraintViolationException(
                "", // Empty Message
                Set.of()
        );

        // When
        GraphQLError actual = tested.resolveToSingleError(exception, dataFetchingEnvironment);

        // Then
        Assertions.assertThat(actual).isNotNull();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(actual.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            softly.assertThat(actual.getExtensions().get("classification")).isEqualTo(ErrorType.BAD_REQUEST.name());
            softly.assertThat(actual.getMessage()).isEmpty();
        });
    }
}