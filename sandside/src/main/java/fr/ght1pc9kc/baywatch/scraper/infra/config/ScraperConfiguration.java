package fr.ght1pc9kc.baywatch.scraper.infra.config;

import fr.ght1pc9kc.baywatch.scraper.api.NewsFilter;
import fr.ght1pc9kc.baywatch.scraper.domain.filters.OpenGraphFilter;
import fr.ght1pc9kc.baywatch.scraper.domain.filters.RedditNewsFilter;
import fr.ght1pc9kc.baywatch.scraper.domain.filters.SanitizerFilter;
import fr.ght1pc9kc.scraphead.core.HeadScraper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Configuration
public class ScraperConfiguration {

    @Bean
    @ScraperQualifier
    public Scheduler getScraperScheduler() {
        return Schedulers.newBoundedElastic(5, Integer.MAX_VALUE, "scrapper");
    }

    @Bean
    @Order(1)
    public NewsFilter redditNews() {
        return new RedditNewsFilter();
    }

    @Bean
    @Order(2)
    public NewsFilter openGraph(HeadScraper headScraper) {
        return new OpenGraphFilter(headScraper);
    }

//    @Bean
//    @Order(3)
//    public NewsFilter imageLinkValidation(WebClient http) {
//        return new ImageLinkValidationFilter(http);
//    }

    @Bean
    @Order(4)
    public NewsFilter newsSanitizer() {
        return new SanitizerFilter();
    }
}
