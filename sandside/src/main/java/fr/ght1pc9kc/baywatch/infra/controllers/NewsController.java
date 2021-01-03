package fr.ght1pc9kc.baywatch.infra.controllers;

import fr.ght1pc9kc.baywatch.api.NewsService;
import fr.ght1pc9kc.baywatch.api.model.News;
import fr.ght1pc9kc.baywatch.infra.request.PageRequestFormatter;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController("/api")
@AllArgsConstructor
public class NewsController {

    private final NewsService newsService;

    @GetMapping("/news")
    public Flux<News> listNews(@RequestParam Map<String, String> queryStringParams) {
        return newsService.list(PageRequestFormatter.parse(queryStringParams));
    }
}
