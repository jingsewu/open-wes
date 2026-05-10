# Aside Border Fix Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix the sidebar right-border divider disappearing when a sub-menu is expanded.

**Architecture:** Move the 1px separator from `border-right` on `.cxd-Layout-aside` to `border-left` on `.cxd-Layout-body`. The content body element is independent of the aside's stacking context, so the separator stays visible regardless of sub-menu state.

**Tech Stack:** SCSS (`src/scss/style.scss`), AMIS layout classes (`cxd-Layout-*`)

---

### Task 1: Update `style.scss` — replace aside border with body border-left

**Files:**
- Modify: `src/scss/style.scss:38-41`

- [ ] **Step 1: Remove `border-right` from `.cxd-Layout-aside` and add `border-left` to `.cxd-Layout-body`**

In `src/scss/style.scss`, find the sidebar section (line ~37) and make this exact change:

**Before:**
```scss
/* ─── Sidebar ─────────────────────────────────────── */
.cxd-Layout-aside {
    background: #fff !important;
    border-right: 1px solid #e5e7eb !important;
}
```

**After:**
```scss
/* ─── Sidebar ─────────────────────────────────────── */
.cxd-Layout-aside {
    background: #fff !important;
}

.cxd-Layout-body {
    border-left: 1px solid #e5e7eb !important;
}
```

- [ ] **Step 2: Visual verification**

Start the dev server and open the app in a browser:

```bash
npm run dev
# or: yarn dev
```

Check the following:

1. **No sub-menu open**: Left sidebar shows a 1px gray `#e5e7eb` vertical line on its right edge.
2. **First-level item clicked (sub-menu expands)**: The vertical line remains visible between the expanded sidebar and the content area.
3. **Sidebar folded** (`store.asideFolded = true`, click the fold toggle): The 1px line appears at the new (narrower) sidebar boundary.
4. **Sidebar folded + hover over icon item** (shows flyout sub-list): The flyout panel appears to the right; the border-left on `.cxd-Layout-body` is still visible at the body's left edge (to the right of the flyout).

- [ ] **Step 3: Commit**

```bash
git add src/scss/style.scss
git commit -m "fix(aside): move sidebar separator to body border-left to fix sub-menu disappearing line"
```
