package fr.ght1pc9kc.baywatch.techwatch.infra.model.graphql;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.DESCRIPTION;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.FEED_ID;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.ID;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.KEEP;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.POPULAR;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.PUBLICATION;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.READ;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.SHARED;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.TAGS;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.TITLE;

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
        List<String> tags,
        List<String> feeds,
        Boolean read,
        Boolean shared,
        Boolean popular,
        Boolean keep
) {
    public Map<String, List<String>> toPageRequest() {
        Map<String, List<String>> params = new HashMap<>();
        if (!Objects.isNull(_p)) params.put("_p", List.of(Integer.toString(_p)));
        if (!Objects.isNull(_pp)) params.put("_pp", List.of(Integer.toString(_pp)));
        if (!Objects.isNull(_from)) params.put("_from", List.of(Integer.toString(_from)));
        if (!Objects.isNull(_to)) params.put("_to", List.of(Integer.toString(_to)));
        if (!Objects.isNull(_s)) params.put("_s", List.of(_s));
        if (!Objects.isNull(id)) params.put(ID, List.of(id));
        if (!Objects.isNull(title)) params.put(TITLE, List.of(title));
        if (!Objects.isNull(description)) params.put(DESCRIPTION, List.of(description));
        if (!Objects.isNull(publication)) params.put(PUBLICATION, List.of(publication));
        if (!Objects.isNull(tags)) params.put(TAGS, tags);
        if (!Objects.isNull(feeds)) params.put(FEED_ID, feeds);
        if (!Objects.isNull(read)) params.put(READ, List.of(Boolean.toString(read)));
        if (!Objects.isNull(shared)) params.put(SHARED, List.of(Boolean.toString(shared)));
        if (!Objects.isNull(popular)) params.put(POPULAR, List.of(Boolean.toString(popular)));
        if (!Objects.isNull(keep)) params.put(KEEP, List.of(Boolean.toString(keep)));
        return Map.copyOf(params);
    }
}
