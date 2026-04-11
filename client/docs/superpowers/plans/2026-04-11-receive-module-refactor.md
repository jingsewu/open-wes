# Receive Module Refactor Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Eliminate the hollow `LayoutInner`/`LayoutContent` split, move business logic into a dedicated hook, and fix two confirmed bugs in `ContainerHandler`.

**Architecture:** Replace the empty `useReceiveState` hook with `useReceiveWorkflow` that owns all local state and handlers; collapse the dual layout components into one `observer` component; fix the `ContainerHandler` `useEffect` bugs.

**Tech Stack:** React 18, MobX + mobx-react-lite, TypeScript, Ant Design

**Spec:** `docs/superpowers/specs/2026-04-11-receive-module-refactor-design.md`

---

## File Map

| File | Action | What changes |
|------|--------|--------------|
| `src/pages/wms/station/instances/receive/hooks/index.ts` | Modify | Remove `useReceiveState`, add `useReceiveWorkflow` |
| `src/pages/wms/station/instances/receive/layout.tsx` | Modify | Collapse `LayoutInner`+`LayoutContent` → single `ReceiveLayout` observer |
| `src/pages/wms/station/instances/receive/operations/containerHandler.tsx` | Modify | Fix duplicate focus effect; fix self-triggering useEffect deps |

---

## Task 1: Add `useReceiveWorkflow` to `hooks/index.ts`

**Files:**
- Modify: `src/pages/wms/station/instances/receive/hooks/index.ts`

### What this task does

`useReceiveState` is a hollow hook — it just returns 4 raw `useState` pairs with no logic. We replace it with `useReceiveWorkflow`, which also owns the three business handlers (`onScanSubmit`, `onSkuChange`, `onConfirm`) that currently live in the layout render function.

The existing hooks (`useContainerSpecs`, `useFocusManagement`, `useSkuScanner`, `useQuantityControl`) are left exactly as-is.

- [ ] **Step 1: Replace `useReceiveState` with `useReceiveWorkflow` in `hooks/index.ts`**

Replace the entire file with:

