import React, {useEffect} from "react"

import type {WorkStationView} from "@/pages/wms/station/event-loop/types"
import {observer, useWorkStation} from "@/pages/wms/station/state"

import ComponentWrapper from "../../component-wrapper"
import {OPERATION_MAP} from "./config"
import {valueFilter as orderFilter} from "./operations/OrderHandler"
import {valueFilter as shelfFilter} from "./operations/ShelfHandler"
import {valueFilter as stocktakeFilter} from "./operations/StocktakeHandler"
import Standby from "./operations/components/Standby"
import type {StandbyStatus} from "./operations/components/Standby"
import {StationOperationType} from "./type"
import "./index.module.scss"

interface StocktakeLayoutProps {
    workStationEvent?: WorkStationView
}

const Layout = observer((props: StocktakeLayoutProps) => {
    // The station page renders <InstanceLayout /> without props, so read the
    // live view from the store; the observer wrapper keeps the standby state
    // and bench switch in sync with CONTAINER_ARRIVED / SCAN_BARCODE.
    const {workStationEvent: storeEvent} = useWorkStation()
    const workStationEvent = props.workStationEvent ?? storeEvent

    useEffect(() => {
        if (workStationEvent?.warehouseAreaId) {
            localStorage.setItem(
                "warehouseAreaId",
                workStationEvent.warehouseAreaId
            )
        }
    }, [workStationEvent?.warehouseAreaId])

    const views = workStationEvent?.skuArea?.operationViews || []
    const hasTasks = views.length > 0
    const activeTask = views[0]?.operationTaskDTOs?.[0]
    const workLocationSlots = (
        workStationEvent?.workLocationArea?.workLocationViews || []
    ).flatMap((view) => view.workLocationSlots || [])
    const arrivedContainer =
        workLocationSlots.find(
            (slot) =>
                slot.arrivedContainer?.containerCode ===
                activeTask?.sourceContainerCode
        )?.arrivedContainer ||
        workLocationSlots.find((slot) => slot.arrivedContainer)
            ?.arrivedContainer
    const stationStatus = workStationEvent?.stationProcessingStatus

    let standbyStatus: StandbyStatus = "waitContainer"
    if (!workStationEvent) {
        standbyStatus = "connecting"
    } else if (stationStatus === "WAIT_ROBOT") {
        standbyStatus = "waitRobot"
    } else if (stationStatus === "WAIT_CALL_CONTAINER") {
        standbyStatus = "callContainer"
    } else if (arrivedContainer) {
        standbyStatus = "preparing"
    } else if (stationStatus === "NO_TASK") {
        standbyStatus = "noTask"
    }

    return (
        <div className="stocktake-station">
            {hasTasks ? (
                <div className="stocktake-bench">
                    <div className="stocktake-bench__rail">
                        <ComponentWrapper
                            type={StationOperationType.orderArea}
                            Component={
                                OPERATION_MAP[StationOperationType.orderArea]
                            }
                            valueFilter={orderFilter}
                            withWrapper={false}
                        />
                    </div>
                    <div className="stocktake-bench__stage">
                        <ComponentWrapper
                            type={StationOperationType.stocktakeArea}
                            Component={
                                OPERATION_MAP[
                                    StationOperationType.stocktakeArea
                                ]
                            }
                            valueFilter={stocktakeFilter}
                            withWrapper={false}
                        />
                    </div>
                    <div className="stocktake-bench__tote">
                        <ComponentWrapper
                            type={StationOperationType.shelfArea}
                            Component={
                                OPERATION_MAP[StationOperationType.shelfArea]
                            }
                            valueFilter={shelfFilter}
                            withWrapper={false}
                        />
                    </div>
                </div>
            ) : (
                <Standby status={standbyStatus} />
            )}
        </div>
    )
})

export default Layout
