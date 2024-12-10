/**
 * @file entry of this example.
 */
import * as React from "react"
import { render } from "react-dom"

import App from "./App"
import "./react-i18next-config"

export function bootstrap(mountTo: HTMLElement) {
    render(<App />, mountTo)
}

;(self as any).MonacoEnvironment = {}

bootstrap(document.getElementById("root")!)
