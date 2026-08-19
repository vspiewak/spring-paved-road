package com.vspiewak.sample;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

/**
 * The paved-road property layering, from lowest to highest precedence :
 *
 * <p>paved-road-default.yaml &lt; application.yaml &lt; paved-road-override(-profile).yaml
 */
class PavedRoadPropertiesTest {

  @Nested
  @SpringBootTest
  class Defaults {

    @Autowired private Environment environment;

    @Test
    void platformDefaultAppliesWhenTheServiceSaysNothing() {
      // the service does not configure endpoint exposure → the platform default applies
      assertThat(environment.getProperty("management.endpoints.web.exposure.include"))
          .isEqualTo("health,info");
    }
  }

  @Nested
  @SpringBootTest(properties = "management.endpoints.web.exposure.include=*")
  class ServiceOverDefaults {

    @Autowired private Environment environment;

    @Test
    void serviceConfigurationWinsOverPlatformDefaults() {
      assertThat(environment.getProperty("management.endpoints.web.exposure.include"))
          .isEqualTo("*");
    }
  }

  @Nested
  @SpringBootTest
  class PlatformOverrides {

    @Autowired private Environment environment;

    @Test
    void platformOverrideWinsOverServiceConfiguration() {
      // application.yaml says 'always' — the platform override says 'never', and wins
      assertThat(environment.getProperty("management.endpoint.env.show-values")).isEqualTo("never");
    }
  }

  @Nested
  @SpringBootTest
  @ActiveProfiles("dev")
  class ProfileOverrides {

    @Autowired private Environment environment;

    @Test
    void profileSpecificOverrideAppliesWhenProfileIsActive() {
      assertThat(environment.getProperty("paved-road.database.host")).isEqualTo("dev-db.internal");
    }
  }
}
