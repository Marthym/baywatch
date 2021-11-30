package fr.ght1pc9kc.baywatch.infra.opml;

import fr.ght1pc9kc.baywatch.api.opml.OpmlService;
import fr.ght1pc9kc.baywatch.domain.opml.OpmlReader;
import fr.ght1pc9kc.baywatch.domain.opml.OpmlServiceImpl;
import fr.ght1pc9kc.baywatch.domain.opml.OpmlWriter;
import fr.ght1pc9kc.baywatch.domain.ports.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.infra.adapters.persistence.FeedRepository;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Service;

@Service
public class OpmlServiceAdapter implements OpmlService {
    @Delegate
    private final OpmlService delegate;

    public OpmlServiceAdapter(AuthenticationFacade authFacade, FeedRepository feedRepository) {
        this.delegate = new OpmlServiceImpl(feedRepository, authFacade, OpmlWriter::new, OpmlReader::new);
    }
}
