# Homepage Layout Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Upgrade the main admin layout from the old flat-blue AMIS template to a clean light theme with a unified full-width header and white sidebar.

**Architecture:** CSS overrides in `style.scss` handle AMIS/Ant Design class recoloring; `LayoutHeader.tsx` is restructured to output a single-row header (Logo zone left + nav controls right); hardcoded inline `color: "#fff"` styles in `LayoutAside.tsx` are removed so CSS drives text color; the AI floating button is shrunk to a standard 40px circle.

**Tech Stack:** React (class components), TypeScript, AMIS (`cxd-` CSS prefix), Ant Design v4, SCSS modules, MobX

---

## File Map

| File | Change Type | Responsibility |
|---|---|---|
| `src/scss/style.scss` | Modify | All AMIS + Ant Design CSS overrides — sidebar color, header height/layout, active states, tab bar, content background |
| `src/components/LayoutHeader.tsx` | Modify | JSX restructure: hide `cxd-Layout-brandBar`, move Logo into `cxd-Layout-headerBar`, flex layout with Logo zone + app nav + right controls |
| `src/components/LayoutAside.tsx` | Modify | Remove three occurrences of hardcoded `style={{ color: "#fff" }}` on icon `<i>` and label `<span>` elements |
| `src/pages/index.module.scss` | Modify | Resize floating AI button to 40px circle, update shadow and hover |
| `src/pages/index.tsx` | Modify | Change `RobotSvg` fontSize from 60 to 20, add `type="primary"` and size styles to Button |

---

## Task 1: Update CSS overrides in `style.scss`

**Files:**
- Modify: `src/scss/style.scss`

**Background:** AMIS uses `cxd-` prefixed classes. Our `style.scss` already has overrides for sidebar and header. We're replacing the blue theme with white/light. The brandBar will be hidden via `display: none` — its Logo content moves to the headerBar in Task 2. Tab bar uses Ant Design v4 `.ant-tabs-card` classes.

- [ ] **Step 1: Open the dev server to capture the before state**

  Run in terminal (leave running throughout):
  ```bash
  cd D:/git_workspace/open-wes/client
  npm run dev
  ```
  Open `http://localhost:3000` (or the port shown). Note the current blue sidebar and split header. This is the baseline.

- [ ] **Step 2: Replace the layout-related overrides in `style.scss`**

  In `src/scss/style.scss`, replace the block from line 9 to line 35 (the four existing layout overrides) with the following:

  ```scss
  /* ─── Unified Header ─────────────────────────────── */
  .cxd-Layout-brandBar {
      display: none !important;
  }

  .cxd-Layout-headerBar {
      display: flex !important;
      align-items: center !important;
      height: 52px !important;
      padding: 0 !important;
      background-color: #fff;
      border-bottom: 1px solid #e5e7eb;
      box-shadow: 0 1px 4px rgb(0 0 0 / 6%);
  }

  /* ─── Sidebar ─────────────────────────────────────── */
  .cxd-Layout-aside {
      background: #fff !important;
      border-right: 1px solid #e5e7eb;
  }

  .cxd-AsideNav-item.is-active > a {
      background: #eff6ff !important;
      color: #2563eb !important;
      position: relative;

      &::before {
          content: "";
          position: absolute;
          left: 0;
          top: 50%;
          transform: translateY(-50%);
          width: 3px;
          height: 16px;
          background: #2563eb;
          border-radius: 2px;
      }

      .cxd-AsideNav-itemLabel {
          color: #2563eb !important;
      }

      i {
          color: #2563eb !important;
      }
  }

  .cxd-AsideNav-subList {
      background-color: #fff !important;
      color: #374151 !important;
  }

  .cxd-AsideNav-item a:hover {
      background: #f9fafb !important;
      color: #374151 !important;

      .cxd-AsideNav-itemLabel {
          color: #374151 !important;
      }
  }

  .cxd-AsideNav-itemLabel {
      color: #374151 !important;
  }
  ```

- [ ] **Step 3: Update content background**

  In the same file, find and update the `.cxd-Layout-content` block (currently around line 223):
  ```scss
  // Before:
  .cxd-Layout-content {
      background-color: #f4f4f5;
  }

  // After:
  .cxd-Layout-content {
      background-color: #f9fafb;
  }
  ```