```ts
import { useState, useCallback, useMemo } from 'react'
import type { InputRef } from 'antd'
import { useEffect, useRef } from 'react'
import { message } from 'antd'
import { useTranslation } from 'react-i18next'

import { useWorkStation } from '@/pages/wms/station/state'
import { MessageType } from '@/pages/wms/station/widgets/message'

import type { SkuInfo, OrderDetail } from '../types'
import { receiveApiService, createApiHandler } from '../services/api'
import { utils } from '../utils'
import { WAREHOUSE_CODE } from '../constants'

// 接收工作流Hook — 拥有所有本地状态和业务 handler
export const useReceiveWorkflow = () => {
    const [orderNo, setOrderNo] = useState("")
    const [orderInfo, setOrderInfo] = useState<any>()
    const [currentSkuInfo, setCurrentSkuInfo] = useState<any>({})
    const [focusValue, setFocusValue] = useState("")

    const { store, message: showMessage, onActionDispatch } = useWorkStation()

    const apiHandler = useMemo(
        () => createApiHandler((error: any) => {
            showMessage?.({ type: MessageType.ERROR, content: error.message })
        }),
        [showMessage]
    )

    const onScanSubmit = useCallback(async () => {
        await apiHandler(async () => {
            const orderData = await receiveApiService.queryPlan(orderNo, WAREHOUSE_CODE!)
            setOrderInfo(orderData)
            setFocusValue("sku")
        })
    }, [apiHandler, orderNo])

    const onSkuChange = useCallback((detail: any) => {
        setCurrentSkuInfo(detail)
        setFocusValue("container")
    }, [])

    const onConfirm = useCallback(async ({
        containerCode,
        containerSpecCode,
        containerId,
        activeSlot,
        inputValue
    }: any) => {
        const workStationEvent = store.workStationEvent
        await apiHandler(async () => {
            const res = await receiveApiService.acceptPlan({
                inboundPlanOrderId: orderInfo.id,
                inboundPlanOrderDetailId: currentSkuInfo.id,
                warehouseCode: WAREHOUSE_CODE!,
                qtyAccepted: inputValue,
                skuId: currentSkuInfo.skuId,
                targetContainerCode: containerCode,
                targetContainerSpecCode: containerSpecCode,
                targetContainerSlotCode: activeSlot[0],
                batchAttributes: {},
                targetContainerId: containerId,
                workStationId: workStationEvent!.workStationId
            })
            if (res.status === 200) {
                if (workStationEvent?.hasOrder) await onScanSubmit()
                setCurrentSkuInfo({})
                setFocusValue("sku")
            }
        })
    }, [apiHandler, orderInfo, currentSkuInfo, store, onScanSubmit])

    return {
        orderNo, setOrderNo,
        orderInfo,
        currentSkuInfo,
        focusValue,
        setFocusValue,
        onScanSubmit,
        onSkuChange,
        onConfirm,
        store,
        onActionDispatch
    }
}

// 容器规格管理Hook
export const useContainerSpecs = () => {
    const [specOptions, setSpecOptions] = useState<any[]>([])
    const [containerSpec, setContainerSpec] = useState<any>({})
    const [containerSlotSpec, setContainerSlotSpec] = useState<any>("")
    const [activeSlot, setActiveSlot] = useState<string[]>([])

    const initializeSpecs = async (warehouseCode: string, containerType: string) => {
        try {
            const options = await receiveApiService.getContainerSpecs(warehouseCode, containerType)
            setSpecOptions(options)

            if (options.length > 0) {
                setContainerSpec({ containerSpecCode: options[0].value })
                setContainerSlotSpec(JSON.parse(options[0].containerSlotSpecs || "[]"))
            }

            return options
        } catch (error) {
            console.error("Failed to initialize container specs:", error)
            return []
        }
    }

    const updateContainerSpec = (specCode: string, specOptions: any[]) => {
        setContainerSpec((prev: any) => ({ ...prev, containerSpecCode: specCode }))
        const slotSpec = specOptions.find(item => item.value === specCode)?.containerSlotSpecs
        setContainerSlotSpec(JSON.parse(slotSpec || "[]"))
    }

    const resetSpecs = () => {
        setContainerSpec({})
        setContainerSlotSpec("")
        setActiveSlot([])
    }

    return {
        specOptions,
        containerSpec,
        containerSlotSpec,
        activeSlot,
        setActiveSlot,
        setContainerSpec,
        setContainerSlotSpec,
        initializeSpecs,
        updateContainerSpec,
        resetSpecs
    }
}

// 焦点管理Hook
export const useFocusManagement = (focusValue: string) => {
    const containerRef = useRef<InputRef>(null)
    const countRef = useRef<any>(null)

    useEffect(() => {
        if (focusValue === "container") {
            containerRef.current?.focus()
        } else if (focusValue === "count") {
            countRef.current?.focus()
        }
    }, [focusValue])

    return { containerRef, countRef }
}

// SKU扫描Hook
export const useSkuScanner = (onSkuChange: (detail: any) => void) => {
    const [skuCode, setSkuCode] = useState<string>("")
    const { t } = useTranslation()

    const scanSku = async (details?: any[]) => {
        try {
            if (details && details.length > 0) {
                const detail = utils.findSkuInOrderDetails(details, skuCode)
                if (!detail) {
                    setSkuCode("")
                    message.warning(t("receive.station.warning.skuNotInOrder"))
                    return
                }
                onSkuChange(detail)
                return
            }

            const sku = await receiveApiService.getSkuByCode(skuCode)
            onSkuChange({
                skuId: sku.id,
                skuCode: sku.skuCode,
                skuName: sku.skuName
            })
        } catch (error) {
            console.error("SKU lookup error:", error)
            setSkuCode("")
            message.warning(t("receive.station.warning.skuNotExist"))
        }
    }

    const resetSkuCode = () => setSkuCode("")

    return { skuCode, setSkuCode, scanSku, resetSkuCode }
}

// 数量控制Hook
export const useQuantityControl = () => {
    const [inputValue, setInputValue] = useState<number | string>("")

    const handleQuantityChange = {
        onChange: (value: number) => setInputValue(value),
        minus: () => {
            if (inputValue) {
                setInputValue((prev: number | string) => Math.max(0, Number(prev) - 1))
            }
        },
        plus: () => setInputValue((prev: number | string) => (Number(prev) || 0) + 1)
    }

    const resetQuantity = () => setInputValue("")

    return { inputValue, setInputValue, handleQuantityChange, resetQuantity }
}
```

- [ ] **Step 2: Verify TypeScript compiles**

```bash
cd D:/open-wes/client && npx tsc --noEmit 2>&1 | head -30
```

