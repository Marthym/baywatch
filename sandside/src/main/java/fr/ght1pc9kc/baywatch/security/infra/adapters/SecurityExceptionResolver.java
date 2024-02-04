package fr.ght1pc9kc.baywatch.security.infra.adapters;

import fr.ght1pc9kc.baywatch.security.infra.exceptions.BaywatchCredentialsException;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.jetbrains.annotations.NotNull;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;

@Component
public class SecurityExceptionResolver extends DataFetcherExceptionResolverAdapter {
    @Override
    protected GraphQLError resolveToSingleError(@NotNull Throwable ex, @NotNull DataFetchingEnvironment env) {
        return switch (ex) {
            case BaywatchCredentialsException bce -> GraphqlErrorBuilder.newError()
                    .errorType(ErrorType.UNAUTHORIZED)
                    .message(bce.getLocalizedMessage())
                    .path(env.getExecutionStepInfo().getPath())
                    .location(env.getField().getSourceLocation())
                    .build();

            case NoSuchElementException nsee -> GraphQLError.newError()
                    .errorType(ErrorType.NOT_FOUND)
                    .message(nsee.getLocalizedMessage())
                    .path(env.getExecutionStepInfo().getPath())
                    .location(env.getField().getSourceLocation())
                    .build();

            case IllegalArgumentException iae -> GraphQLError.newError()
                    .errorType(ErrorType.BAD_REQUEST)
                    .message(iae.getLocalizedMessage())
                    .path(env.getExecutionStepInfo().getPath())
                    .location(env.getField().getSourceLocation())
                    .build();

            default -> null;
        };
    }
}
