# AMIS Frontend Standard

Scope: every page built with `schema2component()` / `AMisRenderer` in `client/src/pages`.
Legacy pages are migrated in waves; new pages must follow this document from the
first commit.

Read `docs/standards/frontend.md` first for general React, MobX, logging, and
security rules. This document covers the AMIS schema layer only.

---

## 1. Design identity: operations console

The people using Open WES are warehouse operators, supervisors, and support
engineers. They work in this UI for a whole shift, on large monitors, often next
to a scanner and a physical conveyor. The interface is a **console**, not a
marketing site:

- Density over decoration. A screen should show as many complete rows as possible.
- One primary action per page. Everything else is quiet.
- Identifiers (LPN, SKU, container, order number) are the primary way finding;
  they must be monospaced, copyable, and never truncated.
- Status is read at a glance: a dot plus a word, always with a consistent colour.
- Empty and error states tell the operator what to do next.
- No card grids, no gradient washes, no decorative illustration inside work pages.

The visual system is implemented by `client/src/scss/amis-console.scss`, loaded
after the cxd theme in `App.tsx`. Do not restyle AMIS internals per page; if a
pattern is missing, add a reusable helper to the theme bridge or the presets.

## 2. Tokens

### Colour

| Token | Value | Use |
|---|---|---|
| Brand | `#2563eb` | Primary button, focused input, active nav, links |
| Brand hover | `#3b82f6` | Hover of primary controls |
| Brand active | `#1d4ed8` | Pressed state |
| Text primary | `#1e293b` | Body, table cells |
| Text secondary | `#475569` | Labels, secondary copy |
| Text muted | `#64748b` | Help text, empty state |
| Border | `#e2e8f0` | Dividers, table rules, drawer edges |
| Surface subtle | `#f8fafc` | Table header, row hover |
| Success | `#047857` | Enabled, completed, in stock |
| Warning | `#b45309` | Pending, at risk, needs attention |
| Danger | `#dc2626` / text `#b91c1c` | Failed, blocked, destructive |
| Neutral status | `#64748b` | Disabled, archived, closed |

Rules:

- Use one blue. Do not introduce a second brand hue, including the old antd
  `#1890ff` or cxd `#2468f2`.
- Colour is never the only carrier of a status. Always render a dot and a label.
- Destructive red is reserved for destructive actions and failure states.

### Type

| Role | Size / weight |
|---|---|
| Page title | 16px / 600 |
| Section title | 14px / 600 |
| Body, form label, table cell | 13–14px / 400–500 |
| Table header | 12px / 600 |
| Identifier, code | 12px mono, tabular figures |
| Numeric measures | 13px, right-aligned, tabular figures |

The family is Plus Jakarta Sans with the system CJK fallback. Do not set font
families inside a schema; the theme bridge owns typography.

### Spacing and shape

- Page header: 14px vertical, 20px horizontal.
- Page body: 16px top, 20px sides and bottom.
- Form rows: 16px apart.
- Radii: 4px inputs, 8px drawers, 8px dialogs. No 12px+ cards inside a table page.
- Shadows: use cxd's existing elevation; do not add per-schema shadows.

## 3. Page anatomy

Every list page is built from the same five bands:

```
┌──────────────────────────────────────────────────────────────┐
│ Page title                                      [+ 新增]     │  page.toolbar
├──────────────────────────────────────────────────────────────┤
│ [ filter 1 ] [ filter 2 ] [ filter 3 ]      [查询] [重置]     │  crud.filter
├──────────────────────────────────────────────────────────────┤
│ reload  [导入] [导出]                                        │  crud.headerToolbar
├──────────────────────────────────────────────────────────────┤
│ identifier │ name │ qty │ status │ audit    │ actions         │  table
├──────────────────────────────────────────────────────────────┤
│ selected 3  [批量操作]      10/page  total 128  < 1 2 3 >     │  crud.footerToolbar
└──────────────────────────────────────────────────────────────┘
```

- The page title is the only H1. It is not repeated in the toolbar.
- The page toolbar holds the primary create action, right-aligned. If the page
  has no create action, it may stay empty.
- Export and import are secondary utilities: put them in the CRUD header toolbar
  (`tableActions`), not in the page header. `export-excel` is a CRUD toolbar
  renderer and will fail if placed in `page.toolbar`.
