package com.vspiewak.pavedroad.tracing;

import io.micrometer.observation.ObservationPredicate;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.observation.ServerRequestObservationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * The tracing Boot leaves to each service. Boot already traces every request, propagates W3C
 * headers and exports over OTLP once {@code management.opentelemetry.tracing.export.otlp.endpoint}
 * is set ; the platform defaults ({@code platform-default.yaml}) sample everything and turn
 * {@code @Observed} on. What is left, and lives here :
 *
 * <ul>
 *   <li>actuator requests are not traced, and neither is anything they cause : a health probe every
 *       few seconds — and the database calls its health indicators make — would otherwise bury the
 *       business traces. Opt back in with {@code platform.tracing.actuator.enabled=true}
 *   <li>spans can be printed to the console, no collector needed, with {@code
 *       platform.tracing.exporter.logging.enabled=true} — for the local dev loop
 * </ul>
 */
@AutoConfiguration
@ConditionalOnClass(ObservationPredicate.class)
public class TracingConfig {

  @Bean
  @ConditionalOnClass(LoggingSpanExporter.class)
  @ConditionalOnProperty(prefix = "platform.tracing.exporter.logging", name = "enabled")
  @ConditionalOnMissingBean(name = "loggingSpanProcessor")
  public SpanProcessor loggingSpanProcessor() {
    // simple, not batched : a span shows up the moment it ends, which is the point locally
    return SimpleSpanProcessor.create(LoggingSpanExporter.create());
  }

  @Configuration(proxyBeanMethods = false)
  @ConditionalOnClass(ServerRequestObservationContext.class)
  @ConditionalOnWebApplication(type = Type.SERVLET)
  static class ActuatorObservationConfig {

    @Bean
    @ConditionalOnProperty(
        prefix = "platform.tracing.actuator",
        name = "enabled",
        havingValue = "false",
        matchIfMissing = true)
    @ConditionalOnMissingBean(name = "actuatorObservationFilter")
    public ObservationPredicate actuatorObservationFilter(
        @Value("${management.endpoints.web.base-path:/actuator}") String actuatorBasePath) {
      // the request's own observation starts before the request is bound to its thread, every
      // other one (the health indicator's database calls...) runs while it is : dropping only the
      // former would leave the latter as orphan root traces
      return (name, context) -> {
        var request =
            context instanceof ServerRequestObservationContext server
                ? server.getCarrier()
                : currentRequest();
        return request == null || !isUnder(request, actuatorBasePath);
      };
    }

    private static HttpServletRequest currentRequest() {
      return RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes current
          ? current.getRequest()
          : null;
    }

    private static boolean isUnder(HttpServletRequest request, String basePath) {
      var path = request.getRequestURI().substring(request.getContextPath().length());
      return path.equals(basePath) || path.startsWith(basePath + "/");
    }
  }
}
