import { Typography } from "antd"
import React from "react"
import { WorkStationEvent } from "@/pages/wms/station/event-loop/types"
import type { OperationProps } from "@/pages/wms/station/instances/types"

const { Title } = Typography

/**
 * @Description: 对event中的数据进行filter处理
 * @param data
 */
export const valueFilter = (data?: WorkStationEvent<any>) => {
    if (!data) return {}
    return {
        orderInfo: data.operationOrderArea?.currentStocktakeOrder
    }
}

const OrderHandler = (props: OperationProps<any, unknown>) => {
    const { value } = props
    const { orderInfo } = value
    return (
        <>
            <Title level={3} className="pb-12">
                Order
            </Title>
            <div className="text-xl d-flex flex-col justify-center items-center">
                <div>
                    <span>盘点单号：</span>
                    <span>{orderInfo?.taskNo}</span>
                </div>
                <div>
                    <span>盘点方式：</span>
                    <span>{orderInfo?.stocktakeMethod}</span>
                </div>
                <div>
                    <span>盘点类型：</span>
                    <span>{orderInfo?.stocktakeType}</span>
                </div>
            </div>
        </>
    )
}

export default OrderHandler
