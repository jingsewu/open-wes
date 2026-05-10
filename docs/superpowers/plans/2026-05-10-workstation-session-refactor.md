# Workstation Session Refactor Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix the exit-to-WorkStationCard bug by splitting station-session state (which workstation) from task-session state (current operation), creating a `useStationSession` hook as a single source of truth.

**Architecture:** A new `useStationSession` hook reads/writes `localStorage.stationId` and owns the `isStationSelected` boolean. The MobX `workStationStore` is reset inside `eventLoop.stop()` so WorkStationCard never sees stale navigation data after exit. SelectStation and index.tsx are updated to use the new hook interface.

**Tech Stack:** React 17+, TypeScript, MobX 4.x, React Router 5, localStorage

---

## File Map

| Action | Path | Responsibility |
|--------|------|----------------|
| **Create** | `src/pages/wms/station/state/hooks/useStationSession.ts` | Owns station-session state (stationId persistence) |
| **Modify** | `src/pages/wms/station/event-loop/index.tsx` | Reset MobX store inside `stop()` |
| **Modify** | `src/pages/wms/station/tab-actions/action-configs/existTask.tsx` | Remove stationId removal on exit |
| **Modify** | `src/pages/wms/station/WorkStationCard.tsx` | Guard auto-navigation when store is empty |
| **Modify** | `src/pages/wms/station/SelectStation.tsx` | Accept `onStationSelected` instead of `setIsConfigStationId` |
| **Modify** | `src/pages/wms/station/index.tsx` | Replace `isConfigStationId` state with `useStationSession()` |

---

## Task 1: Create `useStationSession` hook

**Files:**
- Create: `src/pages/wms/station/state/hooks/useStationSession.ts`

- [ ] **Step 1: Create the file**

```typescript
// src/pages/wms/station/state/hooks/useStationSession.ts
import { useState } from "react"

/**
 * Owns the station-session layer:
 * "Which workstation am I at?"
 *
 * Persists stationId to localStorage. Never clears it on task exit —
 * only a future "change station" action would do that.
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

    return { isStationSelected, selectStation }
}
```

- [ ] **Step 2: Verify it compiles**

```bash
cd client
npx tsc --noEmit
```

Expected: no errors related to the new file.

- [ ] **Step 3: Commit**

```bash
git add src/pages/wms/station/state/hooks/useStationSession.ts
git commit -m "feat(station): add useStationSession hook for station-session state"
```

---

## Task 2: Reset MobX store in `eventLoop.stop()`

**Files:**
- Modify: `src/pages/wms/station/event-loop/index.tsx`

This is the fix for Bug 1 (the primary bug). After stop(), the MobX store will be empty so WorkStationCard's auto-navigation `useEffect` won't fire.

- [ ] **Step 1: Add `workStationStore.reset()` to `stop()`**

Open `src/pages/wms/station/event-loop/index.tsx`.

Find the `stop` method (currently lines 63–80). Replace it with:

```typescript
public stop: () => Promise<void> = async () => {
    console.log("%c =====> event loop stop", "color:red;font-size:20px;")

    // Clear event listener
    this.eventListener = null

    // Disconnect WebSocket
    if (this.websocketManager) {
        this.websocketManager.disconnect()
        this.websocketManager = null
    }

    // Reset current event
    this.currentEvent = undefined

    // Reset MobX store so stale workStationStatus/workStationMode
    // don't trigger WorkStationCard's auto-navigation useEffect.
    workStationStore.reset()

    return Promise.resolve()
}
```

`workStationStore` is already imported at the top of the file (line 11: `import { workStationStore } from "../state/WorkStationStore"`). No new import needed.

- [ ] **Step 2: Verify compilation**

```bash
npx tsc --noEmit
```

Expected: no errors.

- [ ] **Step 3: Commit**

```bash
git add src/pages/wms/station/event-loop/index.tsx
git commit -m "fix(station): reset workStationStore inside eventLoop.stop()"
```

---

