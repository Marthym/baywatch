package fr.ght1pc9kc.baywatch.teams.infra.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.common.infra.model.Page;
import fr.ght1pc9kc.baywatch.teams.api.TeamsService;
import fr.ght1pc9kc.baywatch.teams.api.model.Team;
import fr.ght1pc9kc.baywatch.teams.infra.model.SearchTeamsRequest;
import fr.ght1pc9kc.baywatch.teams.infra.model.TeamForm;
import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.juery.api.PageRequest;
import fr.ght1pc9kc.juery.basic.QueryStringParser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.ID;

@Controller
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
public class TeamsController {
    private static final QueryStringParser qsParser = QueryStringParser.withDefaultConfig();
    private final TeamsService teamsService;
    private final ObjectMapper mapper;

    @MutationMapping
    public Mono<Map<String, Object>> teamCreate(@Argument("name") @NotNull String name, @Argument("topic") String topic) {
        MapType gqlType = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
        return teamsService.create(name, topic)
                .map(e -> mapper.convertValue(e, gqlType));
    }

    @MutationMapping
    @PreAuthorize("hasAuthority('MANAGER:'+#id) or hasRole('ADMIN')")
    public Mono<Map<String, Object>> teamUpdate(@Argument("id") String id, @Valid @Argument("team") TeamForm team) {
        MapType gqlType = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
        return teamsService.update(id, team.name(), team.topic())
                .map(e -> mapper.convertValue(e, gqlType));
    }

    @QueryMapping
    public Mono<Page<Entity<Team>>> teamsSearch(@Arguments SearchTeamsRequest request) {
        PageRequest teamsPageRequest = qsParser.parse(request.toPageRequest());
        Flux<Entity<Team>> teams = teamsService.list(teamsPageRequest);

        return teamsService.count(teamsPageRequest)
                .map(count -> Page.of(teams, count));
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public Flux<Map<String, Object>> teamDelete(@Argument("id") List<String> teamIds) {
        MapType gqlType = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
        return teamsService.list(PageRequest.all(Criteria.property(ID).in(teamIds)))
                .collectList()
                .flatMapMany(teams -> teamsService.delete(teams.stream().map(e -> e.id).toList())
                        .thenMany(Flux.fromIterable(teams)))
                .map(e -> mapper.convertValue(e, gqlType));
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
