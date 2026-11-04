import { types, getEnv } from "mobx-state-tree"
import User from "./User"
import Warehouse from "@/stores/Warehouse"
import copy from "copy-to-clipboard"
import request from "@/utils/requestInterceptor"
import { toast, alert, confirm } from "amis"
import axios from "axios"
import React from "react"
import { Translation } from "react-i18next"

export type TableDensity = "standard" | "compact"

const TABLE_DENSITY_KEY = "owesTableDensity"

function readTableDensity(): TableDensity {
    return localStorage.getItem(TABLE_DENSITY_KEY) === "compact"
        ? "compact"
        : "standard"
}

function applyTableDensity(density: TableDensity) {
    document.documentElement.setAttribute("data-owes-density", density)
}

const MainStore = types
    .model("MainStore", {
        theme: "cxd",
        user: types.optional(User, {}),
        warehouse: types.optional(Warehouse, {}),
        asideFixed: true,
        asideFolded: false,
        offScreen: false,
        tableDensity: types.optional(
            types.enumeration<TableDensity>("TableDensity", [
                "standard",
                "compact"
            ]),
            readTableDensity()
        ),
        locale: localStorage.getItem("i18nextLng") || "zh-CN"
    })
    .views((self) => ({
        get fetcher() {
            return getEnv(self).fetcher
        },
        get notify() {
            return getEnv(self).notify
        },
        get alert() {
            return getEnv(self).alert
        },
        get copy() {
            return getEnv(self).copy
        }
    }))
    .actions((self) => {
        function toggleAsideFolded() {
            self.asideFolded = !self.asideFolded
            localStorage.setItem("asideFolded", self.asideFolded ? "1" : "")
        }

        function toggleAsideFixed() {
            self.asideFixed = !self.asideFixed
        }

        function toggleOffScreen() {
            self.offScreen = !self.offScreen
        }

        function toggleLanguage(e: any) {
            self.locale = e.value
        }

        function setTableDensity(density: TableDensity) {
            self.tableDensity = density
            localStorage.setItem(TABLE_DENSITY_KEY, density)
            applyTableDensity(density)
        }

        return {
            toggleAsideFolded,
            toggleAsideFixed,
            toggleOffScreen,
            toggleLanguage,
            setTableDensity,
            afterCreate: function () {
                self.asideFolded = !!localStorage.getItem("asideFolded")
                applyTableDensity(self.tableDensity)
            }
        }
    })

const store = ((window as any).store = MainStore.create(
    {},
    {
        fetcher: ({
            url,
            method,
            data,
            responseType,
            config,
            headers
        }: any) => {
            config = config || {}
            config.headers = config.headers || {}
            config.withCredentials = true
            responseType && (config.responseType = responseType)

            if (config.cancelExecutor) {
                config.cancelToken = new axios.CancelToken(
                    config.cancelExecutor
                )
            }

            config.headers = headers || {}
            config.method = method

            if (method === "get" && data) {
                config.params = data
            } else if (
                data &&
                typeof data !== "string" &&
                !(data instanceof Blob) &&
                !(data instanceof ArrayBuffer) &&
                !(data instanceof FormData)
            ) {
                data = JSON.stringify(data)
                config.headers["Content-Type"] = "application/json"
            }

            data && (config.data = data)
            config.url = url
            return request(config)
        },
        isCancel: (e: any) => axios.isCancel(e),
        notify: (type: "success" | "error" | "info", msg: string) => {
            toast[type]
                ? toast[type](msg, {
                      //   title:
                      //       type === "error" ? (
                      //           <Translation>
                      //               {(t) => t("toast.systemError")}
                      //           </Translation>
                      //       ) : (
                      //           // "系统错误"
                      //           <Translation>
                      //               {(t) => t("toast.systemMessage")}
                      //           </Translation>
                      //       ),
                      timeout: 5000
                  })
                : console.warn("[Notify]", type, msg)
            console.log("[notify]", type, msg)
        },
        alert,
        confirm,
        copy: (contents: string, options: any = {}) => {
            const ret = copy(contents, options)
            ret &&
                (!options || options.shutup !== true) &&
                toast.info("内容已拷贝到剪切板")
            return ret
        }
    }
))
export { MainStore }

export type IMainStore = typeof MainStore.Type

export default store
