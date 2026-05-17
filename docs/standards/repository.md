# Repository Hygiene Standards

---

## What Must NOT Be Committed

### AI tooling artifacts

AI coding assistants generate local working files that must never be tracked in git:

| Path | Description |
|------|-------------|
| `.superpowers/` | Brainstorm session HTML previews and state files |
| `.claude/settings.local.json` | Local Claude Code permission overrides |
| `.claude/projects/` | Local project memory (if present) |

These are already covered by the root `.gitignore`. If you add new AI tooling, update `.gitignore` immediately — before making the first commit on that branch.

### General artifacts to keep out

```gitignore
# IDE
.idea/
.vscode/
*.iml

# Build outputs
target/
build/
dist/
node_modules/

# Secrets & credentials
.env
.env.*
*.pem
*.key

# OS noise
.DS_Store
Thumbs.db

# Logs
*.log
```

---

## Checking Before You Commit

Run `git status` and scan for unexpected files before staging. Common patterns to look for:

- HTML files in non-`src` directories (brainstorm outputs)
- `server-info`, `server.pid` files (tooling process state)
- Any file under `.claude/`, `.superpowers/`, `.cursor/`, `.windsurf/`
- `.env` or credential files

If a file should not be tracked, add it to `.gitignore` and run `git rm --cached <file>` rather than simply deleting it — deletion alone does not remove it from git history.

---

## Updating .gitignore

When adding new tooling to the project (IDE plugins, AI assistants, build tools):

1. Identify all files/directories the tool creates locally
2. Add them to the **root** `.gitignore` immediately
3. If the tool only operates within a subdirectory (e.g., `client/`), also add to that directory's `.gitignore`
4. Verify with `git check-ignore -v <path>` that the pattern is effective

---

## CI / GitHub Actions

### Secrets

- Secret names in workflow files must exactly match the names configured in **Settings → Secrets and variables → Actions**
- Never hardcode credentials in workflow YAML files
- Use `${{ secrets.SECRET_NAME }}` syntax only

### Third-party actions

- Pin actions to a specific version tag (e.g., `actions/checkout@v4`), not `@main` or `@latest`
- When using `anthropics/claude-code-action`, verify the `model` parameter against the current supported model list — model IDs change over time

### Permissions

- Grant the minimum permissions needed (prefer `contents: read` over `contents: write` where possible)
- The `pull-requests: write` permission is required to post review comments
