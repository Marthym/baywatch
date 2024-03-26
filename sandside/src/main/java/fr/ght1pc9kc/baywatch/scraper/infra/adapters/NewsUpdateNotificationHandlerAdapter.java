package fr.ght1pc9kc.baywatch.scraper.infra.adapters;

import fr.ght1pc9kc.baywatch.notify.api.NotifyService;
import fr.ght1pc9kc.baywatch.scraper.api.ScrapingEventHandler;
import fr.ght1pc9kc.baywatch.scraper.domain.actions.NewsUpdateNotificationHandler;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NewsUpdateNotificationHandlerAdapter implements ScrapingEventHandler {
    @Delegate
    private final ScrapingEventHandler delegate;

    @Autowired
    public NewsUpdateNotificationHandlerAdapter(NotifyService notifyService) {
        this.delegate = new NewsUpdateNotificationHandler(notifyService);
    }
}
