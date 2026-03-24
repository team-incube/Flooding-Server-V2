---
name: new-branch
description: Creates a new git branch following the project's branch naming convention. Use this skill whenever the user wants to start new work, create a branch, switch to a new branch.
---

Create a new git branch and switch to it, following the project branch naming convention.

Steps:
1. If the purpose of the branch is unclear, ask the user before proceeding
2. Determine the appropriate type and write a concise kebab-case description in English
3. Run `git checkout -b type/description`
4. Confirm with `git branch --show-current`

Branch name format: `type/description`

Types:
- feat: new feature
- fix: bug fix
- style: code formatting (no logic change)
- refactor: code refactoring
- comment: add or update comments
- docs: documentation update
- test: test related changes
- chore: build or package manager config (e.g. Gradle)
- rename: rename or move files/folders
- remove: delete files/folders

Rules:
- Use kebab-case for the description
- Keep the description short and descriptive
- Write the description in English

Example: `feat/user-jpa-entity`