- `reload` is the first action in the CRUD header toolbar. AMIS renders its
  automatic column toggler in a leading slot; do not add an explicit
  `columns-toggler` unless you need custom configuration, and keep `reload`
  before it when you do.
- Pagination always shows `switch-per-page`, `statistics`, `pagination`.

Use `crudPage()` from `@/utils/amis/presets` unless the page is a detail or
dashboard:

```ts
import {
    addButton,
    auditColumns,
    codeColumn,
    crudPage,
    deleteButton,
    editButton,
    exportButton,
    formDrawer,
    numberColumn,
    statusColumn
} from "@/utils/amis/presets"
import { api_crud_search, api_crud_search_total } from "@/pages/constantApi"

const addDrawer = formDrawer({
    title: "userCenter.roleManagement.addTitle",
    body: form,
    api: api_role_add,
    reload: "RoleTable"
})

const schema = crudPage({
    title: "userCenter.roleManagement.title",
    name: "RoleTable",
    searchIdentity: "URole",
    api: api_crud_search,
    columns,
    pageActions: [addButton({ target: "RoleTable", drawer: addDrawer })],
    tableActions: [
        exportButton({ api: api_crud_search_total, filename: "roles" })
    ],
    rowActions: [editButton(editDrawer), deleteButton({ api: api_role_delete })]
})

export default schema2component(schema)
```

## 4. Tables

### Column order

1. Operational identifier (`codeColumn`) — LPN, SKU, container, order number.
2. Human name or description.
3. Quantities and measures (`numberColumn`) — right-aligned.
4. Status (`statusColumn`) — dot plus label.
5. Audit columns (`auditColumns`) — created/updated by and time.
6. Actions (`operationColumn`, fixed right).

Hide internal database IDs. Show an ID only when an operator can act on it or
search for it; otherwise join the display name.

### Column rules

- Identifiers: `codeColumn`, mono, copyable, never truncated.
- Numbers: `numberColumn`; no thousands separators unless the backend returns a
  formatted string.
- Dates: `dateTimeColumn` at minute precision. Use `{ seconds: true }` only for
  event/log tables where ordering within a minute matters.
- Long free text: `remarkColumn` (truncate + hover popover), never a 300px column
  of raw text.
- Booleans: name them for the domain (`启用/禁用`, `成功/失败`), not `是/否`.
- Empty values render `--`, not a blank cell.
- Default visible columns: at most 10–12. Put audit columns behind the column
  settings so the operational columns fit at 1440px.

### Actions

- At most three row actions. More than three belongs in a dropdown.
- Row actions are text links, neutral by default. `editButton()` and
  `deleteButton()` add `owes-action`; delete turns red only on hover.
- Never use a filled `level: "danger"` button in a table row.
- Destructive actions always use `confirmText`; the confirm names the object and
  the consequence, not just "Are you sure?".
- Bulk actions live in the selection bar, state the count, and use a verb
  (`关闭 3 个入库单`). Irreversible bulk actions require typed or explicit confirm.

### Empty and loading states

- Provide `placeholder` text with an action hint, e.g. "尚无入库计划单，先新建或导入".
- Do not show a spinner over an empty table; AMIS handles loading.
- A filter that returns nothing shows "没有符合条件的记录，调整筛选条件".
- Errors are shown as an inline alert with a retry action, not only a toast.

## 5. Filters

- Up to four searchable fields: `autoGenerateFilter` is acceptable.
- More than four: pass explicit `filters` to `crudPage`. Order by how often
  operators use them, not by database order.
- Date ranges use `input-date-range` with `valueFormat: "x"`.
- Every filter has a visible label; placeholders never replace labels.
- `filterTogglable` is on when there are more than four filters; it is off for
  short, frequently used filter rows.
- Search submits on Enter and on the explicit 查询 button. Reset restores the
  default range and clears all fields.
- An explicit filter must declare its own `actions`. AMIS 6.13 ignores
  `resetText`/`submitText` on the CRUD `filter`, so without this the panel
  renders only the default 查询 button and there is no way to reset:

  ```ts
  actions: [
      { type: "reset", label: "button.reset" },
      { type: "submit", level: "primary", label: "common.search" }
  ]
  ```

  `crudPage` in `client/src/utils/amis/presets.ts` already emits this.

## 6. Forms and drawers

### Width

