import { Typography } from "antd"
import React from "react"
import { WorkStationEvent } from "@/pages/wms/station/event-loop/types"
import type { OperationProps } from "@/pages/wms/station/instances/types"

import Operation from "./components/Operation"

const { Title } = Typography

type StocktakeAreaProps = {
    scanCode?: string
    activeTask: any
}

/**
 * @Description: 对event中的数据进行filter处理
 * @param data
 */
export const valueFilter = (data?: WorkStationEvent<any>) => {
    if (!data) return {}
    return {
        activeTask: data.skuArea?.pickingViews?.[0],
        scanCode: data.scanCode
    }
}

const StocktakeAreaHandler = (
    props: OperationProps<StocktakeAreaProps, unknown>
) => {
    const { value } = props

    const { activeTask, scanCode } = value || {}

    return (
        <>
            <Title level={3} className="pb-6">
                SKU
            </Title>
            <Operation activeTask={activeTask} scanCode={scanCode} />
        </>
    )
}

export default StocktakeAreaHandler
