package fr.ght1pc9kc.baywatch.security.infra.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import fr.ght1pc9kc.baywatch.security.api.model.Permission;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = PermissionMixin.PermissionDeserializer.class)
public abstract class PermissionMixin {
    @JsonValue
    public abstract String toString();

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public abstract Permission from(String permissionRepresentation);

    public static final class PermissionDeserializer extends ValueDeserializer<Permission> {
        @Override
        public Permission deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
            return Permission.from(jsonParser.readValueAs(String.class));
        }
    }
}