- [ ] **Step 4: Add Tab bar overrides**

  At the end of `src/scss/style.scss`, append:
  ```scss
  /* ─── Tab bar (Ant Design v4 editable-card) ─────── */
  .ant-tabs-card > .ant-tabs-nav .ant-tabs-tab {
      background: #f9fafb !important;
      border: 1px solid #f3f4f6 !important;
      border-bottom: none !important;
      border-radius: 6px 6px 0 0 !important;
      color: #6b7280;
      margin: 0 2px 0 0 !important;
      transition: background 0.15s;
  }

  .ant-tabs-card > .ant-tabs-nav .ant-tabs-tab-active {
      background: #f0f9ff !important;
      border: 1px solid #bae6fd !important;
      border-bottom: 2px solid #0284c7 !important;
      margin-bottom: -1px !important;
      border-radius: 6px 6px 0 0 !important;

      .ant-tabs-tab-btn {
          color: #0284c7 !important;
          font-weight: 500;
      }
  }

  .ant-tabs-card > .ant-tabs-nav .ant-tabs-tab .ant-tabs-tab-btn {
      color: #6b7280;
  }
  ```

- [ ] **Step 5: Verify CSS changes in browser**

  Check in `http://localhost:3000` after saving:
  - Sidebar background is now **white** (not blue)
  - Sidebar text is **dark gray** (not white)
  - The header still exists (even if visually wrong — that's fixed in Task 2)
  - Active nav item has a **blue left border + light blue background**
  - Inactive nav items show **dark text** with a light hover state
  - Content area background is slightly lighter

- [ ] **Step 6: Commit**

  ```bash
  git add src/scss/style.scss
  git commit -m "style: update layout theme to clean light — white sidebar, unified header CSS"
  ```

---

## Task 2: Refactor `LayoutHeader.tsx` — unified full-width header

**Files:**
- Modify: `src/components/LayoutHeader.tsx`

**Background:** Currently the component outputs two sibling divs: `cxd-Layout-brandBar` (Logo) and `cxd-Layout-headerBar` (nav). After Task 1, `brandBar` is hidden. This task moves the Logo into `headerBar` and builds the flex layout: Logo zone (180px, right border) → collapse toggle → app Menu → spacer → warehouse/language/user controls.

The AMIS `Layout` component renders whatever you pass as `header` prop directly. We just need our JSX to output a single `.cxd-Layout-brandBar` (empty/hidden) + one `.cxd-Layout-headerBar` with all the content.

**Add one new import** at the top: `Divider` from antd is not needed — we'll use a plain `<span>` for the vertical divider.

- [ ] **Step 1: Replace the entire return statement in `LayoutHeader.tsx`**

  The file currently exports a `Header` functional component. Replace its `return (...)` (lines 71–157) with:

  ```tsx
  return (
      <>
          {/* brandBar is hidden via CSS (display:none) — kept so AMIS layout structure is intact */}
          <div className="cxd-Layout-brandBar" />

          <div className="cxd-Layout-headerBar">
              {/* ── Logo zone — visually aligns with sidebar width ── */}
              <div
                  style={{
                      width: 180,
                      height: "100%",
                      display: "flex",
                      alignItems: "center",
                      padding: "0 16px",
                      gap: 8,
                      flexShrink: 0,
                      borderRight: "1px solid #e5e7eb"
                  }}
              >
                  <Icon
                      component={() => <LogoSvg />}
                      style={{ fontSize: "22px", color: "#2563eb" }}
                  />
                  <span style={{ fontSize: 14, fontWeight: 700, color: "#111827", whiteSpace: "nowrap" }}>
                      OPEN-WES
                  </span>
              </div>

              {/* ── Collapse toggle ── */}
              <Button
                  className="no-shadow navbar-btn"
                  type="text"
                  style={{ marginLeft: 8 }}
                  onClick={store.toggleAsideFolded}
              >
                  <i className={store.asideFolded ? "fa fa-indent" : "fa fa-outdent"} />
              </Button>

              {/* ── App navigation ── */}
              <Menu
                  onClick={onApplicationChange}
                  selectedKeys={[selectedApp]}
                  mode="horizontal"
                  items={applications}
                  style={{ borderBottom: "none", background: "transparent", marginTop: 0 }}
              />

              {/* ── Right controls ── */}
              <div style={{ marginLeft: "auto", display: "flex", alignItems: "center", gap: 8, paddingRight: 16 }}>
                  {selectedApp === "wms" && (
                      <Select
                          placeholder="select warehouse"
                          optionFilterProp="children"
                          onChange={onWarehouseChange}
                          value={selectedWarehouse}
                          options={warehouses}
                      />
                  )}
                  <Language onLanguageChange={onLanguageChange} />
                  <Dropdown
                      menu={{ items, onClick: handleMenuClick }}
                      trigger={["click"]}
                  >
                      <Button type="primary" shape="round">
                          <Space>
                              {store.user.name}
                              <DownOutlined />
                          </Space>
                      </Button>
                  </Dropdown>
                  <ChangePasswordForm
                      isModalOpen={isModalOpen}
                      onModalCancel={handleModalCancel}
                  />
              </div>
          </div>
      </>
  )
  ```

- [ ] **Step 2: Remove the now-unused `pull-left` / `pull-right` wrapper divs**

  The old nav divs with Bootstrap utility classes (`nav navbar-nav hidden-xs pull-left`, `m-l-auto hidden-xs pull-right`) are replaced by the new flex layout in Step 1. Confirm the file no longer has these div wrappers — they were removed in the replacement above.

- [ ] **Step 3: Verify in browser**

  Check `http://localhost:3000`:
  - Header is a **single row** at the top, spanning full width
  - **Logo + OPEN-WES** text is in the leftmost ~180px, right-aligned with the sidebar
  - A vertical divider separates Logo zone from the rest
  - App navigation (仓储平台, etc.) is in the middle
  - Warehouse selector, language, user button are on the **right**
  - The blue brand bar section is **gone**
  - Collapse toggle (≡) still works — click it to fold/unfold sidebar

- [ ] **Step 4: Commit**

  ```bash
  git add src/components/LayoutHeader.tsx
  git commit -m "refactor(header): merge brandBar and headerBar into unified single-row header"
  ```

---

## Task 3: Update `LayoutAside.tsx` — remove hardcoded white text colors

**Files:**
- Modify: `src/components/LayoutAside.tsx`

**Background:** Three places in `LayoutAside.tsx` hardcode `style={{ color: "#fff" }}` on icon and label elements. These override the CSS we set in Task 1, keeping the text white. We remove them so CSS classes control the colors.

- [ ] **Step 1: Remove hardcoded color from the folded icon with `link.icon` (line ~74)**

  Find this block:
  ```tsx
  if (store.asideFolded && link.icon) {
      children.push(
          <i
              key="icon"
              className={cx(`AsideNav-itemIcon`, link.icon)}
              style={{ color: "#fff" }}
          />
      )
  ```

  Remove `style={{ color: "#fff" }}`:
  ```tsx
  if (store.asideFolded && link.icon) {
      children.push(
          <i
              key="icon"
              className={cx(`AsideNav-itemIcon`, link.icon)}
          />
      )
  ```

- [ ] **Step 2: Remove hardcoded color from the folded fallback icon (line ~80)**

  Find this block:
  ```tsx
  } else if (store.asideFolded && depth === 1) {
      children.push(
          <i
              key="icon"
              className={cx(
                  `AsideNav-itemIcon`,
                  hasChildren ? "fa fa-folder" : "fa fa-info"
              )}
              style={{ color: "#fff" }}
          />
      )
  ```

  Remove `style={{ color: "#fff" }}`:
  ```tsx
  } else if (store.asideFolded && depth === 1) {
      children.push(
          <i
              key="icon"
              className={cx(
                  `AsideNav-itemIcon`,
                  hasChildren ? "fa fa-folder" : "fa fa-info"
              )}
          />
      )
  ```

- [ ] **Step 3: Remove hardcoded color from the label `<span>` (line ~89)**

  Find this block:
  ```tsx
  children.push(
      <span
          key="label"
          className={cx("AsideNav-itemLabel")}
          style={{
              color: "#fff"
          }}
      >
  ```

  Remove the `style` prop entirely:
  ```tsx
  children.push(
      <span
          key="label"
          className={cx("AsideNav-itemLabel")}
      >
  ```

- [ ] **Step 4: Verify in browser**

  Check `http://localhost:3000`:
  - Sidebar text is **dark gray** (`#374151`) in all states
  - Active nav item text is **blue** (`#2563eb`)
  - Collapse the sidebar (click toggle) — icons should appear in **dark/blue** (not white on white)
  - Expand back — text labels are dark again

- [ ] **Step 5: Commit**

  ```bash
  git add src/components/LayoutAside.tsx
  git commit -m "style(aside): remove hardcoded white color from nav items, let CSS theme control colors"
  ```

---

## Task 4: Resize the AI floating button

**Files:**
- Modify: `src/pages/index.module.scss`
- Modify: `src/pages/index.tsx`

**Background:** The current button uses a 60px `RobotSvg` icon inside a text-type Ant Design button, which renders as an oversized floating element. We change it to a 40px primary circle button with a blue box-shadow and smaller icon.

- [ ] **Step 1: Replace the `.fixButton` block in `index.module.scss`**

  Replace the entire content of `src/pages/index.module.scss` with:

  ```scss
  .fixButton {
      position: fixed;
      bottom: 24px;
      right: 24px;
      z-index: 1000;
      transition: transform 0.2s ease;

      &:hover {
          transform: scale(1.08);
      }
  }

  /* Tooltip */
  .fixButton .tooltip {
      visibility: hidden;
      width: 120px;
      background-color: #1e293b;
      color: #fff;
      text-align: center;
      border-radius: 6px;
      padding: 5px;
      position: absolute;
      z-index: 1;
      bottom: 125%;
      left: 50%;
      margin-left: -60px;
      opacity: 0;
      transition: opacity 0.2s;
      font-size: 12px;
      white-space: nowrap;
  }

  .fixButton .tooltip::after {
      content: "";
      position: absolute;
      top: 100%;
      left: 50%;
      margin-left: -5px;
      border-width: 5px;
      border-style: solid;
      border-color: #1e293b transparent transparent transparent;
  }

  .fixButton:hover .tooltip {
      visibility: visible;
      opacity: 1;
  }
  ```

- [ ] **Step 2: Update the AI button JSX in `index.tsx`**

  In `src/pages/index.tsx`, find the `<Affix>` block (around line 347):

  ```tsx
  <Affix className={cx("fixButton")}>
      <Button
          type="text"
          shape="circle"
          icon={<RobotSvg style={{fontSize: 60}}/>}
          onClick={this.handleClick}
      ></Button>
      <span className="tooltip">
          {<Translation>{(t) => t("ai.chat.span")}</Translation>}
      </span>
  </Affix>
  ```

  Replace with:

  ```tsx
  <Affix className={cx("fixButton")}>
      <Button
          type="primary"
          shape="circle"
          icon={<RobotSvg style={{ fontSize: 20 }} />}
          onClick={this.handleClick}
          style={{
              width: 40,
              height: 40,
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              boxShadow: "0 4px 14px rgba(37, 99, 235, 0.4)"
          }}
      />
      <span className="tooltip">
          <Translation>{(t) => t("ai.chat.span")}</Translation>
      </span>
  </Affix>
  ```

- [ ] **Step 3: Verify in browser**

  Check `http://localhost:3000`:
  - AI button in the **bottom-right** is a small 40px blue circle
  - No oversized SVG overflowing the button boundary
  - Hover shows tooltip text above the button
  - Clicking still opens the Chatbot modal

- [ ] **Step 4: Commit**

  ```bash
  git add src/pages/index.module.scss src/pages/index.tsx
  git commit -m "style(ai-button): resize floating chat button to 40px circle with blue shadow"
  ```

---

## Task 5: Final cross-check and polish commit

**Files:**
- Modify: `src/scss/style.scss` (if any residual issues found)

- [ ] **Step 1: Full visual walkthrough checklist**

  Open `http://localhost:3000` and verify each point:

  | Check | Expected |
  |---|---|
  | Top header | Single row, white background, full width, 52px tall |
  | Logo area | OPEN-WES icon + text in left ~180px, vertical separator on right |
  | Sidebar background | White (`#fff`), right border `#e5e7eb` |
  | Sidebar text (default) | Dark gray (`#374151`) |
  | Sidebar active item | Light blue bg (`#eff6ff`), blue text + 3px left bar |
  | Sidebar hover | Very light gray (`#f9fafb`) |
  | Sidebar collapsed | Icons show in dark/blue (not white on white) |
  | Tab bar — active tab | Light blue bg, blue bottom underline, blue text |
  | Tab bar — inactive tabs | Gray bg, gray text |
  | Content area bg | Slightly warm gray (`#f9fafb`) |
  | AI button | 40px blue circle, bottom-right, proper shadow |
  | Collapse toggle | Works — sidebar folds/unfolds |
  | App switch (仓储/接口) | Works — menu items still clickable |
  | Language switch | Works |
  | User dropdown | Works — logout / change password |

- [ ] **Step 2: Fix any residual issues found in the checklist**

  Common issues to watch for:
  - If folded sidebar shows white text on white background: ensure `LayoutAside.tsx` has no remaining `style={{ color: "#fff" }}` (re-check all three locations from Task 3)
  - If the header is double-height: check that `cxd-Layout-brandBar` is `display: none !important` in `style.scss` and the empty `<div className="cxd-Layout-brandBar" />` in `LayoutHeader.tsx` has no content
  - If the Logo zone and sidebar are misaligned: adjust `width: 180` in `LayoutHeader.tsx` to match the actual rendered AMIS aside width (inspect in DevTools → find `.cxd-Layout-aside` width)
  - If tab active underline doesn't show: check if Ant Design CSS specificity wins — add an extra selector: `.ant-tabs-top > .ant-tabs-nav .ant-tabs-card .ant-tabs-tab-active`

- [ ] **Step 3: Final commit**

  ```bash
  git add -p   # stage only intended changes
  git commit -m "style: homepage layout redesign — clean light theme complete"
  ```
