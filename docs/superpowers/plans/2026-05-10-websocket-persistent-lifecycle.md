# WebSocket Persistent Lifecycle Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Bind the WebSocket connection lifetime to the station session (`stationId`) instead of the React component mount/unmount cycle, eliminating unnecessary reconnects when the user navigates away and back.

**Architecture:** Split `WorkStationEventLoop.stop()` into `pause()` (navigation away — keeps WS alive) and `destroy()` (station change/logout — full teardown). Make `start()` idempotent so re-entering the workstation page reuses an existing connection. `useStationSession` grows a `clearStation()` method that calls `destroy()` before clearing localStorage.

**Tech Stack:** TypeScript, React, MobX, native WebSocket (`WebSocketManager` wrapper)

**Spec:** `docs/superpowers/specs/2026-05-10-websocket-persistent-lifecycle-design.md`

---

## File Map

| File | Change |
|------|--------|
| `src/pages/wms/station/event-loop/websocketManager.ts` | Add `resume()` method |
| `src/pages/wms/station/event-loop/index.tsx` | Add `pause()`; rename `stop()` → `destroy()`; fix internal `this.stop()` call; make `start()` idempotent |
| `src/pages/wms/station/state/hooks/useStationSession.ts` | Add `clearStation()` |
| `src/pages/wms/station/index.tsx` | Call `pause()` instead of `stop()` in cleanup |

---

## Task 1: Add `resume()` to `WebSocketManager`

**Files:**
- Modify: `src/pages/wms/station/event-loop/websocketManager.ts`

`disconnect()` sets `this.isDestroyed = true`, which blocks all future `connect()` calls. When the EventLoop calls `destroy()` followed later by a fresh `start()` that rebuilds the manager, this is fine — a new instance is created. But if we ever want to reconnect an existing manager instance, we need a way to un-mark it as destroyed. This method is a safety valve for that scenario and also makes the manager's state machine explicit.

- [ ] **Step 1: Add `resume()` after the `disconnect()` method**

Open `src/pages/wms/station/event-loop/websocketManager.ts`. After the `disconnect()` method (line 225), add:

```typescript
  /**
   * 重置销毁状态，允许重新连接
   * 在 destroy() 后重新调用 start() 时使用
   */
  resume(): void {
    this.isDestroyed = false
    this.reconnectAttempts = 0
  }
```

- [ ] **Step 2: Verify the file compiles**

In the `client` directory, run:
```bash
npx tsc --noEmit
```
Expected: no errors related to `websocketManager.ts`.

- [ ] **Step 3: Commit**

```bash
git add src/pages/wms/station/event-loop/websocketManager.ts
git commit -m "feat(station): add WebSocketManager.resume() to reset destroyed state"
```

---

## Task 2: Refactor `WorkStationEventLoop` — `pause()`, `destroy()`, idempotent `start()`

**Files:**
- Modify: `src/pages/wms/station/event-loop/index.tsx`

This is the core change. Four things happen in this file:

1. `stop()` is renamed to `destroy()` — same body, just the public name changes.
2. New `pause()` method is added — only clears `eventListener`, leaves everything else alone.
3. Internal call `await this.stop()` inside `setDebuggerConfig` is updated to `await this.destroy()`.
4. `start()` becomes idempotent: if `websocketManager` already exists, skip re-connection and only refresh data.

- [ ] **Step 1: Rename `stop()` to `destroy()`**

In `src/pages/wms/station/event-loop/index.tsx`, change line 63:
```typescript
// BEFORE:
public stop: () => Promise<void> = async () => {
    console.log("%c =====> event loop stop", "color:red;font-size:20px;")
```
```typescript
// AFTER:
public destroy: () => Promise<void> = async () => {
    console.log("%c =====> event loop destroy", "color:red;font-size:20px;")
```

- [ ] **Step 2: Fix the internal `this.stop()` call in `setDebuggerConfig`**

Still in `src/pages/wms/station/event-loop/index.tsx`, change line 46:
```typescript
// BEFORE:
            await this.stop()
```
```typescript
// AFTER:
            await this.destroy()
```

- [ ] **Step 3: Add `pause()` method**

Add this method after `destroy()` (after line 83):

