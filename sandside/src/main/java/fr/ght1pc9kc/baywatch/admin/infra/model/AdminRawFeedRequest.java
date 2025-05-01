package fr.ght1pc9kc.baywatch.admin.infra.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The request with all allowed properties to look up mobile applications
 *
 * @see <a href="https://marthym.github.io/juery/juery.basic/fr/ght1pc9kc/juery/basic/ParserConfiguration.html#DEFAULT">Juery Documentation</a>
 */
@SuppressWarnings("DuplicatedCode")
public record AdminRawFeedRequest(
        Integer _p,
        Integer _pp,
        Integer _from,
        Integer _to,
        String _s,
        List<String> _id,
        String name,
        String url
) {
    public Map<String, List<String>> toPageRequest() {
        Map<String, List<String>> params = new HashMap<>();
        if (!Objects.isNull(_p)) {
            params.put("_p", List.of(Integer.toString(_p)));
        }
        if (!Objects.isNull(_pp)) {
            params.put("_pp", List.of(Integer.toString(_pp)));
        }
        if (!Objects.isNull(_from)) {
            params.put("_from", List.of(Integer.toString(_from)));
        }
        if (!Objects.isNull(_to)) {
            params.put("_to", List.of(Integer.toString(_to)));
        }
        if (!Objects.isNull(_s)) {
            params.put("_s", List.of(_s));
        }
        if (!Objects.isNull(_id)) {
            params.put("_id", _id);
        }
        if (!Objects.isNull(name)) {
            params.put("name", List.of(name));
        }
        if (!Objects.isNull(url)) {
            params.put("url", List.of(url));
        }
        return Map.copyOf(params);
    }
}
