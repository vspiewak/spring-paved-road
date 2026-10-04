package com.vspiewak.pavedroad.mongo;

import static org.assertj.core.api.Assertions.assertThat;

import com.mongodb.MongoClientSettings;
import com.mongodb.observability.micrometer.MicrometerObservabilitySettings;
import io.micrometer.observation.ObservationRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.mongodb.autoconfigure.MongoClientSettingsBuilderCustomizer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

class MongoTracingConfigTest {

  private final ApplicationContextRunner runner =
      new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(MongoTracingConfig.class))
          .withBean(ObservationRegistry.class, ObservationRegistry::create);

  @Test
  void shouldHandTheObservationRegistryToTheDriver() {
    runner.run(
        context -> {
          // given
          var builder = MongoClientSettings.builder();

          // when
          context.getBean(MongoClientSettingsBuilderCustomizer.class).customize(builder);

          // then
          assertThat(builder.build().getObservabilitySettings())
              .isInstanceOfSatisfying(
                  MicrometerObservabilitySettings.class,
                  settings -> {
                    assertThat(settings.getObservationRegistry())
                        .isSameAs(context.getBean(ObservationRegistry.class));
                    assertThat(settings.isEnableCommandPayloadTracing()).isFalse();
                  });
        });
  }

  @Test
  void shouldBeDisabledByProperty() {
    runner
        .withPropertyValues("platform.mongo.tracing.enabled=false")
        .run(context -> assertThat(context).doesNotHaveBean("mongoTracingCustomizer"));
  }

  @Test
  void shouldBackOffWithoutAnObservationRegistry() {
    new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(MongoTracingConfig.class))
        .run(context -> assertThat(context).doesNotHaveBean("mongoTracingCustomizer"));
  }

  @Test
  void shouldBackOffWhenServiceDefinesItsOwnCustomizer() {
    runner
        .withUserConfiguration(CustomTracingConfig.class)
        .run(
            context -> {
              // given
              var builder = MongoClientSettings.builder();

              // when
              context
                  .getBean("mongoTracingCustomizer", MongoClientSettingsBuilderCustomizer.class)
                  .customize(builder);

              // then
              assertThat(builder.build().getObservabilitySettings()).isNull();
            });
  }

  @Configuration(proxyBeanMethods = false)
  static class CustomTracingConfig {
    @Bean
    MongoClientSettingsBuilderCustomizer mongoTracingCustomizer() {
      return clientSettingsBuilder -> {};
    }
  }
}
