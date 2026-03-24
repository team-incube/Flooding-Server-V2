Analyze all local changes and create multiple commits split by logical unit. Do not commit everything in a single commit.

Steps:
1. Run `git status` and `git diff` to review all changes
2. Group changed files into logical units (e.g., one unit per feature, fix, or refactor)
3. For each unit:
   - Stage only the relevant files with `git add`
   - Commit with a message following the convention below
   - Run `git commit -m "message"`
4. After all commits, verify with `git log --oneline -n [number of commits]`

Commit message format: `type : description`

Types:
- add : add new code or file
- update : modify existing code
- fix : fix a bug
- delete : remove code or file
- docs : update documentation
- test : add or modify tests
- merge : merge branch
- init : initialize project

Rules:
- Write the description in Korean
- Keep the description concise and clear
- Do not include a period at the end
- Do not include local only files like ".env","settings.local.json"

Example: `add : userJpaEntity 추가`
