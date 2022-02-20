package fr.ght1pc9kc.baywatch.security.infra.config;

import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.security.infra.model.UserForm;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SecurityMapper {
    User formToUser(UserForm form);
}
