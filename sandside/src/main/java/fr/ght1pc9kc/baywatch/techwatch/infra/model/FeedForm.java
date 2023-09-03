package fr.ght1pc9kc.baywatch.techwatch.infra.model;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

import java.util.Arrays;
import java.util.Objects;

public record FeedForm(
        String name,
        @NotBlank @URL String location,
        String[] tags
) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FeedForm feedForm = (FeedForm) o;

        if (!Objects.equals(name, feedForm.name)) return false;
        if (!Objects.equals(location, feedForm.location)) return false;
        return Arrays.equals(tags, feedForm.tags);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (location != null ? location.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(tags);
        return result;
    }

    @Override
    public String toString() {
        return "FeedForm{" +
                "name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", tags=" + Arrays.toString(tags) +
                '}';
    }
}
