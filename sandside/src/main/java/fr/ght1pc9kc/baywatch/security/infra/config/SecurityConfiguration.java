package fr.ght1pc9kc.baywatch.security.infra.config;

import fr.ght1pc9kc.baywatch.security.api.model.Role;
import fr.ght1pc9kc.baywatch.security.domain.ports.JwtTokenProvider;
import fr.ght1pc9kc.baywatch.security.infra.JwtTokenAuthenticationFilter;
import fr.ght1pc9kc.baywatch.security.infra.TokenCookieManager;
import fr.ght1pc9kc.baywatch.security.infra.model.SecurityParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
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
            ServerHttpSecurity http, JwtTokenProvider jwtTokenProvider,
            TokenCookieManager cookieManager, ReactiveUserDetailsService userService,
            @Value("${baywatch.base-route}") String baseRoute) {
        return http
                .csrf().disable()
                .httpBasic().disable()
                .formLogin().disable()

                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .authorizeExchange()
                .pathMatchers(HttpMethod.OPTIONS).permitAll()
                .pathMatchers(HttpMethod.GET, baseRoute + "/stats").permitAll()
                .pathMatchers(HttpMethod.GET, baseRoute + "/news").permitAll()
                .pathMatchers(HttpMethod.GET, baseRoute + "/feeds").permitAll()
                .pathMatchers(HttpMethod.POST, baseRoute + "/auth/login").permitAll()
                .pathMatchers(HttpMethod.PUT, baseRoute + "/auth/refresh").permitAll()
                .pathMatchers(HttpMethod.DELETE, baseRoute + "/auth/logout").authenticated()
                .pathMatchers("/*").permitAll()
                .pathMatchers("/assets/*").permitAll()
                .pathMatchers("/graphiql/*").permitAll()
                .pathMatchers("/api/g").permitAll()
                .pathMatchers(baseRoute + "/**").hasAnyRole(
                        Role.USER.name(), Role.MANAGER.name(), Role.ADMIN.name())
                .anyExchange().denyAll()

                .and()

                .addFilterAt(new JwtTokenAuthenticationFilter(jwtTokenProvider, cookieManager, userService), SecurityWebFiltersOrder.HTTP_BASIC)
                .exceptionHandling().authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED))
                .and()

                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
