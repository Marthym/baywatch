package fr.ght1pc9kc.baywatch.teams.infra.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import fr.ght1pc9kc.baywatch.teams.api.TeamsService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
public class TeamMembersController {

    private final TeamsService teamsService;
    private final ObjectMapper mapper;

    @QueryMapping
    public Flux<Map<String, Object>> teamMembersList(@Argument("teamId") String teamId) {
        MapType gqlType = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
        return teamsService.members(teamId)
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
}
