---
name: kotest-guide
description: Kotest + MockK testing patterns for this project — BehaviorSpec structure, mock creation, stubbing, argument capture, and exception assertions. Reference when writing or reviewing test code.
---

# Kotest + MockK Testing Guide

## Test Class Structure

Use `BehaviorSpec` with Given/When/Then:

```kotlin
class ExampleServiceTest : BehaviorSpec({
    val repository = mockk<ExampleRepository>()
    val service = ExampleServiceImpl(repository)

    given("활성 폼이 존재할 때") {
        every { repository.findById(1L) } returns entity

        `when`("execute를 호출하면") {
            val result = service.execute(1L)

            then("결과가 반환된다") {
                result.id shouldBe 1L
            }
        }
    }
})
```

## MockK Patterns

### Mock Creation

```kotlin
private val repository = mockk<ExampleRepository>()
private val service = ExampleServiceImpl(repository)
```

### Stubbing

```kotlin
// Return value
every { repository.findById(1L) } returns Optional.of(entity)

// Return null
every { repository.findByIdOrNull(1L) } returns null

// Any matcher
every { repository.save(any()) } returns entity
```

### Verification

```kotlin
// Verify call count
verify(exactly = 1) { repository.save(any()) }
verify(exactly = 0) { repository.delete(any()) }
```

### Argument Capture

```kotlin
val slot = slot<ExampleJpaEntity>()
every { repository.save(capture(slot)) } returns entity

service.execute(request)

slot.captured.name shouldBe "expected"
```

## Exception Testing

```kotlin
then("NOT_FOUND 예외가 발생한다") {
    val exception = shouldThrow<ExpectedException> {
        service.execute(999L)
    }
    exception.statusCode shouldBe HttpStatus.NOT_FOUND
}
```

## Assertions

```kotlin
result shouldBe expected
result shouldNotBe null
result.list shouldHaveSize 3
result.list[0].value shouldBe "first"
```

## Dependencies (build.gradle.kts)

```kotlin
testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
testImplementation("io.kotest:kotest-assertions-core:5.9.1")
testImplementation("io.mockk:mockk:1.13.17")
```

## Conventions

- Test file: `{ServiceName}Test.kt` under `src/test/kotlin/.../service/`
- No Spring context loading — instantiate service impl directly with mocked repos
- Test description in Korean using backtick method names or Given/When/Then structure