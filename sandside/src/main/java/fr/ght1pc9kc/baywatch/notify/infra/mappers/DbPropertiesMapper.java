package fr.ght1pc9kc.baywatch.notify.infra.mappers;

import fr.ght1pc9kc.baywatch.notify.domain.model.NotifyConstants;
import lombok.experimental.UtilityClass;
import org.jooq.Field;

import java.util.Map;

import static fr.ght1pc9kc.baywatch.dsl.tables.MailTemplates.MAIL_TEMPLATES;

@UtilityClass
public class DbPropertiesMapper {
    public static final Map<String, Field<?>> MATE_PROPERTIES_MAPPING = Map.of(
            NotifyConstants.MATE_PROP_ID, MAIL_TEMPLATES.MATE_ID,
            NotifyConstants.MATE_PROP_NAME, MAIL_TEMPLATES.MATE_NAME,
            NotifyConstants.MATE_PROP_LOCALE, MAIL_TEMPLATES.MATE_LANG
    );
}
