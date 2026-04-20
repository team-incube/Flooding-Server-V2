---
name: security-checklist
description: Verify security vulnerabilities — hardcoded secrets, SQL injection, JWT validation, sensitive logging, and authorization checks. Run before merging any auth or API-related changes.
---

# Security Checklist

## Verification Items

### 1. Hardcoded Secrets
- [ ] No API Key, Secret, Password in code?
- [ ] Using environment variables or config files?

```bash
grep -r "password.*=.*\"" --include="*.kt" src/
grep -r "secret.*=.*\"" --include="*.kt" src/
grep -r "password\|secret\|jwt" --include="*.yml" --include="*.yaml" src/
```

### 2. SQL Injection
- [ ] Using JPA / Spring Data derived queries?
- [ ] Not concatenating SQL strings directly?

### 3. JWT Verification
- [ ] Verifying JWT signature?
- [ ] Checking expiration time?
- [ ] Claims validated?

### 4. Logging
- [ ] Not logging sensitive info (password, token, etc.)?
- [ ] Appropriate log level?

### 5. Authorization
- [ ] Auth-required endpoints protected via Spring Security?
- [ ] Users can only access their own resources?
- [ ] `CurrentUserProvider.getCurrentUser()` used instead of accepting userId from request body?

## Report Format

For each item:
- ✓ Pass
- ⚠ Warning (recommendation)
- ✗ Error (needs fix)

Final summary: `{n} items — {p} passed, {w} warnings, {e} errors`