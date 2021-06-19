package fr.ght1pc9kc.baywatch.infra.controllers;

import fr.ght1pc9kc.baywatch.api.StatService;
import fr.ght1pc9kc.baywatch.infra.model.Statistics;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("${baywatch.base-route}/stats")
@RequiredArgsConstructor
public class StatController {
    private final StatService statService;

    @GetMapping()
    public Mono<Statistics> stats() {
        return Mono.zip(
                statService.getFeedsCount(),
                statService.getNewsCount(),
                statService.getUnreadCount()
        ).map(t -> Statistics.builder()
                .feeds(t.getT1())
                .news(t.getT2())
                .unread(t.getT3())
                .build());
    }
}
