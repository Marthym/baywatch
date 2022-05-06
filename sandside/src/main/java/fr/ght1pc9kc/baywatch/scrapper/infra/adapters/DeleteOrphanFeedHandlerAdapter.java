package fr.ght1pc9kc.baywatch.scrapper.infra.adapters;

import fr.ght1pc9kc.baywatch.techwatch.api.NewsService;
import fr.ght1pc9kc.baywatch.admin.api.FeedAdminService;
import fr.ght1pc9kc.baywatch.scrapper.api.ScrappingHandler;
import fr.ght1pc9kc.baywatch.scrapper.domain.actions.DeleteOrphanFeedHandler;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Component;

@Component
public class DeleteOrphanFeedHandlerAdapter implements ScrappingHandler {
    @Delegate
    private final ScrappingHandler delegate;

    public DeleteOrphanFeedHandlerAdapter(FeedAdminService feedService, NewsService newsService) {
        this.delegate = new DeleteOrphanFeedHandler(feedService, newsService);
    }
}
