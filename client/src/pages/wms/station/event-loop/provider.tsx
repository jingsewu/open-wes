import { useHistory } from "react-router"

import { useWindowFocus } from "@/pages/wms/station/event-loop/hooks/use-window-focus"
import type {
    WorkStationAPIContextProps,
    WorkStationContextProps,
    WorkStationEvent,
    WorkStationInfo,
    WorkStationProviderProps
} from "@/pages/wms/station/event-loop/types"
import Message from "@/pages/wms/station/widgets/message"
import type { FC, ReactNode } from "react"
import React, { useEffect, useState } from "react"
import config from "./config"
import WorkStationEventLoop, { DEFAULT_WORKSTATION_INFO } from "./index"

const workStationEventLoop = new WorkStationEventLoop(config)

/**
 * @Description: 工作站当前事件Context
 * @usage: 获取当前事件
 */
const WorkStationContext = React.createContext<WorkStationContextProps>({
    workStationEvent: workStationEventLoop.getCurrentEvent(),
    workStationInfo: DEFAULT_WORKSTATION_INFO
})

/**
 * @Description: 工作站APIContext
 * @usage: 获取当前事件循环所需API
 */
const WorkStationAPIContext = React.createContext<WorkStationAPIContextProps>({
    message: Message,
    onConfirm: async () => {
        return {
            code: "0",
            msg: ""
        }
    },
    onCustomActionDispatch: async () => {
        return {
            code: "0",
            msg: ""
        }
    }
})

/**
 * @Description: 工作站Operation注册表 Context
 * @usage: 获取当前工作站注册的所有Operation实例
 */
const WorkStationOperationsContext = React.createContext<{
    operationsMap: Map<string, any>
    setOperationsMap: (operationsMap: Map<string, any>) => void
}>({
    operationsMap: new Map(),
    setOperationsMap: () => {}
})

function WorkStationValueProvider(props: WorkStationProviderProps) {
    const {
        children,
        stationCode,
        type,
        debugType = false,
        mockData = {}
    } = props
    const history = useHistory()

    const [workStationInfo, setWorkStationInfo] = useState<WorkStationInfo>(
        DEFAULT_WORKSTATION_INFO
    )
    const [workStationEvent, setWorkStationEvent] = useState<
        WorkStationEvent<any> | undefined
    >(workStationEventLoop.getCurrentEvent())
    /** 用于页面重新聚焦时，重新渲染页面 */
    useWindowFocus(setWorkStationEvent)

    useEffect(() => {
        workStationEventLoop.setStationCode(stationCode)
        workStationEventLoop.setDebuggerConfig(debugType, mockData as any[])
        workStationEventLoop.initListener({
            eventListener: (workStationEvent) => {
                setWorkStationEvent(workStationEvent)
            },
            infoListener: (workStationInfo) => {
                setWorkStationInfo(workStationInfo)
            }
        })
        if (type === "card" && !workStationEventLoop.getCurrentEvent()) {
            workStationEventLoop.start()
        }

        return () => {
            workStationEventLoop.resetCurrentEvent()
            workStationEventLoop.resetCurrentInfo()
        }
    }, [])

    useEffect(() => {
        if (history.location.pathname.includes("workStation")) return
        // workStationEventLoop.stop()
    }, [history.location.pathname])

    return (
        <WorkStationContext.Provider
            value={{
                workStationEvent,
                workStationInfo
            }}
        >
            {children}
        </WorkStationContext.Provider>
    )
}

const WorkStationAPIProvider: FC<ReactNode> = (props) => {
    const { children } = props

    return (
        <WorkStationAPIContext.Provider
            value={{
                message: Message,
                onConfirm: workStationEventLoop.actionConfirm,
                onCustomActionDispatch:
                    workStationEventLoop.customActionDispatch
            }}
        >
            {children}
        </WorkStationAPIContext.Provider>
    )
}

const WorkStationComponentsProvider: FC<ReactNode> = (props) => {
    const [operationsMap, setOperationsMap] = useState<Map<string, any>>(
        new Map()
    )
    const { children } = props

    return (
        <WorkStationOperationsContext.Provider
            value={{
                operationsMap,
                setOperationsMap
            }}
        >
            {children}
        </WorkStationOperationsContext.Provider>
    )
}

const WorkStationProvider: FC<WorkStationProviderProps> = (props) => {
    const { children } = props

    return (
        <WorkStationAPIProvider>
            <WorkStationComponentsProvider>
                <WorkStationValueProvider {...props}>
                    {children}
                </WorkStationValueProvider>
            </WorkStationComponentsProvider>
        </WorkStationAPIProvider>
    )
}

export {
    WorkStationProvider as Provider,
    WorkStationAPIContext as APIContext,
    WorkStationOperationsContext as OperationsContext,
    WorkStationContext
}
