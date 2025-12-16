package fr.ght1pc9kc.baywatch.admin.infra.exceptions;

import fr.ght1pc9kc.baywatch.admin.domain.exceptions.AdministrationModuleException;
import fr.ght1pc9kc.baywatch.common.api.exceptions.TranslatableException;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class InvalidConfigurationException extends AdministrationModuleException implements TranslatableException {
    private final List<String> properties;

    public InvalidConfigurationException(String message, Collection<String> properties) {
        super(message);
        this.properties = List.copyOf(properties);
    }

    @Override
    public String getTranslationKey() {
        return "config.admin.mail.messages.updateError";
    }

    @Override
    public String classification() {
        return "VALIDATION_ERROR";
    }

    @Override
    public Map<String, Object> getExtensions() {
        return Map.of("properties", properties);
    }
}
