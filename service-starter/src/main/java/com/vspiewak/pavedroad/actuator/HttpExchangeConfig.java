package com.vspiewak.pavedroad.actuator;

import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.web.exchanges.HttpExchangeRepository;
import org.springframework.boot.actuate.web.exchanges.HttpExchangesEndpoint;
import org.springframework.boot.actuate.web.exchanges.InMemoryHttpExchangeRepository;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Makes {@code /actuator/httpexchanges} actually work : Boot ships the endpoint but deliberately
 * never auto-configures the {@link HttpExchangeRepository} backing it — expose the endpoint without
 * one and you get nothing. This fills that gap with the in-memory repository (last 100 exchanges)
 * whenever the endpoint is available, giving every service a free HTTP flight recorder.
 *
 * <p>A service defining its own {@code HttpExchangeRepository} bean replaces it. Boot's endpoint
 * and recording-filter auto-configurations are {@code @ConditionalOnBean(HttpExchangeRepository)},
 * so this one must be ordered {@code before} them — a plain {@code @Configuration} class would not
 * need that, an {@code @AutoConfiguration} does.
 */
@AutoConfiguration(
    beforeName = {
      "org.springframework.boot.actuate.autoconfigure.web.exchanges.HttpExchangesEndpointAutoConfiguration",
      "org.springframework.boot.servlet.autoconfigure.actuate.web.exchanges.ServletHttpExchangesAutoConfiguration"
    })
@ConditionalOnClass(HttpExchangeRepository.class)
@ConditionalOnAvailableEndpoint(endpoint = HttpExchangesEndpoint.class)
public class HttpExchangeConfig {

  @Bean
  @ConditionalOnMissingBean(HttpExchangeRepository.class)
  public HttpExchangeRepository httpExchangeRepository() {
    return new InMemoryHttpExchangeRepository();
  }
}
