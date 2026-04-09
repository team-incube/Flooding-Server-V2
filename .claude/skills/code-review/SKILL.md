---
name: code-review
description: Run a structured checklist over changed files — Kotlin style, JPA/transaction correctness, naming conventions, test coverage, and security basics. Produces a ✓/⚠/✗ report.
---

# Code Review Guide

## Check Changes

```bash
git diff develop...HEAD --stat
git diff develop...HEAD
```

Read each changed file for detailed analysis.

## Checklist

### Naming Conventions
- [ ] Entity: `{Domain}JpaEntity`?
- [ ] Redis repository: `{Domain}RedisRepository`?
- [ ] Table name singular + snake_case?
- [ ] DTO prefix correct? (`Get`, `Create`, `Patch`, `Delete`, `Search`)
- [ ] URL plural for collections, singular for actions?

### Kotlin Style
- [ ] Using `val` over `var` where possible?
- [ ] Constructor injection (not field injection)?
- [ ] Null safety handled properly (no `!!` unless justified)?
- [ ] `@field:` annotation prefix used on JPA/validation annotations?

### JPA / Database
- [ ] `FetchType.LAZY` on all `@ManyToOne`, `@OneToMany`?
- [ ] `EnumType.STRING` for all `@Enumerated`?
- [ ] `GenerationType.IDENTITY` for `@GeneratedValue`?
- [ ] No transaction opened in repository layer?
- [ ] N+1 problem? (consider batch queries / `saveAll`)

### Service Layer
- [ ] `@Transactional` in service only, not repository?
- [ ] `ExpectedException("msg", HttpStatus.XXX)` used directly (no subclasses)?
- [ ] Interface + impl pattern followed?

### Controller Layer
- [ ] Swagger `@Operation`, `@ApiResponse` annotations present?
- [ ] `@RequestBody` parameter named `request`?
- [ ] Correct HTTP status codes returned?

### Test
- [ ] Test code written for new service logic?
- [ ] Mocking repositories with MockK (no Spring context)?
- [ ] Edge cases covered (not found, conflict, bad request)?

### Security
- [ ] No hardcoded secrets?
- [ ] No sensitive info in logs?

## Report Format

For each item:
- ✓ Pass
- ⚠ Warning (recommendation)
- ✗ Error (needs fix)

Final summary: `{n} items — {p} passed, {w} warnings, {e} errors`