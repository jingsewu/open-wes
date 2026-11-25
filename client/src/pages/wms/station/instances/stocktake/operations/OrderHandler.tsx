import React from "react"
import {useTranslation} from "react-i18next"

import type {
    PickingViewItem,
    WorkStationView
} from "@/pages/wms/station/event-loop/types"
import type {OperationProps} from "@/pages/wms/station/instances/types"
import {
    isBlindStocktake,
    stocktakeMethodLabel,
    stocktakeTypeLabel
} from "./enumLabels"

export interface StocktakeRailValue {
    orderNo?: string
    method?: string
    type?: string
    views: PickingViewItem[]
}

export const valueFilter = (data?: WorkStationView): StocktakeRailValue => {
    if (!data) return {views: []}

    const views = data.skuArea?.operationViews || []
    const firstTask = views[0]?.operationTaskDTOs?.[0]
    const currentOrder = data.orderArea?.currentOrder

    return {
        orderNo: currentOrder?.orderNo || firstTask?.extendInfo?.stocktakeOrderNo,
        method:
            currentOrder?.stocktakeMethod ||
            firstTask?.extendInfo?.stocktakeMethod,
        type:
            currentOrder?.stocktakeType || firstTask?.extendInfo?.stocktakeType,
        views
    }
}

const JobRail = (props: OperationProps<StocktakeRailValue, unknown>) => {
    const {t} = useTranslation()
    const {orderNo, method, type, views = []} = props.value || {views: []}
    const isBlind = isBlindStocktake(method)

    return (
        <aside className="stocktake-rail">
            <div className="stocktake-rail__head">
                <span className="stocktake-field-label">
                    {t("stocktake.station.taskNo")}
                </span>
                <div className="stocktake-rail__order stocktake-mono">
                    {orderNo || "—"}
                </div>
            </div>

            <dl className="stocktake-rail__meta">
                <div className="stocktake-rail__pair">
                    <dt>
                        {t(
                            "stocktake.station.orderArea.div.stocktakeMethod"
                        )}
                    </dt>
                    <dd>{stocktakeMethodLabel(method, t)}</dd>
                </div>
                <div className="stocktake-rail__pair">
                    <dt>
                        {t("stocktake.station.orderArea.div.stocktakeType")}
                    </dt>
                    <dd>{stocktakeTypeLabel(type, t)}</dd>
                </div>
            </dl>

            <div className="stocktake-rail__queue">
                <div className="stocktake-rail__queue-head">
                    <span>{t("stocktake.station.queue.title")}</span>
                    <span className="stocktake-rail__badge">
                        {views.length}
                    </span>
                </div>

                <ol className="stocktake-queue">
                    {views.map((view, index) => {
                        const task = view.operationTaskDTOs?.[0]
                        const sku = view.skuMainDataDTO
                        const isCurrent = index === 0
                        return (
                            <li
                                key={task?.id || `${sku?.skuCode}-${index}`}
                                className={
                                    "stocktake-queue__item" +
                                    (isCurrent
                                        ? " stocktake-queue__item--current"
                                        : "")
                                }
                            >
                                <span className="stocktake-queue__index stocktake-mono">
                                    {String(index + 1).padStart(2, "0")}
                                </span>
                                <span className="stocktake-queue__info">
                                    <span className="stocktake-queue__code stocktake-mono">
                                        {sku?.skuCode || "—"}
                                    </span>
                                    <span
                                        className="stocktake-queue__name"
                                        title={sku?.skuName || undefined}
                                    >
                                        {sku?.skuName || ""}
                                    </span>
                                </span>
                                {!isBlind && (
                                    <span className="stocktake-queue__qty stocktake-mono">
                                        {task?.requiredQty ?? 0}
                                    </span>
                                )}
                                {isCurrent && (
                                    <span className="stocktake-sr-only">
                                        {t("stocktake.station.queue.current")}
                                    </span>
                                )}
                            </li>
                        )
                    })}
                </ol>
            </div>
        </aside>
    )
}

export default JobRail
