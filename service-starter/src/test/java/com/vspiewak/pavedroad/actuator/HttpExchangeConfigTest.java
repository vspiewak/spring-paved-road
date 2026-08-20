package com.vspiewak.pavedroad.actuator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.web.exchanges.HttpExchange;
import org.springframework.boot.actuate.web.exchanges.HttpExchangeRepository;
import org.springframework.boot.actuate.web.exchanges.InMemoryHttpExchangeRepository;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

class HttpExchangeConfigTest {

  private final ApplicationContextRunner runner =
      new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(HttpExchangeConfig.class));

  @Test
  void shouldProvideRepositoryWhenEndpointIsExposed() {
    runner
        .withPropertyValues("management.endpoints.web.exposure.include=health,info,httpexchanges")
        .run(context -> assertThat(context).hasSingleBean(InMemoryHttpExchangeRepository.class));
  }

  @Test
  void shouldBackOffWhenEndpointIsNotExposed() {
    runner.run(context -> assertThat(context).doesNotHaveBean(HttpExchangeRepository.class));
  }

  @Test
  void shouldBackOffWhenServiceDefinesItsOwnRepository() {
    runner
        .withPropertyValues("management.endpoints.web.exposure.include=httpexchanges")
        .withUserConfiguration(CustomRepositoryConfig.class)
        .run(
            context -> {
              assertThat(context).doesNotHaveBean(InMemoryHttpExchangeRepository.class);
              assertThat(context).hasSingleBean(HttpExchangeRepository.class);
            });
  }

  @Configuration(proxyBeanMethods = false)
  static class CustomRepositoryConfig {
    @Bean
    HttpExchangeRepository serviceOwnedRepository() {
      return new HttpExchangeRepository() {
        @Override
        public List<HttpExchange> findAll() {
          return List.of();
        }

        @Override
        public void add(HttpExchange exchange) {}
      };
    }
  }
}
