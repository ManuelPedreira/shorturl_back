package com.manuelpedreira.shorturl;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.manuelpedreira.shorturl.dto.ApiError;
import com.manuelpedreira.shorturl.dto.FieldValidationError;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  private final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
  private final MessageSource messageSource;

  public GlobalExceptionHandler(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  // 1) @RequestBody validation errors
  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {

    List<FieldValidationError> errors = ex.getBindingResult().getFieldErrors().stream()
        .map(fe -> toFieldValidationError(fe))
        .collect(Collectors.toList());

    ApiError body = new ApiError(
        Instant.now(),
        status.value(),
        "Validation Failed",
        "Validation failed for one or more fields",
        servletPath(request),
        errors);

    return handleExceptionInternal(ex, body, headers, HttpStatus.valueOf(status.value()), request);
  }

  // Helper: convert FieldError -> DTO (with i18n)
  private FieldValidationError toFieldValidationError(FieldError fe) {
    String message = messageSource.getMessage(fe, LocaleContextHolder.getLocale());
    return new FieldValidationError(fe.getField(), fe.getRejectedValue(), message);
  }

  // 2) Parameter / path validation (e.g. @PathVariable, @RequestParam when
  // @Validated present)
  @ExceptionHandler(ConstraintViolationException.class)
  protected ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
    List<FieldValidationError> errors = ex.getConstraintViolations().stream()
        .map(cv -> new FieldValidationError(cv.getPropertyPath().toString(), null, cv.getMessage()))
        .collect(Collectors.toList());

    ApiError body = new ApiError(Instant.now(), 400, "Constraint Violation",
        "Request parameters validation failed", servletPath(request), errors);

    return ResponseEntity.badRequest().body(body);
  }

  // 3) Malformed JSON
  @Override
  protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
      HttpHeaders headers, HttpStatusCode status, WebRequest request) {

    ApiError body = new ApiError(Instant.now(), status.value(),
        "Malformed JSON request",
        ex.getMostSpecificCause() == null ? ex.getMessage() : ex.getMostSpecificCause().getMessage(),
        servletPath(request), null);

    return handleExceptionInternal(ex, body, headers, HttpStatus.valueOf(status.value()), request);
  }

  // 4) DB conflicts (unique constraints)
  @ExceptionHandler(DataIntegrityViolationException.class)
  protected ResponseEntity<Object> handleDataIntegrity(DataIntegrityViolationException ex, WebRequest request) {
    log.warn("Data integrity violation: {}", ex.getMessage());
    ApiError body = new ApiError(Instant.now(), 409, "Data integrity violation",
        "Conflict saving entity (unique constraint?)", servletPath(request), null);
    return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
  }

  // 5) Fallback (internal server error) — no detalles sensibles al cliente
  @ExceptionHandler(Exception.class)
  protected ResponseEntity<Object> handleAll(Exception ex, WebRequest request) {
    log.error("Unhandled exception", ex);
    ApiError body = new ApiError(Instant.now(), 500, "Internal Server Error",
        "An unexpected error occurred", servletPath(request), null);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
  }

  private String servletPath(WebRequest request) {
    if (request instanceof ServletWebRequest sw) {
      return sw.getRequest().getRequestURI();
    }
    return null;
  }
}
