/**
 * 当前工作站实例的布局
 */
import {Button, Col, Input, message, Row} from "antd"
import classNames from "classnames/bind"
import React, {useState} from "react"
import request from "@/utils/requestInterceptor"

import type {WorkStationView} from "@/pages/wms/station/event-loop/types"

import type {OperationProps} from "@/pages/wms/station/instances/types"

import ComponentWrapper from "../../component-wrapper"
import {OPERATION_MAP} from "./config"
import style from "./index.module.scss"
import ContainerHandler from "./operations/containerHandler"
import {valueFilter as scanInfoFilter} from "./operations/tips"
import {StationOperationType} from "./type"
import SkuHandler from "./operations/skuHandler"
import OrderHandler from "./operations/orderHandler"
import {useTranslation} from "react-i18next";

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
    const [orderNo, setOrderNo] = useState("")
    const [orderInfo, setOrderInfo] = useState<any>()
    const [currentSkuInfo, setCurrentSkuInfo] = useState<any>({})
    const [focusValue, setFocusValue] = useState("")

    const onScanSubmit = () => {
        request({
            method: "post",
            url: `/wms/inbound/plan/query/${orderNo}/` + warehouseCode
        })
            .then((res: any) => {
                console.log("res", res)
                setOrderInfo(res.data.data)
                setFocusValue("sku")
            })
            .catch((error) => {
                console.log("error", error)
                message.error(error.message)
            })
    }

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
        }).then((res: any) => {
            console.log("confirm", res)
            if (res.status === 200) {
                onScanSubmit()
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

    const {t} = useTranslation();

    return (
        <>
            {orderInfo ? (
                <Row className="h-full" justify="space-between" gutter={16}>
                    <Col span={24}>
                        <OrderHandler value={orderInfo}/>
                    </Col>
                    <Col span={12} className="pt-4">
                        <SkuHandler
                            details={orderInfo.details}
                            currentSkuInfo={currentSkuInfo}
                            focusValue={focusValue}
                            onSkuChange={onSkuChange}
                        />
                    </Col>
                    <Col span={12} className="pt-4">
                        <ContainerHandler
                            value={orderInfo}
                            focusValue={focusValue}
                            onConfirm={onConfirm}
                            changeFocusValue={changeFocusValue}
                            onScanSubmit={onScanSubmit}
                            workStationEvent={workStationEvent}
                        />
                    </Col>
                </Row>
            ) : (
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
