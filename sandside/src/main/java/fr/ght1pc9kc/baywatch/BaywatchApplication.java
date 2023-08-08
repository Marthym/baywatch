package fr.ght1pc9kc.baywatch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import java.util.Locale;

@Slf4j
@SpringBootApplication
@ConfigurationPropertiesScan
public class BaywatchApplication {

    public static final String APPLICATION_NAME = "Baywatch";

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        SpringApplication.run(BaywatchApplication.class, args);
    }

}