## Task 3: Remove `localStorage.removeItem("stationId")` from exit action

**Files:**
- Modify: `src/pages/wms/station/tab-actions/action-configs/existTask.tsx`

Exit ends the *task session*, not the *station session*. The stationId must persist so the user returns to WorkStationCard (not SelectStation).

- [ ] **Step 1: Remove the stationId removal**

Open `src/pages/wms/station/tab-actions/action-configs/existTask.tsx`.

Replace the `emitter` function (currently lines 31–42):

```typescript
emitter: async (props) => {
    const { history, message, onActionDispatch } = props
    const { code, msg } = await onActionDispatch({
        eventCode: CustomActionType.OFFLINE
    })
    if (code === "-1") {
        message?.({ type: MessageType.ERROR, content: msg })
    } else {
        history.push("/wms/workStation")
    }
}
```

The `localStorage.removeItem("stationId")` line is deleted. Everything else is unchanged.

- [ ] **Step 2: Verify compilation**

```bash
npx tsc --noEmit
```

Expected: no errors.

- [ ] **Step 3: Commit**

```bash
git add src/pages/wms/station/tab-actions/action-configs/existTask.tsx
git commit -m "fix(station): do not clear stationId on exit — station session persists"
```

---

## Task 4: Guard WorkStationCard auto-navigation when store is empty

**Files:**
- Modify: `src/pages/wms/station/WorkStationCard.tsx`

