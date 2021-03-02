package fr.ght1pc9kc.baywatch.infra.controllers;

import fr.ght1pc9kc.baywatch.api.NewsService;
import fr.ght1pc9kc.baywatch.api.model.Flags;
import fr.ght1pc9kc.baywatch.api.model.News;
import fr.ght1pc9kc.baywatch.infra.request.PageRequestFormatter;
import lombok.AllArgsConstructor;
import org.intellij.lang.annotations.MagicConstant;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpServerErrorException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/news")
@AllArgsConstructor
public class NewsController {

    private final NewsService newsService;

    @MagicConstant(flagsFromClass = Flags.class)
    private static int stringToFlag(String flag) {
        switch (flag.toUpperCase()) {
            case "READ":
                return Flags.READ;
            case "STAR":
            case "STARED":
                return Flags.STAR;
            default:
                return Flags.NONE;
        }
    }

    @GetMapping
    public Flux<News> listNews(@RequestParam Map<String, String> queryStringParams) {
        return newsService.list(PageRequestFormatter.parse(queryStringParams));
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{newsId}/mark/{flag}")
    public Mono<News> markAs(@PathVariable("newsId") String newsId, @PathVariable("flag") String flag) {
        int iFlag = stringToFlag(flag);
        if (iFlag <= 0) {
            return Mono.error(new HttpServerErrorException(
                    HttpStatus.BAD_REQUEST, String.format("Unknown flag %s !", flag)));
        } else {
            return newsService.mark(newsId, iFlag);
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{newsId}/unmark/{flag}")
    public Mono<News> unmarkAs(@PathVariable("newsId") String newsId, @PathVariable("flag") String flag) {
        int iFlag = stringToFlag(flag);
        if (iFlag < 0) {
            return Mono.error(new HttpServerErrorException(
                    HttpStatus.BAD_REQUEST, String.format("Unknown flag %s !", flag)));
        } else {
            return newsService.unmark(newsId, iFlag);
        }
    }
}
