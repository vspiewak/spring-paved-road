package com.vspiewak.pavedroad.mongo;

import com.mongodb.observability.micrometer.MicrometerObservabilitySettings;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.mongodb.autoconfigure.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;

/**
 * Turns on the MongoDB driver's own tracing : every operation and the command it sends become
 * spans, children of whatever request or {@code @Observed} method issued them. Boot does not wire
 * it, and Spring Data's {@code MongoObservationCommandListener} — the usual answer on Boot 3 — is
 * deprecated for removal in favor of exactly this.
 *
 * <p>Command payloads are left out, so query values never reach a span. A service defining its own
 * {@code mongoTracingCustomizer} bean replaces this one. Disable with {@code
 * platform.mongo.tracing.enabled=false}.
 */
@AutoConfiguration(
    afterName =
        "org.springframework.boot.micrometer.observation.autoconfigure.ObservationAutoConfiguration")
@ConditionalOnClass({MicrometerObservabilitySettings.class, ObservationRegistry.class})
@ConditionalOnBean(ObservationRegistry.class)
public class MongoTracingConfig {

  @Bean
  @ConditionalOnProperty(prefix = "platform.mongo.tracing", name = "enabled", matchIfMissing = true)
  @ConditionalOnMissingBean(name = "mongoTracingCustomizer")
  public MongoClientSettingsBuilderCustomizer mongoTracingCustomizer(
      ObservationRegistry observationRegistry) {
    return clientSettingsBuilder ->
        clientSettingsBuilder.observabilitySettings(
            MicrometerObservabilitySettings.builder()
                .observationRegistry(observationRegistry)
                .build());
  }
}
