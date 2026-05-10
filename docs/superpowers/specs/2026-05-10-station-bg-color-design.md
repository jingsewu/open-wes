# Design Spec: Station Page Background Color Unification

**Date:** 2026-05-10
**Branch:** feature_shelf_ai_refactor
**Status:** Approved

## Problem

The workstation page (`/wms/workStation/`) inherits `background-color: #f9fafb` from the global `.cxd-Layout-content` rule, while the operation area cards inside are hardcoded to `#fff`. This contrast creates a visual style that feels inconsistent with the homepage, which renders white panel content in a way that reads as a cleaner, unified white surface.

## Goal

Make the workstation page background white (`#fff`) to match the homepage's visual tone.

## Chosen Approach: Add background-color to .container (Method A)

Add `background-color: #fff` to the `.container` rule in `styles.module.scss`.

This is the minimal, scope-isolated change:
- CSS Modules scope ensures no other pages are affected.
- Does not touch global stylesheets.
- Does not require route-level wrapper changes.

## Change

**File:** `client/src/pages/wms/station/layout/styles.module.scss`

```scss
.container {
    display: flex;
    flex-direction: column;
    width: 100%;
    height: 100%;
    background-color: #fff;  /* NEW: override inherited #f9fafb */
    margin-bottom: 48px;
    overflow-x: hidden;
    overflow-y: scroll;
    ...
}
```

## Out of Scope

- `component-wrapper.tsx` inline `backgroundColor: "#fff"` — unchanged (redundant but harmless).
- `SelectStation` page — not part of the scoped layout, not affected.
- `WorkStationCard` (card view) — separate component, not affected.
- Global `style.scss` `.cxd-Layout-content` — unchanged.

## Visual Layer Hierarchy After Change

| Layer | Color |
|---|---|
| `cxd-Layout-content` (global) | `#f9fafb` |
| `.container` (station) | `#fff` (new) |
| `.operation-area` | `#fff` (inline, unchanged) |
| `.footer` | `#fff` (unchanged) |

Operation area visual separation is maintained through the existing `highlight` state (blue border + shadow) when an area is active.
