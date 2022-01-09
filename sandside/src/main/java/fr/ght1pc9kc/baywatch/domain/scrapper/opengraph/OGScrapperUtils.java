package fr.ght1pc9kc.baywatch.domain.scrapper.opengraph;

import fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.model.Meta;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.net.URI;

@Slf4j
@UtilityClass
public class OGScrapperUtils {
    private static final String META_PROPERTY = "property";
    private static final String META_NAME = "name";
    private static final String META_CONTENT = "content";

    public static String removeQueryString(String uri) {
        int idx = uri.indexOf('?');
        return (idx < 0) ? uri : uri.substring(0, idx);
    }

    public static URI removeQueryString(URI uri) {
        if (uri.getQuery() == null) {
            return uri;
        } else {
            return URI.create(removeQueryString(uri.toString()));
        }
    }

    public static Flux<Meta> extractMetaHeaders(String html) {
        return Flux.create(sink -> {
            for (String head : html.split("(<meta )")) {
                try {
                    char[] chars = head.toCharArray();
                    int nextIdx = 0;
                    String metaProperty = null;
                    String metaValue = null;

                    while ((metaValue == null || metaProperty == null) || nextIdx < head.length()) {
                        int eqIdx = head.indexOf('=', nextIdx);
                        if (eqIdx < 0) {
                            break;
                        }
                        String prop = head.substring(nextIdx, eqIdx).trim();
                        StringBuilder valueBld = new StringBuilder(head.length() - eqIdx);
                        char q = 0;
                        nextIdx = eqIdx + 1;
                        for (int i = nextIdx; i < chars.length; i++) {
                            if ((q != '\'' && q != '"') && (chars[i] == '\'' || chars[i] == '"')) {
                                q = chars[i];
                                continue;
                            } else if ((q == '\'' || q == '"') && (chars[i] == q)) {
                                nextIdx = i + 1;
                                break;
                            }
                            valueBld.append(chars[i]);
                        }
                        String value = valueBld.toString();

                        switch (prop) {
                            case META_NAME:
                                if (metaProperty == null) {
                                    metaProperty = value;
                                }
                                break;
                            case META_PROPERTY:
                                metaProperty = value;
                                break;
                            case META_CONTENT:
                                metaValue = value;
                                break;
                        }
                    }

                    if (metaProperty == null || metaValue == null) {
                        continue;
                    }
                    sink.next(new Meta(metaProperty, metaValue));
                } catch (Exception e) {
                    log.debug("Fail to parse meta {}", head);
                    log.debug("STACKTRACE", e);
                }
            }
            sink.complete();
        });
    }
}
