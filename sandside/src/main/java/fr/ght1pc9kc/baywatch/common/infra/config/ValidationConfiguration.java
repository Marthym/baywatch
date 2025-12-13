package fr.ght1pc9kc.baywatch.common.infra.config;

import fr.ght1pc9kc.baywatch.common.api.constraints.NullOrNotBlank;
import fr.ght1pc9kc.baywatch.common.infra.validators.NullOrNotBlankValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
public class ValidationConfiguration {

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource ms =
                new ReloadableResourceBundleMessageSource();
        ms.setBasename("classpath:messages/ValidationMessages");
        ms.setDefaultEncoding("UTF-8");
        return ms;
    }

    @Bean
    public LocalValidatorFactoryBean validator(MessageSource messageSource) {
        LocalValidatorFactoryBean factory = new LocalValidatorFactoryBean();

        factory.setValidationMessageSource(messageSource);

        factory.setConfigurationInitializer(configuration -> {
            if (configuration instanceof HibernateValidatorConfiguration hibernateConfig) {

                ConstraintMapping mapping = hibernateConfig.createConstraintMapping();

                mapping
                        .constraintDefinition(NullOrNotBlank.class)
                        .validatedBy(NullOrNotBlankValidator.class);

                hibernateConfig.addMapping(mapping);
            }
        });

        return factory;
    }
}
