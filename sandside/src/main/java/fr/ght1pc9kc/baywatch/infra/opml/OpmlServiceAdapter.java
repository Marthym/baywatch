package fr.ght1pc9kc.baywatch.infra.opml;

import fr.ght1pc9kc.baywatch.api.FeedService;
import fr.ght1pc9kc.baywatch.api.opml.OpmlService;
import fr.ght1pc9kc.baywatch.domain.opml.OpmlServiceImpl;
import fr.ght1pc9kc.baywatch.domain.opml.OpmlWriter;
import fr.ght1pc9kc.baywatch.domain.ports.AuthenticationFacade;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Service;

@Service
public class OpmlServiceAdapter implements OpmlService {
    @Delegate
    private final OpmlService delegate;

    public OpmlServiceAdapter(AuthenticationFacade authFacade, FeedService feedService) {
        this.delegate = new OpmlServiceImpl(feedService, authFacade, OpmlWriter::new);
    }
}
