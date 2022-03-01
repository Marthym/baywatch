package fr.ght1pc9kc.baywatch.scrapper.domain.opengraph.plugins;

import fr.ght1pc9kc.scraphead.core.OpenGraphPlugin;

import java.net.URI;
import java.time.Instant;
import java.time.Period;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.regex.Pattern;

public class YoutubeRequestPlugin implements OpenGraphPlugin {
    private static final Pattern YOUTUBE_URI_PATTERN = Pattern.compile("(youtube|youtu\\.be|googlevideo|ytimg)");

    @Override
    public String name() {
        return "Youtube OG Plugin";
    }

    @Override
    public boolean isApplicable(URI location) {
        return YOUTUBE_URI_PATTERN.matcher(location.getHost()).find();
    }

    @Override
    public Map<String, String> additionalHeaders() {
        String format = Instant.now().atZone(ZoneOffset.UTC).plus(Period.ofYears(1)).format(DateTimeFormatter.RFC_1123_DATE_TIME);
        return Map.of("Cookie", "CONSENT=YES+0; Path=/; Domain=youtube.com; Secure; Expires=" + format + ";");
    }
}
