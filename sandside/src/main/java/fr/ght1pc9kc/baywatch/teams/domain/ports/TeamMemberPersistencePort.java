package fr.ght1pc9kc.baywatch.teams.domain.ports;

import fr.ght1pc9kc.baywatch.techwatch.domain.model.QueryContext;
import reactor.core.publisher.Flux;

import java.util.Collection;
import java.util.Map.Entry;

public interface TeamMemberPersistencePort {
    Flux<Entry<String, String>> list(QueryContext qCtx);

    void add(String teamId, String memberId);

    void remove(String teamId, Collection<String> membersIds);
}
