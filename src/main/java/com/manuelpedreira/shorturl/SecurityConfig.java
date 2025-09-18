package com.manuelpedreira.shorturl;

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
                        .requestMatchers(HttpMethod.GET, "/{shortCode:[a-zA-Z0-9]{7}}").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler));
        // .oauth2Login(oauth2 -> oauth2.defaultSuccessUrl("/dashboard", true))
        // .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }
}
