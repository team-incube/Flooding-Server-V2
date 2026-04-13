---
name: new-api
description: Implement a new API endpoint end-to-end — Repository → Service interface → Service impl → Controller, following Flooding project conventions.
---

# New API Implementation Flow (Flooding)

## Directory Structure

```
domain/{domain}/
├── entity/              # (already exists)
├── repository/          # Step 1
├── presentation/
│   ├── controller/      # Step 4
│   └── data/
│       ├── request/     # Step 2
│       └── response/    # Step 2
├── service/             # Step 3a
└── service/impl/        # Step 3b
```

## Step 1 — Repository

```kotlin
// src/main/kotlin/.../domain/{domain}/repository/{Domain}Repository.kt
package team.incube.flooding.domain.{domain}.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.incube.flooding.domain.{domain}.entity.{Domain}JpaEntity

interface {Domain}Repository : JpaRepository<{Domain}JpaEntity, Long> {
    // Add derived query methods as needed
    fun findByXxxAndYyy(xxx: Long, yyy: Boolean): {Domain}JpaEntity?
    fun findAllByXxxOrderByField(xxx: Long): List<{Domain}JpaEntity>
    fun existsByXxxAndYyy(xxx: Long, yyy: Long): Boolean
}
```

## Step 2 — DTOs

**Request:**
```kotlin
// presentation/data/request/{Verb}{Domain}Request.kt
data class Create{Domain}Request(
    val field1: String,
    val field2: String?,
)
```

**Response:**
```kotlin
// presentation/data/response/{Verb}{Domain}Response.kt
data class Get{Domain}Response(
    val id: Long,
    val field1: String,
)
```

## Step 3a — Service Interface

```kotlin
// service/{Verb}{Domain}Service.kt
package team.incube.flooding.domain.{domain}.service

interface {Verb}{Domain}Service {
    fun execute(/* params */): {Verb}{Domain}Response
}
```

## Step 3b — Service Impl

```kotlin
// service/impl/{Verb}{Domain}ServiceImpl.kt
@Service
class {Verb}{Domain}ServiceImpl(
    private val repository: {Domain}Repository,
) : {Verb}{Domain}Service {
    @Transactional
    override fun execute(/* params */): {Verb}{Domain}Response {
        val entity = repository.findById(id).orElseThrow {
            ExpectedException("설명", HttpStatus.NOT_FOUND)
        }
        // business logic
        return {Verb}{Domain}Response(...)
    }
}
```

## Step 4 — Controller

```kotlin
// presentation/controller/{Domain}Controller.kt
@Tag(name = "{도메인명}", description = "{도메인} 관련 API")
@RestController
@RequestMapping("/{domain}s")
class {Domain}Controller(
    private val createService: Create{Domain}Service,
    private val getService: Get{Domain}Service,
) {
    @Operation(summary = "...", description = "...")
    @ApiResponse(responseCode = "201", description = "생성 성공")
    @PostMapping
    fun create{Domain}(
        @Valid @RequestBody request: Create{Domain}Request,
    ): ResponseEntity<CommonApiResponse<Create{Domain}Response>> =
        ResponseEntity.status(HttpStatus.CREATED)
            .body(CommonApiResponse.success(createService.execute(request)))

    @Operation(summary = "...", description = "...")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/{id}")
    fun get{Domain}(
        @PathVariable id: Long,
    ): ResponseEntity<CommonApiResponse<Get{Domain}Response>> =
        ResponseEntity.ok(CommonApiResponse.success(getService.execute(id)))
}
```

## Checklist

- [ ] Repository interface created
- [ ] Request/Response DTOs created
- [ ] Service interface created
- [ ] Service impl with `@Transactional` created
- [ ] Controller with Swagger annotations created
- [ ] `@RequestBody` parameter named `request`
- [ ] Correct HTTP status codes (200 GET, 201 POST)
- [ ] `ExpectedException` used for errors (no subclasses)
- [ ] Test written for service impl