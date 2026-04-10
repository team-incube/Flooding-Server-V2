---
name: commit
description: Create Git commits following Flooding project conventions. Splits changes into logical units with correct type prefix and Korean description.
---

## Commit Message Rules

Format: `type : 설명`

- **Space around colon** — always `type : 설명`, never `type:설명`
- **Types** (English): `feat` / `fix` / `update` / `add` / `test` / `docs` / `style` / `perf` / `refactor` / `merge`
- **Description**: Korean, no period, concise
- Subject line only (no body)
- Do NOT add AI tool as co-author

### Type Guide

| Type | When to use |
|------|------------|
| `feat` | New feature |
| `add` | Add files, configs, dependencies |
| `fix` | Bug fix |
| `update` | Modify existing feature or review feedback |
| `refactor` | Code restructuring without behavior change |
| `test` | Add or modify tests |
| `docs` | Documentation only |
| `style` | Formatting, ktlint |
| `perf` | Performance improvement |
| `merge` | Merge commit |

### Examples

```
feat : 동아리 폼 생성 API 구현
fix : 활성 폼 조회 시 NPE 수정
update : 리뷰 반영 - 폼 필드 UniqueConstraint 추가
test : 동아리 신청 서비스 단위 테스트 추가
perf : save 루프를 saveAll로 변경
style : ktlint 포맷팅 적용
```

## Commit Flow

1. Inspect changes: `git status`, `git diff`
2. Categorize into logical units (feature / bug fix / refactoring / etc.)
3. Group files per unit
4. For each group:
   - Stage only relevant files with `git add`
   - Write a commit message following the rules above
   - `git commit -m "message"`
5. Verify with `git log --oneline -n <count>`

## Important

- Only commit when the user explicitly asks (`커밋 ㄱㄱ`, `commit` 등)
- Never auto-commit without explicit instruction