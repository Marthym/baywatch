package fr.ght1pc9kc.baywatch.techwatch.infra.model.graphql;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record SearchNewsRequest(
        Integer _p,
        Integer _pp,
        Integer _from,
        Integer _to,
        String _s,
        String id,
        String title,
        String description,
        String publication,
        String tags,
        Boolean read,
        Boolean shared
) {
    public Map<String, List<String>> toPageRequest() {
        Map<String, List<String>> params = new HashMap<>();
        if (!Objects.isNull(_p)) params.put("_p", List.of(Integer.toString(_p)));
        if (!Objects.isNull(_pp)) params.put("_pp", List.of(Integer.toString(_pp)));
        if (!Objects.isNull(_from)) params.put("_from", List.of(Integer.toString(_from)));
        if (!Objects.isNull(_to)) params.put("_to", List.of(Integer.toString(_to)));
        if (!Objects.isNull(_s)) params.put("_s", List.of(_s));
        if (!Objects.isNull(id)) params.put("id", List.of(id));
        if (!Objects.isNull(title)) params.put("title", List.of(title));
        if (!Objects.isNull(description)) params.put("description", List.of(description));
        if (!Objects.isNull(publication)) params.put("publication", List.of(publication));
        if (!Objects.isNull(tags)) params.put("tags", List.of(tags));
        if (!Objects.isNull(read)) params.put("read", List.of(Boolean.toString(read)));
        if (!Objects.isNull(shared)) params.put("shared", List.of(Boolean.toString(shared)));
        return Map.copyOf(params);
    }
}
