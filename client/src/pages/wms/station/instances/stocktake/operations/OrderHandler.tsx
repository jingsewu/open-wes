import { Typography } from "antd"
import React from "react"
import { WorkStationView } from "@/pages/wms/station/event-loop/types"
import type { OperationProps } from "@/pages/wms/station/instances/types"
import {useTranslation} from "react-i18next";

const { Title } = Typography

/**
 * @Description: 对event中的数据进行filter处理
 * @param data
 */
export const valueFilter = (data?: WorkStationView<any>) => {
    if (!data) return {}
    return {
        orderInfo: data.operationOrderArea?.currentStocktakeOrder
    }
}

const OrderHandler = (props: OperationProps<any, unknown>) => {
    const { value } = props
    const { orderInfo } = value
    const {t} = useTranslation();
    return (
        <>
            <Title level={3} className="pb-12">
                Order
            </Title>
            <div className="text-xl d-flex flex-col justify-center items-center">
                <div>
                    <span>{t("stocktake.station.orderArea.div.stocktakeOrderNo")}：</span>
                    <span>{orderInfo?.taskNo}</span>
                </div>
                <div>
                    <span>{t("stocktake.station.orderArea.div.stocktakeMethod")}：</span>
                    <span>{orderInfo?.stocktakeMethod}</span>
                </div>
                <div>
                    <span>{t("stocktake.station.orderArea.div.stocktakeType")}：</span>
                    <span>{orderInfo?.stocktakeType}</span>
                </div>
            </div>
        </>
    )
}

export default OrderHandler
