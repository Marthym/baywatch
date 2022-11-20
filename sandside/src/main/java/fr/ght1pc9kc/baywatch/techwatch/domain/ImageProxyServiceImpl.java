package fr.ght1pc9kc.baywatch.techwatch.domain;

import fr.ght1pc9kc.baywatch.techwatch.api.ImageProxyService;
import fr.ght1pc9kc.baywatch.techwatch.api.model.ImagePresets;
import fr.ght1pc9kc.baywatch.techwatch.api.model.ImageProxyProperties;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Objects;

@Slf4j
public class ImageProxyServiceImpl implements ImageProxyService {
    private final ImageProxyProperties properties;

    public ImageProxyServiceImpl(ImageProxyProperties properties) {
        this.properties = properties;
    }

    @Override
    public URI proxify(URI image, ImagePresets preset) {
        if (Objects.isNull(image)) {
            return null;
        }
        String imgPath = "/pr:" + preset.name().toLowerCase() + '/'
                + Base64.getUrlEncoder().encodeToString(image.toString().getBytes(StandardCharsets.UTF_8));

        return URI.create(properties.pathBase() + signPath(imgPath));
    }

    private String signPath(String path) {
        try {
            final String HMACSHA256 = "HmacSHA256";

            Mac sha256HMAC = Mac.getInstance(HMACSHA256);
            SecretKeySpec secretKey = new SecretKeySpec(properties.signingKey().array(), HMACSHA256);
            sha256HMAC.init(secretKey);
            sha256HMAC.update(properties.signingSalt().array());

            String hash = Base64.getUrlEncoder().withoutPadding().encodeToString(sha256HMAC.doFinal(path.getBytes()));

            return '/' + hash + path;
        } catch (IllegalArgumentException | NoSuchAlgorithmException | InvalidKeyException e) {
            log.debug("Unable to sign proxy URL !");
            log.trace("STACKTRACE", e);
            return "/insecure/" + path;
        }
    }
}
