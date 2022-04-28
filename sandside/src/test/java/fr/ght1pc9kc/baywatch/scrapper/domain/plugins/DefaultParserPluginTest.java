package fr.ght1pc9kc.baywatch.scrapper.domain.plugins;

import fr.ght1pc9kc.baywatch.techwatch.api.model.RawNews;
import fr.ght1pc9kc.baywatch.tests.samples.FeedSamples;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class DefaultParserPluginTest {

    private static final String LOREM_IPSUM_1 = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed non risus. " +
            "Suspendisse lectus tortor, dignissim sit amet, adipiscing nec, ultricies sed, dolor. Cras elementum " +
            "ultrices diam. Maecenas ligula massa, varius a, semper congue, euismod non, mi. Proin porttitor, " +
            "orci nec nonummy molestie, enim est eleifend mi, non fermentum diam nisl sit amet erat. Duis semper. " +
            "Duis arcu massa, scelerisque vitae, consequat in, pretium a, enim. Pellentesque congue. Ut in risus " +
            "volutpat libero pharetra tempor. Cras vestibulum bibendum augue. Praesent egestas leo in pede. Praesent " +
            "blandit odio eu enim. Pellentesque sed dui ut augue blandit sodales. Vestibulum ante ipsum primis in " +
            "faucibus orci luctus et ultrices posuere cubilia Curae; Aliquam nibh. Mauris ac mauris sed pede " +
            "pellentesque fermentum. Maecenas adipiscing ante non diam sodales hendrerit";

    private final DefaultParserPlugin tested = new DefaultParserPlugin();

    @Test
    void should_get_domain() {
        Assertions.assertThat(tested.pluginForDomain()).isEqualTo("*");
    }

    @Test
    void should_handle_item_event() {
        RawNews.RawNewsBuilder actual = tested.handleItemEvent();
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void should_handle_item_link() {
        RawNews.RawNewsBuilder builder = RawNews.builder();
        RawNews actual = tested.handleLinkEvent(builder, FeedSamples.JEDI.getUrl()).build();

        Assertions.assertThat(actual.getId()).isEqualTo(FeedSamples.JEDI.getId());
        Assertions.assertThat(actual.getLink()).isEqualTo(FeedSamples.JEDI.getUrl());
    }

    @Test
    void should_handle_item_description() {
        RawNews.RawNewsBuilder builder = RawNews.builder()
                .id(FeedSamples.JEDI.getId())
                .link(FeedSamples.JEDI.getUrl());
        {
            RawNews actual = tested.handleDescriptionEvent(builder,
                    LOREM_IPSUM_1 + LOREM_IPSUM_1 + LOREM_IPSUM_1 + LOREM_IPSUM_1 + LOREM_IPSUM_1 + LOREM_IPSUM_1).build();
            Assertions.assertThat(actual.getDescription()).hasSize(3_000);
        }
        {
            RawNews actual = tested.handleDescriptionEvent(builder, LOREM_IPSUM_1).build();
            Assertions.assertThat(actual.getDescription()).hasSize(LOREM_IPSUM_1.length() - 1);
        }
    }
}