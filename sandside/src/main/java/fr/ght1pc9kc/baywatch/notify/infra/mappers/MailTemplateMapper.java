package fr.ght1pc9kc.baywatch.notify.infra.mappers;

import fr.ght1pc9kc.baywatch.dsl.tables.records.MailTemplatesRecord;
import fr.ght1pc9kc.baywatch.notify.domain.model.MailTemplate;
import fr.ght1pc9kc.entity.api.Entity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MailTemplateMapper {
    default Entity<MailTemplate> toEntity(MailTemplatesRecord r) {
        return Entity.identify(toObject(r))
                .withId(r.getMateId());
    }

    @Mapping(target = "name", source = "mateName")
    @Mapping(target = "locale", source = "mateLang")
    @Mapping(target = "subject", source = "mateSubject")
    @Mapping(target = "body", source = "mateBody")
    MailTemplate toObject(MailTemplatesRecord r);
}
