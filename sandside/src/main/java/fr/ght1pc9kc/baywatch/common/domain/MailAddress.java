package fr.ght1pc9kc.baywatch.common.domain;

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
            if (c == '@') {
                ignore = false;
                sanitized.append(c);
            } else if (c == '+' || c == '<') {
                ignore = true;
            } else if (c != ' ' && !ignore) {
                sanitized.append(c);
            }
        }
        return sanitized.toString();
    }
}
