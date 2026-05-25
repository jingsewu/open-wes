import React from "react"
import { Button, Col, Input, Row } from "antd"
import { useTranslation } from "react-i18next"

import type { OperationProps } from "@/pages/wms/station/instances/types"
import { WorkStationView } from "@/pages/wms/station/event-loop/types"
import { observer } from "@/pages/wms/station/state"

import ComponentWrapper from "../../component-wrapper"
import { OPERATION_MAP } from "./config"
import { StationOperationType } from "./types"
import { valueFilter as scanInfoFilter } from "./operations/tips"
import ContainerHandler from "./operations/containerHandler"
import SkuHandler from "./operations/skuHandler"
import OrderHandler from "./operations/orderHandler"
import { useReceiveWorkflow } from "./hooks"

interface ReplenishLayoutProps extends OperationProps<any, any> {
    workStationEvent: WorkStationView
}

const ReceiveLayout = observer((props: ReplenishLayoutProps) => {
    const { t } = useTranslation()
    const {
        orderNo, setOrderNo,
        orderInfo,
        currentSkuInfo,
        focusValue,
        setFocusValue,
        onScanSubmit,
        onSkuChange,
        onConfirm,
        store,
        onActionDispatch
    } = useReceiveWorkflow()

    if (!store?.workStationEvent) {
        return <div>{t("common.loading")}</div>
    }

    const hasOrder = store.workStationEvent?.hasOrder ?? ""

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

export default ReceiveLayout