```typescript
    public pause: () => void = () => {
        console.log("%c =====> event loop pause", "color:orange;font-size:20px;")

        // Detach React listener — component is unmounting.
        // WebSocket and store are intentionally preserved.
        this.eventListener = null
    }
```

- [ ] **Step 4: Make `start()` idempotent**

Change the existing `start()` method (lines 58–61):

```typescript
// BEFORE:
    public start: () => void = async () => {
        await this.getApiData()
        await this.initWebsocket()
    }
```

```typescript
// AFTER:
    public start: () => void = async () => {
        // Always refresh data from the server on (re-)entry.
        await this.getApiData()

        // Only create a new WebSocket if one doesn't already exist.
        // An existing websocketManager is either connected or self-reconnecting.
        if (!this.websocketManager) {
            await this.initWebsocket()
        }
    }
```

- [ ] **Step 5: Verify the full file compiles**

```bash
npx tsc --noEmit
```
Expected: no errors.

- [ ] **Step 6: Manual smoke test — pause path**

Start the dev server. Open the workstation page. Open DevTools → Network → WS tab. Note the open WebSocket connection. Navigate to another menu (e.g., orders). Verify in the Network tab that the WebSocket connection **remains open** (no close frame). Navigate back to the workstation. Verify only one WebSocket connection exists (no duplicate connection).

- [ ] **Step 7: Commit**

```bash
git add src/pages/wms/station/event-loop/index.tsx
git commit -m "feat(station): add pause(), rename stop()→destroy(), make start() idempotent"
```

---

## Task 3: Add `clearStation()` to `useStationSession`

**Files:**
- Modify: `src/pages/wms/station/state/hooks/useStationSession.ts`

`clearStation()` is the "change workstation" action. It performs a full teardown: calls `destroy()` on the EventLoop (closes WS + resets store), removes `stationId` from localStorage, and flips React state back to "no station selected" — which causes `WorkStation` to render `<SelectStation>`.

- [ ] **Step 1: Add the import and `clearStation()`**

Replace the entire file with:

```typescript
import { useState } from "react"
import { workStationEventLoop } from "../../event-loop/eventLoopInstance"

/**
 * Owns the station-session layer:
 * "Which workstation am I at?"
 *
 * Persists stationId to localStorage. Never clears it on task exit —
 * only clearStation() (explicit station change) does that.
 *
 * Extension point: add autoSelectByIp() here for Phase 2 IP binding.
 */
export function useStationSession() {
    const [isStationSelected, setIsStationSelected] = useState(
        () => !!localStorage.getItem("stationId")
    )

    const selectStation = (id: string) => {
        localStorage.setItem("stationId", id)
        setIsStationSelected(true)
    }

    /**
     * Full teardown: closes WebSocket, resets store, clears stationId.
     * Call this when the user explicitly changes workstation or logs out.
     */
    const clearStation = () => {
        workStationEventLoop.destroy()
        localStorage.removeItem("stationId")
        setIsStationSelected(false)
    }

    return { isStationSelected, selectStation, clearStation }
}
```

- [ ] **Step 2: Verify compilation**

```bash
npx tsc --noEmit
```
Expected: no errors.

- [ ] **Step 3: Commit**

```bash
git add src/pages/wms/station/state/hooks/useStationSession.ts
git commit -m "feat(station): add clearStation() to useStationSession for explicit station teardown"
```

---

## Task 4: Update `WorkStation` component — use `pause()` instead of `stop()`

**Files:**
- Modify: `src/pages/wms/station/index.tsx`

The cleanup function in the second `useEffect` currently calls `workStationEventLoop.stop()` when leaving the workstation route. After this change it calls `pause()`, keeping the WebSocket alive.

- [ ] **Step 1: Update the cleanup call**

In `src/pages/wms/station/index.tsx`, find the cleanup inside the second `useEffect` (around line 138). Change:

```typescript
// BEFORE:
                workStationEventLoop.stop()
```
```typescript
// AFTER:
                workStationEventLoop.pause()
```

