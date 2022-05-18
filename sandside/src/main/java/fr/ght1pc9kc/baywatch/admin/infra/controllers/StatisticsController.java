package fr.ght1pc9kc.baywatch.admin.infra.controllers;

import fr.ght1pc9kc.baywatch.admin.api.StatisticsService;
import fr.ght1pc9kc.baywatch.admin.api.model.Counter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("${baywatch.base-route}/stats")
public class StatisticsController {
    private final StatisticsService statService;

    @GetMapping()
    public Flux<Counter> stats() {
        return statService.compute();
    }
}
