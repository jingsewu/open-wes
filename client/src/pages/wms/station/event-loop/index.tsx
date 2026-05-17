import { DebugType } from "@/pages/wms/station/instances/types"
import { toast } from "amis"
import type { WorkStationView } from "./types"
import { PrintData, qzPrinter } from "@/pages/wms/station/widgets/printer"
import {
    request_work_station_event,
    request_work_station_view,
    STATION_WEBSOCKET_URL
} from "@/pages/wms/station/constants/constant"
import WebSocketManager from "./websocketManager"
import { workStationStore } from "../state/WorkStationStore"

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
            await this.destroy()
            await this.getMockEventData()
        }
    }

    public initListener: (listenerMap: {
        eventListener: EventListener
    }) => void = (listenerMap) => {
        const { eventListener } = listenerMap
        this.eventListener = eventListener
    }

    public start: () => void = async () => {
        // Always refresh data from the server on (re-)entry.
        await this.getApiData()

        // Only create a new WebSocket if one doesn't already exist.
        // An existing websocketManager is either connected or self-reconnecting.
        if (!this.websocketManager) {
            await this.initWebsocket()
        }
    }

    public destroy: () => Promise<void> = async () => {

        // Clear event listener
        this.eventListener = null

        // Disconnect WebSocket
        if (this.websocketManager) {
            this.websocketManager.disconnect()
            this.websocketManager = null
        }

        // Reset current event
        this.currentEvent = undefined

        // Reset MobX store so stale workStationStatus/workStationMode
        // don't trigger WorkStationCard's auto-navigation useEffect.
        workStationStore.reset()

        return Promise.resolve()
    }

    public pause: () => void = () => {

        // Detach React listener — component is unmounting.
        // WebSocket and store are intentionally preserved.
        this.eventListener = null
    }

    public actionDispatch: (payload: any) => Promise<any> = async (payload) => {
        try {
            const result = await request_work_station_event(payload)
            return result
        } catch (error) {
            console.error("send event http error:", error)
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
        // 更新当前事件
        this.currentEvent = event

        // 更新 MobX store
        workStationStore.setWorkStationEvent(event)

        // 通知事件监听者
        this.eventListener && this.eventListener(event)

        // 保存基本信息到本地存储
        if (event) {
            const essentialData = {
                stationCode: event.stationCode,
                workStationStatus: event.workStationStatus,
                chooseArea: event.chooseArea,
                workStationId: event.workStationId,
                timestamp: Date.now()
            }
            localStorage.setItem("sseInfo", JSON.stringify(essentialData))
        }
    }

    private readonly getWebsocketData: () => Promise<void> = async () => {

        const stationId = this.stationId ?? localStorage.getItem("stationId")

        if(!stationId){
            return;
        }

        const wsUrl =
            STATION_WEBSOCKET_URL +
            `?stationCode=${stationId}&Authorization=` +
            encodeURIComponent(localStorage.getItem("ws_token") as string)

        this.websocketManager = new WebSocketManager({
            url: wsUrl,
            maxReconnectAttempts: 5,
            reconnectDelay: 1000,
            heartbeatInterval: 30000,
            connectionTimeout: 15000,
            onMessage: (message) => {
                if (message.type === "DATA_CHANGED") {
                    this.getApiData()
                } else if (message.type === "PRINT") {
                    qzPrinter.printAndUpdateRecord(message as PrintData)
                }
            },
            onConnect: () => {
                console.log("WebSocket connected successfully")
            },
            onDisconnect: () => {
                console.log("WebSocket disconnected")
            },
            onError: (error) => {
                console.error("WebSocket connection failed:", error)
            },
            onTimeout: () => {
                toast.warning("WebSocket connection timed out, retrying...")
            }
        })

        await this.websocketManager.connect()
        // Initialize QZ Tray
        await this.initPrinter()
    }

    private async initPrinter() {
        try {
            await qzPrinter.initialize()
        } catch (error) {
            console.error("Failed to initialize QZ Tray:", error)
        }
    }

    private readonly getApiData: () => Promise<void> = async () => {
        try {
            const res: any = await request_work_station_view()

            if (res && res.data) {
                this.stationId = res.data.workStationId
                this.handleEventChange(res.data)
            } else {
                console.warn("Unexpected API response format:", res)
                this.handleEventChange(undefined)
            }
        } catch (error) {
            console.error("Failed to fetch workstation data:", error)
            toast.error("Failed to fetch workstation data, please check the network connection")
            this.handleEventChange(undefined)
        }
    }

    private readonly getMockEventData: () => Promise<
        WorkStationView<any> | undefined
    > = async () => {
        const topEvent = this.mockData

        if (topEvent) {
            this.handleEventChange(topEvent)
        } else {
            this.handleEventChange(undefined)
        }

        return Promise.resolve(topEvent)
    }
}
