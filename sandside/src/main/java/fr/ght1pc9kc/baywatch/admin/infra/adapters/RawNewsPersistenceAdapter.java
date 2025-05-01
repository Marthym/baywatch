package fr.ght1pc9kc.baywatch.admin.infra.adapters;

import fr.ght1pc9kc.baywatch.admin.domain.ports.RawNewsPersistencePort;
import fr.ght1pc9kc.baywatch.common.domain.QueryContext;
import fr.ght1pc9kc.baywatch.techwatch.infra.adapters.persistence.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class RawNewsPersistenceAdapter implements RawNewsPersistencePort {
    private final NewsRepository newsRepository;

    @Override
    public Mono<Void> delete(QueryContext qCtx) {
        return newsRepository.delete(qCtx).then();
    }
}
