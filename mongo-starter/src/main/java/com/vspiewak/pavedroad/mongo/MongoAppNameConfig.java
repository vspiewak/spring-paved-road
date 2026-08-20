package com.vspiewak.pavedroad.mongo;

import com.mongodb.MongoClientSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.mongodb.autoconfigure.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

/**
 * Defaults the MongoDB driver {@code applicationName} to {@code spring.application.name}, so
 * connections show up under the service name in Atlas / server logs without every service having to
 * append {@code appName=...} to its connection URI.
 *
 * <p>An {@code appName} set explicitly in the URI wins : Boot's own settings customizer (order 0)
 * applies the connection string first, and this unordered customizer runs after it, only filling
 * the name in when none is present. Disable with {@code platform.mongo.app-name.enabled=false}.
 */
@AutoConfiguration
@ConditionalOnClass(MongoClientSettings.class)
public class MongoAppNameConfig {

  @Bean
  @ConditionalOnProperty(
      prefix = "platform.mongo.app-name",
      name = "enabled",
      matchIfMissing = true)
  @ConditionalOnMissingBean(name = "mongoAppNameCustomizer")
  public MongoClientSettingsBuilderCustomizer mongoAppNameCustomizer(
      @Value("${spring.application.name:}") String applicationName) {
    return clientSettingsBuilder -> {
      if (StringUtils.hasText(applicationName)
          && clientSettingsBuilder.build().getApplicationName() == null) {
        clientSettingsBuilder.applicationName(applicationName);
      }
    };
  }
}
