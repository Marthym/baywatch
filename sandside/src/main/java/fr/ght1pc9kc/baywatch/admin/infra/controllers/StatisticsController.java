package fr.ght1pc9kc.baywatch.admin.infra.controllers;

import fr.ght1pc9kc.baywatch.admin.api.StatisticsService;
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
                statService.getFeedsCount().defaultIfEmpty(0),
                statService.getNewsCount().defaultIfEmpty(0),
                statService.getUsersCount().defaultIfEmpty(0)
        ).map(t -> Statistics.builder()
                .feeds(t.getT1())
                .news(t.getT2())
                .users(t.getT3())
                .build());
    }
}
