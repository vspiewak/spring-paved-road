package com.vspiewak.pavedroad.env;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

/** Proves both post processors are picked up from META-INF/spring.factories at boot time. */
@SpringBootTest(classes = ServiceStarterIT.TestApplication.class)
class ServiceStarterIT {

  @SpringBootConfiguration
  @EnableAutoConfiguration
  static class TestApplication {}

  @Autowired private Environment environment;

  @Test
  void defaultsAndOverridesShouldBeLoadedThroughSpringFactories() {
    assertThat(environment.getProperty("management.endpoints.web.exposure.include"))
        .isEqualTo("health,info");
    assertThat(environment.getProperty("management.endpoint.env.show-values")).isEqualTo("never");
  }
}
