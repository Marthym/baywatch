package fr.ght1pc9kc.baywatch.tests.metrics;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.stereotype.Component;

@Component
public class MockObservationRegistry implements ObservationRegistry {
    private final ObservationRegistry delegate = ObservationRegistry.create();

    @Override
    public Observation getCurrentObservation() {
        return delegate.getCurrentObservation();
    }

    @Override
    public Observation.Scope getCurrentObservationScope() {
        return delegate.getCurrentObservationScope();
    }

    @Override
    public void setCurrentObservationScope(Observation.Scope scope) {

    }

    @Override
    public ObservationConfig observationConfig() {
        return delegate.observationConfig();
    }
}
