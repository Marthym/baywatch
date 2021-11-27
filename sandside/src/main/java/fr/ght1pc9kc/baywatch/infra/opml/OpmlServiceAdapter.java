package fr.ght1pc9kc.baywatch.infra.opml;

import fr.ght1pc9kc.baywatch.api.FeedService;
import fr.ght1pc9kc.baywatch.api.opml.OpmlService;
import fr.ght1pc9kc.baywatch.domain.opml.OpmlServiceImpl;
import fr.ght1pc9kc.baywatch.domain.ports.AuthenticationFacade;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Service;

import java.time.Clock;

@Service
public class OpmlServiceAdapter implements OpmlService {
    @Delegate
    private final OpmlService delegate;

    public OpmlServiceAdapter(AuthenticationFacade authFacade, FeedService feedService) {
        this.delegate = new OpmlServiceImpl(feedService, authFacade, Clock.systemUTC());
    }
}
