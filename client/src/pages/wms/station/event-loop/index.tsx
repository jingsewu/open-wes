import {DebugType} from "@/pages/wms/station/instances/types"
import {toast} from "amis"
import type {WorkStationView} from "./types"
import {PrintData, qzPrinter} from "@/pages/wms/station/widgets/printer";
import {
    request_work_station_event,
    request_work_station_view,
    STATION_WEBSOCKET_URL
} from "@/pages/wms/station/constants/constant";
import WebSocketManager from "./websocketManager";

type EventListener = (event: WorkStationView<any> | undefined) => void

export default class WorkStationEventLoop {
    /** 当前需要执行的事件 */
    private currentEvent: WorkStationView<any> | undefined
    /** 事件监听者 */
    private eventListener: EventListener | null = null
    /** 是否开启调试模式 */
    private debugType: DebugType | boolean = false
    /** mock数据 */
    private mockData: any
    private websocketManager: WebSocketManager | null = null
    private stationId: string | null = null

    public resetCurrentEvent() {
        this.currentEvent = undefined
    }

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

    public initListener: (listenerMap: {
        eventListener: EventListener
    }) => void = (listenerMap) => {
        const {eventListener} = listenerMap
        this.eventListener = eventListener
    }

    public start: () => void = async () => {
        await this.getApiData()
        await this.initWebsocket()
    }

    public stop: () => Promise<void> = async () => {
        console.log("%c =====> event loop stop", "color:red;font-size:20px;")
        
        // 清理事件监听器
        this.eventListener = null
        
        // 断开 WebSocket 连接
        if (this.websocketManager) {
            this.websocketManager.disconnect()
            this.websocketManager = null
        }
        
        // 重置当前事件
        this.currentEvent = undefined
        
        return Promise.resolve()
    }

    public actionDispatch: (payload: any) => Promise<any> = async (
        payload
    ) => {
        try {
            const res: any = await request_work_station_event(payload);
            return res
        } catch (error) {
            console.log("send event http error: %c", "color:red;font-size:20px;", error)
            toast["error"](error.message)
            return {
                code: "-1",
                data: {},
                message: ""
            }
        }
    }

    private readonly initWebsocket: () => Promise<void> = async () => {
        if (
            this.debugType === DebugType.DYNAMIC &&
            process.env.NODE_ENV === "development"
        ) {
            await this.getMockEventData()
        } else {
            await this.getWebsocketData()
        }
    }

    private readonly handleEventChange: (
        event: WorkStationView<any> | undefined
    ) => void = (event) => {
        this.currentEvent = event
        this.eventListener && this.eventListener(event)
        localStorage.setItem("sseInfo", JSON.stringify(event))
    }

    private readonly getWebsocketData: () => Promise<void> =
        async () => {
            const wsUrl = STATION_WEBSOCKET_URL + `?stationCode=${this.stationId ?? localStorage.getItem("stationId")}&Authorization=`
                + encodeURIComponent(localStorage.getItem("ws_token") as string)

            this.websocketManager = new WebSocketManager({
                url: wsUrl,
                maxReconnectAttempts: 5,
                reconnectDelay: 1000,
                heartbeatInterval: 30000,
                onMessage: (message) => {
                    console.log("websocket receive data: ", message)
                    if (message.type === "DATA_CHANGED") {
                        this.getApiData()
                    } else if (message.type === "PRINT") {
                        qzPrinter.printAndUpdateRecord(message as PrintData);
                    }
                },
                onConnect: () => {
                    console.log(`websocket connect successfully and the session id is: ${wsUrl}`)
                },
                onDisconnect: () => {
                    console.log("WebSocket disconnected")
                },
                onError: (error) => {
                    console.error(`websocket connect failed:`, error)
                }
            })

            await this.websocketManager.connect()
            // Initialize QZ Tray
            await this.initPrinter();
        }

    private async initPrinter() {
        try {
            await qzPrinter.initialize();
        } catch (error) {
            console.error("Failed to initialize QZ Tray:", error);
        }
    }

    private readonly getApiData: () => Promise<void> = async () => {
        try {
            const res: any = await request_work_station_view();
            this.stationId = res.data.workStationId
            this.handleEventChange(res.data)
        } catch (error) {
            console.error('获取工作站数据失败:', error);
            toast.error('获取工作站数据失败，请检查网络连接');
        }
    }

    private readonly getMockEventData: () => Promise<WorkStationView<any> | undefined> =
        async () => {
            console.log("getMockEventData", this.mockData)
            const topEvent = this.mockData
            this.handleEventChange(topEvent)
            return Promise.resolve(topEvent)
        }
}
