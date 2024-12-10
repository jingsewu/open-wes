import React from "react"
import { Translation } from "react-i18next"

import type { WorkStationConfig } from "@/pages/wms/station/instances/types"
import { DebugType } from "@/pages/wms/station/instances/types"
import { TabActionType } from "@/pages/wms/station/tab-actions/constant"

import Tips from "./operations/tips"
import TaskDetail from "./custom-actions/TaskDetail"
import InstanceLayout from "./layout"
import mockData from "./mock-events"
import PickingHandler from "./operations/pickingHandler"
import RobotHandler from "./operations/RobotHandler"
import OrderHandler from "./operations/orderHandler"

import { StationOperationType } from "./type"

export const OPERATION_MAP = {
    [StationOperationType.robotArea]: RobotHandler,
    [StationOperationType.selectDetailArea]: PickingHandler,
    [StationOperationType.orderArea]: OrderHandler,
    [StationOperationType.tips]: Tips
}

/**
 * 工作站物理设备类型
 */
export const CurrentModeType: any = {
    SELECT_CONTAINER_PUT_AWAY: (
        <Translation>{(t) => t("replenish.title")}</Translation>
    ),
    RECOMMEND: "收货",
    NONE: ""
}
/**
 * 工作站物理设备类型
 */

const config: WorkStationConfig<string> = {
    type: "receive",
    title: "收货",
    stepsDescribe: [
        {
            type: "collectingDoc",
            name: "领单据"
        },
        {
            type: "callBin",
            name: "呼叫容器"
        },
        {
            type: "infoInput",
            name: "信息录入"
        }
    ],
    actions: [TaskDetail, TabActionType.EXIT],
    operationMap: OPERATION_MAP,
    layout: InstanceLayout,
    debugType: DebugType.STATIC,
    mockData
}

export default config
