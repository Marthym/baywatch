package fr.ght1pc9kc.baywatch.common.infra.config.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Locale;

public class LocaleToLanguageTagSerializer extends JsonSerializer<Locale> {
    @Override
    public void serialize(Locale locale, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(locale.toLanguageTag());
    }
}