After `store.reset()`, `workStationEvent` is `undefined`. The existing `useEffect` reads `workStationStatus` and `workStationMode` from it; without a guard it would navigate to `WORK_STATION_PATH_PREFIX` (a no-op since we're already there), but adding the guard makes the intent explicit and prevents any future regression.

- [ ] **Step 1: Add null guard to the navigation `useEffect`**

Open `src/pages/wms/station/WorkStationCard.tsx`.

Find the `useEffect` that calls `history.replace` (currently lines 159–168). Replace it:

```typescript
useEffect(() => {
    // Do not navigate while store is empty (e.g., immediately after
    // eventLoop.stop() resets the store on exit).
    if (!workStationEvent) return

    const targetPath =
        workStationStatus !== "OFFLINE" && workStationMode
            ? `${WORK_STATION_PATH_PREFIX}/${
                  StationTypes[workStationMode as keyof typeof StationTypes]
              }`
            : WORK_STATION_PATH_PREFIX

    if (history.location.pathname !== targetPath) {
        history.replace(targetPath)
    }
}, [workStationEvent, workStationMode, workStationStatus, history])
```

The only changes are: add `if (!workStationEvent) return` at the top, and add `workStationEvent` to the deps array.

- [ ] **Step 2: Verify compilation**

```bash
npx tsc --noEmit
```

Expected: no errors.

- [ ] **Step 3: Commit**

```bash
git add src/pages/wms/station/WorkStationCard.tsx
git commit -m "fix(station): guard WorkStationCard auto-nav when store is empty"
```

---

## Task 5: Update `SelectStation` prop interface

**Files:**
- Modify: `src/pages/wms/station/SelectStation.tsx`

Replace `isConfigStationId` + `setIsConfigStationId` props with a single `onStationSelected(id)` callback. The hook now owns localStorage, so SelectStation doesn't call `localStorage.setItem` directly.

- [ ] **Step 1: Rewrite SelectStation with the new prop interface**

Replace the entire contents of `src/pages/wms/station/SelectStation.tsx`:

```typescript
import React, { memo, useEffect, useState } from "react"
import { Button, message, Select, Typography, Spin, Alert } from "antd"
import store from "@/stores"
import { useTranslation } from "react-i18next"
import { request_work_station } from "@/pages/wms/station/constants/constant"

const { Title } = Typography

interface SelectStationProps {
    onStationSelected: (id: string) => void
}

const SelectStation = ({ onStationSelected }: SelectStationProps) => {
    const { t } = useTranslation()
    const [stationId, setStationId] = useState("")
    const [options, setOptions] = useState<any[]>([])
    const [loading, setLoading] = useState(false)
    const [fetchError, setFetchError] = useState("")

    useEffect(() => {
        let isMounted = true

        const fetchStations = async () => {
            try {
                setLoading(true)
                setFetchError("")
                const res: any = await request_work_station(store.warehouse.code)

                if (!isMounted) return

                setOptions(res?.data?.items || [])
            } catch (error: any) {
                if (!isMounted) return

                console.error("获取工作站列表失败:", error)
                const errorMsg = error?.message || "获取工作站列表失败"
                setFetchError(errorMsg)
                message.error("获取工作站列表失败，请刷新重试")
            } finally {
                if (isMounted) {
                    setLoading(false)
                }
            }
        }

        fetchStations()

        return () => {
            isMounted = false
        }
    }, [store.warehouse.code])

    const handleChange = (val: string) => {
        setStationId(val)
    }

    const handleConfirm = () => {
        if (!stationId) {
            message.error(t("station.home.div.selectStation"))
            return
        }
        onStationSelected(stationId)
    }

    const handleRetry = () => {
        window.location.reload()
    }

    const isDisabled = loading || !!fetchError

    return (
        <div
            className="w-full h-full d-flex flex-col justify-center items-center"
            style={{ backgroundColor: "#fff" }}
        >
            <Title level={4} className="mb-3">
                {t("station.home.div.selectStation")}
            </Title>

            {fetchError && (
                <Alert
                    message="获取工作站列表失败"
                    description={fetchError}
                    type="error"
                    showIcon
                    style={{ width: 300, marginBottom: 16 }}
                    action={
                        <Button size="small" onClick={handleRetry}>
                            重试
                        </Button>
                    }
                />
            )}

            <Spin spinning={loading}>
                <Select
                    style={{ width: 300 }}
                    value={stationId}
                    onChange={handleChange}
                    options={options}
                    fieldNames={{ label: "stationName", value: "id" }}
                    disabled={isDisabled}
                    placeholder="请选择工作站"
                    notFoundContent="暂无工作站"
                />
            </Spin>

            <Button
                type="primary"
                style={{
                    width: 300,
                    backgroundColor: "#23c560",
                    borderColor: "#23c560",
                    marginTop: 10
                }}
                onClick={handleConfirm}
                disabled={isDisabled}
            >
                {t("station.home.button.confirm")}
            </Button>
        </div>
    )
}

export default memo(SelectStation)
```

Key changes from the original:
- `isConfigStationId` prop removed
- `setIsConfigStationId` prop replaced by `onStationSelected: (id: string) => void`
- `handleConfirm` calls `onStationSelected(stationId)` instead of `localStorage.setItem` + `setIsConfigStationId(true)`
- `useEffect` dep array simplified to `[store.warehouse.code]` (no `isConfigStationId`)

- [ ] **Step 2: Verify compilation**

```bash
npx tsc --noEmit
```

Expected: TypeScript will report an error in `index.tsx` because it still passes the old props. That's expected — it will be fixed in Task 6.

- [ ] **Step 3: Commit**

```bash
git add src/pages/wms/station/SelectStation.tsx
git commit -m "refactor(station): update SelectStation to accept onStationSelected callback"
```

---

## Task 6: Wire `useStationSession` in `index.tsx`

**Files:**
- Modify: `src/pages/wms/station/index.tsx`

This is the final wiring step. Replace `isConfigStationId` state + the server override with `useStationSession()`. Pass `selectStation` to SelectStation.

- [ ] **Step 1: Rewrite `index.tsx`**

Replace the entire contents of `src/pages/wms/station/index.tsx`:

```typescript
import "./state/mobxConfig"

import type { WorkStationConfig } from "@/pages/wms/station/instances/types"
import React, { useEffect, useState, useRef, useMemo } from "react"
import type { RouteComponentProps } from "react-router"
import { Spin } from "antd"
import { workStationStore } from "./state/WorkStationStore"
import { workStationEventLoop } from "./event-loop/eventLoopInstance"
import Layout from "./layout"
import WorkStationCard from "./WorkStationCard"
import SelectStation from "./SelectStation"
import { request_work_station_view } from "@/pages/wms/station/constants/constant"
import { useStationSession } from "./state/hooks/useStationSession"

const WORK_STATION_TYPE_CARD = "card"

type WorkStationProps = RouteComponentProps & {
    code: string
    type: string
}

const WorkStationFactor: Record<string, WorkStationConfig<string>> = {}

const initWorkStationFactor = (): Record<string, WorkStationConfig<string>> => {
    const factor: Record<string, WorkStationConfig<string>> = {}

    try {
        // @ts-ignore
        const res = require.context("./instances", true, /config\.(ts|tsx)$/)
        res.keys().forEach((key: string) => {
            const { default: WorkStation } = res(key)
            if (WorkStation?.type) {
                factor[WorkStation.type] = WorkStation
            }
        })
    } catch (error) {
        console.error("初始化工作站配置失败:", error)
    }

    return factor
}

Object.assign(WorkStationFactor, initWorkStationFactor())

const WorkStation = (props: WorkStationProps) => {
    const { type } = props
    const isInitialized = useRef(false)
    const [isLoadingStatus, setIsLoadingStatus] = useState(true)

    // Station-session layer: "which workstation am I at?"
    // Owned entirely by useStationSession (localStorage-backed).
    // The server call below populates MobX store data but does NOT
    // override this value.
    const { isStationSelected, selectStation } = useStationSession()

    const workStationConfig = useMemo(
        () => WorkStationFactor[type] || {},
        [type]
    )

    const {
        actions,
        layout: InstanceLayout,
        stepsDescribe,
        title,
        extraTitleInfo
    } = workStationConfig

    const debugType = workStationConfig.debugType ?? false
    const mockData = useMemo(
        () => workStationConfig.mockData ?? [],
        [workStationConfig]
    )

    useEffect(() => {
        let isMounted = true

        const loadInitialStationData = async () => {
            try {
                setIsLoadingStatus(true)
                const res: any = await request_work_station_view()

                if (!isMounted) return

                // Populate MobX store with initial data.
                // NOTE: we do NOT call setIsStationSelected here.
                // isStationSelected is owned by useStationSession.
                if (res?.data) {
                    workStationStore.setWorkStationEvent(res.data)
                }
            } catch (error) {
                if (!isMounted) return
                console.error("获取工作站状态失败:", error)
            } finally {
                if (isMounted) {
                    setIsLoadingStatus(false)
                }
            }
        }

        loadInitialStationData()

        return () => {
            isMounted = false
        }
    }, [])

    useEffect(() => {
        if (isLoadingStatus || (isInitialized.current && !isStationSelected))
            return
        isInitialized.current = true

        workStationEventLoop.setDebuggerConfig(debugType, mockData as any[])
        workStationEventLoop.initListener({
            eventListener: (workStationEvent) => {
                workStationStore.setWorkStationEvent(workStationEvent)
            }
        })

        if (
            type === WORK_STATION_TYPE_CARD &&
            !workStationEventLoop.getCurrentEvent()
        ) {
            workStationEventLoop.start()
        }

        return () => {
            const currentPath = window.location.pathname
            const isLeavingWorkStation =
                !currentPath.startsWith("/wms/workStation/")

            if (isLeavingWorkStation) {
                workStationEventLoop.stop()

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

            if (process.env.NODE_ENV === "development") {
                try {
                    const maxTimerId = setTimeout(() => {}, 0)
                    for (let i = maxTimerId; i > maxTimerId - 100; i--) {
                        clearTimeout(i)
                    }
                } catch (error) {
                    // ignore
                }
            }
        }
    }, [debugType, mockData, type, isStationSelected, isLoadingStatus])

    if (isLoadingStatus) {
        return (
            <div
                className="w-full h-full d-flex justify-center items-center"
                style={{ backgroundColor: "#fff" }}
            >
                <Spin size="large" tip="正在加载工作站信息..." />
            </div>
        )
    }

    if (!isStationSelected) {
        return <SelectStation onStationSelected={selectStation} />
    }

    return type === WORK_STATION_TYPE_CARD ? (
        <WorkStationCard />
    ) : (
        <Layout
            extraTitleInfo={extraTitleInfo}
            actions={actions}
            title={title}
            stepsDescribe={stepsDescribe}
        >
            <InstanceLayout />
        </Layout>
    )
}

export default WorkStation
```

Key changes from the original:
- Added import: `import { useStationSession } from "./state/hooks/useStationSession"`
- Removed: `const [isConfigStationId, setIsConfigStationId] = useState(...)`
- Added: `const { isStationSelected, selectStation } = useStationSession()`
- Renamed `getStationStatus` → `loadInitialStationData` to reflect its new purpose
- Removed: `setIsConfigStationId(isConfigured)` from the async function
- Renamed: `STATION_STATUS_NOT_CONFIGURED` constant no longer needed (removed)
- Updated: all `isConfigStationId` → `isStationSelected` throughout
- Updated: `<SelectStation isConfigStationId={...} setIsConfigStationId={...} />` → `<SelectStation onStationSelected={selectStation} />`

- [ ] **Step 2: Verify compilation with no errors**

```bash
npx tsc --noEmit
```

Expected: zero TypeScript errors. If you see errors, the most likely causes are:
- `useStationSession` import path wrong → check that the file is at `src/pages/wms/station/state/hooks/useStationSession.ts`
- SelectStation prop mismatch → verify Task 5 is complete

- [ ] **Step 3: Manual smoke test**

Start the dev server:
```bash
npm run dev
# or: yarn dev
```

Test the exit flow:
1. Navigate to `/wms/workStation`
2. If SelectStation appears: select a workstation and confirm
3. WorkStationCard appears — click any task type card (e.g., 拣货)
4. You are now on `/wms/workStation/outbound` (or similar)
5. Click the "退出" button → confirm in the dialog
6. **Expected:** You are now at `/wms/workStation` showing WorkStationCard (not SelectStation, not the operation page)
7. Click another task type card to verify it still works

Test the refresh flow:
1. Select a workstation and enter an operation page
2. Refresh the browser
3. **Expected:** Page loads, shows WorkStationCard (stationId still in localStorage), then event loop navigates to the appropriate operation page based on server state

Test the first-time flow:
1. Open DevTools → Application → Local Storage → delete `stationId` key
2. Navigate to `/wms/workStation`
3. **Expected:** SelectStation appears
4. Select a workstation and confirm
5. **Expected:** WorkStationCard appears

- [ ] **Step 4: Commit**

```bash
git add src/pages/wms/station/index.tsx
git commit -m "feat(station): wire useStationSession in WorkStation, fix exit-to-card bug"
```

---

## Post-Implementation Checklist

- [ ] All 6 files modified/created
- [ ] `npx tsc --noEmit` passes with zero errors
- [ ] Exit → WorkStationCard (not SelectStation) ✓
- [ ] Browser refresh preserves station selection ✓
- [ ] First-time visit (no stationId) shows SelectStation ✓
- [ ] Selecting a station and confirming shows WorkStationCard ✓
- [ ] Entering a task type from WorkStationCard and exiting returns to WorkStationCard ✓

---

## Phase 2 Extension Point (IP Binding)

When implementing IP-based auto-binding, add `autoSelectByIp()` to `useStationSession`:

```typescript
// In useStationSession.ts — Phase 2 addition only
const autoSelectByIp = async () => {
    const res = await fetchStationByIp()  // new API call
    if (res.stationId) {
        selectStation(res.stationId)
    }
}

// In the hook's useEffect (Phase 2):
useEffect(() => {
    if (!isStationSelected) {
        autoSelectByIp()
    }
}, [])
```

`index.tsx` does not need to change. The hook is the single extension point.
