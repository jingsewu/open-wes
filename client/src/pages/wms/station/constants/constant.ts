import request from "@/utils/requestInterceptor";

/** 统一按钮防抖时间 */
export const DEBOUNCE_TIME = 500
/** 工作站卡片页 页面PATH */
export const STATION_MENU_PATH = "/wms/workStation"

export const STATION_API_URL = "/station/api"

export const STATION_WEBSOCKET_URL = "/gw/station/websocket"

export function request_work_station_view() {
    return request({
        method: "get",
        url: "/station/api"
    })
}

export function request_work_station_event(payload: any) {
    return request({
        method: "put",
        url: STATION_API_URL + `?apiCode=${payload.eventCode}`,
        data: payload.data,
        headers: {"Content-Type": "text/plain"}
    })
}

export function request_work_station(warehouseCoe: string) {
    return request({
        method: "post",
        url:
            "/search/search?page=1&perPage=1000&warehouseCode-op=eq&warehouseCode=" + warehouseCoe,
        data: {
            searchIdentity: "WWorkStation",
            searchObject: {
                orderBy: "update_time desc"
            },
            showColumns: [
                {
                    name: "id",
                    label: "ID",
                    hidden: true
                },
                {
                    name: "version",
                    label: "Version",
                    hidden: true
                },
                {
                    name: "warehouseCode",
                    label: "table.warehouseCode",
                    hidden: true
                },
                {
                    name: "stationCode",
                    label: "table.workstationCoding",
                    searchable: true
                },
                {
                    name: "stationName",
                    label: "table.workstationName"
                }
            ]
        }
    })
}
