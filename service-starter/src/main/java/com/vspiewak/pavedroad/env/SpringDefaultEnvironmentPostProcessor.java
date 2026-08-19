package com.vspiewak.pavedroad.env;

import java.io.IOException;
import java.io.UncheckedIOException;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.ClassPathResource;

/**
 * Loads {@code paved-road-default.yaml} with the LOWEST precedence : sane platform defaults that
 * any application property overrides.
 */
public class SpringDefaultEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

  private static final String PROPERTY_SOURCE_NAME = "paved-road-default";

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }

  @Override
  public void postProcessEnvironment(ConfigurableEnvironment env, SpringApplication app) {
    var loader = new YamlPropertySourceLoader();

    // Load shared defaults
    load(loader, env, PROPERTY_SOURCE_NAME);
  }

  private void load(YamlPropertySourceLoader loader, ConfigurableEnvironment env, String name) {
    var resource = new ClassPathResource(name + ".yaml");
    if (!resource.exists()) return;
    try {
      var propertySources = loader.load(name, resource);
      for (var ps : propertySources) {
        env.getPropertySources().addLast(ps);
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
