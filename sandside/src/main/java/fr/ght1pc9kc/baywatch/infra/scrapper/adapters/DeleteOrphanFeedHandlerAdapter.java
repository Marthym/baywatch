package fr.ght1pc9kc.baywatch.infra.scrapper.adapters;

import fr.ght1pc9kc.baywatch.api.techwatch.NewsService;
import fr.ght1pc9kc.baywatch.api.admin.FeedAdminService;
import fr.ght1pc9kc.baywatch.api.scrapper.ScrappingHandler;
import fr.ght1pc9kc.baywatch.domain.scrapper.actions.DeleteOrphanFeedHandler;
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
