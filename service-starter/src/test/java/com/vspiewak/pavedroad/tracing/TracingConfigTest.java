package com.vspiewak.pavedroad.tracing;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationPredicate;
import io.opentelemetry.sdk.trace.SpanProcessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.http.server.observation.ServerRequestObservationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class TracingConfigTest {

  private final WebApplicationContextRunner runner =
      new WebApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(TracingConfig.class));

  @Test
  void shouldNotTraceActuatorRequests() {
    runner.run(
        context -> {
          // given
          var filter = context.getBean("actuatorObservationFilter", ObservationPredicate.class);

          // when / then
          assertThat(filter.test("http.server.requests", serverRequest("", "/actuator/health")))
              .isFalse();
          assertThat(filter.test("http.server.requests", serverRequest("", "/actuator"))).isFalse();
        });
  }

  @Test
  void shouldTraceEverythingElse() {
    runner.run(
        context -> {
          // given
          var filter = context.getBean("actuatorObservationFilter", ObservationPredicate.class);

          // when / then
          assertThat(filter.test("http.server.requests", serverRequest("", "/orders/v1/orders")))
              .isTrue();
          assertThat(filter.test("http.server.requests", serverRequest("", "/actuators"))).isTrue();
          assertThat(filter.test("my.service.span", new Observation.Context())).isTrue();
        });
  }

  @Test
  void shouldNotTraceWhatAnActuatorRequestCauses() {
    runner.run(
        context -> {
          // given : a health indicator about to query the database, on the request's thread
          var filter = context.getBean("actuatorObservationFilter", ObservationPredicate.class);
          bindRequest("/actuator/health");

          // when / then
          assertThat(filter.test("mongodb.operation", new Observation.Context())).isFalse();
        });
  }

  @Test
  void shouldTraceWhatABusinessRequestCauses() {
    runner.run(
        context -> {
          // given
          var filter = context.getBean("actuatorObservationFilter", ObservationPredicate.class);
          bindRequest("/orders/v1/orders");

          // when / then
          assertThat(filter.test("mongodb.operation", new Observation.Context())).isTrue();
        });
  }

  @Test
  void shouldFollowTheActuatorBasePathAndTheContextPath() {
    runner
        .withPropertyValues("management.endpoints.web.base-path=/manage")
        .run(
            context -> {
              // given
              var filter = context.getBean("actuatorObservationFilter", ObservationPredicate.class);

              // when / then
              assertThat(filter.test("http.server.requests", serverRequest("/api", "/manage/info")))
                  .isFalse();
              assertThat(
                      filter.test("http.server.requests", serverRequest("/api", "/actuator/info")))
                  .isTrue();
            });
  }

  @Test
  void shouldTraceActuatorRequestsWhenAskedTo() {
    runner
        .withPropertyValues("platform.tracing.actuator.enabled=true")
        .run(context -> assertThat(context).doesNotHaveBean("actuatorObservationFilter"));
  }

  @Test
  void shouldNotFilterOutsideAServletApplication() {
    new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(TracingConfig.class))
        .run(context -> assertThat(context).doesNotHaveBean("actuatorObservationFilter"));
  }

  @Test
  void shouldNotPrintSpansByDefault() {
    runner.run(context -> assertThat(context).doesNotHaveBean(SpanProcessor.class));
  }

  @Test
  void shouldPrintSpansWhenAskedTo() {
    runner
        .withPropertyValues("platform.tracing.exporter.logging.enabled=true")
        .run(context -> assertThat(context).hasSingleBean(SpanProcessor.class));
  }

  @AfterEach
  void unbindRequest() {
    RequestContextHolder.resetRequestAttributes();
  }

  private static void bindRequest(String path) {
    RequestContextHolder.setRequestAttributes(
        new ServletRequestAttributes(new MockHttpServletRequest("GET", path)));
  }

  private static ServerRequestObservationContext serverRequest(String contextPath, String path) {
    var request = new MockHttpServletRequest("GET", contextPath + path);
    request.setContextPath(contextPath);
    return new ServerRequestObservationContext(request, new MockHttpServletResponse());
  }
}
