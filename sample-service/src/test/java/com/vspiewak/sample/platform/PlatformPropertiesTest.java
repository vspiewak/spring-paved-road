package com.vspiewak.sample.platform;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

/**
 * The platform property layering, from lowest to highest precedence :
 *
 * <p>platform-default.yaml &lt; application.yaml &lt; platform-override(-profile).yaml
 *
 * <p>A service value comes from a config file ({@code spring.config.additional-location}), never an
 * inline test property : that one outranks every config file, so it would win whatever the ladder.
 */
class PlatformPropertiesTest {

  @Nested
  @SpringBootTest
  class Defaults {

    @Autowired private Environment environment;

    @Test
    void platformDefaultAppliesWhenTheServiceSaysNothing() {
      // the service does not configure endpoint exposure → the platform default applies
      assertThat(environment.getProperty("management.endpoints.web.exposure.include"))
          .isEqualTo("health,info,httpexchanges");
    }

    @Test
    void everyServiceGetsThePlatformBannerWithoutAskingForIt() {
      assertThat(environment.getProperty("spring.banner.location"))
          .isEqualTo("classpath:platform-banner.txt");
    }

    @Test
    void everyServiceLogsInThePlatformShape() {
      // the platform decides the line : app name and trace ids in, PID and '---' out
      assertThat(environment.getProperty("logging.pattern.console"))
          .contains("APPLICATION_NAME")
          .contains("%correlationId")
          .doesNotContain("${PID:");
    }

    @Test
    void theBootConventionIsLeftAlone() {
      // deliberately NOT set : a service dropping its own logback-spring.xml is still picked up,
      // which setting logging.config would have silently disabled
      assertThat(environment.getProperty("logging.config")).isNull();
    }

    @Test
    void andTheChattyDependenciesAreAlreadyQuiet() {
      // proof the shipped logback config actually took effect, not just that the property is set
      var driver = LoggerFactory.getLogger("org.mongodb.driver");
      assertThat(driver.isWarnEnabled()).isTrue();
      assertThat(driver.isInfoEnabled()).isFalse();
    }
  }

  @Nested
  @SpringBootTest(
      properties =
          "spring.config.additional-location=classpath:/platform/service-over-log-levels.yaml")
  class ServiceOverLogLevels {

    @Test
    void aServiceDebuggingItsDriverWinsOverTheShippedConfig() {
      // levels are defaults, not mandates : logging.level.* is applied after the config is loaded
      assertThat(LoggerFactory.getLogger("org.mongodb.driver").isDebugEnabled()).isTrue();
    }
  }

  @Nested
  @SpringBootTest(
      properties = "spring.config.additional-location=classpath:/platform/service-over-banner.yaml")
  class ServiceOverBanner {

    @Autowired private Environment environment;

    @Test
    void aServiceThatWantsItsOwnBannerKeepsIt() {
      // the banner is a default, not a mandate — the road is paved, not fenced
      assertThat(environment.getProperty("spring.banner.location"))
          .isEqualTo("classpath:my-own-banner.txt");
    }
  }

  @Nested
  @SpringBootTest(
      properties =
          "spring.config.additional-location=classpath:/platform/service-over-defaults.yaml")
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
  @SpringBootTest(properties = "spring.mvc.problemdetails.enabled=false")
  class ServiceUnderTheErrorContract {

    @Autowired private Environment environment;

    @Test
    void aServiceCannotOptOutOfTheErrorContract() {
      // the error shape is what fleet consumers parse — it is not the service's to choose
      assertThat(environment.getProperty("spring.mvc.problemdetails.enabled")).isEqualTo("true");
    }
  }

  @Nested
  @SpringBootTest
  @ActiveProfiles("dev")
  class ProfileOverrides {

    @Autowired private Environment environment;

    @Test
    void profileSpecificOverrideAppliesWhenProfileIsActive() {
      assertThat(environment.getProperty("platform.database.host")).isEqualTo("dev-db.internal");
    }
  }
}
