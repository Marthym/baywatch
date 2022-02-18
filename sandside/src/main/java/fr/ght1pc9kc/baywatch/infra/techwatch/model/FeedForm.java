package fr.ght1pc9kc.baywatch.infra.techwatch.model;

import lombok.Value;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.NotBlank;

@Value
public class FeedForm {
    public String name;
    public @NotBlank @URL String url;
    public String[] tags;
}
