package fr.ght1pc9kc.baywatch.infra.opml;

import fr.ght1pc9kc.baywatch.api.FeedService;
import fr.ght1pc9kc.baywatch.api.opml.OpmlService;
import fr.ght1pc9kc.baywatch.domain.opml.OpmlServiceImpl;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Service;

@Service
public class OpmlServiceAdapter implements OpmlService {
    @Delegate
    private final OpmlService delegate;

    public OpmlServiceAdapter(FeedService feedService) {
        this.delegate = new OpmlServiceImpl(feedService);
    }
}
