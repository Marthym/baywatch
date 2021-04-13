package fr.ght1pc9kc.baywatch.infra.model;

import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.NotBlank;

public final class FeedForm {
    public @NotBlank String name;
    public @NotBlank @URL String url;
}
