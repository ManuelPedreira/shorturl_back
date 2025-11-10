package com.manuelpedreira.shorturl.websocket;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class CookieWSHandshakeInterceptor implements HandshakeInterceptor {

  private final String jwtWsSecret;
  private static final Logger logger = LoggerFactory.getLogger(CookieWSHandshakeInterceptor.class);

  public CookieWSHandshakeInterceptor(@Value("${jwt.ws.secret}") String jwtWsSecret) {
    this.jwtWsSecret = jwtWsSecret;
  }

  @Override
  public boolean beforeHandshake(@NonNull ServerHttpRequest request,
      @NonNull ServerHttpResponse response,
      @NonNull WebSocketHandler wsHandler,
      @NonNull Map<String, Object> attributes) {

    List<String> cookies = request.getHeaders().get("Cookie");
    if (cookies == null)
      return false;

    String cookieHeader = String.join("; ", cookies);
    String token = extractCookieValue(cookieHeader, "WSAccess");
    if (token == null)
      return false;

    try {
      var key = Keys.hmacShaKeyFor(jwtWsSecret.getBytes(StandardCharsets.UTF_8));
      Claims claims = Jwts.parserBuilder()
          .setSigningKey(key)
          .build()
          .parseClaimsJws(token)
          .getBody();

      String code = claims.get("code", String.class);
      attributes.put("JWTshortCode", code);
      return true;

    } catch (Exception e) {
      logger.warn("Invalid JWT : " + e.getMessage());
    }

    return false;
  }

  private String extractCookieValue(String cookieHeader, String name) {
    for (String part : cookieHeader.split("; ")) {
      if (part.startsWith(name + "=")) {
        return part.substring(name.length() + 1);
      }
    }
    return null;
  }

  @Override
  public void afterHandshake(@NonNull ServerHttpRequest request,
      @NonNull ServerHttpResponse response,
      @NonNull WebSocketHandler wsHandler,
      @Nullable Exception exception) {
  }
}