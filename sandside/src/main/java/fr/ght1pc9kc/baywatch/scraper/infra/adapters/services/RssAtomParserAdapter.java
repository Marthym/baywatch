package fr.ght1pc9kc.baywatch.scraper.infra.adapters.services;

import fr.ght1pc9kc.baywatch.scraper.api.RssAtomParser;
import fr.ght1pc9kc.baywatch.scraper.domain.RssAtomParserImpl;
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
