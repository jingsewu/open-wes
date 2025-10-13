import React from "react"
import { Translation } from "react-i18next"

import type { WorkStationConfig } from "@/pages/wms/station/instances/types"
import { DebugType } from "@/pages/wms/station/instances/types"
import { TabActionType } from "@/pages/wms/station/tab-actions/constant"

import { StationOperationType } from "./types"
import InstanceLayout from "./layout"
import mockData from "./mock-events"
import Tips from "./operations/tips"
import TaskDetail from "./custom-actions/TaskDetail"
import SkuHandler from "./operations/skuHandler"
import ContainerHandler from "./operations/containerHandler"
import OrderHandler from "./operations/orderHandler"

export const OPERATION_MAP = {
    [StationOperationType.robotArea]: ContainerHandler,
    [StationOperationType.selectDetailArea]: SkuHandler,
    [StationOperationType.orderArea]: OrderHandler,
    [StationOperationType.tips]: Tips
}

const config: WorkStationConfig<string> = {
    type: "receive",
    title: <Translation>{(t) => t("receive.station.title")}</Translation>,
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
