package fr.ght1pc9kc.baywatch.admin.infra.adapters;

import fr.ght1pc9kc.baywatch.admin.api.model.RawFeed;
import fr.ght1pc9kc.baywatch.techwatch.api.ImageProxyService;
import fr.ght1pc9kc.baywatch.techwatch.api.model.ImagePresets;
import fr.ght1pc9kc.entity.api.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AdministrationProxifierAdapterTest {
    private static final Entity<RawFeed> TEST_FEED = Entity.identify(RawFeed.builder()
            .icon(URI.create("https://test.com/icon.png"))
            .build()).withId("test-id");

    private ImageProxyService imageProxyService;
    private AdministrationProxifierAdapter tested;

    @BeforeEach
    void setUp() {
        imageProxyService = Mockito.mock(ImageProxyService.class);
    }

    @Test
    void should_return_same_feed_when_no_proxy_service() {
        tested = new AdministrationProxifierAdapter(null);
        Entity<RawFeed> result = tested.proxifyRawFeed(TEST_FEED);
        assertThat(result).isEqualTo(TEST_FEED);
    }

    @Test
    void should_proxify_feed_icon() {
        tested = new AdministrationProxifierAdapter(imageProxyService);
        when(imageProxyService.proxify(any(), any())).thenReturn(URI.create("https://proxy.com/icon.png"));

        Entity<RawFeed> result = tested.proxifyRawFeed(TEST_FEED);

        assertThat(result.self().icon()).hasToString("https://proxy.com/icon.png");
        Mockito.verify(imageProxyService).proxify(TEST_FEED.self().icon(), ImagePresets.ICON);
    }
}