| Fields | Layout | Drawer size |
|---|---|---|
| 1–6 | one column | `md` (500px) |
| 7–12 | two columns | `lg` (800px) |
| 13+ | two columns with sections | `lg` or `xl` for preview |

Never use the AMIS default 400px drawer for a form. `formDrawer()` chooses
`md`/`lg` from the field count; pass `size` explicitly for exceptions.

### Structure

- Drawer title names the entity and the intent: 新增角色 / 编辑角色 / 分配菜单权限.
  Never use the raw 新增 / 修改 as a drawer title.
- When the drawer operates on a row, show a one-line context block with the
  identifier (`owes-drawer-context`) so the operator knows which record is open.
- Field order follows the business process, not the database column order.
- More than 12 fields, or fields from unrelated domains: split into sections
  (`type: "fieldSet"` or a tabbed form). Do not produce a 30-field single column.
- Units belong in the label: `长度 (mm)`, `重量 (kg)`. Do not use a separate
  `description` for every numeric field.
- Immutable identifiers (`code` after creation) are disabled on edit, not hidden.
- Required fields use `required: true`. Optional fields are the exception; label
  help text only where the operator would otherwise guess.
- Long forms set `closeOnOutside: false` (the preset does this for >3 fields).
- Footer: 取消 on the left of 保存; 保存 is primary. No 确认/确定 for form submit.
- After a successful save, close the drawer and reload the table. Do not show a
  second toast on top of the AMIS success message.

### Validation

- Validate on submit, not on every keystroke, except for scanner input.
- Error text names the field and the fix: "容器编码已存在，请扫描其他容器".
- Required fields use `requiredField("validation.…Required")` from the presets,
  not a bare `required: true`. The message names the expected action
  ("请选择容器类型"), not just "不能为空".
- Forms set `messages.validateFailed` (the `formDrawer` preset does) so a failed
  submit explains what to look for. AMIS renders the error inline on the field.
- Do not clear the form when validation fails.
- Server-side validation errors must remain visible after the drawer stays open;
  do not duplicate them in a toast.

## 7. Detail pages and dialogs

- A detail view is a page or a full drawer (`xl`), not a 600px dialog with a
  scrolling table.
- Read-only detail forms use `static`/`text`, not disabled inputs that look
  editable.
- Dialogs are for a single decision or a short form (import, confirm, password).
  Anything with more than four fields is a drawer.
- Import dialogs show the template download, accepted formats, size limit, and
  validation rules as a scannable list, never one paragraph.
- Never use native `alert`/`confirm`; use the AMIS `alert`/`confirm` actions.

## 8. Status and data display

Use `statusColumn` with an explicit label and tone map:

```ts
statusColumn("status", "table.status", {
    labels: { "1": "common.enabled", "0": "common.disabled" },
    tones: { "1": "success", "0": "muted" },
    searchable: true
})
```

- Do not use `yes_no_options` for a status field.
- Do not map an enum to raw numbers in a table.
- Do not use colour alone; the dot and the label always travel together.
- Timestamps render in the operator's locale, minute precision by default.
- Seed data must not produce 1970 dates. Fix the seed or hide the field.

## 9. Internationalisation

AMIS schemas are translated by `replaceText` in `AmisRenderer`, using the flat
dotted keys in `client/src/locales/zh-cn.json` and `en-us.json`.

Rules:

- Every user-facing string is a locale key present in **both** files: labels,
  titles, placeholders, confirm text, empty states, unit names, error copy.
- Never put raw Chinese or hardcoded English in a schema string.
- Keys follow `feature.section.element`: `userCenter.roleManagement.addTitle`.
- Shared vocabulary lives under `common.*`, `table.*`, `button.*`, `toast.*`.
  Reuse `common.enabled` / `common.disabled` instead of inventing synonyms.
- Conventional tokens need no key: acronyms (`ID`, `SKU`, `URL`) and coordinate
  axes (`x`, `y`, `z`). Code-sample placeholders (editor placeholders holding
  `function` / `class` / `//` snippets) stay in one language on purpose.
- `replaceText` performs substring replacement on every string value. Do not
  use a key that is a substring of another key, and do not use locale keys as
  values of ignored props: `type`, `name`, `mode`, `target`, `reload`,
  `persistData`.
- A missing key renders literally in the UI. `npm run lint:amis` checks both
  locale files.
