---
name: new-entity
description: Create a new JPA entity following Flooding project conventions — table naming, @field: annotation prefix, FetchType.LAZY, EnumType.STRING, and enum class patterns.
---

# New JPA Entity Guide (Flooding)

## Entity Template

```kotlin
package team.incube.flooding.domain.{domain}.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "tb_{table_name}")  // singular snake_case with tb_ prefix
class {Domain}JpaEntity(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    // ManyToOne relation
    @field:ManyToOne(fetch = FetchType.LAZY)
    @field:JoinColumn(name = "{fk}_id", nullable = false)
    val relation: OtherJpaEntity,

    // Basic column
    @field:Column(name = "field_name", nullable = false)
    var field: String,

    // Nullable column
    @field:Column(name = "optional_field")
    var optionalField: String?,

    // Enum column
    @field:Column(name = "status", nullable = false, length = 50)
    @field:Enumerated(EnumType.STRING)
    var status: {Domain}Status,

    // Boolean column
    @field:Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    // Timestamps (optional)
    @field:Column(name = "created_at", nullable = false, updatable = false)
    @field:CreationTimestamp
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @field:Column(name = "updated_at", nullable = false)
    @field:UpdateTimestamp
    var updatedAt: LocalDateTime = LocalDateTime.now(),
)
```

## Enum Template

```kotlin
package team.incube.flooding.domain.{domain}.entity

enum class {Domain}Status {
    ACTIVE,
    INACTIVE,
}
```

## Conventions

| Rule | Detail |
|------|--------|
| Annotation prefix | `@field:` on all JPA/Hibernate annotations |
| Table name | `tb_` prefix + singular snake_case (e.g. `tb_club_form`) |
| FetchType | Always `LAZY` on `@ManyToOne`, `@OneToMany` |
| EnumType | Always `STRING` |
| GenerationType | Always `IDENTITY` |
| Immutable fields | `val` (id, createdAt, FK relations) |
| Mutable fields | `var` (business fields that can be updated) |
| Enum columns | Add `length = 50` to `@Column` |

## UniqueConstraint / Index (if needed)

```kotlin
@Entity
@Table(
    name = "tb_{domain}",
    uniqueConstraints = [UniqueConstraint(columnNames = ["col1", "col2"])],
    indexes = [Index(name = "idx_{domain}_col1", columnList = "col1")],
)
```

## File Location

`src/main/kotlin/team/incube/flooding/domain/{domain}/entity/{Domain}JpaEntity.kt`