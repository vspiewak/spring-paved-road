package com.vspiewak.pavedroad.env;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;

class SpringOverrideEnvironmentPostProcessorTest {

  private final SpringOverrideEnvironmentPostProcessor postProcessor =
      new SpringOverrideEnvironmentPostProcessor();
  private final SpringApplication application = new SpringApplication();

  @Test
  void shouldLoadSharedOverrides() {
    var environment = new MockEnvironment();

    postProcessor.postProcessEnvironment(environment, application);

    assertThat(environment.getProperty("management.endpoint.env.show-values")).isEqualTo("never");
  }

  @Test
  void shouldOverrideExistingProperties() {
    var environment = new MockEnvironment();
    environment.setProperty("management.endpoint.env.show-values", "always");

    postProcessor.postProcessEnvironment(environment, application);

    assertThat(environment.getProperty("management.endpoint.env.show-values")).isEqualTo("never");
  }

  @Test
  void shouldLoadProfileSpecificOverrides() {
    var environment = new MockEnvironment();
    environment.setActiveProfiles("dev");

    postProcessor.postProcessEnvironment(environment, application);

    assertThat(environment.getProperty("paved-road.database.host")).isEqualTo("dev-db.internal");
  }

  @Test
  void shouldNotLoadProfilePropertiesWhenNoProfileIsActive() {
    var environment = new MockEnvironment();

    postProcessor.postProcessEnvironment(environment, application);

    assertThat(environment.getProperty("paved-road.database.host")).isNull();
  }

  @Test
  void shouldNotFailWhenProfileHasNoOverrideFile() {
    var environment = new MockEnvironment();
    environment.setActiveProfiles("unknown");

    postProcessor.postProcessEnvironment(environment, application);

    assertThat(environment.getProperty("management.endpoint.env.show-values")).isEqualTo("never");
  }

  @Test
  void shouldLoadDevOverrideWhenMultipleProfilesIncludingDev() {
    var environment = new MockEnvironment();
    environment.setActiveProfiles("unknown", "dev");

    postProcessor.postProcessEnvironment(environment, application);

    assertThat(environment.getProperty("management.endpoint.env.show-values")).isEqualTo("never");
    assertThat(environment.getProperty("paved-road.database.host")).isEqualTo("dev-db.internal");
  }
}