Expected: no errors from `hooks/index.ts`. Any errors about missing `useReceiveState` will be fixed in Task 2.

- [ ] **Step 3: Commit**

```bash
cd D:/open-wes/client
git add src/pages/wms/station/instances/receive/hooks/index.ts
git commit -m "refactor(receive): replace useReceiveState with useReceiveWorkflow"
```

---

## Task 2: Collapse `layout.tsx` to single observer component

**Files:**
- Modify: `src/pages/wms/station/instances/receive/layout.tsx`

### What this task does

Remove the `LayoutInner`/`LayoutContent` two-component pattern. Replace with a single `ReceiveLayout` observer that calls `useReceiveWorkflow()` for everything it needs. No business logic in the render body.

- [ ] **Step 1: Replace `layout.tsx` with the collapsed single-component version**

```tsx
import React from "react"
import { Button, Col, Input, Row } from "antd"
import { useTranslation } from "react-i18next"

import type { OperationProps } from "@/pages/wms/station/instances/types"
import { WorkStationView } from "@/pages/wms/station/event-loop/types"
import { observer } from "@/pages/wms/station/state"

import ComponentWrapper from "../../component-wrapper"
import { OPERATION_MAP } from "./config"
import { StationOperationType } from "./types"
import { valueFilter as scanInfoFilter } from "./operations/tips"
import ContainerHandler from "./operations/containerHandler"
import SkuHandler from "./operations/skuHandler"
import OrderHandler from "./operations/orderHandler"
import { useReceiveWorkflow } from "./hooks"

interface ReplenishLayoutProps extends OperationProps<any, any> {
    workStationEvent: WorkStationView<any>
}

const ReceiveLayout = observer((props: ReplenishLayoutProps) => {
    const { t } = useTranslation()
    const {
        orderNo, setOrderNo,
        orderInfo,
        currentSkuInfo,
        focusValue,
        setFocusValue,
        onScanSubmit,
        onSkuChange,
        onConfirm,
        store,
        onActionDispatch
    } = useReceiveWorkflow()

    if (!store?.workStationEvent) {
        return <div>{t("common.loading")}</div>
    }

    const hasOrder = store.workStationEvent?.hasOrder ?? ""

    const renderScanOrderView = () => (
        <div className="w-full h-full d-flex flex-col justify-center items-center">
            <div className="w-1/3">
                <div className="text-xl">{t("receive.station.button.scanLpn")}</div>
                <Input
                    size="large"
                    className="my-4 w-full"
                    value={orderNo}
                    onChange={(e) => setOrderNo(e.target.value)}
                />
                <Button type="primary" block onClick={onScanSubmit}>
                    {t("receive.station.button.confirm")}
                </Button>
            </div>
        </div>
    )

    const renderWorkView = () => (
        <Row className="h-full" justify="space-between" gutter={16}>
            {hasOrder && orderInfo && (
                <Col span={24}>
                    <OrderHandler value={orderInfo} />
                </Col>
            )}
            <Col span={12} className="pt-4">
                <SkuHandler
                    details={orderInfo?.details}
                    currentSkuInfo={currentSkuInfo}
                    focusValue={focusValue}
                    onSkuChange={onSkuChange}
                    displayQty={hasOrder}
                />
            </Col>
            <Col span={12} className="pt-4">
                <ContainerHandler
                    focusValue={focusValue}
                    onConfirm={onConfirm}
                    changeFocusValue={setFocusValue}
                    onScanSubmit={onScanSubmit}
                    hasOrder={hasOrder}
                    onActionDispatch={onActionDispatch}
                />
            </Col>
        </Row>
    )

    return (
        <>
            {(hasOrder === false || orderInfo) ? renderWorkView() : renderScanOrderView()}
            <ComponentWrapper
                type={StationOperationType.tips}
                Component={OPERATION_MAP[StationOperationType.tips]}
                valueFilter={scanInfoFilter}
                withWrapper={false}
            />
        </>
    )
})

export default ReceiveLayout
```

- [ ] **Step 2: Verify TypeScript compiles clean**

```bash
cd D:/open-wes/client && npx tsc --noEmit 2>&1 | head -30
```

Expected: 0 errors from `layout.tsx` or `hooks/index.ts`.

- [ ] **Step 3: Commit**

