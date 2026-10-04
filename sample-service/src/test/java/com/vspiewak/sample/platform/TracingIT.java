package com.vspiewak.sample.platform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.jayway.jsonpath.JsonPath;
import com.vspiewak.sample.Containers;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.micrometer.tracing.test.autoconfigure.AutoConfigureTracing;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.client.RestTestClient;

/**
 * Proves the platform tracing end-to-end : one request is one trace, from the HTTP server span
 * through the {@code @Observed} service down to the MongoDB driver — and the health probes stay out
 * of it. Spans are captured in memory, exactly as an OTLP collector would receive them.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@AutoConfigureTracing
@Import({Containers.class, TracingIT.CapturedSpans.class})
class TracingIT {

  @Autowired private RestTestClient client;

  @Autowired private InMemorySpanExporter spans;

  @BeforeEach
  void setUp() {
    spans.reset();
  }

  @Test
  void shouldTraceARequestFromHttpDownToMongo() {
    // when
    client.get().uri("/orders/v1/orders").exchange().expectStatus().isOk();

    // then
    var server = awaitServerSpan("/orders/v1/orders");
    var service = onlySpan(server, "OrderService#findAll");
    var mongo = childrenOf(service);
    assertThat(mongo)
        .isNotEmpty()
        .allSatisfy(
            span -> {
              assertThat(span.getKind()).isEqualTo(SpanKind.CLIENT);
              assertThat(span.getName()).startsWith("find ");
            });
  }

  @Test
  void shouldStampTheTraceIdOnTheErrorContract() {
    // when
    var body =
        client
            .get()
            .uri("/orders/v1/orders/999")
            .exchange()
            .expectStatus()
            .isNotFound()
            .returnResult(String.class)
            .getResponseBody();

    // then
    var server = awaitServerSpan("/orders/v1/orders/{orderId}");
    assertThat(JsonPath.<String>read(body, "$.traceId")).isEqualTo(server.getTraceId());
  }

  @Test
  void shouldKeepHealthProbesOutOfTheTraces() {
    // given : the probe hits MongoDB too, through the health indicator
    client.get().uri("/actuator/health").exchange().expectStatus().isOk();

    // when : a business request, so we know when the exporter has caught up
    client.get().uri("/orders/v1/orders").exchange().expectStatus().isOk();
    var server = awaitServerSpan("/orders/v1/orders");

    // then : every exported span belongs to the business trace
    assertThat(spans.getFinishedSpanItems())
        .allSatisfy(
            span ->
                assertThat(span.getTraceId()).as(span.getName()).isEqualTo(server.getTraceId()));
  }

  private SpanData awaitServerSpan(String route) {
    return await()
        .atMost(Duration.ofSeconds(5))
        .until(
            () ->
                spans.getFinishedSpanItems().stream()
                    .filter(span -> span.getKind() == SpanKind.SERVER)
                    .filter(span -> span.getName().endsWith(" " + route))
                    .findFirst(),
            java.util.Optional::isPresent)
        .orElseThrow();
  }

  private SpanData onlySpan(SpanData parent, String name) {
    var children = childrenOf(parent);
    assertThat(children).extracting(SpanData::getName).containsExactly(name);
    return children.getFirst();
  }

  private List<SpanData> childrenOf(SpanData parent) {
    return spans.getFinishedSpanItems().stream()
        .filter(span -> span.getParentSpanId().equals(parent.getSpanId()))
        .toList();
  }

  @TestConfiguration(proxyBeanMethods = false)
  static class CapturedSpans {

    @Bean
    InMemorySpanExporter inMemorySpanExporter() {
      return InMemorySpanExporter.create();
    }

    @Bean
    SpanProcessor inMemorySpanProcessor(InMemorySpanExporter exporter) {
      return SimpleSpanProcessor.create(exporter);
    }
  }
}
