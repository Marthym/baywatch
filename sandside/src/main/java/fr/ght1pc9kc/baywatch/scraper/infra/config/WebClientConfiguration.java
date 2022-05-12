package fr.ght1pc9kc.baywatch.scraper.infra.config;

import io.netty.channel.ChannelOption;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.Http11SslContextSpec;
import reactor.netty.http.client.HttpClient;

import java.util.Set;

@Configuration
public class WebClientConfiguration {

    @Bean
    @ScraperQualifier
    public WebClient getScraperWebClient(ScraperProperties properties) {
        return WebClient.builder().clientConnector(new ReactorClientHttpConnector(
                HttpClient.create()
                        .resolver(spec -> spec.queryTimeout(properties.dns().timeout()))
                        .followRedirect(true)
                        .followRedirect((req, res) -> // 303 was not in the default code
                                Set.of(301, 302, 303, 307, 308).contains(res.status().code()))
                        .compress(true)
                        .secure(spec -> spec.sslContext(Http11SslContextSpec.forClient()))
                        .responseTimeout(properties.timeout())
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) properties.timeout().toMillis())
        )).build();
    }
}
