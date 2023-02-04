package fr.ght1pc9kc.baywatch.security.infra.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.DESCRIPTION;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.ID;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.PUBLICATION;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.TITLE;

public record UserSearchRequest(
        Integer _p,
        Integer _pp,
        Integer _from,
        Integer _to,
        String _s,
        String _id,
        String login,
        String name,
        String mail
) {
    public Map<String, List<String>> toPageRequest() {
        Map<String, List<String>> params = new HashMap<>();
        if (!Objects.isNull(_p)) params.put("_p", List.of(Integer.toString(_p)));
        if (!Objects.isNull(_pp)) params.put("_pp", List.of(Integer.toString(_pp)));
        if (!Objects.isNull(_from)) params.put("_from", List.of(Integer.toString(_from)));
        if (!Objects.isNull(_to)) params.put("_to", List.of(Integer.toString(_to)));
        if (!Objects.isNull(_s)) params.put("_s", List.of(_s));
        if (!Objects.isNull(_id)) params.put(ID, List.of(_id));
        if (!Objects.isNull(login)) params.put(TITLE, List.of(login));
        if (!Objects.isNull(name)) params.put(DESCRIPTION, List.of(name));
        if (!Objects.isNull(mail)) params.put(PUBLICATION, List.of(mail));
        return Map.copyOf(params);
    }
}
