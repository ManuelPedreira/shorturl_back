package com.manuelpedreira.shorturl.error;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

// import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
// import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
// import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Returns ProblemDetail (application/problem+json) para 401 Unauthorized.
public class ProblemAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private static final Logger log = LoggerFactory.getLogger(ProblemAuthenticationEntryPoint.class);

  private final ObjectMapper mapper;

  public ProblemAuthenticationEntryPoint(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException authException) throws IOException, ServletException {

    if (response.isCommitted()) {
      log.warn("Response already committed, cannot write ProblemDetail");
      return;
    }
    /* 
    // Si viene de OAuth2 (Bearer token), extraemos el error para la cabecera
    // WWW-Authenticate

    if (authException instanceof OAuth2AuthenticationException oauthEx) {
      OAuth2Error err = oauthEx.getError();
      String header = buildWwwAuthenticateHeader(err);
      response.setHeader(HttpHeaders.WWW_AUTHENTICATE, header);
      // No incluir tokens u otros datos sensibles en detail
    } */

    ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
    problemDetail.setTitle("Unauthorized");
    problemDetail.setDetail("Authentication is required to access this resource.");
    problemDetail.setInstance(URI.create(request.getRequestURI()));
    problemDetail.setProperty("timestamp", Instant.now().toString()); // extra field opcional

    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    mapper.writeValue(response.getWriter(), problemDetail);
  }
  /* 
  // Construye un valor seguro para WWW-Authenticate: p. ej. Bearer
  // error="invalid_token", error_description="..."

  private String buildWwwAuthenticateHeader(OAuth2Error err) {
    StringBuilder stringBuilder = new StringBuilder("Bearer");
    String code = sanitizeHeaderToken(err.getErrorCode());
    String desc = sanitizeHeaderToken(err.getDescription());
    if (code != null && !code.isBlank()) {
      stringBuilder.append(" error=\"").append(code).append("\"");
    }
    if (desc != null && !desc.isBlank()) {
      stringBuilder.append(", error_description=\"").append(desc).append("\"");
    }
    return stringBuilder.toString();
  }

  // Quitar comillas / saltos de línea para evitar romper la cabecera
  private String sanitizeHeaderToken(String v) {
    if (v == null)
      return null;
    return v.replace("\"", "'").replaceAll("[\\r\\n]", " ");
  } */
}