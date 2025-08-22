package fr.ght1pc9kc.baywatch.security.api;

public interface PasswordService extends PasswordChecker {
    String encode(CharSequence rawPassword);

    boolean matches(CharSequence rawPassword, String encodedPassword);
}
