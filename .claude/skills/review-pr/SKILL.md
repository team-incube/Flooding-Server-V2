---
name: review-pr
description: Check PR review comments, reflect valid feedback into code, commit, push, then reply to each comment with the resolving commit hash.
---

## Step 1 — Collect PR Comments

```bash
gh pr view --json number -q .number
gh api "repos/$(gh repo view --json nameWithOwner -q .nameWithOwner)/pulls/<pr_number>/comments" \
  --jq '.[] | {id: .id, path: .path, line: .line, body: .body}'
```

## Step 2 — Evaluate Each Comment

For each comment, decide:
- **반영** — valid suggestion, implement it
- **무시** — not applicable or disagree (explain why in reply)

## Step 3 — Implement Changes

Apply code changes for accepted comments.
Run `./gradlew ktlintFormat` after edits.

## Step 4 — Commit & Push

Only after the user says to commit:
```bash
git add <files>
git commit -m "update : 리뷰 반영 - <설명>"
git push
```

Get the short commit hash:
```bash
git log --oneline -1
```

## Step 5 — Reply to Each Comment

For each **반영** comment:
```bash
gh api "repos/<owner>/<repo>/pulls/<pr_number>/comments/<comment_id>/replies" \
  -f body="<short_hash> 에서 반영했습니다."
```

For each **무시** comment:
```bash
gh api "repos/<owner>/<repo>/pulls/<pr_number>/comments/<comment_id>/replies" \
  -f body="<이유> 때문에 반영하지 않았습니다."
```

## Step 6 — Report

```
## 반영한 코멘트
- [file] "comment" → <hash> 에서 반영했습니다.

## 반영하지 않은 코멘트
- [file] "comment" → 사유: ...
```

## Important

- Commit first, then post replies (replies must include the hash)
- Never reply before committing