package fr.ght1pc9kc.baywatch;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Tag("integration")
@ActiveProfiles("test")
@SpringBootTest
class BaywatchApplicationTests {

    @Test
    void contextLoads() {
    }

}
