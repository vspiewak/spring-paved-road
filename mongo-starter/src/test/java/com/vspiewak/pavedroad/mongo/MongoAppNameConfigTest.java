package com.vspiewak.pavedroad.mongo;

import static org.assertj.core.api.Assertions.assertThat;

import com.mongodb.MongoClientSettings;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration;
import org.springframework.boot.mongodb.autoconfigure.MongoClientSettingsBuilderCustomizer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

class MongoAppNameConfigTest {

  private final ApplicationContextRunner runner =
      new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(MongoAppNameConfig.class));

  @Test
  void shouldApplySpringApplicationNameWhenNoneSet() {
    runner
        .withPropertyValues("spring.application.name=sample-service")
        .run(
            context -> {
              // given
              var builder = MongoClientSettings.builder();

              // when
              context.getBean(MongoClientSettingsBuilderCustomizer.class).customize(builder);

              // then
              assertThat(builder.build().getApplicationName()).isEqualTo("sample-service");
            });
  }

  @Test
  void shouldDoNothingWithoutSpringApplicationName() {
    runner.run(
        context -> {
          // given
          var builder = MongoClientSettings.builder();

          // when
          context.getBean(MongoClientSettingsBuilderCustomizer.class).customize(builder);

          // then
          assertThat(builder.build().getApplicationName()).isNull();
        });
  }

  @Test
  void shouldBeDisabledByProperty() {
    runner
        .withPropertyValues(
            "spring.application.name=sample-service", "platform.mongo.app-name.enabled=false")
        .run(context -> assertThat(context).doesNotHaveBean("mongoAppNameCustomizer"));
  }

  @Test
  void shouldBackOffWhenServiceDefinesItsOwnCustomizer() {
    runner
        .withUserConfiguration(CustomAppNameConfig.class)
        .withPropertyValues("spring.application.name=sample-service")
        .run(
            context -> {
              // given
              var builder = MongoClientSettings.builder();

              // when
              context
                  .getBean("mongoAppNameCustomizer", MongoClientSettingsBuilderCustomizer.class)
                  .customize(builder);

              // then
              assertThat(builder.build().getApplicationName()).isEqualTo("overridden");
            });
  }

  @Test
  void shouldLetUriAppNameWinThroughBootsFullCustomizerChain() {
    runner
        .withConfiguration(AutoConfigurations.of(MongoAutoConfiguration.class))
        .withPropertyValues(
            "spring.application.name=sample-service",
            "spring.mongodb.uri=mongodb://localhost/test?appName=fromUri")
        .run(
            context ->
                assertThat(
                        applyAllCustomizers(
                            context
                                .getBeanProvider(MongoClientSettingsBuilderCustomizer.class)
                                .orderedStream()
                                .toList()))
                    .isEqualTo("fromUri"));
  }

  @Test
  void shouldFillInSpringApplicationNameThroughBootsFullCustomizerChain() {
    runner
        .withConfiguration(AutoConfigurations.of(MongoAutoConfiguration.class))
        .withPropertyValues(
            "spring.application.name=sample-service", "spring.mongodb.uri=mongodb://localhost/test")
        .run(
            context ->
                assertThat(
                        applyAllCustomizers(
                            context
                                .getBeanProvider(MongoClientSettingsBuilderCustomizer.class)
                                .orderedStream()
                                .toList()))
                    .isEqualTo("sample-service"));
  }

  private static String applyAllCustomizers(
      List<MongoClientSettingsBuilderCustomizer> customizers) {
    var builder = MongoClientSettings.builder();
    customizers.forEach(customizer -> customizer.customize(builder));
    return builder.build().getApplicationName();
  }

  @Configuration(proxyBeanMethods = false)
  static class CustomAppNameConfig {
    @Bean
    MongoClientSettingsBuilderCustomizer mongoAppNameCustomizer() {
      return clientSettingsBuilder -> clientSettingsBuilder.applicationName("overridden");
    }
  }
}
