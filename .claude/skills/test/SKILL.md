---
name: test
description: Run tests and report results. Determines test scope based on context and analyzes failures in detail.
---

Run tests following these steps:

## Steps

1. **Determine test scope**:
    - If specific test class mentioned: run that test
    - If specific domain affected: run related tests
    - Otherwise: run all tests

2. **Run tests**:

    ```bash
    # Specific test class
    ./gradlew test --tests "*{TestClassName}*"

    # All tests
    ./gradlew test
    ```

3. **Analyze results**:
    - Show test summary
    - If failures: show failure messages and suggest fixes
    - If success: confirm all tests passed

4. **Report**:
    - Total tests run
    - Pass/Fail count

Do NOT claim success without running the tests first.