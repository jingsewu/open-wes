import type {
    WorkStationAPIContextProps,
    WorkStationContextProps
} from "@/pages/wms/station/event-loop/types"
import type { WorkStationConfig } from "@/pages/wms/station/instances/types"
import type { FC } from "react"
import React, { useContext, useState, useEffect, memo } from "react"
import type { RouteComponentProps } from "react-router"
import { APIContext, Provider, WorkStationContext } from "./event-loop/provider"
import Layout from "./layout"
import WorkStationCard from "./WorkStationCard"
import request from "@/utils/requestInterceptor"
import SelectStation from "./SelectStation"

type WorkStationProps = RouteComponentProps & {
    /** TODO: 此处应该修改为由hook获取 */
    code: string
    station: string
    /** 工作站类型 用于调用initStation接口，后期推动后台直接改用station字段 */
    type: string
}

const WorkStationFactor: Record<string, WorkStationConfig<string>> = {}

/**
 * @Description: 根据目录结构自动注册工作站
 * @Attention: 此处动作依赖webpack.
 */
const initWorkStationFactor = (): void => {
    // @ts-ignore
    const res = require.context("./instances", true, /config\.(ts|tsx)$/)
    res.keys().forEach((key: any) => {
        const { default: WorkStation } = res(key)
        WorkStationFactor[WorkStation.type] = WorkStation
    })
}
initWorkStationFactor()

const WorkStation = (props: WorkStationProps) => {
    const { code, type } = props
    const workStationConfig = WorkStationFactor[type] || {}

    const {
        actions,
        layout,
        stepsDescribe,
        title,
        debugType,
        mockData,
        extraTitleInfo
    } = workStationConfig

    const InstanceLayout = layout

    const [isConfigStationId, setIsConfigStationId] = useState(
        !!localStorage.getItem("stationId")
    )

    useEffect(() => {
        getStationId()
    }, [])

    const getStationId = async () => {
        const res: any = await request({
            method: "get",
            url: "/station/api"
        })
        if (res?.data?.status === "SAT010001") {
            setIsConfigStationId(false)
        } else {
            setIsConfigStationId(true)
        }
    }

    return isConfigStationId ? (
        <Provider
            stationCode={code}
            type={type}
            debugType={debugType}
            mockData={mockData}
        >
            {type === "card" ? (
                <InstanceLayoutWrapper>
                    <WorkStationCard />
                </InstanceLayoutWrapper>
            ) : (
                <Layout
                    extraTitleInfo={extraTitleInfo}
                    actions={actions}
                    title={title}
                    stepsDescribe={stepsDescribe}
                >
                    <InstanceLayoutWrapper>
                        <InstanceLayout />
                    </InstanceLayoutWrapper>
                </Layout>
            )}
        </Provider>
    ) : (
        <SelectStation
            isConfigSationId={isConfigStationId}
            setIsConfigStationId={setIsConfigStationId}
        />
    )
}

const InstanceLayoutWrapper: FC<any> = (props) => {
    const { children } = props
    const { workStationEvent, workStationInfo } =
        useContext<WorkStationContextProps>(WorkStationContext)
    const { onCustomActionDispatch, message } =
        useContext<WorkStationAPIContextProps>(APIContext)

    const childrenWithProps = React.Children.map(children, (child) => {
        if (React.isValidElement(child)) {
            return React.cloneElement(child, {
                // @ts-ignore
                workStationEvent,
                workStationInfo,
                onCustomActionDispatch,
                message
            })
        }
        return child
    })

    return <div style={{ height: "100%" }}>{childrenWithProps}</div>
}

export default memo(WorkStation)
