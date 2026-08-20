package com.vspiewak.sample.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.vspiewak.sample.Containers;
import com.vspiewak.sample.domain.Order;
import com.vspiewak.sample.repositories.OrderRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.DockerClientFactory;

/** Full e2e : controller → service → repository → a real MongoDB. Each test seeds its own data. */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@Import(Containers.class)
class OrderControllerIT {

  @BeforeAll
  static void dockerRequired() {
    assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker is required");
  }

  @Autowired private RestTestClient client;

  @Autowired private OrderRepository repository;

  @BeforeEach
  void setUp() {
    repository.deleteAll();
  }

  @Test
  void shouldReturnAllOrders() {
    // given
    repository.save(new Order(null, "1", 42));
    repository.save(new Order(null, "2", 7));

    // when
    var response = client.get().uri("/orders/v1/orders").exchange();

    // then
    var orders = response.expectStatus().isOk().returnResult(Order[].class).getResponseBody();
    assertThat(orders).hasSize(2).extracting(Order::orderId).containsExactlyInAnyOrder("1", "2");
  }

  @Test
  void shouldReturnOneOrderByOrderId() {
    // given
    repository.save(new Order(null, "1", 42));

    // when
    var response = client.get().uri("/orders/v1/orders/1").exchange();

    // then
    var order = response.expectStatus().isOk().returnResult(Order.class).getResponseBody();
    assertThat(order.amount()).isEqualTo(42);
  }

  @Test
  void shouldReturnNotFoundForUnknownOrder() {
    // when
    var response = client.get().uri("/orders/v1/orders/999").exchange();

    // then
    response.expectStatus().isNotFound();
  }
}
