# 🛣️ spring-paved-road

![Java](https://img.shields.io/badge/Java-25-orange) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-green)

**The paved road : parent, BOM & Spring Boot starters that align a 99-service fleet — distilled into one runnable monorepo.**

At work, these patterns govern ~100 Spring Boot microservices maintained by ~100 engineers :
one `<parent>` line in a service's pom, and it inherits version coherence, formatting law,
style rules and platform behavior. This repo is the pattern, extracted and runnable.

📝 The story so far : [migrating 1,273 repos in under an hour](https://vspiewak.com/migrating-1200-repos-from-bitbucket-to-github-in-under-an-hour) ·
[27,000+ PRs with gh-auto-updater](https://vspiewak.com/gh-auto-updater-mass-pull-requests-across-a-repo-fleet) — more on [vspiewak.com](https://vspiewak.com)

## The idea

A **paved road** is not a fence. Services get :

* **defaults they can override** — sane platform behavior, applied unless the service says otherwise
* **mandates they cannot** — security & governance values that win over any service configuration
* and everything else — versions, formatting, style — **by inheritance, not by copy-paste**

The whole pitch fits in one diff. A service pom, before and after :

```diff
 <parent>
-   <groupId>org.springframework.boot</groupId>
-   <artifactId>spring-boot-starter-parent</artifactId>
-   <version>4.1.0</version>
+   <groupId>com.vspiewak</groupId>
+   <artifactId>parent</artifactId>
+   <version>0.0.1-SNAPSHOT</version>
 </parent>
```

## Modules

| Module | Role |
|---|---|
| [`bom/`](./bom) | Dependency versions, once — imports `spring-boot-dependencies`, ready for your own pins |
| [`parent/`](./parent) | The paved road : plugin pinning, compiler config, [Spotless](https://github.com/diffplug/spotless) (google-java-format + sortPom), a deliberately tiny [Checkstyle](https://checkstyle.org) ruleset — and it **imports** the bom (no parent-chaining) |
| [`service‑starter/`](./service-starter) | Platform behavior as a dependency : the default / override property mechanism |
| [`mongo‑starter/`](./mongo-starter) | Local-dev Mongo auto-load : seeds from `mongo/import/<collection>/*.json`, host-guarded — it can never touch a remote cluster |
| [`sample‑service/`](./sample-service) | A start.spring.io-shaped service consuming all of it — **the tests are the documentation** |

Coming next, each with its blog post : `conventions-starter` (ArchUnit rules as executable law)
and `cucumber-starter` (canonical BDD steps).

## The property ladder 🪜

`service-starter` layers configuration around the application, lowest to highest precedence :

```text
platform-default.yaml      # platform defaults  — the service CAN override
        <  application.yaml          # the service's own configuration
                <  platform-override.yaml     # platform mandates — the service CANNOT override
                        <  platform-override-<profile>.yaml
```

Proven by [`PlatformPropertiesTest`](./sample-service/src/test/java/com/vspiewak/sample/platform/PlatformPropertiesTest.java) —
including the fun one : `sample-service` *tries* to set `management.endpoint.env.show-values: always`,
and the platform answers `never` 🔒

## Local Mongo auto-load 🌱

`mongo-starter` seeds your local MongoDB at startup, from plain JSON files :

```text
src/test/resources/mongo/import/
├── orders/                  # directory name = collection name
│   ├── order1.json          # one document per file
│   └── order2.json
└── products/
    └── product1.json
```

* runs only under the `local` profile, on `ApplicationReadyEvent`
* **host-guarded** 🔒 : unless every Mongo host is `localhost` / `127.0.0.1`, it refuses to load —
  a misconfigured URI can never seed a shared or production cluster
* knobs : `platform.mongo.data-import.enabled` (default `true`) and `platform.mongo.data-import.path`

Proven by [`MongoDataImporterTest`](./mongo-starter/src/test/java/com/vspiewak/pavedroad/mongo/MongoDataImporterTest.java) —
including the one that matters : remote hosts → nothing gets loaded.

## Quick start

```bash
sdk env install      # Java 25 (Temurin) via sdkman, pinned in .sdkmanrc
./mvnw install       # build everything : bom → parent → starters → sample-service
./format.sh          # apply the formatting law (spotless) on every governed module

# the local dev loop : sample-service + a MongoDB container + the auto-load seed
./mvnw -pl sample-service spring-boot:test-run
```

## Boot 4 field notes 📓

Building this on Spring Boot 4.1 / Java 25 surfaced real migration intel :

* `EnvironmentPostProcessor` moved : `org.springframework.boot.env.EnvironmentPostProcessor`
  is `@Deprecated(since = "4.0.0")` — the native home is now `org.springframework.boot.EnvironmentPostProcessor`,
  and the `META-INF/spring.factories` key follows. The factories mechanism itself is alive and well :
  Boot 4 registers its *own* post processors with it.
* The web starter is now `spring-boot-starter-webmvc` (Boot 4 modularization).

## At work vs here

| | At work | This repo |
|---|---|---|
| Repos | ~10, independent releases, CODEOWNERS | one reactor, for your cloning pleasure |
| Platform | Java 21 · Spring Boot 3.5 | Java 25 · Spring Boot 4.1 |
| Fleet | ~100 services, ~100 engineers | one sample service — yours to fork |

Same patterns, two platform generations apart — that's rather the point 😉
