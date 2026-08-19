package com.vspiewak.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;

/**
 * The local dev loop : {@code ./mvnw spring-boot:test-run} boots the service with a MongoDB
 * container and the {@code local} profile — so the auto-load seeds it from {@code
 * src/test/resources/mongo/import}.
 */
public class RunWithTestcontainers {

  @TestConfiguration(proxyBeanMethods = false)
  @ImportTestcontainers(Containers.class)
  public static class TestConfig {}

  public static void main(String[] args) {
    SpringApplication.from(SampleServiceApplication::main)
        .with(TestConfig.class)
        .withAdditionalProfiles("local")
        .run(args);
  }
}
