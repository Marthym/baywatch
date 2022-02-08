package fr.ght1pc9kc.baywatch.domain.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class MailAddress {
    /**
     * Remove the possible `+` suffix or name description to keep only original mail address
     *
     * @param mail The mail address to sanitize
     * @return The sanitized mail
     */
    public static String sanitize(String mail) {
        boolean ignore = false;
        StringBuilder sanitized = new StringBuilder();
        for (char c : mail.toCharArray()) {
            switch (c) {
                case ' ':
                    continue;
                case '+':
                case '<':
                    ignore = true;
                    break;
                case '@':
                    ignore = false;
                default:
                    if (ignore) continue;
                    sanitized.append(c);
            }
        }
        return sanitized.toString();
    }
}
