package fr.ght1pc9kc.baywatch.common.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.*;

@Configuration
public class SpaRouterConfig {

    private static final String WEBJAR_ROOT = "META-INF/resources/";

    @Bean
    public RouterFunction<ServerResponse> spaRoutes() {
        return RouterFunctions
                // Serves assets from the webjar
                .resources("/**", new ClassPathResource(WEBJAR_ROOT))

                // Fallback to index.html for all other routes (except /api)
                .andRoute(
                        RequestPredicates.GET("/{path:^(?!api).*$}"),
                        req -> ServerResponse.ok()
                                .contentType(MediaType.TEXT_HTML)
                                .bodyValue(new ClassPathResource(WEBJAR_ROOT + "index.html"))
                )
                .andRoute(
                        RequestPredicates.GET("/{path:^(?!api).*$}/**"),
                        req -> ServerResponse.ok()
                                .contentType(MediaType.TEXT_HTML)
                                .bodyValue(new ClassPathResource(WEBJAR_ROOT + "index.html"))
                );
    }
}
