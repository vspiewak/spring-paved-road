← [spring-paved-road](../README.md)

# 📌 `bom` — versions, decided once

Imports `spring-boot-dependencies`, then adds the pins Boot doesn't manage — `cucumber-bom`,
`archunit`, and the platform's own starters — under a comment that says exactly that : *our own
pins start here*. Every module and service downstream declares its dependencies **versionless** ;
upgrading the fleet is one diff in one file.
