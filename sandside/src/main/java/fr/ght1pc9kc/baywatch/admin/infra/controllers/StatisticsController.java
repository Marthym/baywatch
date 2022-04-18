package fr.ght1pc9kc.baywatch.admin.infra.controllers;

import fr.ght1pc9kc.baywatch.admin.api.StatisticsService;
import fr.ght1pc9kc.baywatch.admin.infra.model.Statistics;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("${baywatch.base-route}/stats")
public class StatisticsController {
    private final StatisticsService statService;

    @GetMapping()
    public Mono<Statistics> stats() {
        return Mono.zip(
                statService.getFeedsCount(),
                statService.getNewsCount(),
                statService.getUsersCount()
        ).map(t -> Statistics.builder()
                .feeds(t.getT1())
                .news(t.getT2())
                .unread(t.getT3())
                .build());
    }
}
