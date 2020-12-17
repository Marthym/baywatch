package fr.ght1pc9kc.baywatch.domain.scrapper.actions;

import fr.ght1pc9kc.baywatch.api.NewsPersistencePort;
import fr.ght1pc9kc.baywatch.api.model.RawNews;
import fr.ght1pc9kc.baywatch.api.model.search.Criteria;
import fr.ght1pc9kc.baywatch.api.scrapper.PreScrappingAction;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.Period;

//@Component
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class PurgeNewsAction implements PreScrappingAction {

    private final NewsPersistencePort newsPersistence;
    private final Clock clock;

    @Override
    public Mono<Void> call() {
        LocalDateTime maxPublicationPasDate = LocalDateTime.now(clock).minus(Period.ofMonths(3));
        Criteria criteria = Criteria.property("publication").lt(maxPublicationPasDate);
        return null;
//        return newsPersistence.list(criteria)
//                .map(RawNews::getId)
//                .collectList()
//                .flatMapMany(newsIds -> newsPersistence.listState(Criteria.property("newsId").in(newsIds)))
//                .buffer(500)
//                .flatMap(newsPersistence::delete)
//                .then();
    }
}
