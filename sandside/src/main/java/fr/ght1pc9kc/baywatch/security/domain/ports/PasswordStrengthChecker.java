package fr.ght1pc9kc.baywatch.security.domain.ports;

import fr.ght1pc9kc.baywatch.security.api.model.PasswordEvaluation;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

public interface PasswordStrengthChecker {
    PasswordEvaluation estimate(String password, Locale locale, Collection<String> exclude);

    default PasswordEvaluation estimate(String password, Locale locale) {
        return estimate(password, locale, Collections.emptyList());
    }

    String generate();
}
