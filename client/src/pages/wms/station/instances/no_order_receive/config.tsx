import React from "react"
import {Translation} from "react-i18next"

import type {WorkStationConfig} from "@/pages/wms/station/instances/types"
import {DebugType} from "@/pages/wms/station/instances/types"
import {TabActionType} from "@/pages/wms/station/tab-actions/constant"

import Tips from "../receive/operations/tips"
import TaskDetail from "../receive/custom-actions/TaskDetail"
import InstanceLayout from "./layout"
import mockData from "../receive/mock-events"
import SkuHandler from "../receive/operations/skuHandler"
import ContainerHandler from "../receive/operations/containerHandler"
import OrderHandler from "../receive/operations/orderHandler"

import {StationOperationType} from "../receive/type"

export const OPERATION_MAP = {
    [StationOperationType.robotArea]: ContainerHandler,
    [StationOperationType.selectDetailArea]: SkuHandler,
    [StationOperationType.orderArea]: OrderHandler,
    [StationOperationType.tips]: Tips
}

const config: WorkStationConfig<string> = {
    type: "no_order_receive",
    title: <Translation>{(t) => t("no_order_receive.title")}</Translation>,
    stepsDescribe: [
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
