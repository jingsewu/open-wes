import {useHistory} from "react-router"

import type {
    WorkStationAPIContextProps,
    WorkStationContextProps,
    WorkStationProviderProps,
    WorkStationView
} from "@/pages/wms/station/event-loop/types"
import Message from "@/pages/wms/station/widgets/message"
import type {FC, ReactNode} from "react"
import React, {useEffect, useState} from "react"
import WorkStationEventLoop from "./index"

const workStationEventLoop = new WorkStationEventLoop()

const WorkStationContext = React.createContext<WorkStationContextProps>({
    workStationEvent: workStationEventLoop.getCurrentEvent()
})

const WorkStationAPIContext = React.createContext<WorkStationAPIContextProps>({
    message: Message,
    onActionDispatch: async () => {
        return {
            code: "0",
            msg: ""
        }
    }
})

const WorkStationOperationsContext = React.createContext<{
    operationsMap: Map<string, any>
    setOperationsMap: (operationsMap: Map<string, any>) => void
}>({
    operationsMap: new Map(),
    setOperationsMap: () => {
    }
})

function WorkStationValueProvider(props: Readonly<WorkStationProviderProps>) {
    const {
        children,
        type,
        debugType = false,
        mockData = {}
    } = props
    const history = useHistory()

    const [workStationEvent, setWorkStationEvent] = useState<WorkStationView<any> | undefined>(workStationEventLoop.getCurrentEvent())

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

    return (
        <WorkStationContext.Provider
            value={{
                workStationEvent
            }}
        >
            {children}
        </WorkStationContext.Provider>
    )
}

const WorkStationAPIProvider: FC<ReactNode> = (props) => {
    const {children} = props

    return (
        <WorkStationAPIContext.Provider
            value={{
                message: Message,
                onActionDispatch:
                workStationEventLoop.actionDispatch
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
    const {children} = props

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
    const {children} = props

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
