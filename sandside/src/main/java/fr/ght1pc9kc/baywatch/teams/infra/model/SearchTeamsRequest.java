package fr.ght1pc9kc.baywatch.teams.infra.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record SearchTeamsRequest(
        Integer _p,
        Integer _pp,
        Integer _from,
        Integer _to,
        String _s,
        List<String> id,
        String name,
        String topic
) {
    public Map<String, List<String>> toPageRequest() {
        Map<String, List<String>> params = new HashMap<>();
        if (!Objects.isNull(_p)) params.put("_p", List.of(Integer.toString(_p)));
        if (!Objects.isNull(_pp)) params.put("_pp", List.of(Integer.toString(_pp)));
        if (!Objects.isNull(_from)) params.put("_from", List.of(Integer.toString(_from)));
        if (!Objects.isNull(_to)) params.put("_to", List.of(Integer.toString(_to)));
        if (!Objects.isNull(_s)) params.put("_s", List.of(_s));
        if (!Objects.isNull(id)) params.put("id", id);
        if (!Objects.isNull(name)) params.put("name", List.of(name));
        if (!Objects.isNull(topic)) params.put("topic", List.of(topic));
        return Map.copyOf(params);
    }
}
