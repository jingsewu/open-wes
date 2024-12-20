import axios, {AxiosRequestConfig} from "axios"
import {makeTranslator, toast} from "amis"
import {attachmentAdpator} from "amis-core"
import {ApiObject} from "amis-core/lib/types"
import store from "@/stores"

/**
 * 全局请求拦截，方便对错误进行统一处理
 * @param config
 */
export default function request(config: AxiosRequestConfig) {
    let instance = axios.create()

    config.url = "/gw" + config.url
    config.headers = config.headers || {}
    config.headers["Authorization"] = <string>localStorage.getItem("ws_token")

    const currentUrl = window.location.href
    const domain = new URL(currentUrl).hostname
    // Split the domain by dots and get the first part (before the first dot)
    config.headers["X-TenantID"] = domain.split(".")[0]

    config.headers["X-WarehouseID"] = <string>localStorage.getItem("warehouseCode") || ""
    config.headers["locale"] = store.locale

    let stationId = <string>localStorage.getItem("stationId")
    if (!config.headers["StationCode"] && stationId) {
        config.headers["StationCode"] = stationId
    }

    const __ = makeTranslator("zh-CN")
    return new Promise((resolve, reject) => {
        let onSuccess = async (res: any) => {
            if (res.data === null) {
                console.log("reject data")
                reject(res)
            } else if (res.data === "") {
                console.warn("response body is empty, url: ", config.url)

                // 为空时，需要给 amis 传一个空的 data 对象，避免提示系统错误
                resolve({
                    ...res,
                    data: {}
                })
            } else if (res.status == 401) {
                // 未登陆
                console.warn("redirect url: ", res.data.redirectUrl)
                localStorage.setItem("Authorization", "")
                localStorage.removeIem("ws_token")
                window.location.href = res.data.redirectUrl
            } else if (res.status == 402) {
                // 无权限
                console.warn("not permission, url: ", config.url)
                toast.error("您无访问权限，请申请！", "消息")
                reject(res)
            } else if (res?.data?.status === "SAT010001") {
                resolve(res)
            } else {
                let response = await attachmentAdpator(
                    res,
                    __,
                    config as ApiObject
                )
                resolve(response)
            }
        }

        let onFail = (res: any) => {
            console.error("request failed, url: ", config.url)

            let response = res.response
            if (response?.status == 401) {
                // 未登陆
                console.warn("redirect url: ", response.data.redirectUrl)
                localStorage.setItem("Authorization", "")
                localStorage.removeItem("ws_token")
                window.location.href = response.data.redirectUrl || "/login"
            } else if (response?.status >= 400) {
                let msg =
                    response.data.description ||
                    response.data.error ||
                    response.data.msg ||
                    response.statusText
                toast.error(msg, "消息")
                reject({
                    ...res,
                    message: msg
                })
            } else if (axios.isCancel(res)) {
                console.info("request canceled, url: ", config.url)
            } else {
                toast.error(response?.data?.description, "消息")
                reject(res)
            }
        }
        return instance.request(config).then(onSuccess, onFail).catch(onFail)
    })
}
