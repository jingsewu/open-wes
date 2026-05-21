# JS Converter Test — i18n & Dialog Close Bug Fixes Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix two bugs introduced in commit `86ab271`: missing i18n keys for the converter test UI, and the parameter conversion dialog closing when the error toast is clicked.

**Architecture:** Two isolated changes to three files. No component logic changes. i18n: add 3 keys to each locale file. Dialog bug: remove `closeOnOutside: true` from the parameter conversion configuration dialog only (not the modify drawer).

**Tech Stack:** React 17, TypeScript, AMIS 6.13.0 JSON schema, i18n via flat-key JSON locale files.

---

## File Map

| File | Change |
|------|--------|
| `client/src/locales/en-us.json` | Add 3 missing i18n keys |
| `client/src/locales/zh-cn.json` | Add 3 missing i18n keys (Chinese) |
| `client/src/pages/api_platform/api_management.tsx` | Remove `closeOnOutside: true` from the config dialog |

---

### Task 1: Add missing i18n keys to English locale

**Files:**
- Modify: `client/src/locales/en-us.json`

**Context:** The locale file uses flat dot-notation keys (not nested objects). The new keys must live near their siblings for readability. Existing relevant entries are around line 570 (button group) and lines 593–597 (form group).

- [ ] **Step 1: Add the button key after the existing button entry**

In `client/src/locales/en-us.json`, find:
```json
    "interfacePlatform.interfaceManagement.button.parameterConversionConfiguration": "Parameter conversion configuration",
```
Change it to:
```json
    "interfacePlatform.interfaceManagement.button.parameterConversionConfiguration": "Parameter conversion configuration",
    "interfacePlatform.interfaceManagement.button.testConverter": "Test converter",
```

- [ ] **Step 2: Add the two form keys after the existing form entries**

In `client/src/locales/en-us.json`, find:
```json
    "interfacePlatform.interfaceManagement.form.responseTransformationScripts": "Response transformation scripts",
```
Change it to:
```json
    "interfacePlatform.interfaceManagement.form.responseTransformationScripts": "Response transformation scripts",
    "interfacePlatform.interfaceManagement.form.testInputJson": "Test input JSON",
    "interfacePlatform.interfaceManagement.form.testOutputJson": "Test output JSON",
```

- [ ] **Step 3: Verify the JSON is valid**

```bash
cd client && node -e "require('./src/locales/en-us.json'); console.log('en-us.json OK')"
```
Expected output: `en-us.json OK`

- [ ] **Step 4: Commit**

```bash
git add client/src/locales/en-us.json
git commit -m "fix: add missing i18n keys for JS converter test UI (en)"
```

---

### Task 2: Add missing i18n keys to Chinese locale

**Files:**
- Modify: `client/src/locales/zh-cn.json`

**Context:** Same structure as en-us.json. Existing relevant entries are around the same relative positions in zh-cn.json (lines 570 and 592–597).

- [ ] **Step 1: Add the button key after the existing button entry**

In `client/src/locales/zh-cn.json`, find:
```json
    "interfacePlatform.interfaceManagement.button.parameterConversionConfiguration": "参数转换配置",
```
Change it to:
```json
    "interfacePlatform.interfaceManagement.button.parameterConversionConfiguration": "参数转换配置",
    "interfacePlatform.interfaceManagement.button.testConverter": "测试转换器",
```

- [ ] **Step 2: Add the two form keys after the existing form entries**

In `client/src/locales/zh-cn.json`, find:
```json
    "interfacePlatform.interfaceManagement.form.responseTransformationScripts": "响应转换脚本",
```
Change it to:
```json
    "interfacePlatform.interfaceManagement.form.responseTransformationScripts": "响应转换脚本",
    "interfacePlatform.interfaceManagement.form.testInputJson": "测试输入 JSON",
    "interfacePlatform.interfaceManagement.form.testOutputJson": "测试输出 JSON",
```

- [ ] **Step 3: Verify the JSON is valid**

```bash
cd client && node -e "require('./src/locales/zh-cn.json'); console.log('zh-cn.json OK')"
```
Expected output: `zh-cn.json OK`

- [ ] **Step 4: Commit**

```bash
git add client/src/locales/zh-cn.json
git commit -m "fix: add missing i18n keys for JS converter test UI (zh)"
```

---

### Task 3: Remove `closeOnOutside` from parameter conversion dialog

**Files:**
- Modify: `client/src/pages/api_platform/api_management.tsx` (around line 523)

**Context:** There are two `closeOnOutside: true` entries in this file:
- Line ~508: inside the **modify drawer** (`actionType: "drawer"`) — **leave this one untouched**
- Line ~523: inside the **parameter conversion dialog** (`actionType: "dialog"`) — **remove this one**

Root cause: `ToastComponent` is mounted at page root (outside the dialog DOM). Clicking the error toast triggers the `closeOnOutside` handler on the dialog. Removing this property means the user must explicitly click close/cancel to dismiss the dialog.

- [ ] **Step 1: Remove `closeOnOutside: true` from the config dialog**

In `client/src/pages/api_platform/api_management.tsx`, find:
```typescript
                            dialog: {
                                title: "interfacePlatform.interfaceManagement.dialog.modifyParameterConversionConfiguration",
                                closeOnEsc: true,
                                closeOnOutside: true,
                                size: "xl",
```
Change it to:
```typescript
                            dialog: {
                                title: "interfacePlatform.interfaceManagement.dialog.modifyParameterConversionConfiguration",
                                closeOnEsc: true,
                                size: "xl",
```

- [ ] **Step 2: Verify the modify drawer still has its `closeOnOutside: true` untouched**

```bash
grep -n "closeOnOutside" client/src/pages/api_platform/api_management.tsx
```
Expected output: exactly one line, inside the `drawer:` block (not the `dialog:` block):
```
508:                                closeOnOutside: true,
```
(line number may shift by ±1 after Task 1/2 edits; confirm it's inside the `drawer` block for `button.modify`, not the `dialog` block for param config)

- [ ] **Step 3: Commit**

```bash
git add client/src/pages/api_platform/api_management.tsx
git commit -m "fix: prevent param config dialog from closing when error toast is clicked"
```

---

## Manual Verification Checklist

After all three tasks:

1. Open the Interface Management page.
2. Click "Parameter conversion configuration" on any row.
3. Set the converter type to `JS`.
4. Enter invalid JS in the script editor, enter any JSON in the test input field, click "Test converter".
5. **Expected**: An error toast appears at the top. Clicking the toast dismisses only the toast — the dialog stays open.
6. Enter valid JS that transforms the input, click "Test converter" again.
7. **Expected**: The output field updates with the result. The dialog stays open.
8. Switch the UI language to English, reopen the dialog — verify the test input/output labels and button label appear in English (not as raw key strings).
9. Switch to Chinese — verify the labels appear in Chinese.
