package com.vspiewak.pavedroad.conventions;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION;

import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.Architectures;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.RestController;

/**
 * The platform-wide ArchUnit rules for service MAIN code, evaluated by {@link
 * PlatformConventionsTest#shouldRespectPlatformArchitectureRules()} as one dynamic test per rule.
 */
final class PlatformArchitectureRules {

  private PlatformArchitectureRules() {}

  public static final ArchRule shouldNotUseStandardStreams =
      NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;

  public static final ArchRule shouldNotUseFieldInjection = NO_CLASSES_SHOULD_USE_FIELD_INJECTION;

  public static final ArchRule shouldFollowLayeredArchitecture =
      Architectures.layeredArchitecture()
          .consideringAllDependencies()
          .layer("Controllers")
          .definedBy("..controllers..")
          .layer("Services")
          .definedBy("..services..")
          .layer("Repositories")
          .definedBy("..repositories..")
          .whereLayer("Controllers")
          .mayNotBeAccessedByAnyLayer()
          .whereLayer("Services")
          .mayOnlyBeAccessedByLayers("Controllers")
          .whereLayer("Repositories")
          .mayOnlyBeAccessedByLayers("Services")
          .withOptionalLayers(true);

  public static final ArchRule shouldHaveControllersInControllersPackage =
      classes()
          .that()
          .areAnnotatedWith(RestController.class)
          .should()
          .resideInAPackage("..controllers..")
          .andShould()
          .haveSimpleNameEndingWith("Controller");

  /** Every platform rule, in evaluation order, keyed by the name shown in the test report. */
  static Map<String, ArchRule> all() {
    Map<String, ArchRule> rules = new LinkedHashMap<>();
    rules.put("shouldNotUseStandardStreams", shouldNotUseStandardStreams);
    rules.put("shouldNotUseFieldInjection", shouldNotUseFieldInjection);
    rules.put("shouldFollowLayeredArchitecture", shouldFollowLayeredArchitecture);
    rules.put(
        "shouldHaveControllersInControllersPackage", shouldHaveControllersInControllersPackage);
    return rules;
  }
}
