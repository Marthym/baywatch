package fr.ght1pc9kc.baywatch.security.infra.config;

import fr.ght1pc9kc.baywatch.security.api.model.Role;
import fr.ght1pc9kc.baywatch.security.domain.ports.JwtTokenProvider;
import fr.ght1pc9kc.baywatch.security.infra.JwtTokenAuthenticationFilter;
import fr.ght1pc9kc.baywatch.security.infra.TokenCookieManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

@Slf4j
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfiguration {
    @Bean
    SecurityWebFilterChain springSecurityFilterChain(
            ServerHttpSecurity http, JwtTokenProvider jwtTokenProvider,
            TokenCookieManager cookieManager, ReactiveUserDetailsService userService,
            ReactiveAuthenticationManager authenticationManager,
            @Value("${baywatch.base-route}") String baseRoute) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)

                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .authorizeExchange(ae -> {
                    ae.pathMatchers(HttpMethod.OPTIONS).permitAll();
                    ae.pathMatchers(HttpMethod.GET, "/actuator/**").hasRole(Role.ACTUATOR.name());
                    ae.pathMatchers(HttpMethod.GET, baseRoute + "/stats").permitAll();
                    ae.pathMatchers(HttpMethod.GET, baseRoute + "/news").permitAll();
                    ae.pathMatchers(HttpMethod.GET, baseRoute + "/feeds").permitAll();
                    ae.pathMatchers(HttpMethod.POST, baseRoute + "/auth/login").permitAll();
                    ae.pathMatchers(HttpMethod.PUT, baseRoute + "/auth/refresh").permitAll();
                    ae.pathMatchers(HttpMethod.DELETE, baseRoute + "/auth/logout").authenticated();
                    ae.pathMatchers("/*").permitAll();
                    ae.pathMatchers("/assets/*").permitAll();
                    ae.pathMatchers("/graphiql/*").permitAll();
                    ae.pathMatchers("/api/g").permitAll();
                    ae.pathMatchers(baseRoute + "/**").hasAnyRole(
                            Role.USER.name(), Role.MANAGER.name(), Role.ADMIN.name());
                    ae.anyExchange().denyAll();
                })

                .addFilterAt(new AuthenticationWebFilter(authenticationManager), SecurityWebFiltersOrder.HTTP_BASIC)
                .addFilterAt(new JwtTokenAuthenticationFilter(jwtTokenProvider, cookieManager, userService), SecurityWebFiltersOrder.AUTHENTICATION)

                .exceptionHandling(eh -> eh.authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)))

                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
