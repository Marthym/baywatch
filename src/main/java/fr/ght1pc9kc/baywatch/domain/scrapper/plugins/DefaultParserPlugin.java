package fr.ght1pc9kc.baywatch.domain.scrapper.plugins;

import fr.ght1pc9kc.baywatch.api.scrapper.FeedParserPlugin;
import org.springframework.stereotype.Component;

@Component
public final class DefaultParserPlugin implements FeedParserPlugin {
    @Override
    public String pluginForDomain() {
        return "*";
    }
}
