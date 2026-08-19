package com.vspiewak.pavedroad.env;

import java.io.IOException;
import java.io.UncheckedIOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.io.ClassPathResource;

/**
 * Loads {@code platform-override.yaml} then {@code platform-override-<profile>.yaml} with the
 * HIGHEST precedence : platform-mandated values no application property can override.
 */
@Slf4j
public class SpringOverrideEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

  private static final String PROPERTY_SOURCE_NAME = "platform-override";

  @Override
  public int getOrder() {
    return Ordered.LOWEST_PRECEDENCE;
  }

  @Override
  public void postProcessEnvironment(ConfigurableEnvironment env, SpringApplication app) {
    var loader = new YamlPropertySourceLoader();

    // Load shared overrides
    load(loader, env, PROPERTY_SOURCE_NAME);

    // Load profile-specific overrides
    for (var profile : env.getActiveProfiles()) {
      load(loader, env, PROPERTY_SOURCE_NAME + "-" + profile);
    }
  }

  private void load(YamlPropertySourceLoader loader, ConfigurableEnvironment env, String name) {
    var resource = new ClassPathResource(name + ".yaml");
    if (!resource.exists()) return;
    try {
      var propertySources = loader.load(name, resource);
      for (var ps : propertySources) {
        if (ps instanceof EnumerablePropertySource<?> eps) {
          for (String key : eps.getPropertyNames()) {
            if (env.containsProperty(key)) {
              log.warn("Overriding property: {}", key);
            }
          }
        }
        env.getPropertySources().addFirst(ps);
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
