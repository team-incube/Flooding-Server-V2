---
name: write-pr
description: Generate PR title and body from commits since the base branch, then create the PR on GitHub following the project PR template exactly.
---

## Step 1 — Gather Context

```bash
git branch --show-current
git log origin/develop..HEAD --oneline
git diff origin/develop...HEAD --stat
git diff origin/develop...HEAD
```

## Step 2 — Generate PR Title

Format: `[type] 설명`

- Type: `feat` / `fix` / `update` / `refactor` / `test` / `docs` / `style` / `perf`
- Description: Korean, concise, no emojis, max 50 characters total
- Generate 3 options and mark the best with `← 추천`

## Step 3 — Generate PR Body

Fill in the following template **exactly** (do not change the structure):

```
## #️⃣연관된 이슈

> #이슈번호

## 📝작업 내용

<변경 내용을 구체적으로 작성>

### 스크린샷 (선택)

## 💬리뷰 요구사항(선택)

> 리뷰어가 특별히 봐주었으면 하는 부분이 있다면 작성해주세요
```

Rules:
- Korean
- No emojis in content (template headings already have emojis, keep them)
- Describe what was changed and why concisely

## Step 4 — Show Preview & Confirm

```
## 추천 PR 제목
1. [title1]
2. [title2]
3. [title3] ← 추천

## PR 본문 미리보기
[body content]
```

Ask the user which title to use. If no response, use the recommended option.

## Step 5 — Create PR

```bash
gh pr create --title "<title>" --body "$(cat <<'EOF'
## #️⃣연관된 이슈

> #이슈번호

## 📝작업 내용

<내용>

### 스크린샷 (선택)

## 💬리뷰 요구사항(선택)

> <내용 또는 없으면 없음>
EOF
)"
```

Display the PR URL after creation.