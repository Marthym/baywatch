package fr.ght1pc9kc.baywatch.teams.infra.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.common.infra.model.Page;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.teams.api.TeamsService;
import fr.ght1pc9kc.baywatch.teams.api.model.Team;
import fr.ght1pc9kc.baywatch.teams.infra.model.SearchTeamsRequest;
import fr.ght1pc9kc.juery.api.PageRequest;
import fr.ght1pc9kc.juery.basic.QueryStringParser;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.Arguments;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
public class TeamsController {
    private static final QueryStringParser qsParser = QueryStringParser.withDefaultConfig();
    private final TeamsService teamsService;
    private final ObjectMapper mapper;

    @MutationMapping
    public Mono<Map<String, Object>> teamCreate(@Argument("name") String name, @Argument("topic") String topic) {
        MapType gqlType = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
        return teamsService.create(name, topic)
                .map(e -> mapper.convertValue(e, gqlType));
    }

    @QueryMapping
    public Mono<Page<Entity<Team>>> teamsSearch(@Arguments SearchTeamsRequest request) {
        PageRequest teamsPageRequest = qsParser.parse(request.toPageRequest());
        Flux<Entity<Team>> teams = teamsService.list(teamsPageRequest);

        return teamsService.count(teamsPageRequest)
                .map(count -> Page.of(teams, count));
    }

    @SchemaMapping(typeName = "SearchTeamsResponse")
    public Flux<Map<String, Object>> entities(Page<Entity<Team>> searchNewsResponse) {
        MapType gqlType = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
        return Optional.ofNullable(searchNewsResponse.getBody()).orElse(Flux.empty())
                .map(e -> mapper.convertValue(e, gqlType));
    }

    @SchemaMapping(typeName = "SearchTeamsResponse")
    public Mono<Integer> totalCount(Page<Entity<Team>> searchNewsResponse) {
        return Mono.justOrEmpty(searchNewsResponse.getHeaders().get("X-Total-Count"))
                .map(h -> h.get(0))
                .map(Integer::parseInt)
                .switchIfEmpty(Mono.just(0));
    }
}
