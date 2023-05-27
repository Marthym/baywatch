package fr.ght1pc9kc.baywatch.security.infra.config;

import fr.ght1pc9kc.baywatch.security.api.model.Permission;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.security.infra.model.UserForm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", imports = {Permission.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SecurityMapper {
    @Mapping(target = "roles", expression = "java( form.roles().stream().map(Permission::from).distinct().sorted().toList() )")
    User formToUser(UserForm form);
}
