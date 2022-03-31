package fr.ght1pc9kc.baywatch.techwatch.infra.controllers;

import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.techwatch.api.NewsService;
import fr.ght1pc9kc.baywatch.techwatch.api.model.Flags;
import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import fr.ght1pc9kc.baywatch.common.domain.exceptions.BadRequestCriteria;
import fr.ght1pc9kc.baywatch.common.infra.model.Page;
import fr.ght1pc9kc.baywatch.techwatch.api.model.State;
import fr.ght1pc9kc.juery.api.PageRequest;
import fr.ght1pc9kc.juery.basic.QueryStringParser;
import lombok.AllArgsConstructor;
import org.intellij.lang.annotations.MagicConstant;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
    public Mono<Page<News>> listNews(@RequestParam MultiValueMap<String, String> queryStringParams) {
        PageRequest pageRequest = qsParser.parse(queryStringParams);
        Flux<News> news = newsService.list(pageRequest)
                .onErrorMap(BadRequestCriteria.class, e -> new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getLocalizedMessage()));

        return newsService.count(pageRequest)
                .map(count -> Page.of(news, count));
    }

    @PutMapping("/{newsId}/mark/{flag}")
    public Mono<Entity<State>> markAs(@PathVariable("newsId") String newsId, @PathVariable("flag") String flag) {
        int iFlag = stringToFlag(flag);
        if (iFlag <= 0) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, String.format("Unknown flag %s !", flag)));
        } else {
            return newsService.mark(newsId, iFlag);
        }
    }

    @PutMapping("/{newsId}/unmark/{flag}")
    public Mono<Entity<State>> unmarkAs(@PathVariable("newsId") String newsId, @PathVariable("flag") String flag) {
        int iFlag = stringToFlag(flag);
        if (iFlag < 0) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, String.format("Unknown flag %s !", flag)));
        } else {
            return newsService.unmark(newsId, iFlag);
        }
    }
}
