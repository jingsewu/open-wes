import { useHistory } from "react-router"

import type {
    WorkStationAPIContextProps,
    WorkStationContextProps,
    WorkStationProviderProps,
    WorkStationView
} from "@/pages/wms/station/event-loop/types"
import Message from "@/pages/wms/station/widgets/message"
import type { FC, ReactNode } from "react"
import React, { useEffect, useState } from "react"
import WorkStationEventLoop from "./index"

const workStationEventLoop = new WorkStationEventLoop()

// 统一的WorkStation Context类型
interface UnifiedWorkStationContextProps
    extends WorkStationContextProps,
        WorkStationAPIContextProps {
    operationsMap: Map<string, any>
    setOperationsMap: (operationsMap: Map<string, any>) => void
}

// 创建统一的Context
const WorkStationContext = React.createContext<UnifiedWorkStationContextProps>({
    workStationEvent: workStationEventLoop.getCurrentEvent(),
    message: Message,
    onActionDispatch: async () => {
        return {
            code: "0",
            msg: ""
        }
    },
    operationsMap: new Map(),
    setOperationsMap: () => {}
})

// 统一的WorkStation Provider，合并了所有功能
const WorkStationProvider: FC<WorkStationProviderProps> = (props) => {
    const { children, type, debugType = false, mockData = {} } = props
    const history = useHistory()

    // 工作站事件状态
    const [workStationEvent, setWorkStationEvent] = useState<
        WorkStationView<any> | undefined
    >(workStationEventLoop.getCurrentEvent())

    // 操作映射状态
    const [operationsMap, setOperationsMap] = useState<Map<string, any>>(
        new Map()
    )

    useEffect(() => {
        workStationEventLoop.setDebuggerConfig(debugType, mockData as any[])
        workStationEventLoop.initListener({
            eventListener: (workStationEvent) => {
                setWorkStationEvent(workStationEvent)
            }
        })
        if (type === "card" && !workStationEventLoop.getCurrentEvent()) {
            workStationEventLoop.start()
        }

        return () => {
            workStationEventLoop.resetCurrentEvent()
        }
    }, [])

    useEffect(() => {
        if (history.location.pathname.includes("workStation")) return
    }, [history.location.pathname])

    // 统一的context值
    const contextValue: UnifiedWorkStationContextProps = {
        workStationEvent,
        message: Message,
        onActionDispatch: workStationEventLoop.actionDispatch,
        operationsMap,
        setOperationsMap
    }

    return (
        <WorkStationContext.Provider value={contextValue}>
            {children}
        </WorkStationContext.Provider>
    )
}

export {
    WorkStationProvider as Provider,
    WorkStationContext,
    // 导出统一Context类型
    type UnifiedWorkStationContextProps
}
