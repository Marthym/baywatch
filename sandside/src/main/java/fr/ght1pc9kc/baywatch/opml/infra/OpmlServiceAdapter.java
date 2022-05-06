package fr.ght1pc9kc.baywatch.opml.infra;

import fr.ght1pc9kc.baywatch.opml.api.OpmlService;
import fr.ght1pc9kc.baywatch.opml.domain.OpmlReader;
import fr.ght1pc9kc.baywatch.opml.domain.OpmlServiceImpl;
import fr.ght1pc9kc.baywatch.opml.domain.OpmlWriter;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.techwatch.infra.persistence.FeedRepository;
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
