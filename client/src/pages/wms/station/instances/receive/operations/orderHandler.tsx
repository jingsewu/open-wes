import React from "react"
import {Col, Row, Typography} from "antd"
import {useTranslation} from "react-i18next";

const { Title } = Typography
const OrderHandler = (props: any) => {
    const { value, onCustomActionDispatch } = props
    const { t } = useTranslation();
    const { customerOrderNo, storageType, skuKindNum, totalQty } = value
    return (
        <Row gutter={16} className="py-3 bg-white">
            <Col className="gutter-row" span={6}>
                <div className="flex-1 bg-gray-100 py-2 pl-10 rounded">
                    <div>{t("table.customerOrderNo")}</div>
                    <Title level={4}>{customerOrderNo}</Title>
                </div>
            </Col>
            <Col className="gutter-row" span={6}>
                <div className="flex-1 bg-gray-100 py-2 pl-10 rounded">
                    <div>{t("table.storageType")}</div>
                    <Title level={4}>{storageType}</Title>
                </div>
            </Col>
            <Col className="gutter-row" span={6}>
                <div className="flex-1 bg-gray-100 py-2 pl-10 rounded">
                    <div>{t("receive.station.orderArea.div.skuKindNumber")}</div>
                    <Title level={4}>{skuKindNum}</Title>
                </div>
            </Col>
            <Col className="gutter-row" span={6}>
                <div className="flex-1 bg-gray-100 py-2 pl-10 rounded">
                    <div>{t("receive.station.orderArea.div.totalQty")}</div>
                    <Title level={4}>{totalQty}</Title>
                </div>
            </Col>
        </Row>
    )
}

export default OrderHandler
