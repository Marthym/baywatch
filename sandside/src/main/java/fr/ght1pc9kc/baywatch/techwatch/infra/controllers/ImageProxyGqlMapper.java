package fr.ght1pc9kc.baywatch.techwatch.infra.controllers;

import fr.ght1pc9kc.baywatch.techwatch.api.ImageProxyService;
import fr.ght1pc9kc.baywatch.techwatch.api.model.ImagePresets;
import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
//@ConditionalOnBean(ImageProxyService.class)
@ConditionalOnProperty(prefix = "baywatch.imgproxy", name = "enable", havingValue = "true")
public class ImageProxyGqlMapper {
    private final ImageProxyService imageProxyService;

    @BatchMapping
    public Mono<Map<News, Optional<URI>>> imgm(List<News> news) {
        return Mono.fromCallable(() -> news.stream()
                .collect(Collectors.toUnmodifiableMap(Function.identity(),
                        n -> Optional.ofNullable(imageProxyService.proxify(n.getImage(), ImagePresets.MOBILE)))));
    }

    @BatchMapping
    public Mono<Map<News, Optional<URI>>> imgd(List<News> news) {
        return Mono.fromCallable(() -> news.stream()
                .collect(Collectors.toUnmodifiableMap(Function.identity(),
                        n -> Optional.ofNullable(imageProxyService.proxify(n.getImage(), ImagePresets.DESKTOP)))));
    }
}