- ECharts `name` values (series and axis labels) are excluded from
  `replaceText` because `name` is an identifier key. Keep chart-internal labels
  short English tokens and put the translated wording in the card header.
- Dynamic values use expressions (`${name}`), not string concatenation.
- Do not use `type: "html"` to render translated copy; use `tpl` with a CSS
  helper (`owes-note-list` for multi-line guidance).

## 10. Schema hygiene

These are lint errors, not style preferences:

| Rule | Correct form |
|---|---|
| `hidden: "true"` | `hidden: true` |
| `require: true` | `required: true` |
| `fileName` on export | `filename` (lower-case n) |
| `type: "html"` | `type: "tpl"` with escaped text |
| filled `level: "danger"` in a row | `level: "link"` + `owes-action--danger` |
| stray `}` in a name/source | remove it |
| raw Chinese in a label | locale key |
| missing locale key | add to both locale files |

Additional rules:

- Prefer `type: "hidden"` for data-only fields; do not render an invisible input.
- Do not leave commented-out schema blocks in production pages; delete or move
  them to a ticket.
- One CRUD per page. Multiple tables belong in tabs or separate pages.
- Use `syncLocation: false` for CRUDs inside a tabbed shell (the app owns routing).
- `autoFillHeight: true` on list CRUDs so the table fills the console viewport.
- `showColumns` must include every column the backend may return; do not let the
  visible list and the search payload drift apart.
- APIs live in `constants/api_constant` files, not inline in the schema.
- Do not set inline `style` objects in a schema; use a helper class from the
  theme bridge.

## 11. Accessibility

