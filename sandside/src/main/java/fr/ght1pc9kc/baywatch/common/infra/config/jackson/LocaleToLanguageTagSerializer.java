package fr.ght1pc9kc.baywatch.common.infra.config.jackson;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

import java.util.Locale;

public class LocaleToLanguageTagSerializer extends ValueSerializer<Locale> {
    @Override
    public void serialize(Locale value, tools.jackson.core.JsonGenerator jsonGenerator, SerializationContext ctxt) throws JacksonException {
        jsonGenerator.writeString(value.toLanguageTag());
    }
}
