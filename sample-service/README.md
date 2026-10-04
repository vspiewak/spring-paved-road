← [spring-paved-road](../README.md)

# 🧪 `sample-service` — the tests are the documentation

A start.spring.io-shaped service consuming all of it : one `<parent>` line, the starters, a plain
`orders` API. Its test sources are the living documentation — the platform proofs sit in the
`platform` package, the conventions opt-ins in `conventions`, and the whole test pyramid fits on
one endpoint :

| Test | Kind | Docker |
|---|---|---|
| [`OrderControllerTest`](./src/test/java/com/vspiewak/sample/controllers/OrderControllerTest.java) | `@WebMvcTest` slice — service mocked, `MockMvcTester` | no |
| [`OrderRepositoryIT`](./src/test/java/com/vspiewak/sample/repositories/OrderRepositoryIT.java) | `@DataMongoTest` slice — real MongoDB, data layer only | yes |
| [`OrderControllerIT`](./src/test/java/com/vspiewak/sample/controllers/OrderControllerIT.java) | Full e2e — `RestTestClient`, each test seeds its own data | yes |
| [`CucumberIT`](./src/test/java/com/vspiewak/sample/cucumber/CucumberIT.java) | Full e2e in business language — Gherkin [features](./src/test/resources/features), generic steps from `cucumber-starter` | yes |
| [`ConventionsTest`](./src/test/java/com/vspiewak/sample/conventions/ConventionsTest.java) | The architecture itself, asserted — ArchUnit rules from `conventions-starter` | no |
| [`ConventionsIT`](./src/test/java/com/vspiewak/sample/conventions/ConventionsIT.java) | The runtime conventions, asserted — app name, health probe, from `conventions-starter` | yes |
