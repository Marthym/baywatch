package fr.ght1pc9kc.baywatch.admin.domain.services;

import fr.ght1pc9kc.baywatch.admin.api.AppConfigurationService;
import fr.ght1pc9kc.baywatch.admin.domain.ports.ConfigurationPersistencePort;
import fr.ght1pc9kc.baywatch.admin.domain.samples.AppConfigSamples;
import fr.ght1pc9kc.juery.api.PageRequest;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class AppConfigurationServiceImplTest {

    private ConfigurationPersistencePort mockPersistence;
    private AppConfigurationService tested;

    @BeforeEach
    void setUp() {
        mockPersistence = mock(ConfigurationPersistencePort.class);
        tested = new AppConfigurationServiceImpl(mockPersistence);

        doReturn(Flux.fromIterable(AppConfigSamples.STAR_WARS_PARAMS))
                .when(mockPersistence).list(any(PageRequest.class));
    }

    @Test
    void should_get_app_configuration() {
        StepVerifier.create(tested.get("jedi"))
                .assertNext(actual -> SoftAssertions.assertSoftly(softly -> {
                    Assertions.assertThat(actual)
                            .as("ParameterSet should not be null")
                            .isNotNull();
                    Assertions.assertThat(actual.getId("jedi.master")).isEqualTo("CF01K2F88DYNCAPEAKCZEWXY3Z7N");
                    Assertions.assertThat(actual.getValue("jedi.master")).isEqualTo("Yoda");
                })).verifyComplete();

        var captor = ArgumentCaptor.<PageRequest>captor();
        verify(mockPersistence, times(1)).list(captor.capture());
        Assertions.assertThat(captor.getValue().filter()).hasToString("StartWithOperation('name', jedi)");
    }

}