```bash
cd D:/open-wes/client
git add src/pages/wms/station/instances/receive/layout.tsx
git commit -m "refactor(receive): collapse LayoutInner/LayoutContent into single observer component"
```

---

## Task 3: Fix two bugs in `containerHandler.tsx`

**Files:**
- Modify: `src/pages/wms/station/instances/receive/operations/containerHandler.tsx`

### Bug 1 — Duplicate focus management

`useFocusManagement(focusValue)` (hook at line 46) already sets up a `useEffect` that calls `containerRef.current?.focus()` and `countRef.current?.focus()` when `focusValue` changes.

The component's own `useEffect` (lines 89–97) duplicates these exact focus calls. Two effects fire on the same `focusValue` change. Fix: remove the focus calls from the component's effect, keep only the cleanup (reset container code and quantity).

### Bug 2 — Self-triggering `useEffect`

The init effect (lines 70–87) has `[propContainerCode, containerCode]` as deps. Inside this effect, `setContainerCode(propContainerCode)` is called, which modifies `containerCode` state, which triggers the effect again — calling `initializeSpecs` a second time needlessly. Fix: remove `containerCode` from the dependency array.

- [ ] **Step 1: Apply both fixes to `containerHandler.tsx`**

Find and replace the two `useEffect` blocks. The rest of the file is unchanged.

**Replace the init effect (currently lines ~70–87):**

```tsx
// Before
useEffect(() => {
    if (!WAREHOUSE_CODE) {
        message.error(t("warehouse.code.missing"))
        return
    }

    const initialize = async () => {
        const options = await initializeSpecs(WAREHOUSE_CODE!, CONTAINER_TYPE)

        // 处理预设容器代码
        if (propContainerCode && propContainerCode !== containerCode) {
            setContainerCode(propContainerCode)
            setTimeout(() => onPressEnterLocal(propContainerCode, options), 0)
        }
    }

    initialize()
}, [propContainerCode, containerCode])
```

```tsx
// After — remove containerCode from deps; remove the propContainerCode !== containerCode guard
useEffect(() => {
    if (!WAREHOUSE_CODE) {
        message.error(t("warehouse.code.missing"))
        return
    }

    const initialize = async () => {
        const options = await initializeSpecs(WAREHOUSE_CODE!, CONTAINER_TYPE)

        if (propContainerCode) {
            setContainerCode(propContainerCode)
            setTimeout(() => onPressEnterLocal(propContainerCode, options), 0)
        }
    }

    initialize()
}, [propContainerCode])
```

**Replace the focus effect (currently lines ~89–97):**

```tsx
// Before
useEffect(() => {
    if (focusValue === "container") {
        setContainerCode("")
        resetQuantity()
        containerRef.current?.focus()
    } else if (focusValue === "count") {
        countRef.current?.focus()
    }
}, [focusValue])
```

```tsx
// After — remove focus calls (already handled by useFocusManagement), keep only cleanup
useEffect(() => {
    if (focusValue === "container") {
        setContainerCode("")
        resetQuantity()
    }
}, [focusValue])
```

- [ ] **Step 2: Verify TypeScript compiles clean**

```bash
cd D:/open-wes/client && npx tsc --noEmit 2>&1 | head -30
```

Expected: 0 errors.

- [ ] **Step 3: Commit**

```bash
cd D:/open-wes/client
git add src/pages/wms/station/instances/receive/operations/containerHandler.tsx
git commit -m "fix(receive): remove duplicate focus effect and fix self-triggering useEffect deps in ContainerHandler"
```

---

## Manual Verification Checklist

After all three tasks, open the receive station page and verify:

- [ ] **Scan order flow**: Enter an LPN/order number → click confirm → order info displays, focus moves to SKU input
- [ ] **SKU scan flow**: Scan a SKU barcode → SKU info displays, focus moves to container input
- [ ] **Container scan flow**: Scan a container code → container spec and slot grid appear, focus moves to quantity input
- [ ] **Confirm flow**: Select a slot → enter quantity → click confirm → state resets to SKU input (or order re-fetches if hasOrder)
- [ ] **Container full flow**: Click "满箱" button → state resets, focus returns to SKU input
- [ ] **Focus does not double-fire**: Confirm that scanning a container moves focus to the quantity field exactly once (previously, two effects both called `.focus()`)
- [ ] **No extra API calls**: Opening the container handler should call `getContainerSpecs` exactly once (previously, the self-triggering effect caused it to be called twice)
