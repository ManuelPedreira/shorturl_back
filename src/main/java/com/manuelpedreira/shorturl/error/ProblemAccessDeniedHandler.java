package com.manuelpedreira.shorturl.error;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ProblemDetail;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * Returns ProblemDetail (application/problem+json) para 403 Forbidden.
 */
public class ProblemAccessDeniedHandler implements AccessDeniedHandler {

  private static final Logger log = LoggerFactory.getLogger(ProblemAccessDeniedHandler.class);

  private final ObjectMapper mapper;

  public ProblemAccessDeniedHandler(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response,
      AccessDeniedException accessDeniedException) throws IOException, ServletException {

    if (response.isCommitted()) {
      log.warn("Response already committed, cannot write ProblemDetail (AccessDenied)");
      return;
    }

    ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
    problemDetail.setTitle("Forbidden");
    problemDetail.setDetail("You do not have the necessary permissions to access this resource.");
    problemDetail.setInstance(URI.create(request.getRequestURI()));
    problemDetail.setProperty("timestamp", Instant.now().toString());

    response.setStatus(HttpStatus.FORBIDDEN.value());
    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    mapper.writeValue(response.getWriter(), problemDetail);
  }
}