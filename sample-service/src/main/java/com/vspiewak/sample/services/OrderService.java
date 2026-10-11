package com.vspiewak.sample.services;

import com.vspiewak.sample.domain.Order;
import com.vspiewak.sample.repositories.OrderRepository;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

  private final OrderRepository repository;

  @Observed(name = "orders.order.list", contextualName = "orders.order.list")
  public List<Order> findAll() {
    return repository.findAll();
  }

  @Observed(name = "orders.order.get", contextualName = "orders.order.get")
  public Optional<Order> findByOrderId(String orderId) {
    return repository.findByOrderId(orderId);
  }
}
