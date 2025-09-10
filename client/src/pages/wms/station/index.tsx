// 配置 MobX 4.x 兼容性
import './state/mobxConfig'

import type { WorkStationConfig } from "@/pages/wms/station/instances/types"
import React, { memo, useEffect, useState, useRef } from "react"
import type { RouteComponentProps } from "react-router"
import { useWorkStation } from "./state"
import { workStationStore } from "./state/WorkStationStore"
import WorkStationEventLoop from "./event-loop/index"
import Layout from "./layout"
import WorkStationCard from "./WorkStationCard"
import SelectStation from "./SelectStation"
import { request_work_station_view } from "@/pages/wms/station/constants/constant"

// 事件循环实例
const workStationEventLoop = new WorkStationEventLoop()

// 常量定义
const STATION_STATUS_NOT_CONFIGURED = "SAT010001"
const WORK_STATION_TYPE_CARD = "card"

type WorkStationProps = RouteComponentProps & {
    /** 工作站编码 */
    code: string
    /** 工作站类型 用于调用initStation接口 */
    type: string
}

// 工作站配置缓存
const WorkStationFactor: Record<string, WorkStationConfig<string>> = {}

/**
 * 根据目录结构自动注册工作站
 * @Attention: 此处动作依赖webpack.
 */
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

// 初始化工作站配置
Object.assign(WorkStationFactor, initWorkStationFactor())

const WorkStation = (props: WorkStationProps) => {
    const { code, type } = props
    const isInitialized = useRef(false)

    // 获取工作站配置
    const workStationConfig = WorkStationFactor[type] || {}
    const {
        actions,
        layout: InstanceLayout,
        stepsDescribe,
        title,
        debugType = false,
        mockData = {},
        extraTitleInfo
    } = workStationConfig

    // 工作站配置状态
    const [isConfigStationId, setIsConfigStationId] = useState(
        !!localStorage.getItem("stationId")
    )

    // 获取工作站状态
    const getStationStatus = async () => {
        try {
            const res: any = await request_work_station_view()
            console.log("request_work_station_view 返回值:", res)
            console.log("res.data:", res?.data)
            
            const isConfigured = res?.data?.status !== STATION_STATUS_NOT_CONFIGURED
            console.log("isConfigured:", isConfigured, "status:", res?.data?.status)
            setIsConfigStationId(isConfigured)
            
            // 将获取到的数据设置到 store 中
            if (res?.data) {
                console.log("设置 workStationEvent 到 store:", res.data)
                workStationStore.setWorkStationEvent(res.data)
            }
        } catch (error) {
            console.error("获取工作站状态失败:", error)
            setIsConfigStationId(false)
        }
    }

    // 初始化工作站状态
    useEffect(() => {
        getStationStatus()
    }, [])

    // 初始化事件循环
    useEffect(() => {
        if (isInitialized.current) return
        isInitialized.current = true

        // 配置事件循环
        workStationEventLoop.setDebuggerConfig(debugType, mockData as any[])
        workStationEventLoop.initListener({
            eventListener: (workStationEvent) => {
                workStationStore.setWorkStationEvent(workStationEvent)
            }
        })

        // 启动事件循环（仅卡片类型）
        if (type === WORK_STATION_TYPE_CARD && !workStationEventLoop.getCurrentEvent()) {
            workStationEventLoop.start()
        }

        return () => {
            workStationEventLoop.resetCurrentEvent()
        }
    }, [debugType, mockData, type])


    // 根据配置状态渲染不同内容
    if (!isConfigStationId) {
        return (
            <SelectStation
                isConfigSationId={isConfigStationId}
                setIsConfigStationId={setIsConfigStationId}
            />
        )
    }

    // 根据工作站类型渲染不同布局
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


export default memo(WorkStation)
