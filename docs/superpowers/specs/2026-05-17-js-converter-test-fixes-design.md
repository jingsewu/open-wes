# JS Converter Test — i18n & Dialog Close Bug Fixes

**Date:** 2026-05-17
**Commit under review:** `86ab271ac4ee3c43a9afd55ec6edc0b120e1fc5f`
**Affected file:** `client/src/pages/api_platform/api_management.tsx`

---

## Problem Summary

Commit `86ab271` added a JS converter test UI (test input/output textarea + test button) inside the parameter conversion configuration dialog. It introduced two bugs:

1. **Missing i18n keys** — three new label/button keys were used in the schema but never added to either locale file.
2. **Dialog closes when error toast is clicked** — after a failed test request, clicking the AMIS error notification closes the entire "Parameter conversion configuration" dialog.

---

## Root Cause Analysis

### Bug 1 — Missing i18n keys

Three keys were referenced in `api_management.tsx` but are absent from both `client/src/locales/en-us.json` and `client/src/locales/zh-cn.json`:

| Key | Used at |
|-----|---------|
| `interfacePlatform.interfaceManagement.form.testInputJson` | textarea label (×2) |
| `interfacePlatform.interfaceManagement.form.testOutputJson` | static field label (×2) |
| `interfacePlatform.interfaceManagement.button.testConverter` | button label (×2) |

### Bug 2 — Dialog closes on error toast click

**Component chain:**
- `routes/index.tsx` renders `<ToastComponent position="top-center" />` at the page root — outside the dialog DOM tree.
- `env.notify('error', ...)` fires when `saveRemote` fails, placing the toast in that top-level component.
- The "Parameter conversion configuration" dialog is configured with `closeOnOutside: true`.
- AMIS Dialog.js line 430: `closeOnOutside: !store.dialogOpen && closeOnOutside` — this only suppresses close-on-outside when a *child dialog* is open; it does not account for external toast elements.
- Clicking the toast to dismiss it is a DOM click that falls outside the dialog element, triggering the `closeOnOutside` handler and closing the dialog.

**Why `close: false` on the button would NOT fix this:** The dialog is not being closed by the button's action result. It is closed by a subsequent user click on the toast element after the action has already completed.

---

## Fix Design

### Fix 1 — Add missing i18n keys

Add to `client/src/locales/en-us.json`:
```json
"interfacePlatform.interfaceManagement.form.testInputJson": "Test input JSON",
"interfacePlatform.interfaceManagement.form.testOutputJson": "Test output JSON",
"interfacePlatform.interfaceManagement.button.testConverter": "Test converter"
```

Add to `client/src/locales/zh-cn.json`:
```json
"interfacePlatform.interfaceManagement.form.testInputJson": "测试输入 JSON",
"interfacePlatform.interfaceManagement.form.testOutputJson": "测试输出 JSON",
"interfacePlatform.interfaceManagement.button.testConverter": "测试转换器"
```

Keys should be inserted adjacent to existing `interfacePlatform.interfaceManagement.form.*` and `interfacePlatform.interfaceManagement.button.*` entries for readability.

### Fix 2 — Remove `closeOnOutside: true` from the config dialog

In `api_management.tsx`, the "Parameter conversion configuration" dialog:

```js
// Before
dialog: {
    title: "...",
    closeOnEsc: true,
    closeOnOutside: true,   // <-- remove this
    size: "xl",
    body: { ... }
}
```

Remove `closeOnOutside: true`. Keep `closeOnEsc: true` (pressing Escape to close is intentional and doesn't involve external DOM elements).

This is correct because:
- The dialog contains an unsaved form; accidental close-on-outside would lose the user's script edits.
- The dialog has an explicit save button and close/cancel affordances.
- `closeOnEsc` still allows keyboard-driven dismissal.

---

## Scope

- 3 files modified: `en-us.json`, `zh-cn.json`, `api_management.tsx`
- No backend changes required.
- No new components, no refactoring.
