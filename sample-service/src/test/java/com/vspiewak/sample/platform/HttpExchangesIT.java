package com.vspiewak.sample.platform;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath;
import com.vspiewak.sample.Containers;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.client.RestTestClient;

/**
 * Proves the service-starter HTTP flight recorder end-to-end : the endpoint is exposed by the
 * platform defaults, the repository is auto-configured, and a real request gets recorded.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@Import(Containers.class)
class HttpExchangesIT {

  @Autowired private RestTestClient client;

  @Test
  void shouldRecordHttpExchanges() {
    // given
    client.get().uri("/orders/v1/orders").exchange();

    // when
    var result = client.get().uri("/actuator/httpexchanges").exchange().returnResult(String.class);

    // then
    assertThat(result.getStatus().value()).isEqualTo(200);
    List<String> uris = JsonPath.read(result.getResponseBody(), "$.exchanges[*].request.uri");
    assertThat(uris).anySatisfy(uri -> assertThat(uri).contains("/orders/v1/orders"));
  }
}
