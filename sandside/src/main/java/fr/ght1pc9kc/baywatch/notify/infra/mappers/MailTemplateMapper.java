package fr.ght1pc9kc.baywatch.notify.infra.mappers;

import fr.ght1pc9kc.baywatch.dsl.tables.records.MailTemplatesRecord;
import fr.ght1pc9kc.baywatch.notify.domain.model.MailTemplate;
import fr.ght1pc9kc.entity.api.Entity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MailTemplateMapper {
    default Entity<MailTemplate> toEntity(MailTemplatesRecord r) {
        return Entity.identify(toObject(r))
                .withId(r.getMateId());
    }

    MailTemplate toObject(MailTemplatesRecord r);
}
