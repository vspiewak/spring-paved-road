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
    // given
    var environment = new MockEnvironment();

    // when
    postProcessor.postProcessEnvironment(environment, application);

    // then
    assertThat(environment.getProperty("management.endpoint.env.show-values")).isEqualTo("never");
  }

  @Test
  void shouldOverrideExistingProperties() {
    // given
    var environment = new MockEnvironment();
    environment.setProperty("management.endpoint.env.show-values", "always");

    // when
    postProcessor.postProcessEnvironment(environment, application);

    // then
    assertThat(environment.getProperty("management.endpoint.env.show-values")).isEqualTo("never");
  }

  @Test
  void shouldLoadProfileSpecificOverrides() {
    // given
    var environment = new MockEnvironment();
    environment.setActiveProfiles("dev");

    // when
    postProcessor.postProcessEnvironment(environment, application);

    // then
    assertThat(environment.getProperty("platform.database.host")).isEqualTo("dev-db.internal");
  }

  @Test
  void shouldNotLoadProfilePropertiesWhenNoProfileIsActive() {
    // given
    var environment = new MockEnvironment();

    // when
    postProcessor.postProcessEnvironment(environment, application);

    // then
    assertThat(environment.getProperty("platform.database.host")).isNull();
  }

  @Test
  void shouldNotFailWhenProfileHasNoOverrideFile() {
    // given
    var environment = new MockEnvironment();
    environment.setActiveProfiles("unknown");

    // when
    postProcessor.postProcessEnvironment(environment, application);

    // then
    assertThat(environment.getProperty("management.endpoint.env.show-values")).isEqualTo("never");
  }

  @Test
  void shouldLoadDevOverrideWhenMultipleProfilesIncludingDev() {
    // given
    var environment = new MockEnvironment();
    environment.setActiveProfiles("unknown", "dev");

    // when
    postProcessor.postProcessEnvironment(environment, application);

    // then
    assertThat(environment.getProperty("management.endpoint.env.show-values")).isEqualTo("never");
    assertThat(environment.getProperty("platform.database.host")).isEqualTo("dev-db.internal");
  }
}
