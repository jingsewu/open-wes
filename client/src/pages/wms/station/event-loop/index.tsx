import {DebugType} from "@/pages/wms/station/instances/types"
import {toast} from "amis"
import type {WorkStationView} from "./types"
import {PrintData, qzPrinter} from "@/pages/wms/station/widgets/printer";
import {
    request_work_station_event,
    request_work_station_view,
    STATION_WEBSOCKET_URL
} from "@/pages/wms/station/constants/constant";

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
    private websocket: WebSocket | null = null
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
        this.websocket?.close()
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
            let that = this
            this.websocket = new WebSocket(STATION_WEBSOCKET_URL + `?stationCode=${that.stationId}&Authorization=`
                + encodeURIComponent(localStorage.getItem("ws_token") as string)
            )

            this.websocket.onopen = () => {
                console.log(`websocket connect successfully and the session id is: ${this.websocket?.url}`)
            }
            // 监听消息事件
            this.websocket.addEventListener("message", (event) => {
                if (!event.data) return
                console.log("websocket receive data: ", event.data)
                const message = JSON.parse(event.data);
                if (message.type === "DATA_CHANGED") {
                    that.getApiData()
                } else if (message.type === "PRINT") {
                    qzPrinter.printAndUpdateRecord(message as PrintData);
                }

            })
            this.websocket.onerror = () => {
                console.error(`websocket connect failed, the status is: ${this.websocket?.readyState}`)
                clearInterval(heartbeatInterval);
            }

            this.websocket.onclose = () => {
                console.log("WebSocket closed");
                clearInterval(heartbeatInterval);
            };

            const heartbeatInterval = setInterval(() => {
                if (this.websocket?.readyState === WebSocket.OPEN) {
                    this.websocket.send("ping");
                }
            }, 10000);

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
        const res: any = await request_work_station_view();
        this.stationId = res.data.workStationId
        this.handleEventChange(res.data)
    }

    private readonly getMockEventData: () => Promise<WorkStationView<any> | undefined> =
        async () => {
            console.log("getMockEventData", this.mockData)
            const topEvent = this.mockData
            this.handleEventChange(topEvent)
            return Promise.resolve(topEvent)
        }
}
