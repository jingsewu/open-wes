import type { WorkStationConfig } from "@/pages/wms/station/instances/types"
import { DebugType } from "@/pages/wms/station/instances/types"
import { TabActionType } from "@/pages/wms/station/tab-actions/constant"
import { Translation } from "react-i18next"
import React from "react"
import RecieveTask from "./custom-actions/RecieveTask"
import CreateCountTask from "./custom-actions/CreateCountTask"
import InstanceLayout from "./layout"
import mockData from "./mock-events"
//todo 这里是不是不应该依赖replenish的文件
import DefaultArea from "@/pages/wms/station/instances/receive/operations/defaultPage"
import ShelfHandler from "./operations/ShelfHandler"
import StocktakeHandler from "./operations/StocktakeHandler"
import OrderHandler from "./operations/OrderHandler"
import { StationOperationType } from "./type"

export const OPERATION_MAP = {
    [StationOperationType.orderArea]: OrderHandler,
    [StationOperationType.shelfArea]: ShelfHandler,
    [StationOperationType.stocktakeArea]: StocktakeHandler,
    [StationOperationType.defaultArea]: DefaultArea
}

const config: WorkStationConfig<string> = {
    type: "stocktake",
    title: <Translation>{(t) => t("inventory.title")}</Translation>,
    stepsDescribe: [],
    actions: [RecieveTask, CreateCountTask, TabActionType.EXIT],
    operationMap: OPERATION_MAP,
    layout: InstanceLayout,
    debugType: DebugType.NONE,
    mockData
}

export default config
