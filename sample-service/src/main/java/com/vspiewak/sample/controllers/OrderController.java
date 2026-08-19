package com.vspiewak.sample.controllers;

import com.vspiewak.sample.domain.Order;
import com.vspiewak.sample.services.OrderService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/orders/v1")
@RequiredArgsConstructor
public class OrderController {

  private final OrderService service;

  @GetMapping("/orders")
  public List<Order> getOrders() {
    return service.findAll();
  }

  @GetMapping("/orders/{orderId}")
  public Order getOrder(@PathVariable String orderId) {
    return service
        .findByOrderId(orderId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
  }
}
