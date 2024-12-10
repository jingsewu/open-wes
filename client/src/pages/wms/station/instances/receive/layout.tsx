/**
 * 当前工作站实例的布局
 */
import { Col, Row, Input, Button, message } from "antd"
import classNames from "classnames/bind"
import React, { useState } from "react"
import request from "@/utils/requestInterceptor"

import type {
    WorkStationEvent,
    WorkStationInfo
} from "@/pages/wms/station/event-loop/types"

import type { OperationProps } from "@/pages/wms/station/instances/types"

import ComponentWrapper from "../../component-wrapper"
import { OPERATION_MAP } from "./config"
import style from "./index.module.scss"
import {
    valueFilter as defaultFilter,
    taskStatusText
} from "./operations/defaultPage"
import { valueFilter as pickFilter } from "./operations/pickingHandler"
import { valueFilter as robotFilter } from "./operations/RobotHandler"
import { valueFilter as scanInfoFilter } from "./operations/tips"
import { StationOperationType } from "./type"
import PickingHandler from "./operations/pickingHandler"
import RobotHandler from "./operations/RobotHandler"
import OrderHandler from "./operations/orderHandler"

let warehouseCode = localStorage.getItem("warehouseCode")

interface ReplenishLayoutProps extends OperationProps<any, any> {
    workStationEvent: WorkStationEvent<any>
    workStationInfo: WorkStationInfo
}
const filterMap = {
    robotArea: robotFilter
}

const cx = classNames.bind(style)

const Layout = (props: ReplenishLayoutProps) => {
    const { workStationEvent } = props

    const stationStatus = workStationEvent?.stationProcessingStatus

    const [orderNo, setOrderNo] = useState("")

    const [orderInfo, setOrderInfo] = useState<any>()
    const [currentSkuInfo, setCurrentSkuInfo] = useState<any>({})

    const onScanSubmit = () => {
        // console.log("orderNo",orderNo)
        request({
            method: "post",
            // url: `/wms/inbound/plan/query/${orderNo}/${workStationEvent.warehouseCode}`
            url: `/wms/inbound/plan/query/${orderNo}/`+warehouseCode
        })
            .then((res: any) => {
                console.log("res", res)
                setOrderInfo(res.data.data)
            })
            .catch((error) => {
                console.log("error", error)
                message.error(error.message)
            })
    }

    const onSkuChange = (detail: any) => {
        setCurrentSkuInfo(detail)
    }

    const onConfirm = ({
        containerCode,
        containerSpecCode,
        containerId,
        activeSlot,
        inputValue
    }: any) => {
        request({
            method: "post",
            url: "/wms/inbound/plan/accept",
            data: {
                inboundPlanOrderId: orderInfo.id,
                inboundPlanOrderDetailId: currentSkuInfo.id,
                warehouseCode,
                qtyAccepted: inputValue,
                skuId: currentSkuInfo.skuId,
                targetContainerCode: containerCode,
                targetContainerSpecCode: containerSpecCode,
                targetContainerSlotCode: activeSlot[0],
                batchAttributes: {},
                targetContainerId: containerId,
                workStationId: workStationEvent.workStationId
            },
            headers: {
                "Content-Type": "application/json"
            }
        })
            .then((res: any) => {
                console.log("confirm", res)
                if (res.status === 200) {
                    onScanSubmit()
                }
            })
            .catch((error) => {
                console.log("error", error)
            })
    }

    return (
        <>
            {orderInfo ? (
                <Row className="h-full" justify="space-between" gutter={16}>
                    <Col span={24}>
                        {/* <ComponentWrapper
                            type={StationOperationType.orderArea}
                            Component={
                                OPERATION_MAP[StationOperationType.orderArea]
                            }
                            valueFilter={defaultFilter}
                        /> */}
                        <OrderHandler value={orderInfo} />
                    </Col>
                    <Col span={12} className="pt-4">
                        {/* <ComponentWrapper
                            type={StationOperationType.selectDetailArea}
                            Component={
                                OPERATION_MAP[
                                    StationOperationType.selectDetailArea
                                ]
                            }
                            valueFilter={defaultFilter}
                        /> */}
                        <PickingHandler
                            value={orderInfo}
                            onSkuChange={onSkuChange}
                        />
                    </Col>
                    <Col span={12} className="pt-4">
                        {/* <ComponentWrapper
                            type={StationOperationType.robotArea}
                            Component={
                                OPERATION_MAP[StationOperationType.robotArea]
                            }
                            valueFilter={defaultFilter}
                        /> */}
                        <RobotHandler value={orderInfo} onConfirm={onConfirm} />
                    </Col>
                </Row>
            ) : (
                <div className="w-full h-full d-flex flex-col justify-center items-center">
                    <div className="w-1/3">
                        <div className="text-xl">请扫描LPN号</div>
                        <Input
                            size="large"
                            className="my-4 w-full"
                            value={orderNo}
                            onChange={(e) => setOrderNo(e.target.value)}
                            // style={{ width: "100%" }}
                        />
                        <Button type="primary" block onClick={onScanSubmit}>
                            确定
                        </Button>
                    </div>
                </div>
            )}

            <ComponentWrapper
                type={StationOperationType.tips}
                Component={OPERATION_MAP[StationOperationType.tips]}
                valueFilter={scanInfoFilter}
                withWrapper={false}
            />
        </>
    )
}

export default Layout
