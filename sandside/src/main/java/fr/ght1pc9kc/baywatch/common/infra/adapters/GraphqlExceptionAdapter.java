package fr.ght1pc9kc.baywatch.common.infra.adapters;

import fr.ght1pc9kc.baywatch.common.api.exceptions.UnauthorizedException;
import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.GraphqlErrorException;
import graphql.schema.DataFetchingEnvironment;
import jakarta.validation.ConstraintViolationException;
import org.jetbrains.annotations.NotNull;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GraphqlExceptionAdapter extends DataFetcherExceptionResolverAdapter {
    @Override
    protected GraphQLError resolveToSingleError(@NotNull Throwable ex, @NotNull DataFetchingEnvironment env) {
        return switch (ex) {
            case ConstraintViolationException cve -> GraphqlErrorBuilder.newError(env)
                    .errorType(ErrorType.ValidationError)
                    .message(cve.getLocalizedMessage())
                    .build();
            case UnauthorizedException uex -> GraphqlErrorBuilder.newError(env)
                    .errorType(ErrorType.OperationNotSupported)
                    .extensions(Map.of("classification", "FORBIDDEN"))
                    .message(uex.getLocalizedMessage())
                    .build();
            case IllegalArgumentException iaex -> GraphqlErrorBuilder.newError(env)
                    .errorType(ErrorType.ValidationError)
                    .extensions(Map.of("classification", "ValidationError"))
                    .message(iaex.getLocalizedMessage())
                    .build();
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
}
