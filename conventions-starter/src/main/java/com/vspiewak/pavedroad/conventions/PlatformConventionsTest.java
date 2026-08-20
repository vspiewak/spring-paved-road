package com.vspiewak.pavedroad.conventions;

import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

/**
 * The platform conventions as an executable test : the architecture rules (one dynamic test per
 * rule, MAIN classes only) and the canonical BDD layout.
 *
 * <p>Services opt in with an empty subclass in a {@code conventions} package under their root test
 * package :
 *
 * <pre>{@code
 * package com.acme.myservice.conventions;
 *
 * class ConventionsTest extends PlatformConventionsTest {}
 * }</pre>
 *
 * The scanned package defaults to the subclass's own package, minus the canonical {@code
 * conventions} leaf ; service-local rules go in {@link #additionalArchitectureRules()}.
 */
public abstract class PlatformConventionsTest {

  /**
   * The root package to scan — defaults to the package of the extending test class, stripped of the
   * canonical {@code conventions} leaf it lives in.
   */
  protected String servicePackage() {
    String testPackage = getClass().getPackageName();
    return testPackage.endsWith(".conventions")
        ? testPackage.substring(0, testPackage.length() - ".conventions".length())
        : testPackage;
  }

  /** Service-local rules, evaluated against the same MAIN classes — name shown in the report. */
  protected List<NamedRule> additionalArchitectureRules() {
    return List.of();
  }

  /** A service-local rule with the name shown in the test report. */
  public record NamedRule(String name, ArchRule rule) {}

  @TestFactory
  protected Stream<DynamicTest> shouldRespectPlatformArchitectureRules() {
    JavaClasses mainClasses =
        new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages(servicePackage());
    Stream<NamedRule> platformRules =
        PlatformArchitectureRules.all().entrySet().stream()
            .map(entry -> new NamedRule(entry.getKey(), entry.getValue()));
    return Stream.concat(platformRules, additionalArchitectureRules().stream())
        .map(named -> DynamicTest.dynamicTest(named.name(), () -> named.rule().check(mainClasses)));
  }

  @Test
  protected void shouldHaveTheCanonicalFeatureFiles() {
    for (String feature : List.of("features/actuator.feature", "features/service.feature")) {
      assertThat(getClass().getClassLoader().getResource(feature))
          .as(
              "classpath:%s — canonical BDD layout : actuator.feature (health probes) + "
                  + "service.feature (the service's scenarios)",
              feature)
          .isNotNull();
    }
  }
}
