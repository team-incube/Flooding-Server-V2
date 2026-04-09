---
name: kotlin-spring-arch
description: Architecture reference for Flooding project — Controller/Service/Repository layer responsibilities, @Transactional strategy, ExpectedException usage, entity conventions, and response wrapping.
---

# Kotlin + Spring Boot Architecture Guide (Flooding)

## Layer Structure

### Controller (`presentation/controller/`)
- `@RestController`, `@RequestMapping`
- Swagger: `@Tag`, `@Operation`, `@ApiResponse` on every endpoint
- `@RequestBody` parameter always named `request`
- Return `ResponseEntity<CommonApiResponse<T>>` (SDK wraps automatically)

### Service (`service/` + `service/impl/`)
- Always interface + impl pattern
- `@Transactional` here only — never in repository
- Business logic, validation, exception throwing

### Repository (`repository/`)
- Extend `JpaRepository<Entity, Long>`
- Derived query methods for simple queries
- No `@Transactional` here

## Entity Conventions (`entity/`)

```kotlin
@Entity
@Table(name = "tb_{domain_name}")  // singular, snake_case, tb_ prefix
class {Domain}JpaEntity(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @field:ManyToOne(fetch = FetchType.LAZY)  // always LAZY
    @field:JoinColumn(name = "..._id", nullable = false)
    val relation: OtherJpaEntity,

    @field:Column(name = "field_name", nullable = false)
    var mutableField: String,

    @field:Enumerated(EnumType.STRING)  // always STRING
    @field:Column(name = "status", length = 50)
    var status: StatusEnum,
)
```

Key rules:
- `@field:` prefix on all JPA annotations
- `val` for immutable fields (id, relations), `var` for mutable fields
- `FetchType.LAZY` always
- `EnumType.STRING` always
- Table name: `tb_` prefix + singular snake_case

## Exception Handling

Do NOT subclass `ExpectedException`. Always instantiate directly:

```kotlin
repository.findById(id).orElseThrow {
    ExpectedException("설명", HttpStatus.NOT_FOUND)
}

// Or with ?: throw
repository.findByClubIdAndIsActiveTrue(clubId)
    ?: throw ExpectedException("활성화된 신청 폼이 없습니다.", HttpStatus.NOT_FOUND)
```

Property to check: `exception.statusCode` (not `httpStatus`)

## Response Wrapping

SDK auto-wraps with `CommonApiResponse<T>`. Return the DTO directly from service.
Controller returns `ResponseEntity.ok(CommonApiResponse.success(response))` or similar SDK helper.

## Common Repository Patterns

```kotlin
// Derived queries
fun findByClubIdAndIsActiveTrue(clubId: Long): ClubFormJpaEntity?
fun findAllByFormIdOrderByFieldOrder(formId: Long): List<ClubFormFieldJpaEntity>
fun findAllByFieldIdInOrderByOptionOrder(fieldIds: List<Long>): List<ClubFormFieldOptionJpaEntity>
fun existsByFormIdAndUserId(formId: Long, userId: Long): Boolean

// Batch save instead of loop
repository.saveAll(items.map { ... })
```

## Security / Auth

Current user: `CurrentUserProvider.getCurrentUser()` → `UserJpaEntity`