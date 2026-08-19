package com.vspiewak.sample.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.vspiewak.sample.Containers;
import com.vspiewak.sample.domain.Order;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.DockerClientFactory;

/** The API over the auto-loaded data : controller → service → repository → seeded MongoDB. */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@ActiveProfiles("local")
@Import(Containers.class)
class OrderControllerTest {

  @BeforeAll
  static void dockerRequired() {
    assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker is required");
  }

  @Autowired private TestRestTemplate restTemplate;

  @Test
  void shouldReturnAllSeededOrders() {
    var response = restTemplate.getForEntity("/orders/v1/orders", Order[].class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody())
        .hasSize(2)
        .extracting(Order::orderId)
        .containsExactlyInAnyOrder("1", "2");
  }

  @Test
  void shouldReturnOneOrderByOrderId() {
    var response = restTemplate.getForEntity("/orders/v1/orders/1", Order.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().amount()).isEqualTo(42);
  }

  @Test
  void shouldReturnNotFoundForUnknownOrder() {
    var response = restTemplate.getForEntity("/orders/v1/orders/999", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }
}
