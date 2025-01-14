import {DebugType} from "@/pages/wms/station/instances/types"
import request from "@/utils/requestInterceptor"
import {toast} from "amis"
import type {WorkStationEvent, WorkStationEventLoopConfig, WorkStationInfo} from "./types"
import {CurrentOperationType, WorkStationStatus} from "./types"
import {abnormalVoiceTips} from "@/pages/wms/station/event-loop/utils"

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
        const {eventListener, infoListener} = listenerMap
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
            const res: any = await request({
                method: "put",
                url: `/station/api?apiCode=${payload.eventCode}`,
                data: payload.data,
                headers: {"Content-Type": "text/plain"}
            })
            console.log(
                "%c =====> 切换操作payload",
                "color:red;font-size:20px;",
                payload,
                res
            )
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
            data = await this.getWebsocketData()
        }
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

    private getWebsocketData: () => Promise<WorkStationEvent<any> | undefined> =
        async () => {
            let data
            let that = this
            this.websocket = new WebSocket(
                `/gw/station/websocket?stationCode=${that.stationId}&Authorization=` +
                encodeURIComponent(
                    localStorage.getItem("ws_token") as string
                )
            )

            const heartbeatInterval = setInterval(() => {
                if (this.websocket?.readyState === WebSocket.OPEN) {
                    this.websocket.send("ping");
                }
            }, 10000);

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
                clearInterval(heartbeatInterval);
            }

            this.websocket.onclose = () => {
                clearInterval(heartbeatInterval);
                console.log("WebSocket closed");
                //todo need to reconnect?
            };
            return data
        }

    private getApiData: () => void = async () => {
        const res: any = await request({
            method: "get",
            url: "/station/api"
        })
        this.stationId = res.data.workStationId
        this.handleEventChange(res.data)
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
}
