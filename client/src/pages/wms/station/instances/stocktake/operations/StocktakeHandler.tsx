import React from "react"

import type {
    OperationTaskDTOS,
    PickingViewItem,
    WorkStationView
} from "@/pages/wms/station/event-loop/types"
import type {OperationProps} from "@/pages/wms/station/instances/types"
import Operation from "./components/Operation"
import {isBlindStocktake} from "./enumLabels"

export interface StocktakeStageValue {
    view?: PickingViewItem
    task?: OperationTaskDTOS
    showCount: boolean
    scanMismatched: boolean
    isBlind: boolean
}

const barcodeMatches = (scanCode: string, barcodes: string[]): boolean => {
    if (!scanCode) return false
    const scanned = scanCode.trim().toLowerCase()
    return barcodes.some((barcode) => {
        const candidate = String(barcode || "").trim().toLowerCase()
        if (!candidate) return false
        return (
            candidate === scanned ||
            candidate.includes(scanned) ||
            scanned.includes(candidate)
        )
    })
}

/**
 * Reads the first counting record in this container and decides whether the
 * operator may enter a quantity: the backend marks the matching task
 * PROCESSING when a barcode is scanned, so that is the primary signal; a
 * direct barcode match is the fallback for cached views that lag behind.
 */
export const valueFilter = (
    data?: WorkStationView
): StocktakeStageValue => {
    if (!data?.skuArea) {
        return {showCount: false, scanMismatched: false, isBlind: false}
    }

    const view = data.skuArea.operationViews?.[0]
    const task = view?.operationTaskDTOs?.[0]
    const scanCode = String(data.skuArea.scanCode || "").trim()
    const barcodes = view?.skuMainDataDTO?.skuBarcode?.barcodes || []
    const method =
        data.orderArea?.currentOrder?.stocktakeMethod ||
        task?.extendInfo?.stocktakeMethod
    const isBlind = isBlindStocktake(method)
    const isProcessing =
        String(task?.taskStatus || "").toUpperCase() === "PROCESSING"
    const hasScan = scanCode.length > 0
    const showCount =
        hasScan && (isProcessing || barcodeMatches(scanCode, barcodes))

    return {
        view,
        task,
        showCount,
        scanMismatched: hasScan && !showCount,
        isBlind
    }
}

const StocktakeAreaHandler = (
    props: OperationProps<StocktakeStageValue, unknown>
) => {
    const value = props.value || {
        showCount: false,
        scanMismatched: false,
        isBlind: false
    }

    return (
        <section className="stocktake-stage">
            <Operation {...value} />
        </section>
    )
}

export default StocktakeAreaHandler
