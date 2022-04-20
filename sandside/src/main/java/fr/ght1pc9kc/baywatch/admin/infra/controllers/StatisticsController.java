package fr.ght1pc9kc.baywatch.admin.infra.controllers;

import fr.ght1pc9kc.baywatch.admin.api.StatisticsService;
import fr.ght1pc9kc.baywatch.admin.api.model.Counter;
import fr.ght1pc9kc.baywatch.admin.api.model.CounterType;
import fr.ght1pc9kc.baywatch.admin.infra.model.Statistics;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("${baywatch.base-route}/stats")
public class StatisticsController {
    private final StatisticsService statService;

    @GetMapping()
    public Mono<Statistics> stats() {
        return Mono.zip(
                statService.compute(CounterType.FEEDS_COUNT).defaultIfEmpty(Counter.NONE),
                statService.compute(CounterType.NEWS_COUNT).defaultIfEmpty(Counter.NONE),
                statService.compute(CounterType.USERS_COUNT).defaultIfEmpty(Counter.NONE),
                statService.compute(CounterType.SCRAPING_DURATION).defaultIfEmpty(Counter.NONE)
        ).map(t -> Statistics.builder()
                .feeds(t.getT1())
                .news(t.getT2())
                .users(t.getT3())
                .scrap(t.getT4())
                .build());
    }
}
