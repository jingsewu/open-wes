import React, { useState } from "react"

import { WorkStationEvent } from "@/pages/wms/station/event-loop/types"

import { Input, Divider, message } from "antd"
import SkuInfo from "@/pages/wms/station/widgets/common/SkuInfo"

export interface SKUHandlerConfirmProps {
    operationId: string
}

/**
 * @Description: 对event中的数据进行filter处理
 * @param data
 */
export const valueFilter = (data: WorkStationEvent<any>) => {
    if (!data) return {}
    return {
        operationType: data.operationType,
        orderArea: data.orderArea,
        skuArea: data.skuArea,
        workLocationArea: data.workLocationArea,
        warehouseCode: data.warehouseCode,
        scanedCode: data.scanCode,
        processingType: data.processingType,
        callContainerCount: data.callContainerCount,
        processingInboundOrderDetailId: data.processingInboundOrderDetailId
    }
}

const PickAreaHandler = (props: any) => {
    const { value = {}, onSkuChange } = props
    const { details } = value

    const [skuCode, setSkuCode] = useState<string>("")
    const [currentSkuInfo, setCurrentSkuInfo] = useState<any>({})
    // const { qtyAbnormal, qtyAccepted, qtyRestocked, skuCode, skuName } = value
    const onChange = (e: any) => {
        setSkuCode(e.target.value)
    }

    const onPressEnter = () => {
        const detail = details.find((item: any) => item.skuCode === skuCode)
        if (!detail) {
            setSkuCode("")
            message.warning("sku不属于当前订单，请重新扫码")
            return
        }
        setCurrentSkuInfo(detail)
        onSkuChange(detail)
    }
    return (
        <div className="bg-white p-4 h-full">
            <div className="d-flex items-center bg-white">
                <div className="white-space-nowrap">请扫描商品条码:</div>
                <Input
                    bordered={false}
                    value={skuCode}
                    onChange={onChange}
                    onPressEnter={onPressEnter}
                />
            </div>
            <Divider style={{ margin: "12px 0" }} />
            <div className="bg-gray-100 py-4 pl-6 d-flex">
                <div>
                    <div>收货数</div>
                    <div>/到货数</div>
                </div>
                <div className="border-solid border-gray-200 border-l border-r mx-4"></div>
                <div className="text-2xl">
                    {currentSkuInfo.qtyAccepted}/{currentSkuInfo.qtyRestocked}
                </div>
            </div>
            <div className="bg-gray-100 mt-4 p-3">
                <div>商品详情</div>
                <SkuInfo
                    // {...scannedSkuInfo}
                    imgWidth={160}
                    detailHeight={130}
                    skuAttributes={currentSkuInfo.batchAttributes}
                    skuName={currentSkuInfo.skuName}
                    barCode={currentSkuInfo.skuCode}
                />
            </div>
        </div>
    )
}

export default PickAreaHandler
