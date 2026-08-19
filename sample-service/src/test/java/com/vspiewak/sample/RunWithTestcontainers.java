package com.vspiewak.sample;

import org.springframework.boot.SpringApplication;

/**
 * The local dev loop : {@code ./mvnw spring-boot:test-run} boots the service with a MongoDB
 * container and the {@code local} profile — so the auto-load seeds it from {@code
 * src/test/resources/mongo/import}.
 */
public class RunWithTestcontainers {

  public static void main(String[] args) {
    SpringApplication.from(SampleServiceApplication::main)
        .with(Containers.class)
        .withAdditionalProfiles("local")
        .run(args);
  }
}
