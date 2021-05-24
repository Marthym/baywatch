package fr.ght1pc9kc.baywatch.domain;

import fr.ght1pc9kc.baywatch.api.StatService;
import fr.ght1pc9kc.baywatch.domain.techwatch.ports.FeedPersistencePort;
import fr.ght1pc9kc.baywatch.domain.techwatch.ports.NewsPersistencePort;
import fr.ght1pc9kc.baywatch.domain.ports.UserPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class StatServiceImpl implements StatService {
    private final FeedPersistencePort feedRepository;
    private final NewsPersistencePort newsRepository;
    private final UserPersistencePort userRepository;

    @Override
    public Mono<Integer> getUsersCount() {
        return userRepository.list().count()
                .map(Long::intValue);
    }

    @Override
    public Mono<Integer> getNewsCount() {
        return newsRepository.count();
    }

    @Override
    public Mono<Integer> getFeedsCount() {
        return feedRepository.list().count().map(Long::intValue);
    }
}
