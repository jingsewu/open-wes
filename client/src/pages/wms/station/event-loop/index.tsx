import { DebugType } from "@/pages/wms/station/instances/types"
import request from "@/utils/requestInterceptor"
import { EventSourcePolyfill } from "event-source-polyfill"
import { toast } from "amis"
import type { APIResponse } from "../request"
import type {
    WorkStationEvent,
    WorkStationEventLoopConfig,
    WorkStationInfo
} from "./types"
import { CurrentOperationType, WorkStationStatus } from "./types"
import { abnormalVoiceTips } from "@/pages/wms/station/event-loop/utils"

type EventListener = (event: WorkStationEvent<any> | undefined) => void
type InfoListener = (info: WorkStationInfo) => void

export const DEFAULT_WORKSTATION_INFO: WorkStationInfo = {
    stationCode: "",
    stationStatus: WorkStationStatus.NO_TASK,
    executingTaskCodes: [],
    callRobotNum: 0,
    allContainerCodeList: [],
    inTransitContainerCodeList: [],
    devicePhysicalTypeList: [],
    currentOperationType: CurrentOperationType.NONE,
    extendsRunningInfo: {},
    runningStatusUUID: ""
}

export const OFFLINE_WORKSTATION_INFO: WorkStationInfo = {
    stationCode: "",
    stationStatus: WorkStationStatus.OFFLINE,
    executingTaskCodes: [],
    callRobotNum: 0,
    allContainerCodeList: [],
    inTransitContainerCodeList: [],
    devicePhysicalTypeList: [],
    currentOperationType: CurrentOperationType.NONE,
    extendsRunningInfo: {},
    runningStatusUUID: ""
}

export default class WorkStationEventLoop {
    /** 当前需要执行的事件 */
    private currentEvent: WorkStationEvent<any> | undefined
    /** 当前工作站信息 */
    private workStationInfo: WorkStationInfo | undefined
    /** 轮询id */
    private pollId: number | undefined
    /** 轮询时长 **/
    private readonly pollInterval: number = 5000
    /** 获取当前操作接口 */
    private readonly queryURL: string = ""
    /** 当前操作确认接口 */
    private readonly confirmURL: string = ""
    /** 发送事件接口 */
    private readonly sendEventURL: string = ""
    /** 获取当前工作站信息URL */
    private readonly getWorkStationInfoURL: string = ""
    /** 当前工作站编码 */
    private stationCode = ""
    /** 事件监听者 */
    private eventListener: EventListener | null = null
    /** 工作站信息监听者 */
    private infoListener: InfoListener | null = null
    /** 是否开启调试模式 */
    private debugType: DebugType | boolean = false
    /** mock数据 */
    // private mockData: any[] = []
    private mockData: any
    private eventSource: EventSource | null = null
    private websocket: WebSocket | null = null
    private stationId: string | null = null

    public constructor(config: WorkStationEventLoopConfig) {
        const {
            pollingInterval,
            queryURL,
            confirmURL,
            stationCode,
            sendEventURL,
            getWorkStationInfoURL
        } = config
        this.pollInterval = pollingInterval
        this.confirmURL = confirmURL
        this.queryURL = queryURL
        this.stationCode = stationCode
        this.sendEventURL = sendEventURL
        this.getWorkStationInfoURL = getWorkStationInfoURL
    }

    /**
     * 重置当前事件
     */
    public resetCurrentEvent() {
        this.currentEvent = undefined
    }

    /**
     * 重置当前事件
     */
    public resetCurrentInfo() {
        this.workStationInfo = undefined
    }

    /**
     * 获取当前事件
     */
    public getCurrentEvent() {
        return this.currentEvent
    }

    public setDebuggerConfig: (
        debugType: DebugType | boolean,
        mockData: any
    ) => void = async (debugType, mockData) => {
        this.mockData = mockData
        this.debugType = debugType
        if (
            debugType === DebugType.DYNAMIC &&
            process.env.NODE_ENV === "development"
        ) {
            await this.stop()
            await this.getMockEventData()
        }
    }
    /**
     * @Description: 设置工作站编码
     */
    public setStationCode: (code: string) => void = (code) => {
        this.stationCode = code
    }
    /**
     * @description 初始化listener
     */
    public initListener: (listenerMap: {
        eventListener: EventListener
        infoListener: InfoListener
    }) => void = (listenerMap) => {
        const { eventListener, infoListener } = listenerMap
        this.eventListener = eventListener
        this.infoListener = infoListener
    }

