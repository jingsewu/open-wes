import React from "react"
import {Translation} from "react-i18next"

import type {WorkStationConfig} from "@/pages/wms/station/instances/types"
import {DebugType} from "@/pages/wms/station/instances/types"
import {TabActionType} from "@/pages/wms/station/tab-actions/constant"

import Tips from "./operations/tips"
import TaskDetail from "./custom-actions/TaskDetail"
import InstanceLayout from "./layout"
import mockData from "./mock-events"
import PickingHandler from "./operations/pickingHandler"
import RobotHandler from "./operations/RobotHandler"
import OrderHandler from "./operations/orderHandler"

import {StationOperationType} from "./type"
import CallContainer from "@/pages/wms/station/instances/receive/custom-actions/CallContainer";
import {OperationType} from "@/pages/wms/station/event-loop/types";
import Abnormal from "@/pages/wms/station/instances/receive/custom-actions/Abnormal";

export const OPERATION_MAP = {
    [StationOperationType.robotArea]: RobotHandler,
    [StationOperationType.selectDetailArea]: PickingHandler,
    [StationOperationType.orderArea]: OrderHandler,
    [StationOperationType.tips]: Tips
}

/**
 * 工作站物理设备类型
 */

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
    actions: (info, workStationEvent) => {
        if (workStationEvent?.operationType === OperationType.SELECT_CONTAINER_PUT_AWAY) {
            return [
                TaskDetail,
                Abnormal,
                CallContainer,
                TabActionType.EXIT,
            ];
        }
        return [TaskDetail, Abnormal, TabActionType.EXIT];
    },
    operationMap: OPERATION_MAP,
    layout: InstanceLayout,
    debugType: DebugType.STATIC,
    mockData
}

export default config
