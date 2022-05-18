package fr.ght1pc9kc.baywatch.admin.domain;

import fr.ght1pc9kc.baywatch.admin.api.model.Counter;
import fr.ght1pc9kc.baywatch.admin.api.model.CounterGroup;
import fr.ght1pc9kc.baywatch.admin.api.model.CounterProvider;
import fr.ght1pc9kc.baywatch.admin.api.model.CounterType;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.tests.samples.UserSamples;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class StatisticsServiceImplTest {

    private StatisticsServiceImpl tested;
    private AuthenticationFacade mockFacade;
    private List<CounterProvider> providers;

    @BeforeEach
    void setUp() {
        mockFacade = mock(AuthenticationFacade.class);
        providers = new ArrayList<>();
        tested = new StatisticsServiceImpl(mockFacade, providers);
    }

    @Test
    void should_compute_counter() {
        when(mockFacade.getConnectedUser()).thenReturn(Mono.just(UserSamples.YODA));
        CounterProvider provider = new CounterProvider() {
            @Override
            public CounterGroup group() {
                return CounterGroup.TECHWATCH;
            }

            @Override
            public Mono<Counter> computeCounter() {
                return Mono.just(Counter.create("42", "42", "42"));
            }
        };
        CounterProvider spyProvider = spy(provider);
        providers.add(spyProvider);

        StepVerifier.create(tested.compute(CounterGroup.TECHWATCH))
                .expectNext(Counter.create("42", "42", "42"))
                .verifyComplete();
    }
}