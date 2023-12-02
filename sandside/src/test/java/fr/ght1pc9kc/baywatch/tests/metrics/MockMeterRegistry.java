package fr.ght1pc9kc.baywatch.tests.metrics;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class MockMeterRegistry extends SimpleMeterRegistry {

}
