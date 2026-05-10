import "./state/mobxConfig"

import type { WorkStationConfig } from "@/pages/wms/station/instances/types"
import React, { useEffect, useState, useRef, useMemo } from "react"
import type { RouteComponentProps } from "react-router"
import { Spin } from "antd"
import { workStationStore } from "./state/WorkStationStore"
import { workStationEventLoop } from "./event-loop/eventLoopInstance"
import Layout from "./layout"
import WorkStationCard from "./WorkStationCard"
import BindStation from "./BindStation"
import { request_work_station_view } from "@/pages/wms/station/constants/constant"
import { useStationSession } from "./state/hooks/useStationSession"

const WORK_STATION_TYPE_CARD = "card"

type WorkStationProps = RouteComponentProps & {
    code: string
    type: string
}

const WorkStationFactor: Record<string, WorkStationConfig<string>> = {}

const initWorkStationFactor = (): Record<string, WorkStationConfig<string>> => {
    const factor: Record<string, WorkStationConfig<string>> = {}

    try {
        // @ts-ignore
        const res = require.context("./instances", true, /config\.(ts|tsx)$/)
        res.keys().forEach((key: string) => {
            const { default: WorkStation } = res(key)
            if (WorkStation?.type) {
                factor[WorkStation.type] = WorkStation
            }
        })
    } catch (error) {
        console.error("初始化工作站配置失败:", error)
    }

    return factor
}

Object.assign(WorkStationFactor, initWorkStationFactor())

const WorkStation = (props: WorkStationProps) => {
    const { type } = props
    const isInitialized = useRef(false)
    const [isLoadingStatus, setIsLoadingStatus] = useState(true)

    // Station-session layer: "which workstation am I at?"
    // Owned entirely by useStationSession (localStorage-backed).
    // The server call below populates MobX store data but does NOT
    // override this value.
    const { isStationSelected, selectStation, clearStation } = useStationSession()

    const workStationConfig = useMemo(
        () => WorkStationFactor[type] || {},
        [type]
    )

    const {
        actions,
        layout: InstanceLayout,
        stepsDescribe,
        title,
        extraTitleInfo
    } = workStationConfig

    const debugType = workStationConfig.debugType ?? false
    const mockData = useMemo(
        () => workStationConfig.mockData ?? [],
        [workStationConfig]
    )

    useEffect(() => {
        let isMounted = true

        const loadInitialStationData = async () => {
            let res: any
            try {
                setIsLoadingStatus(true)
                res = await request_work_station_view()

                if (!isMounted) return
            } catch (error) {
                if (!isMounted) return
                console.error("获取工作站状态失败:", error)
            } finally {
                if (isMounted) {
                    // Set loading=false BEFORE updating the store so WorkStationCard
                    // renders once with workStationEvent=undefined (guard fires → no
                    // premature auto-navigation), then the store update triggers a
                    // second render driven by actual server state.
                    setIsLoadingStatus(false)
                }
            }

            // Update store AFTER loading=false so the guard in WorkStationCard fires
            // on the first render (workStationEvent=undefined), protecting against
            // stale or non-OFFLINE data causing immediate re-navigation.
            if (isMounted && res?.data) {
                workStationStore.setWorkStationEvent(res.data)
            }
        }

        loadInitialStationData()

        return () => {
            isMounted = false
        }
    }, [])

    useEffect(() => {
        if (isLoadingStatus || (isInitialized.current && !isStationSelected))
            return
        isInitialized.current = true

        workStationEventLoop.setDebuggerConfig(debugType, mockData as any[])
        workStationEventLoop.initListener({
            eventListener: (workStationEvent) => {
                workStationStore.setWorkStationEvent(workStationEvent)
            }
        })

        // start() is idempotent — reuses existing WebSocket, always refreshes data.
        if (type === WORK_STATION_TYPE_CARD) {
            workStationEventLoop.start()
        }

        return () => {
            const currentPath = window.location.pathname
            const isLeavingWorkStation =
                !currentPath.startsWith("/wms/workStation/")

            if (isLeavingWorkStation) {
                workStationEventLoop.pause()

                try {
                    const resizeObserverManager = (window as any)
                        .ResizeObserverManager
                    if (resizeObserverManager) {
                        resizeObserverManager.disconnectAll()
                    }
                } catch (error) {
                    console.warn("清理 ResizeObserver 时出错:", error)
                }
            }

            if (process.env.NODE_ENV === "development") {
                try {
                    const maxTimerId = setTimeout(() => {}, 0)
                    for (let i = maxTimerId; i > maxTimerId - 100; i--) {
                        clearTimeout(i)
                    }
                } catch (error) {
                    // ignore
                }
            }
        }
    }, [debugType, mockData, type, isStationSelected, isLoadingStatus])

    if (isLoadingStatus) {
        return (
            <div
                className="w-full h-full d-flex justify-center items-center"
                style={{ backgroundColor: "#fff" }}
            >
                <Spin size="large" tip="正在加载工作站信息..." />
            </div>
        )
    }

    if (!isStationSelected) {
        return <BindStation onStationSelected={selectStation} />
    }

    return type === WORK_STATION_TYPE_CARD ? (
        <WorkStationCard clearStation={clearStation} />
    ) : (
        <Layout
            extraTitleInfo={extraTitleInfo}
            actions={actions}
            title={title}
            stepsDescribe={stepsDescribe}
        >
            <InstanceLayout />
        </Layout>
    )
}

export default WorkStation
