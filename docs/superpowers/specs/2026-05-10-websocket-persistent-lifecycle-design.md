# WebSocket Persistent Lifecycle Design

**Date:** 2026-05-10
**Branch:** feature_shelf_ai_refactor
**Status:** Approved for implementation

---

## Problem

The WebSocket connection is currently tied to the `WorkStation` React component lifecycle. Every time the user navigates away from `/wms/workStation/*` and returns, the connection is torn down and rebuilt from scratch. This causes:

- Unnecessary reconnect latency on re-entry
- A full `workStationStore.reset()` on every navigation away, discarding live state
- WebSocket recreated even when workstation identity (`stationId`) has not changed

## Goal

Bind the WebSocket lifetime to the **station session** (the `stationId`), not the React component mount/unmount cycle. The connection closes only when:
- The user explicitly changes to a different workstation
- The user logs out

## Approach: Split `stop()` into `pause()` and `destroy()`

Minimal surgery to `WorkStationEventLoop`. No architectural restructuring required.

---

## API Changes — `WorkStationEventLoop`

### `pause()` (new)
Called when the WorkStation component unmounts due to navigation away.
- Removes `eventListener` (detaches React from the loop)
- Does **not** disconnect WebSocket
- Does **not** reset store
- WebSocket remains connected; incoming `DATA_CHANGED` messages still trigger `getApiData()` and silently update the store in the background

### `destroy()` (replaces `stop()`)
Called only when the station session is explicitly ended (station change or logout).
- Removes `eventListener`
- Disconnects WebSocket (`websocketManager.disconnect()`)
- Sets `websocketManager = null`
- Calls `workStationStore.reset()`

### `start()` — becomes idempotent
- If `websocketManager` exists and is `OPEN`: skip connection creation, just call `getApiData()` and re-attach listener
- If `websocketManager` is null or disconnected: run existing connection flow

---

## Changes — `WebSocketManager`

Add a `resume()` method that resets the `isDestroyed` flag. This is needed if `start()` needs to reconnect after a disconnect (e.g., token expiry during a long navigation-away period).

```ts
resume(): void {
  this.isDestroyed = false
  this.reconnectAttempts = 0
}
```

---

## Changes — `useStationSession`

Add `clearStation()`:

```ts
const clearStation = () => {
  workStationEventLoop.destroy()
  localStorage.removeItem("stationId")
  setIsStationSelected(false)
}
```

Return it alongside `isStationSelected` and `selectStation`.

---

## Changes — `WorkStation` component (`index.tsx`)

In the `useEffect` cleanup, call `pause()` instead of `stop()` when navigating within WMS:

```ts
return () => {
  const currentPath = window.location.pathname
  const isLeavingWorkStation = !currentPath.startsWith("/wms/workStation/")
  if (isLeavingWorkStation) {
    workStationEventLoop.pause()   // was: workStationEventLoop.stop()
    // ... ResizeObserver cleanup unchanged
  }
}
```

---

## Data Flow

### User navigates away and returns
```
WorkStation unmounts → pause()
  WebSocket: still OPEN
  store: unchanged
  onMessage: DATA_CHANGED → getApiData() → store updated silently

User clicks workstation menu → WorkStation mounts → start()
  websocketManager OPEN → skip connect
  getApiData() called → store refreshed
  eventListener re-attached
  Page renders with up-to-date data immediately
```

### Workstation goes OFFLINE (worker clicks "下线")
```
actionDispatch(OFFLINE) → server commit
  WebSocket: DATA_CHANGED received → getApiData() → store.workStationStatus = OFFLINE
  WorkStationCard auto-navigates back to card screen
  WebSocket: never closed
```

### User changes workstation
```
"更换工作站" button → clearStation()
  destroy() → WebSocket closed + store.reset()
  localStorage stationId cleared
  SelectStation shown

User picks new station → selectStation(newId) → start()
  New WebSocket created for new stationId
```

### Logout
```
Logout handler (location TBD) → workStationEventLoop.destroy()
  WebSocket closed, store cleared
```

---

## Files Changed

| File | Change |
|------|--------|
| `event-loop/index.tsx` | Add `pause()`; rename `stop()` → `destroy()`; make `start()` idempotent |
| `event-loop/websocketManager.ts` | Add `resume()` method |
| `state/hooks/useStationSession.ts` | Add `clearStation()` |
| `pages/wms/station/index.tsx` | Call `pause()` instead of `stop()` on unmount |

## Files Unchanged

- `WorkStationStore.ts`
- `WorkStationCard.tsx`
- All action instance files
- WebSocket reconnect logic

---

## Open Item

The exact location of the logout handler (where `destroy()` must be called) is to be confirmed during implementation.
