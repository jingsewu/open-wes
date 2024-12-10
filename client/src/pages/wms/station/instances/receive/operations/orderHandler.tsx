import React from "react"
import type { WorkStationEvent } from "@/pages/wms/station/event-loop/types"
import type { OperationProps } from "@/pages/wms/station/instances/types"
import { Row, Col, Typography } from "antd"

const { Title } = Typography

interface ReceiveOrderProps {}

interface OrderHandlerProps {}

export const valueFilter = (
    data: WorkStationEvent<ReceiveOrderProps> | undefined
): OperationProps<OrderHandlerProps, any>["value"] | Record<string, any> => {
    if (!data) return {}
    return {
        orderArea: data.orderArea,
        operationType: data.operationType
    }
}

const OrderAreaData = [
    {
        label: "客户单号/LPN号",
        value: "customerOrderNo"
    },
    {
        label: "入库类型",
        value: "inboundType"
    },
    {
        label: "品项总数/已收/待收",
        value: ["total", "receivedQty", "toBeReceivedQty"]
    },
    {
        label: "入库总数/已收/待收",
        value: ["total", "receivedQty", "toBeReceivedQty"]
    }
]

const OrderHandler = (props: any) => {
    const { value, onCustomActionDispatch } = props
    const { customerOrderNo, storageType, skuKindNum, totalQty } = value
    return (
        <Row gutter={16} className="py-3 bg-white">
            <Col className="gutter-row" span={6}>
                <div className="flex-1 bg-gray-100 py-2 pl-10 rounded">
                    <div>客户单号/LPN号</div>
                    <Title level={4}>{customerOrderNo}</Title>
                </div>
            </Col>
            <Col className="gutter-row" span={6}>
                <div className="flex-1 bg-gray-100 py-2 pl-10 rounded">
                    <div>入库类型</div>
                    <Title level={4}>{storageType}</Title>
                </div>
            </Col>
            <Col className="gutter-row" span={6}>
                <div className="flex-1 bg-gray-100 py-2 pl-10 rounded">
                    <div>品项总数/已收/待收</div>
                    <Title level={4}>{skuKindNum}</Title>
                </div>
            </Col>
            <Col className="gutter-row" span={6}>
                <div className="flex-1 bg-gray-100 py-2 pl-10 rounded">
                    <div>入库总数/已收/待收</div>
                    <Title level={4}>{totalQty}</Title>
                </div>
            </Col>
        </Row>
    )
}

export default OrderHandler