- Body text and labels meet WCAG AA contrast on white (the tokens above do).
- Focus is always visible: do not remove the theme's focus outline.
- Tables are keyboard reachable; row actions are real buttons, not clickable divs.
- Do not rely on hover alone for destructive confirmation.
- Touch targets are at least 32px high (cxd's default input/button height).
- Motion is limited to state changes; the theme bridge disables transitions under
  `prefers-reduced-motion`.
- Every icon-only button has a translated `tooltip`/`aria-label`.

## 12. Presets

`client/src/utils/amis/presets.ts` is the only sanctioned source of schema
boilerplate.

| Helper | Purpose |
|---|---|
| `crudPage` | Page + CRUD + toolbar + filter + pagination |
| `formDrawer` | Drawer + form + footer actions + width/column rules |
| `addButton` / `editButton` / `deleteButton` | Standard action hierarchy |
| `exportButton` | Correct `filename` property |
| `codeColumn` / `numberColumn` / `dateTimeColumn` | Data display |
| `requiredField` | `required: true` + a domain-specific required message |
| `statusColumn` / `remarkColumn` | Status dot / clamped text |
| `operationColumn` / `auditColumns` | Fixed actions / audit block |

When a page needs a variant, extend the preset with an option instead of copying
the schema. Presets are typed; do not add `any` to work around a schema type.

One AMIS detail the preset already handles: a drawer renders its form body with
`wrapWithPanel: false`, which drops the form's own `actions` array. Put footer
buttons on the drawer schema (`actions`) and use `actionType: "confirm"` for
submit; `formDrawer` does this and passes `reload` on the confirm action.

## 13. Theme bridge

`client/src/scss/amis-console.scss` maps cxd variables onto the console tokens
and provides these helper classes:

| Class | Use |
|---|---|
| `owes-code` | Monospaced identifier cell |
| `owes-number` | Tabular numeric cell |
| `owes-status` + `--success/--warning/--danger/--info/--muted` | Status dot + tone |
| `owes-action` | Neutral row action |
| `owes-action--danger` | Destructive row action (red on hover only) |
| `owes-drawer-context` | Record identity line at the top of a drawer |
| `owes-note-list` | Multi-line guidance with preserved line breaks |
| `owes-text-danger` | Inline error text |

Add a class here only when it is used by at least two pages. Page-specific CSS is
a code smell: fix the schema or the shared token instead.

The same file owns two root-level switches: `data-owes-density` on `<html>`
(`standard` / `compact`, persisted by the store) and a dormant dark token layer
behind `data-owes-theme="dark"`. Nothing sets the theme attribute yet; activating
it later must go through the token layer, never through per-page colour overrides.
cxd redeclares its variables on `.AMISCSSWrapper`, so overrides must match
`:root[...]` and `:root[...] .AMISCSSWrapper` together.

## 14. Lint and definition of done

Run:

```bash
cd client
npm run lint:amis
```

Errors block a merge. Warnings are migration debt; new code must not add new
warnings. `npm run lint:amis -- --strict` fails on warnings as well.

A page is done when:

- [ ] It uses `crudPage` / `formDrawer` or documents why it cannot.
- [ ] There is exactly one primary action, in the page toolbar.
- [ ] Export/import are in the CRUD toolbar, not the page toolbar.
- [ ] Identifiers use `codeColumn`, numbers `numberColumn`, status `statusColumn`.
- [ ] Row actions are text links; delete is red only on hover and confirms.
- [ ] Drawers are `md` for one column and `lg` for two; titles name the entity.
- [ ] Units are in labels; immutable fields are disabled on edit.
- [ ] Every string is a locale key present in both `zh-cn.json` and `en-us.json`.
- [ ] Empty state explains what to do next.
- [ ] `npm run lint:amis` adds no new error or warning.
- [ ] Verified in the browser at 1440×900 and at a narrow width (≥1024px).
- [ ] No console errors, no AMIS "renderer not found" messages.

## 15. Migration waves

Legacy pages are migrated in this order. Each wave is a separate PR and must keep
the existing API contracts unchanged.

**Wave 0 — correctness (done / in progress)**

Boolean strings, `require` typo, `fileName`, stray braces, wrong tpl fields,
malformed dictionary sources, missing locale keys, status shown as 是/否,
unsafe `type: "html"`, filled danger row buttons.

**Wave 1 — i18n and data display (done)**

Replace raw Chinese and hardcoded English labels with keys; convert status
columns to `statusColumn`; apply `codeColumn` / `numberColumn` /
`dateTimeColumn`; add copy-to-clipboard for identifiers; fix 1970 seed dates.

**Wave 2 — page anatomy (done)**

Move create actions to the page toolbar, export/import to the CRUD toolbar,
replace auto-generated filters with explicit filters where there are more than
four fields, add empty states, and set drawer sizes/titles.

**Wave 3 — high-traffic pages (done)**

Redesign the pages operators use every day (inbound plan order, stocktake,
SKU/container master data, workstation) with sections, context lines, bulk
actions, and a consistent audit block.

Converted: stocktake (order list, detail and record drawers, add wizard),
inbound plan order (list and detail drawer), SKU management, workstation
management (add/edit/config drawers), container management. These pages now use
`statusColumn`, `codeColumn`, `numberColumn`, `auditColumns`, drawer-level
actions with `reload`, and `owes-drawer-context` lines. Drawer context is a
`type: "tpl"` row placed as the first child of the drawer form/body; it inherits
the row scope, so reference the identifying fields directly.

Data-dependent states (status dots, drawer context) only appear when the
warehouse under test has rows. Seed at least one record per page, or verify the
shared preset on a page that has data (role management) and the locale keys in
the JSON, before signing the wave off.

**Wave 4 — polish (done)**

Column visibility defaults, keyboard shortcuts for scanners, inline validation
copy, table density preferences, and dark-mode readiness (tokens only; no
per-page colour overrides).

- Audit columns from `auditColumns()` default to `toggled: false`; `crudPage`
  forces the column toggler on whenever a column is hidden by default.
- `/` focuses the page search input (explicit `.cxd-Crud-filter` panels and
  auto-generated `.cxd-Table-searchableForm` panels) and `Alt+N` clicks the
  page's primary action. Keydowns in editable controls, inside an open
  drawer/modal, or within 50 ms of the previous key (scanner bursts) are ignored.
- `requiredField(messageKey)` supplies domain-specific required copy;
  `formDrawer` sets `messages.validateFailed`. Inline feedback plus the
  form-level message replace the generic AMIS text.
- Table density is persisted per browser (`owesTableDensity`) and applied via
  `data-owes-density` on `<html>`; the user menu exposes 标准 / 紧凑.
- `amis-console.scss` carries a dormant dark token layer behind
  `data-owes-theme="dark"` — tokens only, no toggle and no page overrides.

Verified at 1440×900 and 1024×768: shortcuts, toggler defaults and reveal,
density switch and persistence, required copy on role and container drawers,
dark tokens, no console errors and no "renderer not found".

Do not mass-format or mass-rename schemas in a functional PR. One wave, one kind
of change, verified with screenshots.
