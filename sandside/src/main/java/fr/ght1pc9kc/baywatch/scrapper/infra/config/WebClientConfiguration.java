package fr.ght1pc9kc.baywatch.scrapper.infra.config;

import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.resolver.dns.DnsAddressResolverGroup;
import io.netty.resolver.dns.DnsNameResolverBuilder;
import io.netty.resolver.dns.SequentialDnsServerAddressStreamProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.net.InetSocketAddress;
import java.util.Set;

@Configuration
public class WebClientConfiguration {

    @Bean
    @ScraperQualifier
    public DnsAddressResolverGroup getDnsAddressResolver() {
        return new DnsAddressResolverGroup(
                new DnsNameResolverBuilder()
                        .queryTimeoutMillis(10_000)
                        .channelType(EpollDatagramChannel.class)
                        .nameServerProvider(
                                new SequentialDnsServerAddressStreamProvider(
                                        new InetSocketAddress("9.9.9.9", 53),
                                        new InetSocketAddress("2620:fe::fe", 53),
                                        new InetSocketAddress("8.8.8.8", 53))));
    }

    @Bean
    @ScraperQualifier
    public WebClient getScraperWebClient(ScrapperProperties properties, DnsAddressResolverGroup dnsAddressResolverGroup) {
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create()
                                .resolver(dnsAddressResolverGroup)
                                .followRedirect(true)
                                .followRedirect((req, res) -> // 303 was not in the default code
                                        Set.of(301, 302, 303, 307, 308).contains(res.status().code()))
                                .compress(true)
                                .responseTimeout(properties.timeout())
                                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) properties.timeout().toMillis())
                )).build();
    }
}
