← [spring-paved-road](../README.md)

# 🥭 `mongo-starter` — seeded locally, named everywhere

How every service talks to MongoDB : a local dev loop that seeds itself, connections that
identify themselves, and queries that show up in the trace.

## 🌱 Local auto-load

Seeds your local MongoDB at startup, from plain JSON files :

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

Proven by [`MongoDataImporterTest`](./src/test/java/com/vspiewak/pavedroad/mongo/MongoDataImporterTest.java) —
including the one that matters : remote hosts → nothing gets loaded. And end-to-end by
[`MongoAutoLoadIT`](../sample-service/src/test/java/com/vspiewak/sample/platform/MongoAutoLoadIT.java) :
the booted `sample-service`, under the `local` profile, finds the seed in its database.

## 🏷️ Connections, named

Defaults the driver's `applicationName` to `spring.application.name` — so connections show up
under the service name in Atlas / server logs, without every service appending `appName=...` to
its URI. Textbook paved road :

* an `appName` set explicitly in the URI **wins** — Boot's own customizer (order 0) applies the
  connection string first ; this unordered one runs after and only fills the gap
* a service defining its own `mongoAppNameCustomizer` bean replaces it (`@ConditionalOnMissingBean`)
* kill switch : `platform.mongo.app-name.enabled=false`

Proven by [`MongoAppNameConfigTest`](./src/test/java/com/vspiewak/pavedroad/mongo/MongoAppNameConfigTest.java) —
including through Boot's **full** customizer chain, both directions. And end-to-end, server-side, by
[`MongoAppNameIT`](../sample-service/src/test/java/com/vspiewak/sample/platform/MongoAppNameIT.java) :
the very connection running the `$currentOp` aggregation identifies itself as `sample-service`.

## 🔎 Queries, traced

Hands Boot's `ObservationRegistry` to the MongoDB driver's **own** tracing : every operation and
the command it sends become spans, children of the request or `@Observed` method that issued them —
the `find test.orders` / `find` pair in [the Jaeger trace](../service-starter/README.md#-traces-end-to-end).

* the driver's native support (5.7+), not Spring Data's `MongoObservationCommandListener` — the usual
  Boot 3 answer, now deprecated for removal in its favor
* command payloads stay out : query values never reach a span
* a service defining its own `mongoTracingCustomizer` bean replaces it (`@ConditionalOnMissingBean`)
* kill switch : `platform.mongo.tracing.enabled=false`

Proven by [`MongoTracingConfigTest`](./src/test/java/com/vspiewak/pavedroad/mongo/MongoTracingConfigTest.java),
and end-to-end by the same [`TracingIT`](../sample-service/src/test/java/com/vspiewak/sample/platform/TracingIT.java).
