package com.vspiewak.pavedroad.env;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

/** The property ladder through a real config file, not a property set above every config file. */
class SpringDefaultLifecycleTest {

  private static final String SERVICE_CONFIG =
      "--spring.config.location=classpath:/service-config/application.yaml";

  @SpringBootConfiguration
  @EnableAutoConfiguration
  static class TestApplication {}

  private static SpringApplication application() {
    var application = new SpringApplication(TestApplication.class);
    application.setWebApplicationType(WebApplicationType.NONE);
    return application;
  }

  @Test
  void shouldLetTheServiceConfigOverrideADefault() {
    // given
    var application = application();

    // when
    try (ConfigurableApplicationContext context = application.run(SERVICE_CONFIG)) {

      // then
      assertThat(context.getEnvironment().getProperty("management.endpoints.web.exposure.include"))
          .isEqualTo("health");
    }
  }

  @Test
  void shouldKeepTheDefaultsTheServiceConfigDoesNotSet() {
    // given
    var application = application();

    // when
    try (ConfigurableApplicationContext context = application.run(SERVICE_CONFIG)) {

      // then
      assertThat(context.getEnvironment().getProperty("spring.banner.location"))
          .isEqualTo("classpath:platform-banner.txt");
    }
  }

  @Test
  void shouldStillForceTheOverridesOverTheServiceConfig() {
    // given
    var application = application();

    // when
    try (ConfigurableApplicationContext context = application.run(SERVICE_CONFIG)) {

      // then
      assertThat(context.getEnvironment().getProperty("management.endpoint.env.show-values"))
          .isEqualTo("never");
    }
  }

  @Test
  void shouldKeepTheDefaultsAboveTheApplicationDefaultProperties() {
    // given
    var application = application();
    application.setDefaultProperties(Map.of("spring.banner.location", "classpath:other.txt"));

    // when
    try (ConfigurableApplicationContext context = application.run(SERVICE_CONFIG)) {

      // then
      assertThat(context.getEnvironment().getProperty("spring.banner.location"))
          .isEqualTo("classpath:platform-banner.txt");
    }
  }
}
