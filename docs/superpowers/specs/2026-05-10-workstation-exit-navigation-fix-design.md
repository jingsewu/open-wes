# Workstation Exit Navigation Fix

**Date:** 2026-05-10
**Status:** Approved
**Scope:** `src/pages/wms/station/tab-actions/action-configs/existTask.tsx`

---

## Problem

After a user exits a workstation (dispatches OFFLINE action), the UI does not navigate back to the station card selection page (`/wms/workStation`). The user remains permanently stuck on the mode-specific page (e.g., `/wms/workStation/outbound`).

---

## Root Cause

Two compounding issues triggered by a single line in `existTask.tsx`:

```typescript
history.push("/wms/workStation")  // line 39 — fires immediately on action success
```

### Issue 1: Premature navigation kills the WebSocket

When `history.push()` fires immediately after the action success response:

1. The mode WorkStation component unmounts.
2. Its `useEffect` cleanup checks `window.location.pathname = "/wms/workStation"`, which does **not** start with `/wms/workStation/`, so `isLeavingWorkStation = true`.
3. `eventLoop.stop()` is called → WebSocket disconnects, `eventListener = null`, `workStationStore.reset()`.
4. The server's WebSocket `DATA_CHANGED` message (sent after the OFFLINE action) arrives on a **dead connection** — it is silently lost.

### Issue 2: Stale API response triggers the WorkStationCard auto-nav guard

The card WorkStation (`type="card"`) that mounts after the navigation calls `loadInitialStationData()`. This makes a fresh `GET /station/api` request. Because the server may not have fully committed the OFFLINE state by the time this request arrives, it can return the old `ONLINE + PICKING` payload.

`WorkStationCard.useEffect` (the auto-nav guard) sees a non-OFFLINE status with a mode set:

```typescript
// WorkStationCard.tsx lines 164-173
const targetPath =
    workStationStatus !== "OFFLINE" && workStationMode
        ? `${WORK_STATION_PATH_PREFIX}/${StationTypes[workStationMode]}`
        : WORK_STATION_PATH_PREFIX

if (history.location.pathname !== targetPath) {
    history.replace(targetPath)   // bounces user back to /wms/workStation/outbound
}
```

The user is bounced back to the mode page **before** `eventLoop.start()` executes (child effects fire before parent effects in React). The event loop is never restarted, no WebSocket is connected, and no future updates arrive. The user is permanently stuck.

---

## Intended Design

Per the project's design intent:

> After the backend processes the OFFLINE action, it sends a WebSocket `DATA_CHANGED` message. The frontend then queries the workstation status and navigates based on the result.

Navigation should be **event-driven** (WebSocket → query → navigate), not **response-driven** (action HTTP success → immediate navigate).

---

## Solution (Method A)

Remove the `history.push()` call from `existTask.tsx`. Let the existing WebSocket → `getApiData()` → `OFFLINE` → `header.useEffect` chain drive the navigation.

### Change

**File:** `src/pages/wms/station/tab-actions/action-configs/existTask.tsx`

```diff
-    } else {
-        history.push("/wms/workStation")
-    }
+    }
+    // Navigation is driven by the WebSocket DATA_CHANGED message.
+    // When the server transitions the station to OFFLINE, it sends
+    // DATA_CHANGED → getApiData() returns OFFLINE → workStationStore updates
+    // → header.useEffect detects OFFLINE → navigates to /wms/workStation.
```

The `history` parameter can be removed from the `emitter` function signature since it is no longer used.

### Resulting flow

```
User clicks EXIT
  → onActionDispatch({ OFFLINE }) → HTTP success
  → No immediate navigation (WebSocket still connected)
  ↓
Server sends WebSocket DATA_CHANGED
  → eventLoop.onMessage: getApiData()
  → /station/api returns { workStationStatus: "OFFLINE", ... }
  → handleEventChange(OFFLINE) → workStationStore.setWorkStationEvent(OFFLINE)
  ↓
layout/header.tsx useEffect fires:
  workStationStatus === OFFLINE → history.push("/wms/workStation")
  ↓
Mode WorkStation unmounts → cleanup → isLeavingWorkStation = true
  → eventLoop.stop() → store reset, WebSocket disconnected
  ↓
Card WorkStation mounts (type="card")
  → loadInitialStationData() → /station/api returns OFFLINE (state now stable)
  → isLoadingStatus = false → workStationStore.setWorkStationEvent(OFFLINE)
  → eventLoop.start() → getApiData() confirms OFFLINE → WebSocket reconnected
  ↓
WorkStationCard guard: OFFLINE → targetPath = "/wms/workStation" = current path
  → No navigation needed. User sees card selection page. ✓
```

---

## Scope

- **Only file changed:** `existTask.tsx` — remove `history.push()` and the `history` destructure from `emitter`.
- **No changes needed** to `WorkStationCard.tsx`, `layout/header.tsx`, `event-loop/index.tsx`, or routing.
- The existing header OFFLINE-redirect and WorkStationCard auto-nav guard already implement the correct behavior; this fix just stops prematurely interrupting them.

---

## Edge Cases

| Scenario | Behavior |
|---|---|
| WebSocket `DATA_CHANGED` arrives before user sees feedback | Navigation happens within ~100 ms — imperceptible |
| WebSocket message delayed (slow network) | User stays on mode page slightly longer; navigation still occurs when message arrives |
| WebSocket disconnected before `DATA_CHANGED` arrives | WebSocket manager auto-reconnects (up to 5 attempts); if all fail, user would need to manually reload — acceptable given existing reconnect logic |
| Action fails (server returns code "-1") | Error message shown; no navigation (unchanged behavior) |

---

## Out of Scope

- Optimistic loading feedback ("logging out…") while waiting for WebSocket — can be added as a separate UX improvement.
- Backend resilience (ensuring `DATA_CHANGED` is always sent after OFFLINE) — backend concern.
