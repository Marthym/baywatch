package fr.ght1pc9kc.baywatch.techwatch.infra.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Value;
import org.hibernate.validator.constraints.URL;

@Value
public class FeedForm {
    public String name;
    public @NotBlank @URL String url;
    public String[] tags;
}
