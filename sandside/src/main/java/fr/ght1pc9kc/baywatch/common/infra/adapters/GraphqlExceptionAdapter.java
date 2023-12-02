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
        if (ex instanceof ConstraintViolationException cve) {
            return GraphqlErrorBuilder.newError(env)
                    .errorType(ErrorType.ValidationError)
                    .message(cve.getLocalizedMessage())
                    .build();
        }

        if (ex instanceof UnauthorizedException uex) {
            return GraphqlErrorBuilder.newError(env)
                    .errorType(ErrorType.OperationNotSupported)
                    .extensions(Map.of("classification", "FORBIDDEN"))
                    .message(uex.getLocalizedMessage())
                    .build();
        }

        if (ex instanceof IllegalArgumentException iaex) {
            return GraphqlErrorBuilder.newError(env)
                    .errorType(ErrorType.ValidationError)
                    .extensions(Map.of("classification", "ValidationError"))
                    .message(iaex.getLocalizedMessage())
                    .build();
        }

        if (ex instanceof GraphqlErrorException gex) {
            return GraphqlErrorBuilder.newError(env)
                    .errorType(gex.getErrorType())
                    .extensions(gex.getExtensions())
                    .path(gex.getPath())
                    .locations(gex.getLocations())
                    .message(gex.getLocalizedMessage())
                    .build();
        }

        return null;
    }
}
