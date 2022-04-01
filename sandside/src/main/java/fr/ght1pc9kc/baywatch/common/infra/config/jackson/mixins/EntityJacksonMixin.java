package fr.ght1pc9kc.baywatch.common.infra.config.jackson.mixins;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.common.infra.mappers.EntityDeserializer;

import java.time.Instant;

@JsonDeserialize(using = EntityDeserializer.class)
@JsonPropertyOrder({"id", "createdBy", "createdAt", "self"})
public abstract class EntityJacksonMixin {
    @JsonProperty(Entity.IDENTIFIER)
    public String id;
    @JsonProperty(Entity.CREATED_AT)
    public Instant createdAt;
    @JsonProperty(Entity.CREATED_BY)
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = CreatedByNoOneFilter.class)
    public Instant createdBy;

    @JsonUnwrapped
    public Object self;

    public static class CreatedByNoOneFilter {
        @Override
        public int hashCode() {
            return Entity.NO_ONE.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || obj.getClass() != String.class)
                return false;

            return Entity.NO_ONE.equals(obj);
        }
    }
}
