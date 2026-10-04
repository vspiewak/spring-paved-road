← [spring-paved-road](../README.md)

# 👮 `conventions-starter` — conventions as executable law

Code review shouldn't spend its time on layering and naming — those conventions become tests that
fail the build instead. Two lanes, like everything else :

**Static** ([ArchUnit](https://www.archunit.org) on the service's MAIN classes, no Docker) —
opted into with one empty subclass :

```java
class ConventionsTest extends PlatformConventionsTest {}
```

* **layered architecture** — `controllers → services → repositories`, nothing upstream, no shortcuts
* `@RestController` classes live in `..controllers..` and end with `Controller`
* no field injection, no `System.out`
* and the canonical BDD layout : `features/actuator.feature` + `features/service.feature` exist

The subclass lives in a `conventions` package under the service's root test package — the scanned
package is derived from it, and service-local rules plug in via `additionalArchitectureRules()`.

**Runtime** (the booted service) — the subclass only wires the context :

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@Import(Containers.class)
class ConventionsIT extends PlatformConventionsIT {}
```

* the context loads, and `spring.application.name` is set (traces need a `service.name`)...
* ...and it **matches the Maven artifactId** — read from `pom.xml`, deliberately not
  `BuildProperties`, whose backing file only exists after the Maven build ran (IDE runs would fail)
* `/actuator/health` answers `UP` — the deploy probe, proven before deploy

The work version goes further — repositories as interfaces, logger conventions, `@Observed` span
naming, a shared error contract, CI / deploy descriptor coherence — same pattern, grown to fleet
size.
