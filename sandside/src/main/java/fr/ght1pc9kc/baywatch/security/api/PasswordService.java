package fr.ght1pc9kc.baywatch.security.api;

import fr.ght1pc9kc.baywatch.security.PasswordChecker;

public interface PasswordService extends PasswordChecker {
    String encode(CharSequence rawPassword);

    boolean matches(CharSequence rawPassword, String encodedPassword);
}
