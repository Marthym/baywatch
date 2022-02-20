package fr.ght1pc9kc.baywatch.scrapper.domain.opengraph.model;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class Meta {
    public String property;
    public String content;
}
