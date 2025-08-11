package com.manuelpedreira.shorturl;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.GET, "/{shortCode:[a-zA-Z0-9]{7}}").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/links").permitAll()
            .anyRequest().authenticated());
        //.oauth2Login(oauth2 -> oauth2.defaultSuccessUrl("/dashboard", true))
        //.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

    return http.build();
  }
}
