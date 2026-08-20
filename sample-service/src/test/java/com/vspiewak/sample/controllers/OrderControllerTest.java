package com.vspiewak.sample.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.vspiewak.sample.domain.Order;
import com.vspiewak.sample.services.OrderService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/** The controller slice : no server, no MongoDB — the service is mocked. */
@WebMvcTest(OrderController.class)
class OrderControllerTest {

  @Autowired private MockMvcTester mvc;

  @MockitoBean private OrderService service;

  @Test
  void shouldReturnOrdersAsJson() {
    when(service.findAll()).thenReturn(List.of(new Order("id-1", "1", 42)));

    assertThat(mvc.get().uri("/orders/v1/orders"))
        .hasStatusOk()
        .bodyJson()
        .extractingPath("$[0].orderId")
        .isEqualTo("1");
  }

  @Test
  void shouldReturnOneOrderAsJson() {
    when(service.findByOrderId("1")).thenReturn(Optional.of(new Order("id-1", "1", 42)));

    assertThat(mvc.get().uri("/orders/v1/orders/1"))
        .hasStatusOk()
        .bodyJson()
        .extractingPath("$.amount")
        .isEqualTo(42);
  }

  @Test
  void shouldReturnNotFoundForUnknownOrder() {
    when(service.findByOrderId("999")).thenReturn(Optional.empty());

    assertThat(mvc.get().uri("/orders/v1/orders/999")).hasStatus(HttpStatus.NOT_FOUND);
  }
}
