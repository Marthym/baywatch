package fr.ght1pc9kc.baywatch.security.infra.adapters;

import fr.ght1pc9kc.baywatch.security.domain.exceptions.UserCreateException;
import fr.ght1pc9kc.baywatch.security.infra.exceptions.BaywatchCredentialsException;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.jetbrains.annotations.NotNull;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.NoSuchElementException;

import static graphql.ErrorType.ValidationError;

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

            case UserCreateException uce -> GraphQLError.newError()
                    .errorType(ValidationError)
                    .message(uce.getLocalizedMessage())
                    .path(env.getExecutionStepInfo().getPath())
                    .location(env.getField().getSourceLocation())
                    .extensions(Map.of("properties", uce.getFields()))
                    .build();

            case AuthorizationDeniedException ade -> GraphQLError.newError()
                    .errorType(ErrorType.UNAUTHORIZED)
                    .message(ade.getLocalizedMessage())
                    .path(env.getExecutionStepInfo().getPath())
                    .location(env.getField().getSourceLocation())
                    .build();

            default -> null;
        };
    }
}
