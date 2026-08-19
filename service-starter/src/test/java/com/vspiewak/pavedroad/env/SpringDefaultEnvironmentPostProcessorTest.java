package com.vspiewak.pavedroad.env;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;

class SpringDefaultEnvironmentPostProcessorTest {

  private final SpringDefaultEnvironmentPostProcessor postProcessor =
      new SpringDefaultEnvironmentPostProcessor();
  private final SpringApplication application = new SpringApplication();

  @Test
  void shouldLoadDefaults() {
    var environment = new MockEnvironment();

    postProcessor.postProcessEnvironment(environment, application);

    assertThat(environment.getProperty("management.endpoints.web.exposure.include"))
        .isEqualTo("health,info");
  }

  @Test
  void applicationPropertiesShouldWinOverDefaults() {
    var environment = new MockEnvironment();
    environment.setProperty("management.endpoints.web.exposure.include", "*");

    postProcessor.postProcessEnvironment(environment, application);

    assertThat(environment.getProperty("management.endpoints.web.exposure.include")).isEqualTo("*");
  }
}
