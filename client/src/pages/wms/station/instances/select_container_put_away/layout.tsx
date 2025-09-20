import {Button, Col, Input, Row} from "antd"
import React, {useEffect, useState} from "react"
import request from "@/utils/requestInterceptor"

import {WorkStationView} from "@/pages/wms/station/event-loop/types"

import type {OperationProps} from "@/pages/wms/station/instances/types"

import ComponentWrapper from "../../component-wrapper"
import {OPERATION_MAP} from "./config"
import ContainerHandler from "../receive/operations/containerHandler"
import {valueFilter as scanInfoFilter} from "../receive/operations/tips"
import {StationOperationType} from "../receive/type"
import SkuHandler from "../receive/operations/skuHandler"
import OrderHandler from "../receive/operations/orderHandler"
import {useTranslation} from "react-i18next";
import {observer, useWorkStation} from "@/pages/wms/station/state";
import {MessageType} from "@/pages/wms/station/widgets/message";

let warehouseCode = localStorage.getItem("warehouseCode")

interface ReplenishLayoutProps extends OperationProps<any, any> {
    workStationEvent: WorkStationView<any>
}

const useContainerCode = (workStationEvent: WorkStationView<any> | null | undefined): string => {
    const [containerCode, setContainerCode] = useState<string>("")

    useEffect(() => {
        if (!workStationEvent?.workLocationArea?.workLocationViews) {
            setContainerCode("")
            return
        }

        const enabledLocation = workStationEvent.workLocationArea.workLocationViews.find(
            (location) => location.enable
        )

        const code = enabledLocation?.workLocationSlots?.[0]?.arrivedContainer?.containerCode ?? ""
        setContainerCode(code)
    }, [workStationEvent])

    return containerCode
}

const Layout = (props: ReplenishLayoutProps) => {
    const [orderNo, setOrderNo] = useState("")
    const [orderInfo, setOrderInfo] = useState<any>()
    const [currentSkuInfo, setCurrentSkuInfo] = useState<any>({})
    const [focusValue, setFocusValue] = useState("")

    const {store} = useWorkStation()
    const workStationEvent = store.workStationEvent

    const containerCode = useContainerCode(workStationEvent)

    return <LayoutContent {...props} orderNo={orderNo} setOrderNo={setOrderNo} orderInfo={orderInfo}
                          setOrderInfo={setOrderInfo} currentSkuInfo={currentSkuInfo}
                          setCurrentSkuInfo={setCurrentSkuInfo}
                          focusValue={focusValue} setFocusValue={setFocusValue}
                          containerCode={containerCode}/>
}

const LayoutContent = observer((props: ReplenishLayoutProps & {
    orderNo: string
    setOrderNo: (value: string) => void
    orderInfo: any
    setOrderInfo: (value: any) => void
    currentSkuInfo: any
    setCurrentSkuInfo: (value: any) => void
    focusValue: string
    setFocusValue: (value: string) => void
    containerCode: string
}) => {
    const {store, message, onActionDispatch} = useWorkStation()
    const {t} = useTranslation();
    const workStationEvent = store.workStationEvent
    if (!workStationEvent) {
        return <div>{t("common.loading")}</div>
    }
    const hasOrder = workStationEvent.hasOrder ?? ""
    const {
        orderNo,
        setOrderNo,
        orderInfo,
        setOrderInfo,
        currentSkuInfo,
        setCurrentSkuInfo,
        focusValue,
        setFocusValue,
        containerCode
    } = props

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
                message?.({
                    type: MessageType.ERROR,
                    content: error.message
                })
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
                inboundPlanOrderId: orderInfo?.id,
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
            {(!hasOrder || orderInfo) ? (
                <Row className="h-full" justify="space-between" gutter={16}>
                    {hasOrder && orderInfo && (
                        <Col span={24}>
                            <OrderHandler value={orderInfo}/>
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
                            changeFocusValue={changeFocusValue}
                            onScanSubmit={onScanSubmit}
                            containerCode={containerCode}
                            disable={true}
                            isContainerLeave={true}
                            hasOrder={hasOrder}
                            onActionDispatch={onActionDispatch}
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
})

export default Layout
