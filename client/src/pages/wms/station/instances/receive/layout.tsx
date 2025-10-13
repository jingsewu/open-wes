import React from "react"
import { Button, Col, Input, Row } from "antd"
import { useTranslation } from "react-i18next"

import type { OperationProps } from "@/pages/wms/station/instances/types"
import { WorkStationView } from "@/pages/wms/station/event-loop/types"
import { observer, useWorkStation } from "@/pages/wms/station/state"
import { MessageType } from "@/pages/wms/station/widgets/message"

import ComponentWrapper from "../../component-wrapper"
import { OPERATION_MAP } from "./config"
import { StationOperationType } from "./types"
import { WAREHOUSE_CODE } from "./constants"
import { valueFilter as scanInfoFilter } from "./operations/tips"
import ContainerHandler from "./operations/containerHandler"
import SkuHandler from "./operations/skuHandler"
import OrderHandler from "./operations/orderHandler"
import { useReceiveState } from "./hooks"
import { receiveApiService, createApiHandler } from "./services/api"

interface ReplenishLayoutProps extends OperationProps<any, any> {
    workStationEvent: WorkStationView<any>
}

const LayoutInner = (props: ReplenishLayoutProps) => {
    const state = useReceiveState()
    return <LayoutContent {...props} {...state} />
}

const LayoutContent = observer((props: ReplenishLayoutProps & ReturnType<typeof useReceiveState>) => {
    const {store, message, onActionDispatch} = useWorkStation()
    const {t} = useTranslation()

    // 早期返回处理
    if (!store?.workStationEvent) {
        return <div>{t("common.loading")}</div>
    }

    const workStationEvent = store.workStationEvent
    const {
        orderNo, setOrderNo,
        orderInfo, setOrderInfo,
        currentSkuInfo, setCurrentSkuInfo,
        focusValue, setFocusValue
    } = props
    const hasOrder = workStationEvent?.hasOrder ?? ""

    // API错误处理
    const handleApiError = (error: any) => {
        message?.({
            type: MessageType.ERROR,
            content: error.message
        })
    }

    const apiHandler = createApiHandler(handleApiError)

    const onScanSubmit = async () => {
        await apiHandler(async () => {
            const orderData = await receiveApiService.queryPlan(orderNo, WAREHOUSE_CODE!)
            setOrderInfo(orderData)
            setFocusValue("sku")
        })
    }

    const onSkuChange = (detail: any) => {
        setCurrentSkuInfo(detail)
        setFocusValue("container")
    }

    const onConfirm = async ({ containerCode, containerSpecCode, containerId, activeSlot, inputValue }: any) => {
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
                workStationId: workStationEvent.workStationId
            })
            
            if (res.status === 200) {
                if (hasOrder) onScanSubmit()
                setCurrentSkuInfo({})
                setFocusValue("sku")
            }
        })
    }

    // 渲染扫描订单界面
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

    // 渲染主工作界面
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

export default LayoutInner
