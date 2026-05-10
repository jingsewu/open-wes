# Workstation Session Refactor — Design Spec

**Date:** 2026-05-10
**Branch:** feature_shelf_ai_refactor
**Scope:** Client-side only (`client/src/pages/wms/station/`)

---

## Problem Statement

Clicking "Exit" in a workstation operation page fails to return the user to the WorkStationCard (task type selection). The user either gets redirected back to the operation page or sees a loading spinner indefinitely.

### Root Causes (three overlapping bugs)

1. **MobX store never reset on exit.**
   `workStationStore` is a global singleton. On exit, `eventLoop.stop()` disconnects the WebSocket but does not clear the store. `WorkStationCard` reads stale `workStationStatus` and `workStationMode` from the store and auto-navigates back to the operation page via its `useEffect`.

2. **Server status overrides client intent.**
   `index.tsx` calls `getStationStatus()` on every mount, which sets `isConfigStationId` based on the server response. If the server hasn't processed the OFFLINE event yet (or returns a non-`SAT010001` status), it sets `isConfigStationId = true`, bypassing SelectStation even when the client cleared `localStorage.stationId`.

3. **Exit clears the wrong state.**
   `existTask.tsx` calls `localStorage.removeItem("stationId")` and then navigates. The intent was to show SelectStation, but the desired behavior (confirmed with team) is to return to WorkStationCard — the station binding should persist across task exits.

---

## Design Goals

- Exit returns user to **WorkStationCard**, not SelectStation.
- Station selection (`stationId`) persists until the user explicitly changes stations.
- The "is a station selected?" decision is owned by a single place and is not overridable by server state.
- IP-based auto-binding can be added later with a single extension point.

---

## State Architecture

Two layers of state, with clear ownership:

```
┌─────────────────────────────────────────────────────┐
│  Station Session（工作站层）                           │
│  - "Which workstation am I at?"                      │
│  - Persisted in: localStorage.stationId             │
│  - Owner: useStationSession hook                     │
│  - Set by: SelectStation (manual) or IP binding      │
│  - Cleared by: (future) explicit "change station"    │
│  - NOT cleared on task exit                          │
├─────────────────────────────────────────────────────┤
│  Task Session（任务层）                               │
│  - "What task am I doing?"                           │
│  - Runtime state: MobX workStationStore + EventLoop  │
│  - Set by: WorkStationCard card click → ONLINE event │
│  - Cleared by: exit → OFFLINE event + store.reset()  │
└─────────────────────────────────────────────────────┘
```

### State Flow

```
Enter /wms/workStation
  └── useStationSession.isStationSelected?
        ├── false → SelectStation
        │     └── confirm → selectStation(id) → isStationSelected = true
        └── true  → WorkStationCard
              └── card click → ONLINE event → navigate to /wms/workStation/:type

Exit operation page
  └── OFFLINE event
      → eventLoop.stop() → workStationStore.reset()
      → history.push("/wms/workStation")
      → WorkStation(type="card") remounts
      → useStationSession: stationId in localStorage → isStationSelected = true
      → WorkStationCard renders (store is empty, no auto-navigation) ✓

[Future] IP binding
  └── useStationSession checks IP on mount
      → calls selectStation(boundId) automatically
      → SelectStation skipped
```

---

## Changes

### New File

**`src/pages/wms/station/state/hooks/useStationSession.ts`**

A focused hook that owns the station-session layer.

```ts
function useStationSession() {
  const [isStationSelected, setIsStationSelected] = useState(
    () => !!localStorage.getItem("stationId")
  )

  const selectStation = (id: string) => {
    localStorage.setItem("stationId", id)
    setIsStationSelected(true)
  }

  // Extension point for IP auto-binding (Phase 2)
  // const autoSelectByIp = async () => { ... }

  return { isStationSelected, selectStation }
}
```

### Modified Files (5)

#### 1. `index.tsx`

- Replace `useState(!!localStorage.getItem("stationId"))` + `setIsConfigStationId` with `useStationSession()`.
- Remove `setIsConfigStationId` call from inside `getStationStatus()`. The server call is retained for loading initial MobX store data, but it no longer controls which screen renders.
- Pass `selectStation` (not `setIsConfigStationId`) down to `SelectStation`.

#### 2. `SelectStation.tsx`

- Change prop `setIsConfigStationId: (value: boolean) => void` to `onStationSelected: (id: string) => void`.
- In `handleConfirm`, call `onStationSelected(stationId)` instead of `localStorage.setItem` + `setIsConfigStationId(true)`. The hook handles localStorage internally.

#### 3. `existTask.tsx`

- Remove `localStorage.removeItem("stationId")`. Station binding persists across task exits.
- Keep `history.push("/wms/workStation")` and the OFFLINE dispatch unchanged.

#### 4. `event-loop/index.tsx`

- In `stop()`, call `workStationStore.reset()` after disconnecting WebSocket. This ensures the MobX store is clean when WorkStationCard remounts.

#### 5. `WorkStationCard.tsx`

- In the `useEffect` that auto-navigates, add an early return guard: if `workStationEvent == null`, do not navigate. This is a safety net for the brief period between remount and the event loop fetching fresh data.

---

## Error Handling

- If `getStationStatus()` fails (network error), `isStationSelected` is unaffected. The user sees WorkStationCard if they have a stationId, or SelectStation if they don't. No silent failure.
- If the OFFLINE dispatch fails, the exit is blocked and an error message is shown (existing behavior, unchanged).
- If `localStorage.stationId` is present but the workstation no longer exists on the server, the event loop will surface an error on the WorkStationCard. This edge case is out of scope for this change.

---

## Out of Scope

- IP-based auto-binding (Phase 2).
- "Change station" flow (clearing stationId intentionally) — not currently in the UI.
- Backend changes.

---

## Files Touched

```
client/src/pages/wms/station/
  state/hooks/useStationSession.ts        ← NEW
  index.tsx                               ← modified
  SelectStation.tsx                       ← modified
  tab-actions/action-configs/existTask.tsx ← modified
  event-loop/index.tsx                    ← modified
  WorkStationCard.tsx                     ← modified
```
