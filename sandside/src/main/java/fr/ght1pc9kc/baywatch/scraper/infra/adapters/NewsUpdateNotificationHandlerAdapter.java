package fr.ght1pc9kc.baywatch.scraper.infra.adapters;

import fr.ght1pc9kc.baywatch.notify.api.NotifyService;
import fr.ght1pc9kc.baywatch.scraper.api.ScrapingHandler;
import fr.ght1pc9kc.baywatch.scraper.domain.actions.NewsUpdateNotificationHandler;
import fr.ght1pc9kc.baywatch.techwatch.api.NewsService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NewsUpdateNotificationHandlerAdapter implements ScrapingHandler {
    @Delegate
    private final ScrapingHandler delegate;

    @Autowired
    public NewsUpdateNotificationHandlerAdapter(NewsService newsService, NotifyService notifyService) {
        this.delegate = new NewsUpdateNotificationHandler(notifyService, newsService);
    }
}
