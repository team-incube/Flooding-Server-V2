# Flooding Server V2

Always respond in Korean.

## Service Overview

School convenience platform for students. Provides various daily-use features including dormitory applications (study room, massage chair, homebase), meal checking, club management, timetable, AI-based music recommendations, and more.

## Tech Stack

Kotlin, Java 24, Spring Boot 4.0.2, PostgreSQL, Redis, Spring Security, JWT, Swagger, Gradle KTS

## Commands

Run infrastructure (PostgreSQL, Redis):
```bash
docker-compose up -d
```

Run application:
```bash
./gradlew bootRun
```

Run tests:
```bash
./gradlew test
```

Build:
```bash
./gradlew build
```

Required env vars: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `OAUTH_CLIENT_ID`, `OAUTH_CLIENT_SECRET`


## Coding Conventions

### Naming

- DB related fields (`@Table`, column names, etc.) use snake_case
- DTO prefix by purpose (recommended): Get, Search, Create, Patch, Delete
  - example: `GetCertificatesResponse`
- Entity naming: `{Domain}JpaEntity` (example: `UserJpaEntity`)
- Redis repository naming: `{Domain}RedisRepository` (example: `BlackListRedisRepository`)
- Table names are singular
- REST API URLs: Use the "plural" name to denote the collection resource archetype (e.g., `/studies`, `/users`). Controller resources that represent actions use verbs as-is (e.g., `/auth/signin`).
- RequestBody parameter name in presentation layer is fixed to `request`

```kotlin
@PostMapping("/example")
fun exampleEndPoint(@Valid @RequestBody request: ExampleRequest): ResponseEntity<Void> {
    return ResponseEntity.ok().build()
}
```

### Layer Rules

- Transactions must be opened in the service layer only, not in the repository layer.
- Swagger documentation should be as thorough as possible.
