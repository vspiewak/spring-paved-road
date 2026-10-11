package com.vspiewak.pavedroad.conventions;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION;

import com.tngtech.archunit.base.HasDescription;
import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.properties.HasAnnotations;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.CompositeArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.library.Architectures;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

/**
 * The platform-wide ArchUnit rules for service MAIN code, evaluated by {@link
 * PlatformConventionsTest#shouldRespectPlatformArchitectureRules()} as one dynamic test per rule.
 */
final class PlatformArchitectureRules {

  private PlatformArchitectureRules() {}

  /** By name : the rules read the annotation, they do not need Micrometer on the classpath. */
  private static final String OBSERVED = "io.micrometer.observation.annotation.Observed";

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

  /**
   * A public method of a service is a span : the trace shows the business, not just HTTP and I/O.
   */
  public static final ArchRule shouldAnnotatePublicServiceMethodsWithObserved =
      methods()
          .that()
          .arePublic()
          .and()
          .areDeclaredInClassesThat()
          .areAnnotatedWith(Service.class)
          .should()
          .beAnnotatedWith(OBSERVED)
          .because("every public method of a @Service is a span of its own, named")
          .allowEmptyShould(true); // a service with no @Service yet still builds

  /**
   * A span without a name is {@code Class#method} : a name the code chose, that a rename breaks and
   * that Cloud Trace or Jaeger cannot group by. The fleet names spans {@code
   * <domain>.<entity>.<verb>}.
   */
  public static final ArchRule shouldNameObservedSpans =
      CompositeArchRule.of(
              classes()
                  .that()
                  .areAnnotatedWith(OBSERVED)
                  .should(declareASpanName())
                  .allowEmptyShould(true))
          .and(
              methods()
                  .that()
                  .areAnnotatedWith(OBSERVED)
                  .should(declareASpanName())
                  .allowEmptyShould(true))
          .because(
              "a span is named <domain>.<entity>.<verb> : set name and contextualName on @Observed");

  private static <T extends HasAnnotations<?> & HasDescription>
      ArchCondition<T> declareASpanName() {
    return new ArchCondition<>("declare a span name : @Observed(name, contextualName)") {
      @Override
      public void check(T item, ConditionEvents events) {
        boolean named =
            item.tryGetAnnotationOfType(OBSERVED)
                .filter(
                    observed -> hasText(observed, "name") && hasText(observed, "contextualName"))
                .isPresent();
        events.add(
            new SimpleConditionEvent(
                item,
                named,
                item.getDescription()
                    + (named ? " declares" : " does not declare")
                    + " a span name"));
      }
    };
  }

  private static boolean hasText(JavaAnnotation<?> annotation, String property) {
    return annotation
        .get(property)
        .filter(String.class::isInstance)
        .map(String.class::cast)
        .stream()
        .anyMatch(value -> !value.isBlank());
  }

  /** Every platform rule, in evaluation order, keyed by the name shown in the test report. */
  static Map<String, ArchRule> all() {
    Map<String, ArchRule> rules = new LinkedHashMap<>();
    rules.put("shouldNotUseStandardStreams", shouldNotUseStandardStreams);
    rules.put("shouldNotUseFieldInjection", shouldNotUseFieldInjection);
    rules.put("shouldFollowLayeredArchitecture", shouldFollowLayeredArchitecture);
    rules.put(
        "shouldHaveControllersInControllersPackage", shouldHaveControllersInControllersPackage);
    rules.put(
        "shouldAnnotatePublicServiceMethodsWithObserved",
        shouldAnnotatePublicServiceMethodsWithObserved);
    rules.put("shouldNameObservedSpans", shouldNameObservedSpans);
    return rules;
  }
}
