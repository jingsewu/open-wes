import {Col, Row} from "antd"
import classNames from "classnames/bind"
import React, {useState} from "react"
import request from "@/utils/requestInterceptor"

import {WorkStationView} from "@/pages/wms/station/event-loop/types"

import type {OperationProps} from "@/pages/wms/station/instances/types"

import ComponentWrapper from "../../component-wrapper"
import {OPERATION_MAP} from "./config"
import style from "../receive/index.module.scss"
import ContainerHandler from "../receive/operations/containerHandler"
import {valueFilter as scanInfoFilter} from "../receive/operations/tips"
import {StationOperationType} from "../receive/type"
import SkuHandler from "../receive/operations/skuHandler"

let warehouseCode = localStorage.getItem("warehouseCode")

interface ReplenishLayoutProps extends OperationProps<any, any> {
    workStationEvent: WorkStationView<any>
}

const cx = classNames.bind(style)

const Layout = (props: ReplenishLayoutProps) => {
    //TODO by Evelyn 这里可能是undefined,导致后面确定收货提交的时候 workStationEvent.workStationId就会报错
    if (props === undefined) {
        return <div>加载中</div>
    }

    const {workStationEvent} = props
    const [currentSkuInfo, setCurrentSkuInfo] = useState<any>({})
    const [focusValue, setFocusValue] = useState("")

    const onSkuChange = (detail: any) => {
        setCurrentSkuInfo(detail)
        changeFocusValue("container")
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
        }).then((res: any) => {
            console.log("confirm", res)
            if (res.status === 200) {
                setCurrentSkuInfo({})
                changeFocusValue("sku")
            }
        }).catch((error) => {
            console.log("error", error)
        })
    }

    const changeFocusValue = (value: string) => {
        setFocusValue(value)
    }

    return (
        <>
            <Row className="h-full" justify="space-between" gutter={16}>
                <Col span={12} className="pt-4">
                    <SkuHandler
                        currentSkuInfo={currentSkuInfo}
                        focusValue={focusValue}
                        onSkuChange={onSkuChange}
                    />
                </Col>
                <Col span={12} className="pt-4">
                    <ContainerHandler
                        focusValue={focusValue}
                        onConfirm={onConfirm}
                        changeFocusValue={changeFocusValue}
                    />
                </Col>
            </Row>

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
