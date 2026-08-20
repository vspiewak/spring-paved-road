package com.vspiewak.sample.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.vspiewak.sample.Containers;
import com.vspiewak.sample.domain.Order;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.DockerClientFactory;

/** The data slice : a real MongoDB, but only the repository layer — no web, no service. */
@DataMongoTest
@Import(Containers.class)
class OrderRepositoryIT {

  @BeforeAll
  static void dockerRequired() {
    assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker is required");
  }

  @Autowired private OrderRepository repository;

  @Test
  void shouldFindByOrderId() {
    repository.save(new Order(null, "42", 7));

    assertThat(repository.findByOrderId("42"))
        .hasValueSatisfying(order -> assertThat(order.amount()).isEqualTo(7));
  }

  @Test
  void shouldReturnEmptyForUnknownOrderId() {
    assertThat(repository.findByOrderId("nope")).isEmpty();
  }
}
