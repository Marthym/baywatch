package fr.ght1pc9kc.baywatch.infra.security.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import fr.ght1pc9kc.baywatch.api.model.News;
import fr.ght1pc9kc.baywatch.domain.ports.JwtTokenProvider;
import fr.ght1pc9kc.baywatch.infra.config.jackson.NewsMixin;
import fr.ght1pc9kc.baywatch.infra.security.JwtTokenAuthenticationFilter;
import fr.ght1pc9kc.baywatch.infra.security.model.SecurityParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

@Slf4j
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@EnableConfigurationProperties(SecurityParams.class)
public class SecurityConfiguration {
    @Bean
    SecurityWebFilterChain springSecurityFilterChain(
            ServerHttpSecurity http, JwtTokenProvider jwtTokenProvider, SecurityParams securityParams) {
        return http
                .csrf().disable()
                .httpBasic().disable()
                .formLogin().disable()

                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .authorizeExchange()
                .pathMatchers("/**").permitAll()
                .pathMatchers(HttpMethod.OPTIONS).permitAll()
//                .pathMatchers(HttpMethod.POST, "/login").permitAll()
//                .pathMatchers(HttpMethod.PUT, "/refresh").hasAuthority(TokenAuthority.ROLE_REFRESH.toString())
//                .pathMatchers(HttpMethod.GET, "/session/validate").hasAuthority(TokenAuthority.ROLE_REFRESH.toString())
//                .pathMatchers(HttpMethod.DELETE, "/logout").hasAuthority(TokenAuthority.ROLE_REFRESH.toString())
//                .anyExchange().hasAuthority(TokenAuthority.ROLE_ACCESS.toString())
//
//                .and()
                .and()

                .addFilterAt(new JwtTokenAuthenticationFilter(jwtTokenProvider, securityParams.cookie.name), SecurityWebFiltersOrder.HTTP_BASIC)
                .exceptionHandling().authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED))
                .and()

                .build();
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonMapperCustomizer() {
        return builder -> {
            log.debug("Configure Jackson");
            builder.findModulesViaServiceLoader(true);
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            builder.featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            builder.filters(new SimpleFilterProvider().setFailOnUnknownId(false));
            builder.serializationInclusion(JsonInclude.Include.NON_NULL);
            builder.mixIn(News.class, NewsMixin.class);
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ReactiveAuthenticationManager reactiveAuthenticationManager(
            @Qualifier("Baywatch") ReactiveUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        UserDetailsRepositoryReactiveAuthenticationManager authenticationManager =
                new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
        authenticationManager.setPasswordEncoder(passwordEncoder);
        return authenticationManager;
    }
}
