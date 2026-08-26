package com.vspiewak.pavedroad.web;

import org.slf4j.MDC;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Stamps every RFC 9457 problem response with the service it came from, so an error is
 * self-identifying wherever it lands — the same {@code spring.application.name} that is on the
 * banner and on every log line.
 *
 * <p>{@code traceId} is added only when something has put one in the MDC : an empty field would be
 * worse than no field, and the slot fills itself the day a tracing starter is on the classpath.
 *
 * <p>Declared as {@code ResponseBodyAdvice<Object>} on purpose. Spring hands every response body to
 * every advice it selects — the generic parameter is <em>not</em> a filter — so narrowing it to
 * {@code ProblemDetail} would only move the type check to a {@code ClassCastException} on the first
 * successful response. The {@code instanceof} below is the filter.
 */
@ControllerAdvice
public class PlatformProblemDetailAdvice implements ResponseBodyAdvice<Object> {

  static final String SERVICE = "service";
  static final String TRACE_ID = "traceId";

  private final String applicationName;

  public PlatformProblemDetailAdvice(String applicationName) {
    this.applicationName = applicationName;
  }

  @Override
  public boolean supports(
      MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
    return true;
  }

  @Override
  public Object beforeBodyWrite(
      Object body,
      MethodParameter returnType,
      MediaType selectedContentType,
      Class<? extends HttpMessageConverter<?>> converterType,
      ServerHttpRequest request,
      ServerHttpResponse response) {

    if (!(body instanceof ProblemDetail problem)) {
      return body;
    }
    problem.setProperty(SERVICE, applicationName);

    String traceId = MDC.get(TRACE_ID);
    if (traceId != null && !traceId.isBlank()) {
      problem.setProperty(TRACE_ID, traceId);
    }
    return problem;
  }
}
