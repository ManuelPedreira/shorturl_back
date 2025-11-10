package com.manuelpedreira.shorturl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manuelpedreira.shorturl.error.ProblemAccessDeniedHandler;
import com.manuelpedreira.shorturl.error.ProblemAuthenticationEntryPoint;

@Configuration
public class SecurityConfig {

    private final String PATTERN;

    public SecurityConfig(@Value("${custom.url.validation.pattern}") String pattern) {
        PATTERN = pattern;
    }

    @Bean
    public ProblemAuthenticationEntryPoint problemAuthenticationEntryPoint(ObjectMapper mapper) {
        return new ProblemAuthenticationEntryPoint(mapper);
    }

    @Bean
    public ProblemAccessDeniedHandler problemAccessDeniedHandler(ObjectMapper mapper) {
        return new ProblemAccessDeniedHandler(mapper);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
            ProblemAuthenticationEntryPoint authEntryPoint,
            ProblemAccessDeniedHandler accessDeniedHandler) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/{shortCode:" + PATTERN + "}").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api").permitAll()
                        .requestMatchers(HttpMethod.GET, "/ws/**").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler));
        // .oauth2Login(oauth2 -> oauth2.defaultSuccessUrl("/dashboard", true))
        // .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }
}
