package fr.ght1pc9kc.baywatch.infra.controllers;

import fr.ght1pc9kc.baywatch.api.NewsService;
import fr.ght1pc9kc.baywatch.api.model.Flags;
import fr.ght1pc9kc.baywatch.api.model.News;
import fr.ght1pc9kc.baywatch.domain.exceptions.BadRequestCriteria;
import fr.ght1pc9kc.juery.basic.QueryStringParser;
import lombok.AllArgsConstructor;
import org.intellij.lang.annotations.MagicConstant;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@AllArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
@RequestMapping("${baywatch.base-route}/news")
public class NewsController {

    private static final QueryStringParser qsParser = QueryStringParser.withDefaultConfig();
    private final NewsService newsService;

    @MagicConstant(flagsFromClass = Flags.class)
    private static int stringToFlag(String flag) {
        switch (flag.toUpperCase()) {
            case "READ":
                return Flags.READ;
            case "SHARED":
                return Flags.SHARED;
            default:
                return Flags.NONE;
        }
    }

    @GetMapping
    @PreAuthorize("permitAll()")
    public Flux<News> listNews(@RequestParam MultiValueMap<String, String> queryStringParams) {
        return newsService.list(qsParser.parse(queryStringParams))
                .onErrorMap(BadRequestCriteria.class, e -> new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getLocalizedMessage()));
    }

    @PutMapping("/{newsId}/mark/{flag}")
    public Mono<News> markAs(@PathVariable("newsId") String newsId, @PathVariable("flag") String flag) {
        int iFlag = stringToFlag(flag);
        if (iFlag <= 0) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, String.format("Unknown flag %s !", flag)));
        } else {
            return newsService.mark(newsId, iFlag);
        }
    }

    @PutMapping("/{newsId}/unmark/{flag}")
    public Mono<News> unmarkAs(@PathVariable("newsId") String newsId, @PathVariable("flag") String flag) {
        int iFlag = stringToFlag(flag);
        if (iFlag < 0) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, String.format("Unknown flag %s !", flag)));
        } else {
            return newsService.unmark(newsId, iFlag);
        }
    }
}
