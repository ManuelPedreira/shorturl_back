package com.manuelpedreira.shorturl.error;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ProblemDetailAdvice extends ResponseEntityExceptionHandler {

  private final Logger log = LoggerFactory.getLogger(ProblemDetailAdvice.class);

  // 1) Validation errors from @Valid @RequestBody
  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException expection,
      HttpHeaders headers, HttpStatusCode status, WebRequest request) {

    ProblemDetail problemDetail = ProblemDetail.forStatus(status);
    problemDetail.setTitle("Validation failed");
    problemDetail.setDetail("Validation failed for one or more fields");
    problemDetail.setInstance(URI.create(((ServletWebRequest) request).getRequest().getRequestURI()));

    List<Map<String, Object>> invalidParams = expection.getBindingResult().getFieldErrors().stream()
        .map(e -> fieldErrorToMap(e))
        .collect(Collectors.toList());

    problemDetail.setProperty("invalid-params", invalidParams); // campo adicional según RFC (propiedades adicionales
                                                                // permitidas)

    return ResponseEntity.status(status).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(problemDetail);
  }

  private Map<String, Object> fieldErrorToMap(FieldError fieldError) {
    return Map.of(
        "field", fieldError.getField(),
        "rejectedValue", fieldError.getRejectedValue(),
        "message", fieldError.getDefaultMessage());
  }

  // 2) Validation on @RequestParam / @PathVariable (Method validation ->
  // ConstraintViolationException)
  @ExceptionHandler(ConstraintViolationException.class)
  protected ResponseEntity<ProblemDetail> handleConstraintViolation(ConstraintViolationException exception,
      WebRequest request) {

    ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    problemDetail.setTitle("Parameter validation failed");
    problemDetail.setDetail("One or more request parameters are invalid");
    problemDetail.setInstance(URI.create(((ServletWebRequest) request).getRequest().getRequestURI()));

    var errs = exception.getConstraintViolations().stream()
        .map(constantViolation -> Map.<String, Object>of(
            "path", constantViolation.getPropertyPath().toString(),
            "message", constantViolation.getMessage()))
        .collect(Collectors.toList());

    problemDetail.setProperty("invalid-params", errs);
    return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_PROBLEM_JSON).body(problemDetail);
  }

  // 3) DB conflicts -> 409
  @ExceptionHandler(DataIntegrityViolationException.class)
  protected ResponseEntity<ProblemDetail> handleDataIntegrity(DataIntegrityViolationException ex,
      WebRequest request) {
    log.warn("Data integrity violation", ex);

    ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.CONFLICT);
    problemDetail.setTitle("Conflict");
    problemDetail.setDetail("Data integrity violation (unique constraint?)");
    problemDetail.setInstance(URI.create(((ServletWebRequest) request).getRequest().getRequestURI()));

    return ResponseEntity.status(HttpStatus.CONFLICT).contentType(MediaType.APPLICATION_PROBLEM_JSON)
        .body(problemDetail);
  }

  // 4) ResponseStatusException passthrough (puedes lanzar esto desde
  // services/controllers)
  @ExceptionHandler(ResponseStatusException.class)
  protected ResponseEntity<ProblemDetail> handleResponseStatus(ResponseStatusException exception, WebRequest request) {
    ProblemDetail problemDetail = ProblemDetail.forStatus(exception.getStatusCode());
    problemDetail
        .setTitle(exception.getReason() != null ? exception.getReason() : exception.getStatusCode().toString());
    problemDetail.setDetail(exception.getMessage());
    problemDetail.setInstance(URI.create(((ServletWebRequest) request).getRequest().getRequestURI()));
    return ResponseEntity.status(exception.getStatusCode()).contentType(MediaType.APPLICATION_PROBLEM_JSON)
        .body(problemDetail);
  }

  // 5) Fallback: no filtrar stacktrace al cliente — solo logging y message
  // general
  @ExceptionHandler(Exception.class)
  protected ResponseEntity<ProblemDetail> handleAll(Exception exception, WebRequest request) {
    log.error("Unhandled exception", exception);
    ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    problemDetail.setTitle("Internal Server Error");
    problemDetail.setDetail("An unexpected error occurred");
    problemDetail.setInstance(URI.create(((ServletWebRequest) request).getRequest().getRequestURI()));
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_PROBLEM_JSON)
        .body(problemDetail);
  }
}
