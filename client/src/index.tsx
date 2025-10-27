/**
 * @file entry of this example.
 */
import * as React from "react"
import { render } from "react-dom"

import App from "./App"
import "./react-i18next-config"

// 开发环境：忽略热更新导致的 MST 生命周期错误
if (process.env.NODE_ENV === "development") {
    const originalError = console.error
    console.error = (...args: any[]) => {
        const errorMsg = args[0]
        // 忽略 MST 热更新相关的错误
        if (
            typeof errorMsg === "string" &&
            errorMsg.includes("no longer part of a state tree")
        ) {
            return
        }
        originalError.apply(console, args)
    }
}

export function bootstrap(mountTo: HTMLElement) {
    console.log("mountTo", mountTo)
    render(<App />, mountTo)
}

;(self as any).MonacoEnvironment = {}

bootstrap(document.getElementById("root")!)