    /**
     * @description: 事件循环开始
     */
    public start: () => void = async () => {
        await this.getApiData()
        this.queryEvent()
    }

    /**
     * @description: 事件循环结束
     */
    public stop: () => void = async () => {
        console.log("%c =====> event loop stop", "color:red;font-size:20px;")
        // window.clearInterval(this.pollId)
        // this.eventSource?.close()
        this.websocket?.close()
        // const res = await request({
        //     method: "delete",
        //     url: "/station/sse/disconnect"
        // })
    }

    /**
     * @description: 操作确认
     */
    public actionConfirm: (payload: any) => Promise<any> = async (payload) => {
        // const res: any = await request.post(this.confirmURL, {
        //     operationId: this.currentEvent?.operationId,
        //     operationType: this.currentEvent?.operationType,
        //     ...payload
        // })
        const res: any = await request({
            method: "post",
            url: this.confirmURL,
            data: {
                // operationId: this.currentEvent?.operationId,
                operationType: this.currentEvent?.operationType,
                ...payload
            }
        })

        if (res?.errorCode) {
            abnormalVoiceTips().then()
            console.log(
                "%c =====> 切换操作错误",
                "color:red;font-size:20px;",
                res
            )
        }
        console.log(
            "%c =====> 切换操作payload",
            "color:red;font-size:20px;",
            payload
        )
        return res
    }
    /**
     * @description: 切换当前操作
     */
    public customActionDispatch: (payload: any) => Promise<any> = async (
        payload
    ) => {
        try {
            // const res: any = await request.post(this.sendEventURL, payload)
            const res: any = await request({
                method: "put",
                url: `/station/api?apiCode=${payload.eventCode}`,
                data: payload.data,
                headers: { "Content-Type": "text/plain" }
            })
            console.log(
                "%c =====> 切换操作payload",
                "color:red;font-size:20px;",
                payload,
                res
            )
            // if (res?.code !== "0") {
            //     abnormalVoiceTips().then()
            //     console.log(
            //         "%c =====> 切换操作错误",
            //         "color:red;font-size:20px;",
            //         res
            //     )
            // }
            return res
        } catch (error) {
            console.log(
                "%c =====> send event http error",
                "color:red;font-size:20px;",
                error
            )
            toast["error"](error.message)
            return {
                code: "-1",
                data: {},
                message: ""
            }
        }
    }

    /**
     * @description: 请求当前需要执行的事件
     */
    private queryEvent: () => Promise<void> = async () => {
        let data: WorkStationEvent<any> | undefined

        if (
            this.debugType === DebugType.DYNAMIC &&
            process.env.NODE_ENV === "development"
        ) {
            data = await this.getMockEventData()
        } else {
            // data = await this.getSSEMessageData()
            data = await this.getWebsocketData()
            // const workStationInfo = await this.getWorkStationInfo()
            // if (
            //     workStationInfo?.runningStatusUUID !==
            //     this.workStationInfo?.runningStatusUUID
            // ) {
            //     this.handleWorkStationInfoChange(workStationInfo)
            // }
        }
        // if (data && data.operationId !== this.currentEvent?.operationId) {
        //     this.handleEventChange(data)
        // }
        // this.handleEventChange(data)
    }

    /**
     * @description: 当前事件更新
     */
    private handleEventChange: (
        event: WorkStationEvent<any> | undefined
    ) => void = (event) => {
        this.currentEvent = event
        this.eventListener && this.eventListener(event)
        localStorage.setItem("sseInfo", JSON.stringify(event))
    }

