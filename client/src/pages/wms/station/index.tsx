import "./state/mobxConfig"

import type { WorkStationConfig } from "@/pages/wms/station/instances/types"
import React, { useEffect, useState, useRef, useMemo } from "react"
import type { RouteComponentProps } from "react-router"
import { Spin } from "antd"
import { workStationStore } from "./state/WorkStationStore"
import WorkStationEventLoop from "./event-loop/index"
import Layout from "./layout"
import WorkStationCard from "./WorkStationCard"
import SelectStation from "./SelectStation"
import { request_work_station_view } from "@/pages/wms/station/constants/constant"

const workStationEventLoop = new WorkStationEventLoop()

const STATION_STATUS_NOT_CONFIGURED = "SAT010001"
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
    const [isConfigStationId, setIsConfigStationId] = useState(
        !!localStorage.getItem("stationId")
    )

    const workStationConfig = useMemo(
        () => WorkStationFactor[type] || {},
        [type]
    )

    const {
        actions,
        layout: InstanceLayout,
        stepsDescribe,
        title,
        debugType = false,
        mockData = {},
        extraTitleInfo
    } = workStationConfig

    useEffect(() => {
        let isMounted = true

        const getStationStatus = async () => {
            try {
                setIsLoadingStatus(true)
                const res: any = await request_work_station_view()

                if (!isMounted) return

                const isConfigured =
                    res?.data?.status !== STATION_STATUS_NOT_CONFIGURED
                setIsConfigStationId(isConfigured)

                if (res?.data) {
                    workStationStore.setWorkStationEvent(res.data)
                }
            } catch (error) {
                if (!isMounted) return

                console.error("获取工作站状态失败:", error)
                setIsConfigStationId(false)
            } finally {
                if (isMounted) {
                    setIsLoadingStatus(false)
                }
            }
        }

        getStationStatus()

        return () => {
            isMounted = false
        }
    }, [])

    useEffect(() => {
        if (isLoadingStatus || (isInitialized.current && !isConfigStationId))
            return
        isInitialized.current = true

        workStationEventLoop.setDebuggerConfig(debugType, mockData as any[])
        workStationEventLoop.initListener({
            eventListener: (workStationEvent) => {
                workStationStore.setWorkStationEvent(workStationEvent)
            }
        })

        if (
            type === WORK_STATION_TYPE_CARD &&
            !workStationEventLoop.getCurrentEvent()
        ) {
            workStationEventLoop.start()
        }

        return () => {
            const currentPath = window.location.pathname
            const isLeavingWorkStation =
                !currentPath.startsWith("/wms/workStation/")

            if (isLeavingWorkStation) {
                workStationEventLoop.stop()

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
        }
    }, [debugType, mockData, type, isConfigStationId, isLoadingStatus])

    if (isLoadingStatus) {
        return (
            <div className="w-full h-full d-flex justify-center items-center">
                <Spin size="large" tip="正在加载工作站信息..." />
            </div>
        )
    }

    if (!isConfigStationId) {
        return (
            <SelectStation
                isConfigStationId={isConfigStationId}
                setIsConfigStationId={setIsConfigStationId}
            />
        )
    }

    return type === WORK_STATION_TYPE_CARD ? (
        <WorkStationCard />
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
