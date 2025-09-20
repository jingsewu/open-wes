import * as React from "react"
import { render as renderSchema, replaceText } from "amis"
import { IMainStore } from "@/stores"
import { getEnv } from "mobx-state-tree"
import { inject, observer } from "mobx-react"
import { withRouter, RouteComponentProps } from "react-router"
import * as qs from "qs"
import { Action } from "amis/lib/types"
import * as cn from "../locales/zh-cn.json"
import * as en from "../locales/en-us.json"

interface RendererProps {
    schema?: any
    [propName: string]: any
}

const lang: Record<string, any> = {
    "zh-CN": cn,
    "en-US": en
}

@inject("store")
// @ts-ignore
@withRouter
@observer
export default class AMisRenderer extends React.Component<RendererProps, any> {
    env: any = null
    private rendererInstance: any = null
    private resizeObserverManager: any = null

    handleAction = (e: any, action: Action) => {
        this.env.alert(`没有识别的动作：${JSON.stringify(action)}`)
    }

    componentDidMount() {
        // 获取 ResizeObserver 管理器
        this.resizeObserverManager = (window as any).ResizeObserverManager
    }

    componentWillUnmount() {
        // 清理 AMis 渲染器实例
        if (this.rendererInstance) {
            try {
                // 清理 AMis 内部状态，特别是 table store
                if (this.rendererInstance.props && this.rendererInstance.props.store) {
                    // 清理 AMis 内部的 MST store 引用
                    this.rendererInstance.props.store = null
                }

                // 清理 AMis 组件树中的所有子组件
                if (this.rendererInstance.children) {
                    this.cleanupAmisChildren(this.rendererInstance.children)
                }

                // 如果 AMis 提供了销毁方法，调用它
                if (typeof this.rendererInstance.destroy === 'function') {
                    this.rendererInstance.destroy()
                }

                // 清理事件监听器
                if (this.rendererInstance.dispose) {
                    this.rendererInstance.dispose()
                }

                this.rendererInstance = null
            } catch (error) {
                console.warn('清理 AMis 渲染器时出错:', error)
            }
        }

        // 清理 ResizeObserver
        if (this.resizeObserverManager) {
            try {
                // 清理当前组件相关的所有 ResizeObserver
                const container = this.rendererInstance?.container || this.rendererInstance
                if (container && container.nodeType === Node.ELEMENT_NODE) {
                    this.resizeObserverManager.disconnectObserver(container)
                }

                // 清理所有 ResizeObserver 实例
                this.resizeObserverManager.disconnectAll()
            } catch (error) {
                console.warn('清理 ResizeObserver 时出错:', error)
            }
        }

        // 清理 env 中的引用
        this.env = null
    }

    // 递归清理 AMis 子组件
    private cleanupAmisChildren(children: any) {
        if (!children) return

        if (Array.isArray(children)) {
            children.forEach(child => this.cleanupAmisChildren(child))
        } else if (typeof children === 'object') {
            // 清理组件的 store 引用
            if (children.props && children.props.store) {
                children.props.store = null
            }

            // 清理组件的事件监听器
            if (children.componentWillUnmount) {
                try {
                    children.componentWillUnmount()
                } catch (error) {
                    console.warn('清理子组件时出错:', error)
                }
            }

            // 递归清理子组件的 children
            if (children.children) {
                this.cleanupAmisChildren(children.children)
            }
        }
    }

    constructor(props: RendererProps) {
        super(props)
        const store = props.store as IMainStore
        const fetcher = getEnv(store).fetcher
        const notify = getEnv(store).notify
        const alert = getEnv(store).alert
        const confirm = getEnv(store).confirm
        const copy = getEnv(store).copy
        const apiHost = getEnv(store).apiHost
        const getModalContainer = getEnv(store).getModalContainer
        const history = props.history

        const normalizeLink = (to: string) => {
            if (/^\/api\//.test(to)) {
                return to
            }
            to = to || ""
            const location = history.location
            if (to && to[0] === "#") {
                to = location.pathname + location.search + to
            } else if (to && to[0] === "?") {
                to = location.pathname + to
            }
            const idx = to.indexOf("?")
            const idx2 = to.indexOf("#")
            let pathname = ~idx
                ? to.substring(0, idx)
                : ~idx2
                ? to.substring(0, idx2)
                : to
            let search = ~idx ? to.substring(idx, ~idx2 ? idx2 : undefined) : ""
            let hash = ~idx2 ? to.substring(idx2) : ""
            if (!pathname) {
                pathname = location.pathname
            } else if (pathname[0] != "/" && !/^https?\:\/\//.test(pathname)) {
                let relativeBase = location.pathname
                const paths = relativeBase.split("/")
                paths.pop()
                let m
                while ((m = /^\.\.?\//.exec(pathname))) {
                    if (m[0] === "../") {
                        paths.pop()
                    }
                    pathname = pathname.substring(m[0].length)
                }
                pathname = paths.concat(pathname).join("/")
            }
            return pathname + search + hash
        }

        // todo，这个过程可以 cache
        this.env = {
            session: "global",
            updateLocation:
                props.updateLocation ||
                ((location: string, replace: boolean) => {
                    if (location === "goBack") {
                        return history.goBack()
                    }
                    history[replace ? "replace" : "push"](
                        normalizeLink(location)
                    )
                }),
            isCurrentUrl: (to: string) => {
                const link = normalizeLink(to)
                const location = history.location
                let pathname = link
                let search = ""
                const idx = link.indexOf("?")
                if (~idx) {
                    pathname = link.substring(0, idx)
                    search = link.substring(idx)
                }
                if (search) {
                    if (pathname !== location.pathname || !location.search) {
                        return false
                    }
                    const query = qs.parse(search.substring(1))
                    const currentQuery = qs.parse(location.search.substring(1))
                    return Object.keys(query).every(
                        (key) => query[key] === currentQuery[key]
                    )
                } else if (pathname === location.pathname) {
                    return true
                }
                return false
            },
            jumpTo:
                props.jumpTo ||
                ((to: string, action?: any) => {
                    if (to === "goBack") {
                        return history.goBack()
                    }
                    to = normalizeLink(to)
                    if (action && action.actionType === "url") {
                        action.blank === false
                            ? (window.location.href = to)
                            : window.open(to)
                        return
                    }
                    if (/^https?:\/\//.test(to)) {
                        window.location.replace(to)
                    } else {
                        history.push(to)
                    }
                }),
            fetcher,
            notify,
            alert,
            confirm,
            copy,
            apiHost,
            getModalContainer
        }
    }

    render() {
        const { schema, store, onAction, ...rest } = this.props
        this.rendererInstance = renderSchema(
            schema,
            {
                // onAction: onAction || this.handleAction,
                onAction: onAction,
                theme: store && store.theme,
                locale: store && store.locale,
                ...rest
            },
            { ...this.env, replaceText: lang[store.locale] || lang["zh-CN"] }
        )
        return this.rendererInstance
    }
}
