# Design: Fix Sidebar Right Border Disappears on Sub-menu Expand

**Date:** 2026-05-06
**Branch:** feature_shelf_ai_refactor

## Problem

When a first-level menu item is selected/expanded in the left sidebar, the vertical divider line between the sidebar and the right content area disappears.

**Root Cause:** AMIS's `.cxd-Layout-aside::before` pseudo-element uses `border: inherit`, causing it to inherit the `border-right: 1px solid #e5e7eb` we set on `.cxd-Layout-aside`. In normal mode, `::before` has `z-index: -1`; in `asideFixed` mode, it becomes `position: fixed; z-index: 15`. When a sub-menu expands (`overflow: visible`, new stacking context at z-index 1200+ via `asideWrap`), the two borders conflict and the visual separator disappears.

## Solution: Move Separator to `.cxd-Layout-body` (Method B)

Remove `border-right` from `.cxd-Layout-aside` and place `border-left` on `.cxd-Layout-body` instead. The content-area element is completely independent of the aside's internal sub-menu state and stacking context changes.

### Changes to `src/scss/style.scss`

```scss
/* ─── Sidebar ─────────────────────────────────────── */
.cxd-Layout-aside {
    background: #fff !important;
    /* Remove border-right — moved to body side to avoid ::before inheritance conflict */
}

.cxd-Layout-body {
    border-left: 1px solid #e5e7eb !important;
}
```

### Trade-offs Accepted

- The separator position is always fixed at the `.cxd-Layout-body` left edge, meaning if the aside animates its width, the separator stays in the correct final position. This is acceptable since AMIS aside width changes update `.cxd-Layout-body`'s position anyway.
- The folded aside's separator will continue to show correctly since `.cxd-Layout-body` adjusts `margin-left` alongside the aside width.

## Out of Scope

- No changes to the folded-mode flyout sub-list (no right border needed there for this fix)
- No z-index changes
- No AsideNav component changes
