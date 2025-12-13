package fr.ght1pc9kc.baywatch.common.infra.adapters;

import fr.ght1pc9kc.baywatch.common.api.exceptions.TranslatableException;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.GraphqlErrorException;
import graphql.schema.DataFetchingEnvironment;
import jakarta.validation.ConstraintViolationException;
import org.jetbrains.annotations.NotNull;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class GraphqlExceptionAdapter extends DataFetcherExceptionResolverAdapter {

    private static final String EXT_CLASSIFICATION = "classification";
    private static final String EXT_TRANSLATION = "translation";
    private static final String EXT_VIOLATIONS = "violations";
    private static final String CONSTRAINTS_VIOLATION_TRANSLATION_KEY = "sandside.common.constraints.violation.message";

    @Override
    protected GraphQLError resolveToSingleError(@NotNull Throwable ex, @NotNull DataFetchingEnvironment env) {
        return switch (ex) {
            case ConstraintViolationException cve -> handlerConstraintViolationException(cve, env);
            case IllegalArgumentException iaex -> GraphqlErrorBuilder.newError(env)
                    .errorType(ErrorType.BAD_REQUEST)
                    .extensions(Map.of(EXT_CLASSIFICATION, ErrorType.BAD_REQUEST.name()))
                    .message(iaex.getLocalizedMessage())
                    .build();
            case TranslatableException tlx -> GraphQLError.newError()
                    .errorType(ErrorType.valueOf(tlx.classification()))
                    .message(tlx.getLocalizedMessage())
                    .extensions(Stream.concat(
                            tlx.getExtensions().entrySet().stream(),
                            Stream.of(
                                    Map.entry(EXT_TRANSLATION, tlx.getTranslationKey()),
                                    Map.entry(EXT_CLASSIFICATION, tlx.classification())
                            )).collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue))
                    ).build();
            case GraphqlErrorException gex -> GraphqlErrorBuilder.newError(env)
                    .errorType(gex.getErrorType())
                    .extensions(gex.getExtensions())
                    .path(gex.getPath())
                    .locations(gex.getLocations())
                    .message(gex.getLocalizedMessage())
                    .build();
            default -> null;
        };

    }

    private static GraphQLError handlerConstraintViolationException(ConstraintViolationException ex, DataFetchingEnvironment env) {
        return GraphqlErrorBuilder.newError(env)
                .message(ex.getLocalizedMessage())
                .errorType(ErrorType.BAD_REQUEST)
                .extensions(Map.of(
                        EXT_TRANSLATION, CONSTRAINTS_VIOLATION_TRANSLATION_KEY,
                        EXT_CLASSIFICATION, ErrorType.BAD_REQUEST.name(),
                        EXT_VIOLATIONS, ex.getConstraintViolations().stream()
                                .map(v -> Map.of(
                                        "field", v.getPropertyPath().toString(),
                                        "message", v.getMessage()
                                ))
                                .toList()
                ))
                .build();
    }
}
