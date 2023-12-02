package fr.ght1pc9kc.baywatch.common.domain;

import com.machinezoo.noexception.Exceptions;
import fr.ght1pc9kc.baywatch.common.domain.exceptions.HashingException;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Objects;

@UtilityClass
public class Hasher {
    private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);

    public static String identify(@NotNull URI toHash) {
        Objects.requireNonNull(toHash);
        StringBuilder bldr = new StringBuilder(toHash.toString().length());
        bldr.append("://")
                .append(toHash.getHost());
        if (toHash.getPort() > 0 && toHash.getPort() != 80 && toHash.getPort() != 443) {
            bldr.append(":").append(toHash.getPort());
        }
        bldr.append(toHash.getPath());
        return sha3(bldr.toString());
    }

    public static String sha3(String toHash) {
        return Exceptions.wrap(HashingException::new).get(() -> {
            final MessageDigest digest = MessageDigest.getInstance("SHA3-256");
            final byte[] hashbytes = digest.digest(toHash.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashbytes).toLowerCase();
        });
    }

    public static String bytesToHex(byte[] bytes) {
        byte[] hexChars = new byte[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars, StandardCharsets.UTF_8);
    }
}
