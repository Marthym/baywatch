package fr.ght1pc9kc.baywatch.common.infra.mappers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fr.ght1pc9kc.baywatch.common.api.model.Entity;

import java.time.Instant;

@JsonDeserialize(using = EntityDeserializer.class)
@JsonPropertyOrder({"id", "createdAt", "entity"})
public abstract class EntityJacksonMixin {
    @JsonProperty(Entity.IDENTIFIER)
    public String id;
    @JsonProperty(Entity.CREATED_AT)
    public Instant createdAt;

    @JsonUnwrapped
    public Object entity;
}
