/**
 * @file entry of this example.
 */
import * as React from "react"
import { render } from "react-dom"
import { setLivelinessChecking } from "mobx-state-tree"

import App from "./App"
import "./react-i18next-config"

// AMIS (built on mobx-state-tree) sets liveliness checking to "error" in dev mode.
// Its internal RootStore.init() flow races with page unmount — navigating away while
// async renderers are still loading destroys the store, then init() writes `ready`
// to the dead node and throws a noisy "no longer part of a state tree" error in the
// console. Production already ignores this (amis-core sets "ignore" when NODE_ENV is
// production); align dev with a warning so the benign race doesn't throw.
setLivelinessChecking("warn")

export function bootstrap(mountTo: HTMLElement) {
    render(<App />, mountTo)
}

;(self as any).MonacoEnvironment = {}

bootstrap(document.getElementById("root")!)
