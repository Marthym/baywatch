package fr.ght1pc9kc.baywatch;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Tag("integration")
@ActiveProfiles("test")
@SpringBootTest
class BaywatchApplicationTests {

    @Test
    void contextLoads() {
    }

    @AfterAll
    static void tearDown() throws IOException {
        Path path = Paths.get("target/baywatch.mv.db");
        Files.deleteIfExists(path);
    }
}
