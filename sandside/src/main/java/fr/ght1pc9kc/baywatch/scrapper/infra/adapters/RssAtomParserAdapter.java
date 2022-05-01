package fr.ght1pc9kc.baywatch.scrapper.infra.adapters;

import fr.ght1pc9kc.baywatch.scrapper.api.RssAtomParser;
import fr.ght1pc9kc.baywatch.scrapper.domain.RssAtomParserImpl;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Component;

@Component
public class RssAtomParserAdapter implements RssAtomParser {
    @Delegate
    RssAtomParser delegate;

    public RssAtomParserAdapter() {
        delegate = new RssAtomParserImpl();
    }
}
