package fr.ght1pc9kc.baywatch.scrapper.domain.filters;

import fr.ght1pc9kc.baywatch.techwatch.api.model.RawNews;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class RedditNewsFilterTest {
    private static final RawNews RAW = RawNews.builder()
            .id("0")
            .link(URI.create("http://www.reddit.com/u/okenobi"))
            .title("Start with this title")
            .description("""
                    &lt;!-- SC_OFF --&gt;&lt;div class=&quot;md&quot;&gt;&lt;p&gt;These three posts describe
                    how to deploy Jakarta EE 9 applications to &lt;a href=&quot;https://hantsy.medium.com/deploying-jakarta-ee-9-applications-to-wildfly-e271d1403b2c?source=friends_link&amp;amp;sk=c20ec9f09891287d698e29d7368cab8d&quot;&gt;WildFly&lt;/a&gt;,
                    &lt;a href=&quot;https://hantsy.medium.com/deploying-jakarta-ee-9-applications-to-apache-tomee-e1a9b9399d9b?source=friends_link&amp;amp;sk=734db0a3c6e205039c6ad2a392235880&quot;&gt;Apache
                    TomEE&lt;/a&gt; and &lt;a href=&quot;https://hantsy.medium.com/deploying-jakarta-ee-9-applications-to-open-liberty-dac4529f48c6?source=friends_link&amp;amp;sk=47a141c9ad845826e6c822068f37881b&quot;&gt;Open
                    Liberty&lt;/a&gt;.&lt;/p&gt; &lt;/div&gt;&lt;!-- SC_ON --&gt; &amp;#32; submitted by &amp;#32; &lt;a href=&quot;https://www.reddit.com/user/congolomera&quot;&gt;
                    /u/congolomera &lt;/a&gt; &lt;br/&gt; &lt;span&gt;&lt;a href=&quot;https://www.reddit.com/r/java/comments/k3hmfl/deploying_jakarta_ee_9_applications_to_wildfly/&quot;&gt;[link]&lt;/a&gt;&lt;/span&gt;
                    &amp;#32; &lt;span&gt;&lt;a href=&quot;https://www.reddit.com/r/java/comments/k3hmfl/deploying_jakarta_ee_9_applications_to_wildfly/&quot;&gt;[commentaires]&lt;/a&gt;&lt;/span&gt;
                    """).build();

    private final RedditNewsFilter tested = new RedditNewsFilter();

    @Test
    void should_filter_reddit() {
        StepVerifier.create(tested.filter(RAW))
                .assertNext(actual -> Assertions.assertAll(
                        () -> assertThat(actual.getId()).isEqualTo("c74cb819fe7a596814406c9ec164dfa5d502fba8659ec13e634a33d1ae7cbd56"),
                        () -> assertThat(actual.getTitle()).isEqualTo(RAW.getTitle()),
                        () -> assertThat(actual.getDescription()).isEqualTo(RAW.getDescription()),
                        () -> assertThat(actual.getLink()).isEqualTo(URI.create("https://www.reddit.com/r/java/comments/k3hmfl/deploying_jakarta_ee_9_applications_to_wildfly/"))
                )).verifyComplete();
    }

    @Test
    void should_filter_reddit_without_reddit() {
        RawNews rawNewsNotReddit = RAW.withLink(URI.create("https://www.jedi.com/not/reddit"));
        StepVerifier.create(tested.filter(rawNewsNotReddit))
                .assertNext(actual -> assertThat(actual).isSameAs(rawNewsNotReddit)).verifyComplete();
    }
}