package fr.ght1pc9kc.baywatch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import reactor.core.publisher.Hooks;

import java.util.Locale;

@Slf4j
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@ConfigurationPropertiesScan
public class BaywatchApplication {

    public static void main(String[] args) {
        Hooks.enableAutomaticContextPropagation();
        Locale.setDefault(Locale.US);
        SpringApplication.run(BaywatchApplication.class, args);
    }

}
