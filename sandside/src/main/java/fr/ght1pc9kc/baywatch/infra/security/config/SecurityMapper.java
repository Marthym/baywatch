package fr.ght1pc9kc.baywatch.infra.security.config;

import fr.ght1pc9kc.baywatch.api.security.model.User;
import fr.ght1pc9kc.baywatch.infra.security.model.UserForm;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SecurityMapper {
    User formToUser(UserForm form);
}