    /**
     * @description: 获取当期工作站详情
     */
    private handleWorkStationInfoChange: (
        workStationInfo: WorkStationInfo
    ) => void = (workStationInfo) => {
        this.workStationInfo = workStationInfo
        this.infoListener && this.infoListener(workStationInfo)
    }
    /**
     * @description: 获取SSE接口数据
     */
    private getSSEMessageData: () => Promise<
        WorkStationEvent<any> | undefined
    > = async () => {
        let data
        let that = this
        const currentUrl = window.location.href
        const domain = new URL(currentUrl).hostname

        // this.eventSource = new EventSourcePolyfill("/gw/station/sse/connect", {
        //     headers: {
        //         Authorization: localStorage.getItem("ws_token") as string,
        //         "X-TenantID": domain.split(".")[0]
        //     }
        // })

        const hostName =
            process.env.NODE_ENV === "development" ? "connect.test.com" : domain

        this.eventSource = new EventSource(
            `/gw/station/sse/connect?Authorization=` +
                encodeURIComponent(localStorage.getItem("ws_token") as string)
        )

        this.eventSource.onopen = () => {
            console.log(`SSE 连接成功，状态${this.eventSource?.readyState}`)
        }
        // 监听消息事件
        this.eventSource.addEventListener("message", (event) => {
            if (!event.data) return
            data = JSON.parse(event.data)
            that.handleEventChange(data)
            // 服务端推送的数据
            console.log(data, "######")
        })
        this.eventSource.onerror = () => {
            console.log(`SSE 连接错误，状态${this.eventSource?.readyState}`)
        }
        return data
    }

    private getWebsocketData: () => Promise<WorkStationEvent<any> | undefined> =
        async () => {
            let data
            let that = this
            const currentUrl = window.location.href
            const domain = new URL(currentUrl).hostname

            const hostName =
                process.env.NODE_ENV === "development"
                    ? "connect.test.com"
                    : domain

            this.websocket = new WebSocket(
                `/station/websocket?stationCode=${that.stationId}&Authorization=` +
                    encodeURIComponent(
                        localStorage.getItem("ws_token") as string
                    )
            )

            this.websocket.onopen = () => {
                console.log(`websocket 连接成功，状态${this.websocket}`)
            }
            // 监听消息事件
            this.websocket.addEventListener("message", (event) => {
                console.log("websocket", event.data)
                if (!event.data) return
                if (event.data === "changed") {
                    that.getApiData()
                }

                // that.handleEventChange(data)
                // 服务端推送的数据
                console.log(event.data, "######")
            })
            this.websocket.onerror = () => {
                console.log(
                    `websocket 连接错误，状态${this.eventSource?.readyState}`
                )
            }
            return data
        }

    private getApiData: () => void = async () => {
        const res: any = await request({
            method: "get",
            url: "/station/api"
        })
        this.stationId = res.data.workStationId
        this.handleEventChange(res.data)
        // this.queryEvent()
    }

    /**
     * @description: 获取后台接口数据
     */
    private getBackEndEventData: () => Promise<
        WorkStationEvent<any> | undefined
    > = async () => {
        // const res = await request.post(this.queryURL, {})
        const res = await request({
            method: "post",
            url: this.queryURL,
            data: {}
        })
        const { code, data, message } = res as unknown as APIResponse<
            WorkStationEvent<any>
        >

        if (code !== "0") {
            console.log(
                "%c =====> 请求事件失败",
                "color:red;font-size:20px;",
                message
            )
            return
        }

        return data
    }
    /**
     * @description: 获取mock event数据
     */
    private getMockEventData: () => Promise<WorkStationEvent<any> | undefined> =
        async () => {
            console.log("getMockEventData", this.mockData)
            // if (!this.mockData.length) return
            // const topEvent = this.mockData.shift()
            const topEvent = this.mockData
            // this.mockData.push(cloneDeep(topEvent))
            this.handleEventChange(topEvent)
            return Promise.resolve(topEvent)
        }

    private getWorkStationInfo: () => Promise<WorkStationInfo> = async () => {
        // const res = await request.post(this.getWorkStationInfoURL, {})
        const res = await request({
            method: "post",
            url: this.getWorkStationInfoURL,
            data: {}
        })
        const { code, data, message } =
            res as unknown as APIResponse<WorkStationInfo>

        if (code === "B60004") {
            // 此状态码标识工作站已下线
            return OFFLINE_WORKSTATION_INFO
        }

        if (code !== "0") {
            console.log(
                "%c =====> 请求工作站信息失败",
                "color:red;font-size:20px;",
                message
            )
            return DEFAULT_WORKSTATION_INFO
        }

        return data
    }
}
