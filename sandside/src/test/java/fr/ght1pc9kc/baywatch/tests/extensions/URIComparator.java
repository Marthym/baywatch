package fr.ght1pc9kc.baywatch.tests.extensions;

import lombok.experimental.UtilityClass;

import java.util.Set;

@UtilityClass
public class URIComparator {
    public static Set<String> assertableQueryString(String queryString) {
        return Set.of(queryString.split("&"));
    }
}
