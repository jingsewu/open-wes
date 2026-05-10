# Layout Header Right Controls Redesign

**Date:** 2026-05-10
**Status:** Approved
**Scope:** `client/src/components/LayoutHeader.tsx`, `client/src/pages/components/Language.tsx`

---

## Problem

The header's right-side controls (warehouse selector, language switcher, user menu) are visually inconsistent and lack polish:

1. **Warehouse Select** — plain Ant Design `<Select>` with default border; no icon, no visual identity.
2. **Language** — uses AMIS `<Select>` (a different component library), creating inconsistent hover/focus styles and a visual mismatch with the surrounding Ant Design components.
3. **User menu** — heavy `type="primary" shape="round"` Button draws too much visual weight for a utility control in a navbar.

---

## Design

### Overall Principles

- All three controls use **hover-only gray background** (`#f3f4f6`) instead of persistent borders.
- **1px vertical dividers** (`#e5e7eb`, 18px tall) separate the three blocks.
- Color system reuses existing tokens: `#2563eb` (primary blue), `#374151` (text), `#9ca3af` (muted), `#e5e7eb` (border).
- Entire right zone: `marginLeft: "auto"`, `display: "flex"`, `alignItems: "center"`, `gap: 2`, `paddingRight: 16`.

---

### ① Warehouse Selector

**Visible only when `selectedApp === "wms"`** (unchanged condition).

- Ant Design `<Select>` with `bordered={false}` (removes default border).
- `suffixIcon`: small gray chevron (`<DownOutlined style={{ color: "#9ca3af", fontSize: 10 }}/>`).
- Prefix slot: warehouse SVG icon placed in a wrapper `div` before the Select. The wrapper handles the hover background and layout; the Select provides the dropdown. Use `bordered={false}` and `dropdownMatchSelectWidth={false}` on the Select. The visual treatment:

  ```
  [ 🏠  仓库 A  ▾ ]    ← transparent bg, hover → #f3f4f6
  ```

- `style` on the wrapper div:
  ```
  padding: "4px 10px", borderRadius: 6, cursor: "pointer",
  display: "flex", alignItems: "center", gap: 6, fontSize: 13,
  transition: "background 0.15s"
  ```
  `onMouseEnter` → `background: #f3f4f6`, `onMouseLeave` → `background: transparent`.

---

### ② Language Switcher

**Replace the AMIS `<Select>` in `Language.tsx` with an Ant Design `Dropdown` + icon-only `Button`.**

This eliminates the cross-library inconsistency.

**Component structure:**

```tsx
<Tooltip title={t("tooltip.switchLanguage")}>
  <Dropdown menu={{ items: languageMenuItems, onClick: handleLangClick }} trigger={["click"]}>
    <Button
      type="text"
      icon={<GlobalOutlined style={{ fontSize: 16, color: "#6b7280" }} />}
      style={{ width: 32, height: 32, padding: 0, borderRadius: 6 }}
    />
  </Dropdown>
</Tooltip>
```

`languageMenuItems`:
```ts
[
  { key: "zh-CN", label: "中文" },
  { key: "en-US", label: "English" }
]
```

`handleLangClick`: receives `{ key }` from Ant Design menu. Looks up the full option from the existing `languageList` array (`find(o => o.value === key)`), then calls `store.toggleLanguage(option)`, `i18n.changeLanguage(option.value)`, and `onLanguageChange(option)` — preserving the same contract as the old AMIS handler.

The `Language.tsx` file is updated in-place. Its public interface (`onLanguageChange` prop) remains unchanged.

---

### ③ User Menu

**Replace the `type="primary" shape="round" Button` with an avatar + name + chevron trigger.**

```tsx
<Dropdown menu={{ items, onClick: handleMenuClick }} trigger={["click"]}>
  <div style={{
    display: "flex", alignItems: "center", gap: 7,
    padding: "4px 8px", borderRadius: 6, cursor: "pointer",
    transition: "background 0.15s"
  }}
  onMouseEnter={e => (e.currentTarget.style.background = "#f3f4f6")}
  onMouseLeave={e => (e.currentTarget.style.background = "transparent")}
  >
    {/* Avatar: gradient circle with first letter of username */}
    <div style={{
      width: 26, height: 26, borderRadius: "50%", flexShrink: 0,
      background: "linear-gradient(135deg, #2563eb, #4f46e5)",
      display: "flex", alignItems: "center", justifyContent: "center",
      color: "#fff", fontSize: 11, fontWeight: 700
    }}>
      {store.user.name?.[0]?.toUpperCase() ?? "U"}
    </div>
    <span style={{ fontSize: 13, fontWeight: 500, color: "#374151" }}>
      {store.user.name}
    </span>
    <DownOutlined style={{ fontSize: 10, color: "#9ca3af" }} />
  </div>
</Dropdown>
```

The dropdown `items` and `handleMenuClick` logic are unchanged (change password + logout).

---

### Dividers

Two `<div>` dividers between the three blocks:

```tsx
<div style={{ width: 1, height: 18, background: "#e5e7eb", margin: "0 4px", flexShrink: 0 }} />
```

Positions: between ① and ②, and between ② and ③.

---

## Files Changed

| File | Change |
|------|--------|
| `src/components/LayoutHeader.tsx` | Refactor right-controls zone: borderless warehouse Select wrapper, two dividers, new user trigger |
| `src/pages/components/Language.tsx` | Replace AMIS Select with Ant Design `Tooltip` + `Dropdown` + `Button` (GlobalOutlined) |

No new files. No changes to routing, stores, or other components.

---

## Unchanged

- Warehouse selector visibility condition (`selectedApp === "wms"`)
- Dropdown menu items and click handler for user menu
- `ChangePasswordForm` modal logic
- Logo zone and collapse toggle
- App navigation `<Menu>`
- All props on the `Header` component (`HeaderProps` interface unchanged)
