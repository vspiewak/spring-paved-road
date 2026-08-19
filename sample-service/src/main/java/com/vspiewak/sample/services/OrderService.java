package com.vspiewak.sample.services;

import com.vspiewak.sample.domain.Order;
import com.vspiewak.sample.repositories.OrderRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

  private final OrderRepository repository;

  public List<Order> findAll() {
    return repository.findAll();
  }

  public Optional<Order> findByOrderId(String orderId) {
    return repository.findByOrderId(orderId);
  }
}
