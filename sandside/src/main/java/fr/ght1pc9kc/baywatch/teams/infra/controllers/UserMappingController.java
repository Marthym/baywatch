package fr.ght1pc9kc.baywatch.teams.infra.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import fr.ght1pc9kc.baywatch.common.api.DefaultMeta;
import fr.ght1pc9kc.baywatch.security.api.UserService;
import fr.ght1pc9kc.baywatch.security.api.model.Permission;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.entity.api.Entity;
import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.juery.api.PageRequest;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.ID;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.ROLES;

@Controller
@RequiredArgsConstructor
public class UserMappingController {
    private final UserService userService;
    private final ObjectMapper mapper;

    @SchemaMapping(typeName = "Team", value = "_createdBy")
    public Mono<Map<String, Object>> createdBy(Map<String, Object> user) {
        MapType gqlType = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
        return userService.get(user.get("_createdBy").toString()).map(e -> mapper.convertValue(e, gqlType));
    }

    @SchemaMapping(typeName = "Team", value = "_managers")
    public Flux<Map<String, Object>> managers(Map<String, Object> team) {
        MapType gqlType = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
        return userService.list(PageRequest.all(Criteria.property(ROLES).eq(Permission.manager(team.get("_id").toString()))))
                .map(e -> mapper.convertValue(e, gqlType));
    }

    @BatchMapping(typeName = "TeamMember", field = "_user")
    public Mono<Map<Map<String, Object>, Map<String, Object>>> teamMembers(List<Map<String, Object>> members) {
        MapType gqlType = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
        List<String> userIds = members.stream().map(m -> m.get("userId").toString()).toList();
        return userService.list(PageRequest.all(Criteria.property(ID).in(userIds))).collectList()
                .map(users -> users.stream().collect(Collectors.toUnmodifiableMap(Entity::id, Function.identity())))
                .map(users -> members.stream().collect(Collectors.toUnmodifiableMap(
                        Function.identity(),
                        m -> {
                            Entity<User> teamMember = users.get(m.getOrDefault("userId", "").toString());
                            if (teamMember == null) {
                                return mapper.convertValue(Entity.identify(User.ANONYMOUS).withId(DefaultMeta.NO_ONE), gqlType);
                            }
                            teamMember = Entity.identify(filterRoles(teamMember.self(), m.get("_id").toString())).withId(teamMember.id());
                            return mapper.convertValue(teamMember, gqlType);
                        }
                )));
    }

    private User filterRoles(@NotNull User user, @NotNull String entityId) {
        return user.toBuilder()
                .clearRoles()
                .roles(user.roles().stream()
                        .filter(p -> p.entity().map(id -> id.equals(entityId)).orElse(false))
                        .map(Permission::role)
                        .toList()).build();
    }
}
