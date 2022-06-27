package fr.ght1pc9kc.baywatch.common.infra.adapters;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.GraphqlErrorException;
import graphql.schema.DataFetchingEnvironment;
import org.jetbrains.annotations.NotNull;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.stereotype.Component;

@Component
public class GraphqlExceptionAdapter extends DataFetcherExceptionResolverAdapter {
    @Override
    protected GraphQLError resolveToSingleError(@NotNull Throwable ex, @NotNull DataFetchingEnvironment env) {
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
