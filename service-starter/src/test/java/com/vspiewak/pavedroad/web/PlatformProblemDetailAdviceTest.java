package com.vspiewak.pavedroad.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

class PlatformProblemDetailAdviceTest {

  private final PlatformProblemDetailAdvice advice =
      new PlatformProblemDetailAdvice("sample-service");

  @AfterEach
  void clearMdc() {
    MDC.clear();
  }

  private Object write(Object body) {
    return advice.beforeBodyWrite(body, null, null, null, null, null);
  }

  @Test
  void everyProblemNamesTheServiceItCameFrom() {
    var problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);

    assertThat(((ProblemDetail) write(problem)).getProperties())
        .containsEntry(PlatformProblemDetailAdvice.SERVICE, "sample-service");
  }

  @Test
  void theTraceSlotStaysAbsentUntilSomethingFillsIt() {
    var problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);

    assertThat(((ProblemDetail) write(problem)).getProperties())
        .doesNotContainKey(PlatformProblemDetailAdvice.TRACE_ID);
  }

  @Test
  void theTraceSlotIsFilledFromTheMdcWhenThereIsOne() {
    MDC.put(PlatformProblemDetailAdvice.TRACE_ID, "abc123");
    var problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);

    assertThat(((ProblemDetail) write(problem)).getProperties())
        .containsEntry(PlatformProblemDetailAdvice.TRACE_ID, "abc123");
  }

  @Test
  void anyOtherResponseBodyPassesThroughUntouched() {
    // Spring hands EVERY response body to the advice — a narrower generic parameter would not
    // filter, it would only turn this into a ClassCastException on the first successful response
    var body = new Object();

    assertThatCode(() -> assertThat(write(body)).isSameAs(body)).doesNotThrowAnyException();
    assertThat(write(null)).isNull();
  }
}
