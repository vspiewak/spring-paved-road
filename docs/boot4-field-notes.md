← [spring-paved-road](../README.md)

# 📓 Boot 4 field notes

Building this on Spring Boot 4.1 / Java 25 surfaced real migration intel :

* `EnvironmentPostProcessor` moved : `org.springframework.boot.env.EnvironmentPostProcessor`
  is `@Deprecated(since = "4.0.0")` — the native home is now `org.springframework.boot.EnvironmentPostProcessor`,
  and the `META-INF/spring.factories` key follows. The factories mechanism itself is alive and well :
  Boot 4 registers its *own* post processors with it.
* The web starter is now `spring-boot-starter-webmvc` (Boot 4 modularization).
* Test support is modular too : per-starter companions (`spring-boot-starter-webmvc-test`,
  `spring-boot-starter-actuator-test`, ...) instead of one big `spring-boot-starter-test`.
* `TestRestTemplate` relocated to `org.springframework.boot.resttestclient` (not deprecated),
  needs an explicit `@AutoConfigureTestRestTemplate` — and pulls `spring-boot-restclient` for
  its `RestTemplateBuilder`.
* The modern way : **`RestTestClient`** (new in Spring Framework 7) — one fluent,
  WebTestClient-style API that binds to MockMvc *or* a live server. `@AutoConfigureRestTestClient`,
  zero extra modules. See [`OrderControllerIT`](../sample-service/src/test/java/com/vspiewak/sample/controllers/OrderControllerIT.java).
* Test slices moved packages too : `@WebMvcTest` → `org.springframework.boot.webmvc.test.autoconfigure`,
  `@DataMongoTest` → `org.springframework.boot.data.mongodb.test.autoconfigure`.
* **`MockMvcTester`** is the AssertJ-native MockMvc — `assertThat(mvc.get().uri(...)).hasStatusOk().bodyJson()...`
  See [`OrderControllerTest`](../sample-service/src/test/java/com/vspiewak/sample/controllers/OrderControllerTest.java)
  (the slice) next to [`OrderControllerIT`](../sample-service/src/test/java/com/vspiewak/sample/controllers/OrderControllerIT.java) (the real thing).
* Sharing one Testcontainer across several `@SpringBootTest` contexts means every context
  re-runs seeding into the same database — one container **per context**
  ([`Containers`](../sample-service/src/test/java/com/vspiewak/sample/Containers.java)) keeps tests honest.
* **Structured logging needs no Java** — not new in Boot 4, but worth knowing before porting a
  hand-written `StructuredLogFormatter` : `logging.structured.format.console` takes `ecs`, `gelf` or
  `logstash`, and `logging.structured.json.add / rename / include / exclude` reshape the JSON from
  yaml. Those keys already ship with Boot 3.5. Beware : there is no plain `json` format id, only
  those three.
* Mongo moved out of `data` : the auto-configuration now lives in
  `org.springframework.boot.mongodb.autoconfigure` (so `MongoClientSettingsBuilderCustomizer`
  imports change), and the properties renamed `spring.data.mongodb.*` → **`spring.mongodb.*`**.
  The old property is *silently ignored* — our "URI `appName` wins" test failed with the URI never
  applied at all before we spotted it.
* HTTP exchanges split across the modular jars : the endpoint stays in `spring-boot-actuator`
  (packages unchanged), but the servlet recording filter and its auto-configuration moved to
  `spring-boot-servlet` (`ServletHttpExchangesAutoConfiguration`). And since Boot's two exchange
  auto-configurations are `@ConditionalOnBean(HttpExchangeRepository)`, a starter providing that
  bean from an `@AutoConfiguration` must order itself `beforeName` both — a plain `@Configuration`
  (evaluated before all auto-configuration) never needed to care.
* Tracing went modular and renamed : `spring-boot-micrometer-tracing-opentelemetry` holds the
  auto-configuration, and the OTLP keys moved `management.otlp.tracing.*` →
  **`management.opentelemetry.tracing.export.otlp.*`** (the old ones are deprecated). The
  all-in-one `spring-boot-starter-opentelemetry` also ships the OTLP *metrics* registry — we take the
  three tracing jars instead. And in tests, tracing stays off until `@AutoConfigureTracing`
  (`spring-boot-micrometer-tracing-test`).
* MongoDB traces itself now : driver 5.7+ takes an `ObservationRegistry` in `MongoClientSettings`, and
  Spring Data 5.1 deprecates its `MongoObservationCommandListener` for removal. One catch : the driver
  fills the command name in *after* the observation is created, so an `ObservationPredicate` can no
  longer filter `hello` / `ping` by name — filter by what caused them instead.
