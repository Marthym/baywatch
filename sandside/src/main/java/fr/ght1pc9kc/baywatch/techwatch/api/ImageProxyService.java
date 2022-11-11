package fr.ght1pc9kc.baywatch.techwatch.api;

import fr.ght1pc9kc.baywatch.techwatch.api.model.ImagePresets;

import java.net.URI;

public interface ImageProxyService {
    URI proxify(URI image, ImagePresets preset);
}