The surrounding code stays exactly the same:
```typescript
        return () => {
            const currentPath = window.location.pathname
            const isLeavingWorkStation =
                !currentPath.startsWith("/wms/workStation/")

            if (isLeavingWorkStation) {
                workStationEventLoop.pause()   // ← was stop()

                try {
                    const resizeObserverManager = (window as any)
                        .ResizeObserverManager
                    if (resizeObserverManager) {
                        resizeObserverManager.disconnectAll()
                    }
                } catch (error) {
                    console.warn("清理 ResizeObserver 时出错:", error)
                }
            }
            // ... dev timer cleanup unchanged
        }
```

- [ ] **Step 2: Remove the `currentEvent` guard so `start()` always runs on card-type stations**

After `pause()`, `currentEvent` is preserved (not reset). The existing condition `!workStationEventLoop.getCurrentEvent()` would therefore block `start()` on every re-entry after the first, skipping the `getApiData()` refresh. Since `start()` is now idempotent, remove the guard.

In `src/pages/wms/station/index.tsx`, change (approximately line 125–130):

```typescript
// BEFORE:
        if (
            type === WORK_STATION_TYPE_CARD &&
            !workStationEventLoop.getCurrentEvent()
        ) {
            workStationEventLoop.start()
        }
```

```typescript
// AFTER:
        // start() is idempotent — reuses existing WebSocket, always refreshes data.
        if (type === WORK_STATION_TYPE_CARD) {
            workStationEventLoop.start()
        }
```

- [ ] **Step 3: Update the eventLoopInstance comment (optional but helpful)**

In `src/pages/wms/station/event-loop/eventLoopInstance.ts`, update the comment to reflect the new API:

```typescript
/**
 * 全局唯一的 WorkStationEventLoop 实例
 * 由 station/index.tsx 负责 start/pause/initListener
 * 由 useStationSession 的 clearStation() 负责 destroy()
 * 由 useWorkStation hook 负责 actionDispatch
 */
export const workStationEventLoop = new WorkStationEventLoop()
```

- [ ] **Step 4: Verify compilation**

```bash
npx tsc --noEmit
```
Expected: no errors.

- [ ] **Step 5: Full manual end-to-end verification**

Scenario A — Navigate away and back:
1. Open workstation page → WebSocket connects (visible in DevTools Network → WS)
2. Click a different nav menu (e.g., orders) → WebSocket stays open (no close frame)
3. Click workstation menu again → page loads, **no new WebSocket** created, data is fresh

Scenario B — Workstation OFFLINE does not kill WebSocket:
1. In the workstation, click the "下线" (offline) button
2. Workstation status changes to OFFLINE, card selection screen appears
3. WebSocket remains connected in DevTools

Scenario C — Change station triggers full teardown:
1. Call `clearStation()` (e.g., via a "change station" button wired to it, or temporarily from DevTools: `import { workStationEventLoop } from './event-loop/eventLoopInstance'; workStationEventLoop.destroy()`)
2. Verify WebSocket closes (close frame in DevTools)
3. SelectStation screen appears
4. Pick a station → new WebSocket created

- [ ] **Step 6: Commit**

```bash
git add src/pages/wms/station/index.tsx src/pages/wms/station/event-loop/eventLoopInstance.ts
git commit -m "feat(station): use pause() on navigation, preserving WebSocket across route changes"
```

---

## Open Item: Logout Handler

When the user logs out of the application, `workStationEventLoop.destroy()` must be called to close the WebSocket. The exact logout location was not identified during brainstorming. During implementation:

1. Search for the logout action:
   ```bash
   grep -r "logout\|signOut\|clearToken\|removeItem.*token" src/ --include="*.ts" --include="*.tsx" -l
   ```
2. Add `workStationEventLoop.destroy()` before or alongside the token-clearing logic.
3. Commit with message: `fix(station): call destroy() on logout to close WebSocket`

---

## Summary

After all tasks complete, the WebSocket lifecycle is:

```
stationId set (selectStation)    → WebSocket created on next start()
navigate away from workstation   → pause() — WS stays alive
navigate back to workstation     → start() — reuses WS, refreshes data
workstation OFFLINE              → no WS change
change station (clearStation)    → destroy() — WS closed, store reset, new station picker shown
logout                           → destroy() — WS closed
```
