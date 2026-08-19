package com.vspiewak.sample.repositories;

import com.vspiewak.sample.domain.Order;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderRepository extends MongoRepository<Order, String> {

  Optional<Order> findByOrderId(String orderId);
}
