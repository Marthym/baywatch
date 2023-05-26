package fr.ght1pc9kc.baywatch;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Tag("integration")
@ActiveProfiles("test")
@SpringBootTest
class BaywatchApplicationTests {

    @Autowired
    ApplicationContext tested;

    @Test
    void contextLoads() {
        Assertions.assertThat(tested).isNotNull();
    }

    @AfterAll
    static void tearDown() throws IOException, JoranException {
        Path path = Paths.get("target/baywatch.mv.db");
        Files.deleteIfExists(path);

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        ContextInitializer ci = new ContextInitializer(loggerContext);
        loggerContext.reset();
        ci.autoConfig();
//        URL url = ci.findURLOfDefaultConfigurationFile(true);
//        ci.configureByResource(url);
    }
}
