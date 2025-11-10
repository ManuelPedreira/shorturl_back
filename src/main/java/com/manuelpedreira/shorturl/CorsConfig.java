package com.manuelpedreira.shorturl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {

  private final String CORS_SERVER;
  private final String VALIDATION_PATTERN;

  public CorsConfig(@Value("${cors.server}") String corsServer,
      @Value("${custom.url.validation.pattern}") String pattern) {
    CORS_SERVER = corsServer;
    VALIDATION_PATTERN = pattern;
  }

  @Bean
  public CorsFilter corsFilter() {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

    CorsConfiguration serverOnly = new CorsConfiguration();
    serverOnly.setAllowedOrigins(List.of(CORS_SERVER));
    serverOnly.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    serverOnly.setAllowedHeaders(List.of("*"));
    serverOnly.setAllowCredentials(true);

    CorsConfiguration publicAccess = new CorsConfiguration();
    publicAccess.addAllowedOriginPattern("*");
    publicAccess.setAllowedMethods(List.of("GET", "OPTIONS"));
    publicAccess.setAllowedHeaders(List.of("*"));
    publicAccess.setAllowCredentials(false);

    source.registerCorsConfiguration("/api/**", serverOnly);
    source.registerCorsConfiguration("{shortCode:" + VALIDATION_PATTERN + "}", publicAccess);

    return new CorsFilter(source);
  }
}