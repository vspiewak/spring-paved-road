package com.vspiewak.sample.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.vspiewak.sample.Containers;
import com.vspiewak.sample.domain.Order;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.DockerClientFactory;

/** The API over the auto-loaded data : controller → service → repository → seeded MongoDB. */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@ActiveProfiles("local")
@Import(Containers.class)
class OrderControllerIT {

  @BeforeAll
  static void dockerRequired() {
    assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker is required");
  }

  @Autowired private RestTestClient client;

  @Test
  void shouldReturnAllSeededOrders() {
    var orders =
        client
            .get()
            .uri("/orders/v1/orders")
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(Order[].class)
            .getResponseBody();

    assertThat(orders).hasSize(2).extracting(Order::orderId).containsExactlyInAnyOrder("1", "2");
  }

  @Test
  void shouldReturnOneOrderByOrderId() {
    var order =
        client
            .get()
            .uri("/orders/v1/orders/1")
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(Order.class)
            .getResponseBody();

    assertThat(order.amount()).isEqualTo(42);
  }

  @Test
  void shouldReturnNotFoundForUnknownOrder() {
    client.get().uri("/orders/v1/orders/999").exchange().expectStatus().isNotFound();
  }
}
