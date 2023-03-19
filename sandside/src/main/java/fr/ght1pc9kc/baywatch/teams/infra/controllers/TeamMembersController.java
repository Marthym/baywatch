package fr.ght1pc9kc.baywatch.teams.infra.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import fr.ght1pc9kc.baywatch.teams.api.TeamsService;
import fr.ght1pc9kc.baywatch.teams.domain.ports.TeamAuthFacade;
import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.juery.api.PageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.ID;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.USER_ID;

@Controller
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
public class TeamMembersController {
    private final TeamAuthFacade authFacade;
    private final TeamsService teamsService;
    private final ObjectMapper mapper;

    @QueryMapping
    public Flux<Map<String, Object>> teamMembersList(@Argument("teamId") String teamId) {
        MapType gqlType = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
        return teamsService.members(PageRequest.all(Criteria.property(ID).eq(teamId)))
                .map(e -> mapper.convertValue(e, gqlType));
    }

    @MutationMapping
    public Flux<Map<String, Object>> teamMembersAdd(@Argument("_id") String teamId, @Argument("membersIds") List<String> membersIds) {
        MapType gqlType = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
        return teamsService.addMembers(teamId, membersIds)
                .map(e -> mapper.convertValue(e, gqlType));
    }

    @MutationMapping
    public Flux<Map<String, Object>> teamMembersDelete(@Argument("_id") String teamId, @Argument("membersIds") List<String> membersIds) {
        MapType gqlType = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
        return teamsService.removeMembers(teamId, membersIds)
                .map(e -> mapper.convertValue(e, gqlType));
    }

    @BatchMapping(typeName = "Team", value = "_me")
    public Mono<Map<Map<String, Object>, Map<String, Object>>> currentUserAsMember(List<Map<String, Object>> teams) {
        MapType gqlType = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);

        return authFacade.getConnectedUser()
                .flatMapMany(userEntity -> teamsService.members(PageRequest.all(Criteria.property(USER_ID).eq(userEntity.id))))
                .collectMap(e -> e.id, Function.identity())
                .map(currentUsers -> teams.stream()
                        .map(t -> {
                            String teamId = Optional.ofNullable(t.get("_id")).map(Object::toString).orElse("");
                            Map<String, Object> asMap = mapper.convertValue(currentUsers.get(teamId), gqlType);
                            return Map.entry(t, asMap);
                        }).collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue)));
    }
}
