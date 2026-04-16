---
name: write-pr
description: Generate PR title and body from commits since the base branch, then create the PR on GitHub following the project PR template exactly.
---

## Step 1 — Gather Context

```bash
git branch --show-current
```

현재 브랜치가 `develop` 또는 `master`이면 즉시 중단:

```
현재 브랜치: develop
feature 브랜치를 먼저 생성하세요 (/new-branch)
```

feature 브랜치가 맞으면 계속 진행:

```bash
git log origin/develop..HEAD --oneline
git diff origin/develop...HEAD --stat
git diff origin/develop...HEAD
```

## Step 2 — Generate PR Title

Format: `설명`

- Description: Korean, concise, no emojis, max 50 characters
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

## Step 4 — Select Label

커밋 내용을 기반으로 Type 라벨 1개를 선택한다:

| 라벨 | 선택 기준 |
|------|---------|
| `📩 Type: Feature/Function` | 새 기능, API 구현, 성능 개선 |
| `📜 Type: Feature/Document` | 문서, 스킬, 에이전트, 설정 파일 |
| `🐞 Type: Bug/Function` | 버그 수정 |

Status 라벨은 항상 `🌟 Status: Reviewing`으로 고정.

## Step 5 — Show Preview & Confirm

```
## 추천 PR 제목
1. [title1]
2. [title2]
3. [title3] ← 추천

## 라벨
- 📩 Type: Feature/Function
- 🌟 Status: Reviewing

## PR 본문 미리보기
[body content]
```

Ask the user which title to use. If no response, use the recommended option.

## Step 6 — Create PR

Get the current GitHub username for assignee:

```bash
gh api user --jq .login
```

```bash
gh pr create \
  --title "<title>" \
  --base develop \
  --assignee "<github-username>" \
  --reviewer "team-incube/flooding-server" \
  --label "<Type 라벨>" \
  --label "🌟 Status: Reviewing" \
  --body "$(cat <<'EOF'
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