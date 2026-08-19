package com.vspiewak.sample;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.mongodb.MongoDBContainer;

public interface Containers {

  @Container @ServiceConnection MongoDBContainer mongoContainer = new MongoDBContainer("mongo:8.0");
}
