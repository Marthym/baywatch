package fr.ght1pc9kc.baywatch.security.infra.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fr.ght1pc9kc.baywatch.security.api.model.Permission;

import java.io.IOException;

@JsonDeserialize(using = PermissionMixin.PermissionDeserializer.class)
public abstract class PermissionMixin {
    @JsonValue
    public abstract String toString();

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public abstract Permission from(String permissionRepresentation);

    public static final class PermissionDeserializer extends JsonDeserializer<Permission> {
        @Override
        public Permission deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            return Permission.from(jsonParser.readValueAs(String.class));
        }
    }
}
