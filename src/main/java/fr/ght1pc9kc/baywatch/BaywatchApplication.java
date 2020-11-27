package fr.ght1pc9kc.baywatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

@SpringBootApplication
public class BaywatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(BaywatchApplication.class, args);
    }

//    @ConfigurationProperties("spring.datasource")
//    public DataSourceProperties defaultDataSourceProperties() {
//        return new DataSourceProperties();
//    }
//
//    @Bean
//    public DataSource defaultDataSource(DataSourceProperties properties) {
//        return properties.initializeDataSourceBuilder().build();
//    }

}
