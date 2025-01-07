package fr.ght1pc9kc.baywatch.scraper.infra.adapters.handlers;

import fr.ght1pc9kc.baywatch.scraper.domain.model.ex.ScrapingException;
import fr.ght1pc9kc.baywatch.scraper.domain.model.ex.ScrapingExceptionCode;
import graphql.GraphQLError;
import graphql.execution.ExecutionStepInfo;
import graphql.execution.ResultPath;
import graphql.language.Field;
import graphql.schema.DataFetchingEnvironment;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.graphql.execution.ErrorType;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class ScraperExceptionResolverAdapterTest {

    private final ScraperExceptionResolverAdapter tested = new ScraperExceptionResolverAdapter();
    private final DataFetchingEnvironment dataFetchingEnvironment = mock(DataFetchingEnvironment.class);

    @BeforeEach
    void setUp() {
        doReturn(new Field("weapon")).when(dataFetchingEnvironment).getField();
        ExecutionStepInfo executionStepInfo = mock(ExecutionStepInfo.class);
        doReturn(executionStepInfo).when(dataFetchingEnvironment).getExecutionStepInfo();
        doReturn(ResultPath.rootPath()).when(executionStepInfo).getPath();
    }

    @Test
    void should_resolve_scraping_exception() {
        GraphQLError actual = tested.resolveToSingleError(
                new ScrapingException(ScrapingExceptionCode.DEFAULT, new RuntimeException()),
                dataFetchingEnvironment);
        Assertions.assertThat(actual).isNotNull();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(actual.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            softly.assertThat(actual.getMessage()).isEqualTo(ScrapingExceptionCode.DEFAULT.getDefaultMessage());
            softly.assertThat(actual.getExtensions().get("translation")).isEqualTo(ScrapingExceptionCode.DEFAULT.getCode());
        });
    }

    @Test
    void should_resolve_default() {
        GraphQLError actual = tested.resolveToSingleError(
                new RuntimeException(),
                dataFetchingEnvironment);
        Assertions.assertThat(actual)
                .describedAs("Should be null to allow other Exceptions resolver")
                .isNull();
    }
}