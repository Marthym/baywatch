package fr.ght1pc9kc.baywatch.scraper.infra.adapters.handlers;

import fr.ght1pc9kc.baywatch.scraper.domain.model.ex.ScrapingException;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.jetbrains.annotations.NotNull;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ScraperExceptionResolverAdapter extends DataFetcherExceptionResolverAdapter {
    @Override
    protected GraphQLError resolveToSingleError(@NotNull Throwable ex, @NotNull DataFetchingEnvironment env) {
        return switch (ex) {
            case ScrapingException scrapingEx -> GraphqlErrorBuilder.newError(env)
                    .errorType(ErrorType.BAD_REQUEST)
                    .message(scrapingEx.getLocalizedMessage())
                    .extensions(Map.of("translation", scrapingEx.getTranslation().getCode()))
                    .build();
            default -> GraphqlErrorBuilder.newError(env)
                    .errorType(ErrorType.INTERNAL_ERROR)
                    .message("Unknown internal error")
                    .build();
        };
    }
}
