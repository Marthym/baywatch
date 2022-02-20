package fr.ght1pc9kc.baywatch.scrapper.infra.adapters;

import fr.ght1pc9kc.baywatch.techwatch.api.StatService;
import fr.ght1pc9kc.baywatch.notify.api.NotifyService;
import fr.ght1pc9kc.baywatch.scrapper.api.ScrappingHandler;
import fr.ght1pc9kc.baywatch.scrapper.domain.actions.NewsUpdateNotificationHandler;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NewsUpdateNotificationHandlerAdapter implements ScrappingHandler {
    @Delegate
    private final ScrappingHandler delegate;

    @Autowired
    public NewsUpdateNotificationHandlerAdapter(StatService statService, NotifyService notifyService) {
        this.delegate = new NewsUpdateNotificationHandler(notifyService, statService);
    }
}
