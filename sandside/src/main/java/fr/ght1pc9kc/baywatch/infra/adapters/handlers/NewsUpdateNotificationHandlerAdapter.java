package fr.ght1pc9kc.baywatch.infra.adapters.handlers;

import fr.ght1pc9kc.baywatch.api.StatService;
import fr.ght1pc9kc.baywatch.api.notify.NotifyService;
import fr.ght1pc9kc.baywatch.api.scrapper.ScrappingHandler;
import fr.ght1pc9kc.baywatch.domain.scrapper.actions.NewsUpdateNotificationHandler;
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
