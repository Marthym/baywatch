package fr.ght1pc9kc.baywatch.scraper.infra.config;

import fr.ght1pc9kc.scraphead.core.HeadScraper;
import fr.ght1pc9kc.scraphead.core.HeadScrapers;
import fr.ght1pc9kc.scraphead.core.http.ScrapClient;
import fr.ght1pc9kc.scraphead.netty.http.NettyScrapClient;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.netty.http.client.HttpClient;

import java.nio.charset.StandardCharsets;

@Configuration
public class OpenGraphConfig {
    private static final int MAX_FRAME_LENGTH = 600_000;
    private static final ByteBuf FRAME_HEAD_DELIMITER = Unpooled.wrappedBuffer("</head>".getBytes(StandardCharsets.UTF_8));
    private static final ByteBuf FRAME_BODY_DELIMITER = Unpooled.wrappedBuffer("<body".getBytes(StandardCharsets.UTF_8));

    @Bean
    public HeadScraper getOpenGraphScrapper(HttpClient httpClient) {
        HttpClient bodyScraper = httpClient.doOnConnected(c ->
                c.addHandler(new DelimiterBasedFrameDecoder(MAX_FRAME_LENGTH, FRAME_HEAD_DELIMITER, FRAME_BODY_DELIMITER)));//FIXME: when reactor-netty {@link HttpOperations} will implement addHandlerLast
        ScrapClient scrapClient = new NettyScrapClient(bodyScraper);
        return HeadScrapers.builder(scrapClient).build();
    }
}
