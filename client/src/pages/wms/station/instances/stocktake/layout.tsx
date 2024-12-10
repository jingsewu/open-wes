import { Col, Row } from "antd"
import React, { useEffect } from "react"

import type {
    WorkStationEvent,
    WorkStationInfo
} from "@/pages/wms/station/event-loop/types"
import type { OperationProps } from "@/pages/wms/station/instances/types"

import ComponentWrapper from "../../component-wrapper"
import { OPERATION_MAP } from "./config"
import { valueFilter as orderFilter } from "./operations/OrderHandler"
import { valueFilter as shelfFilter } from "./operations/ShelfHandler"
import { valueFilter as stocktakeFilter } from "./operations/StocktakeHandler"
import { StationOperationType } from "./type"
import {
    taskStatusText,
    valueFilter as defaultFilter
} from "@/pages/wms/station/instances/receive/operations/defaultPage"

interface StocktakeLayoutProps extends OperationProps<any, any> {
    workStationEvent: WorkStationEvent<any>
    workStationInfo: WorkStationInfo
}

const Layout = (props: StocktakeLayoutProps) => {
    const { workStationEvent } = props
    const stationStatus = workStationEvent?.stationProcessingStatus

    useEffect(() => {
        if (workStationEvent?.warehouseAreaId) {
            console.log("warehouseAreaId", workStationEvent?.warehouseAreaId)
            localStorage.setItem(
                "warehouseAreaId",
                workStationEvent.warehouseAreaId
            )
        }
    }, [])

    return (
        <Row className="h-full" gutter={24}>
            {stationStatus &&
            Object.keys(taskStatusText).includes(stationStatus) ? (
                <Col className="flex-1 rounded-lg">
                    <ComponentWrapper
                        type={StationOperationType.defaultArea}
                        Component={
                            OPERATION_MAP[StationOperationType.defaultArea]
                        }
                        valueFilter={defaultFilter}
                    />
                </Col>
            ) : (
                <>
                    <Col className="rounded-lg h-full" span={8}>
                        <ComponentWrapper
                            type={StationOperationType.orderArea}
                            Component={
                                OPERATION_MAP[StationOperationType.orderArea]
                            }
                            valueFilter={orderFilter}
                        />
                    </Col>
                    <Col className="rounded-lg h-full" span={8}>
                        <ComponentWrapper
                            type={StationOperationType.stocktakeArea}
                            Component={
                                OPERATION_MAP[
                                    StationOperationType.stocktakeArea
                                ]
                            }
                            valueFilter={stocktakeFilter}
                        />
                    </Col>
                    <Col className="rounded-lg h-full" span={8}>
                        <ComponentWrapper
                            type={StationOperationType.shelfArea}
                            Component={
                                OPERATION_MAP[StationOperationType.shelfArea]
                            }
                            valueFilter={shelfFilter}
                        />
                    </Col>
                </>
            )}
        </Row>
    )
}

export default Layout
