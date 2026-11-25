import {InboxOutlined} from "@ant-design/icons"
import React from "react"
import {useTranslation} from "react-i18next"

import type {
    ArrivedContainer,
    WorkStationView
} from "@/pages/wms/station/event-loop/types"
import type {OperationProps} from "@/pages/wms/station/instances/types"

const MAX_VISIBLE_LEVELS = 6

export interface ContainerPanelValue {
    container?: ArrivedContainer
    face?: string
    slot?: string
    workLocationCode?: string
}

export const valueFilter = (data?: WorkStationView): ContainerPanelValue => {
    if (!data) return {}

    const workLocations = data.workLocationArea?.workLocationViews || []
    const slots = workLocations.flatMap(
        (workLocation) => workLocation.workLocationSlots || []
    )
    const task = data.skuArea?.operationViews?.[0]?.operationTaskDTOs?.[0]
    const activeSlot =
        slots.find(
            (slot) =>
                slot.arrivedContainer?.containerCode ===
                task?.sourceContainerCode
        ) || slots.find((slot) => slot.arrivedContainer)
    const container = activeSlot?.arrivedContainer || undefined

    return {
        container,
        face: task?.sourceContainerFace || container?.face,
        slot: task?.sourceContainerSlot,
        workLocationCode:
            activeSlot?.workLocationCode ||
            workLocations[0]?.workLocationCode
    }
}

const ContainerPanel = (
    props: OperationProps<ContainerPanelValue, unknown>
) => {
    const {t} = useTranslation()
    const {container, face, slot, workLocationCode} = props.value || {}

    const specs = container?.containerSpec?.containerSlotSpecs || []
    const faceSpecs = face
        ? specs.filter((spec) => !spec.face || spec.face === face)
        : specs
    const levels = Array.from(
        new Set(faceSpecs.map((spec) => spec.locLevel))
    ).sort((left, right) => right - left)

    let visibleLevels = levels
    if (levels.length > MAX_VISIBLE_LEVELS) {
        const activeLevel = faceSpecs.find(
            (spec) => spec.containerSlotSpecCode === slot
        )?.locLevel
        const center = activeLevel ? levels.indexOf(activeLevel) : 0
        const start = Math.min(
            Math.max(center - 2, 0),
            levels.length - MAX_VISIBLE_LEVELS
        )
        visibleLevels = levels.slice(start, start + MAX_VISIBLE_LEVELS)
    }

    const rackLabel = t("stocktake.station.container.aria", {
        code: container?.containerCode || "—",
        face: face || "—",
        slot: slot || "—"
    })

    return (
        <aside className="stocktake-tote">
            <div>
                <div className="stocktake-tote__label">
                    {t("stocktake.station.container.title")}
                </div>
                <div className="stocktake-tote__code stocktake-mono">
                    {container?.containerCode || "—"}
                </div>
                <div className="stocktake-tote__meta">
                    <span className="stocktake-tote__chip">
                        {t("stocktake.station.container.face")}
                        <b>{face || "—"}</b>
                    </span>
                    {slot && (
                        <span className="stocktake-tote__chip">
                            {t("stocktake.station.container.slot")}
                            <b>{slot}</b>
                        </span>
                    )}
                </div>
            </div>

            {faceSpecs.length > 0 ? (
                <div
                    className="stocktake-tote__rack"
                    role="img"
                    aria-label={rackLabel}
                >
                    {visibleLevels.map((level) => {
                        const row = faceSpecs
                            .filter((spec) => spec.locLevel === level)
                            .sort((left, right) => left.locBay - right.locBay)
                        return (
                            <div
                                key={level}
                                className="stocktake-tote__level"
                            >
                                {row.map((spec) => {
                                    const isActive =
                                        spec.containerSlotSpecCode === slot
                                    return (
                                        <div
                                            key={spec.containerSlotSpecCode}
                                            className={
                                                "stocktake-tote__slot" +
                                                (isActive
                                                    ? " stocktake-tote__slot--active"
                                                    : "")
                                            }
                                            title={
                                                spec.containerSlotSpecCode
                                            }
                                        >
                                            {isActive
                                                ? spec.containerSlotSpecCode
                                                : ""}
                                        </div>
                                    )
                                })}
                            </div>
                        )
                    })}
                </div>
            ) : (
                <div className="stocktake-tote__empty">
                    <InboxOutlined aria-hidden="true" />
                    <span>
                        {t("stocktake.station.container.empty")}
                    </span>
                </div>
            )}

            {workLocationCode && (
                <div className="stocktake-tote__foot">
                    <span>
                        {t("stocktake.station.container.location")}
                    </span>
                    <b>{workLocationCode}</b>
                </div>
            )}
        </aside>
    )
}

export default ContainerPanel
