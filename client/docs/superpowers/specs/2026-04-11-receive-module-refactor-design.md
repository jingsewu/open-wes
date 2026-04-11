# Receive Module Refactor Design

**Date:** 2026-04-11
**Branch:** feature_shelf
**Scope:** `src/pages/wms/station/instances/receive/`

---

## Background

The receive module has accumulated several state management problems:
- A pointless two-component split (`LayoutInner` → `LayoutContent`) that prop-drills state
- Business logic (API calls, state transitions) defined inside the render function without `useCallback`
- A hollow `useReceiveState` hook that provides zero abstraction (just 4 raw `useState` pairs)
- Two confirmed bugs in `ContainerHandler`: duplicate focus effects and a self-triggering `useEffect`

---

## Goals

1. Eliminate the `LayoutInner`/`LayoutContent` split
2. Move business logic out of the render function into a dedicated hook
3. Fix the two bugs in `ContainerHandler`
4. Keep the diff small — no rewrites, no new patterns, no touching files that are already clean

---

## Out of Scope

- `operations/tips/` — `useImperativeHandle` pattern is coupled to `ConfigControlledModal`, out of scope
- `services/api.ts`, `types/index.ts`, `constants/index.ts`, `utils/index.ts` — already clean
- `operations/orderHandler.tsx`, `operations/skuHandler.tsx` — no issues
- MobX store, event loop, `component-wrapper` — not part of this module's problems

---

## Architecture

### Before

```
LayoutInner                          ← creates useReceiveState()
  └── LayoutContent (observer)       ← receives state as props + calls useWorkStation()
        ├── onScanSubmit()           ← defined in render, no useCallback
        ├── onSkuChange()            ← defined in render, no useCallback
        ├── onConfirm()              ← defined in render, no useCallback
        ├── createApiHandler()       ← recreated every render
        ├── OrderHandler
        ├── SkuHandler
        └── ContainerHandler
              ├── useFocusManagement  ← sets focus via useEffect
              └── useEffect           ← ALSO sets focus (duplicate) + self-triggers on containerCode change
```

### After

```
ReceiveLayout (observer)             ← single component
  └── useReceiveWorkflow()           ← owns all local state + handlers + useWorkStation()
        ├── orderNo, setOrderNo
        ├── orderInfo, currentSkuInfo, focusValue
        ├── onScanSubmit (useCallback)
        ├── onSkuChange  (useCallback)
        └── onConfirm    (useCallback)
  ├── OrderHandler
  ├── SkuHandler
  └── ContainerHandler               ← bugs fixed
        ├── useFocusManagement       ← only focus management (no duplication)
        └── useEffect                ← only cleanup (setContainerCode + resetQuantity), deps fixed
```

---

## Changes

### 1. `hooks/index.ts` — Add `useReceiveWorkflow`

Replace `useReceiveState` with `useReceiveWorkflow`. The new hook:
- Owns all local state: `orderNo`, `orderInfo`, `currentSkuInfo`, `focusValue`
- Calls `useWorkStation()` internally, only reading `store`, `message`, `onActionDispatch`
- Defines `onScanSubmit`, `onSkuChange`, `onConfirm` with `useCallback`
- Creates `apiHandler` with `useMemo` so it isn't recreated every render

Keep the other hooks unchanged: `useContainerSpecs`, `useFocusManagement`, `useSkuScanner`, `useQuantityControl`.

**Interface returned:**
```ts
{
  // State
  orderNo: string
  setOrderNo: (v: string) => void
  orderInfo: any
  currentSkuInfo: any
  focusValue: string
  setFocusValue: (v: string) => void
  // Handlers
  onScanSubmit: () => Promise<void>
  onSkuChange: (detail: any) => void
  onConfirm: (params: ConfirmParams) => Promise<void>
  // Global (passed through for children that need it)
  store: WorkStationStore
  onActionDispatch: (action: any) => Promise<any>
}
```

### 2. `layout.tsx` — Collapse dual components

- Remove `LayoutInner` and `LayoutContent`
- Single `observer` component `ReceiveLayout`
- Call `useReceiveWorkflow()` at the top
- Read `store.workStationEvent` for `hasOrder`
- Wire handlers and state to child components — no logic in the render body

### 3. `operations/containerHandler.tsx` — Fix two bugs

**Bug 1 — duplicate focus (lines 89–97):**
`useFocusManagement(focusValue)` already handles focus via its own `useEffect`.
The component's `useEffect` must only do the cleanup side-effects:
```ts
// Remove focus calls, keep cleanup only
useEffect(() => {
  if (focusValue === "container") {
    setContainerCode("")
    resetQuantity()
  }
}, [focusValue])
```

**Bug 2 — self-triggering useEffect (lines 70–87):**
Dependency `containerCode` is modified inside the effect (`setContainerCode(propContainerCode)`),
causing the effect to re-run and calling `initializeSpecs` twice.
Fix: remove `containerCode` from the dependency array.
```ts
// Before
}, [propContainerCode, containerCode])

// After
}, [propContainerCode])
```

---

## Files Changed

| File | Change |
|------|--------|
| `hooks/index.ts` | Add `useReceiveWorkflow`, remove `useReceiveState` |
| `layout.tsx` | Collapse to single `observer` component using `useReceiveWorkflow` |
| `operations/containerHandler.tsx` | Fix focus duplication + useEffect deps bug |

---

## What Does NOT Change

- The visual output — no UI changes
- The data flow between MobX store and components
- Component APIs (`ContainerHandlerProps`, `SkuHandlerProps`, etc.)
- All other files in the receive module
