package fr.ght1pc9kc.baywatch.infra.request;

import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.juery.basic.filter.ListPropertiesCriteriaVisitor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SpringConfiguration {
    @Bean
    public Criteria.Visitor<List<String>> getListPropertiesVisitor() {
        return new ListPropertiesCriteriaVisitor();
    }
}
