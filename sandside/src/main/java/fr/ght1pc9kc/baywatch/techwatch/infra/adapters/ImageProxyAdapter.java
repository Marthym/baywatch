package fr.ght1pc9kc.baywatch.techwatch.infra.adapters;

import fr.ght1pc9kc.baywatch.techwatch.api.ImageProxyService;
import fr.ght1pc9kc.baywatch.techwatch.domain.ImageProxyServiceImpl;
import fr.ght1pc9kc.baywatch.techwatch.infra.config.ImageProxyConfig;
import fr.ght1pc9kc.baywatch.techwatch.infra.config.TechwatchMapper;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Service;

@Service
public class ImageProxyAdapter implements ImageProxyService {
    @Delegate
    private final ImageProxyService delegate;

    public ImageProxyAdapter(TechwatchMapper mapper, ImageProxyConfig config) {
        this.delegate = new ImageProxyServiceImpl(mapper.toProperties(config));
    }
}
