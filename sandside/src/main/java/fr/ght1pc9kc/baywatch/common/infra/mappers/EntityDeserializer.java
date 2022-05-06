package fr.ght1pc9kc.baywatch.common.infra.mappers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.machinezoo.noexception.Exceptions;
import fr.ght1pc9kc.baywatch.common.api.model.Entity;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class EntityDeserializer extends JsonDeserializer<Entity<?>> implements ContextualDeserializer {
    private final Map<JavaType, JsonDeserializer<Object>> deserCache = new ConcurrentHashMap<>();
    private JsonDeserializer<Object> wrappedDeserializer;

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
        EntityDeserializer deserializer = new EntityDeserializer();
        JavaType paramType = (property == null)
                ? ctxt.getContextualType().containedType(0) : property.getType().containedType(0);

        deserializer.wrappedDeserializer = deserCache.computeIfAbsent(paramType,
                Exceptions.wrap().function(ctxt::findRootValueDeserializer));

        return deserializer;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Entity deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
        JsonToken currentToken = jp.getCurrentToken();

        if (currentToken != JsonToken.START_OBJECT) {
            throw ctxt.wrongTokenException(jp, (JavaType) null, JsonToken.START_OBJECT, "Bad token");
        }

        ObjectNode treeNode = jp.readValueAsTree();
        ObjectNode entityNode = ctxt.getNodeFactory().objectNode();
        ObjectNode wrappedNode = ctxt.getNodeFactory().objectNode();

        treeNode.fields().forEachRemaining(entry -> {
            if (entry.getKey().startsWith("_")) {
                entityNode.set(entry.getKey(), entry.getValue());
            } else {
                wrappedNode.set(entry.getKey(), entry.getValue());
            }
        });

        String id = Optional.ofNullable(treeNode.remove(Entity.IDENTIFIER))
                .map(JsonNode::asText)
                .orElseThrow(IllegalArgumentException::new);
        Instant createdAt = Optional.ofNullable(treeNode.remove(Entity.CREATED_AT))
                .map(JsonNode::asText).map(Instant::parse)
                .orElseThrow(IllegalArgumentException::new);

        JsonParser subParser = treeNode.traverse(jp.getCodec());
        subParser.nextToken();
        Object wrapped = wrappedDeserializer.deserialize(subParser, ctxt);
        entityNode.set("entity", wrappedNode);

        return new Entity<>(id, Entity.NO_ONE, createdAt, wrapped);
    }
}