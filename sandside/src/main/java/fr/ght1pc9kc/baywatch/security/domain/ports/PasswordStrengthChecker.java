package fr.ght1pc9kc.baywatch.security.domain.ports;

import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.security.domain.model.PasswordStrength;

import java.util.Locale;

public interface PasswordStrengthChecker {
    PasswordStrength estimate(String password, User user, Locale locale);
}
