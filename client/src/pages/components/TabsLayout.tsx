import React, { useRef, useState, useEffect } from "react"
import { Tabs } from "antd"
import { useAliveController } from "react-activation"
import path2components from "@/routes/path2Compoment"
import type { Navigations } from "@/pages/index"

type TargetKey = React.MouseEvent | React.KeyboardEvent | string

type Iprops = {
    history: any
    location: { pathname: string }
    store: any
    selectedApp: string
    navigations: Navigations[]
    iframeShow: number
    onIframeTabChange: (param?: string) => void
}

type TabList = {
    cache?: boolean
    label: string
    key: string
    path?: string
    children?: string
    meta?: {
        closable?: boolean
        keepAlive?: boolean
    }
    closable?: boolean
    query?: string
    languageValueMappings: any
}

interface AllTab {
    [param: string]: TabList[]
}

// 路由处理，将数据处理成列表，提供给tab页签
export const handleRouteData = (routes: any) => {
    const [tabList, allowedData]: [TabList[], TabList[]] = [[], []]
    const formatData = (data: any) => {
        data.forEach((item: any) => {
            const meta = item.meta
            if (meta) {
                // 展示在最左方，且不可关闭
                if (meta.keepAlive && meta.closable == false) {
                    tabList.push({
                        ...item,
                        key: item.path,
                        label: item.name
                    })
                }
                // 该路由标记需要页签
                if (meta.keepAlive)
                    allowedData.push({
                        ...item,
                        key: item.path,
                        label: item.name
                    })
            }
            // 该路由存在子路由
            if (item.routes) formatData(item.routes)
        })
    }
    formatData(routes)
    return { tabList, allowedData }
}

const handleIframeData = (routes: any, locale: any) => {
    const routesData: any = []
    const formatData = (data: any) => {
        data.forEach((item: any) => {
            routesData.push({
                key: item.permissions,
                path: item.path,
                label: item.languageValueMappings?.[locale],
                languageValueMappings: item.languageValueMappings
            })
            if (item.children.length) formatData(item.children)
        })
    }
    formatData(routes.children)
    return routesData
}

const TabsLayout = (props: Iprops) => {
    const {
        history,
        location,
        store,
        selectedApp,
        navigations,
        iframeShow,
        onIframeTabChange
    } = props
    const { dropScope, refresh } = useAliveController() // 清除缓存函数
    const [first, setFirst] = useState(true) // 是否首次加载
    const [activeKey, setActiveKey] = useState<string>("") // 当前高亮的路由tab
    const [tabList, setTabList] = useState<TabList[]>([]) // 已经选择过展示的路由tab
    const [allowedData, setAllowedData] = useState<TabList[]>([]) // routes上有keepAlive标记的路由
    const [allTab, setAllTab] = useState<AllTab>({}) // 所以app选择过的展示的路由tab
    const newTabIndex = useRef(0)
    const routeKey = location.pathname.includes("/wms/workStation")
        ? "/wms/workStation"
        : location.pathname

    // 获取当前高亮的tab
    const getTab = (currentPath: string) =>
        tabList.find((item) => item?.key == currentPath)

    // 获取允许显示的tab合集
    const getAllowed = (currentPath: string) =>
        allowedData.find((item) => item.key == currentPath)

    const onRouteChange = (
        newTabList: TabList[],
        newAllowedData: TabList[]
    ) => {
        const tab = getTab(routeKey)
        const allowed = getAllowed(routeKey)
        const isNewTab = !tab && !!allowed
        const arr = isNewTab ? [...newTabList, allowed] : newTabList
        setTabList(arr)
        setAllTab({ ...allTab, [selectedApp]: arr })
        setAllowedData(newAllowedData)
        setActiveKey(routeKey)
    }

    const onIframeRouteChange = (iframeData: any) => {
        const tab = iframeData.find((item: any) => item.key === routeKey)
        const isNewTab = !tabList.find((item: any) => item.key === routeKey)
        const arr = isNewTab ? [...tabList, tab] : tabList
        setTabList(arr)
        setAllTab({ ...allTab, [selectedApp]: arr })
        setActiveKey(routeKey)
    }

    useEffect(() => {
        if (iframeShow) {
            const languageValueMappings =
                navigations[0].children[0].languageValueMappings[store.locale]
            setTabList([
                {
                    key: navigations[0].children[0].permissions || "",
                    label: languageValueMappings[store.locale] || "",
                    path: navigations[0].children[0].path,
                    languageValueMappings: languageValueMappings || {}
                }
            ])
            return
        }
        const { tabList: newTabList, allowedData: newAllowedData } =
            handleRouteData(path2components)
        onRouteChange(newTabList, newAllowedData)
        setFirst(false) // 设置是否首次加载为false
    }, [])

    const allowed = getAllowed(routeKey) // 需要缓存路由的数据
    const routeWhen = !!allowed // 当前路由是否需要缓存、激活页签

    useEffect(() => {
        if (first) return
        if (activeKey !== routeKey) {
            if (iframeShow) {
                const iframeData = handleIframeData(
                    navigations[0],
                    store.locale
                )
                onIframeRouteChange(iframeData)
                return
            }

            onRouteChange(tabList, allowedData)
        }
    }, [routeKey])

    useEffect(() => {
        if (selectedApp) {
            iframeShow ? handleIframAppSelected() : handleAppChange()
        }
    }, [selectedApp])

    useEffect(() => {
        if (iframeShow) {
            tabListLocalChange(tabList)
        }
    }, [store.locale])

    const tabListLocalChange = (list: TabList[]) => {
        const newTabList = list.map((item) => ({
            ...item,
            label: item.languageValueMappings?.[store.locale]
        }))
        setTabList(newTabList)
    }

    const handleAppChange = () => {
        const currentTabList = allTab[selectedApp] || []
        if (currentTabList.length > 0) {
            setTabList(currentTabList)
            setActiveKey(currentTabList[0]?.key || "")
            history.replace({
                pathname: currentTabList[0]?.path,
                query: currentTabList[0]?.query
            })
        } else {
            const defaultPath = navigations[0]?.children?.[0]?.path
            const currentTab = getAllowed(defaultPath as string) as TabList
            setTabList([
                {
                    ...currentTab,
                    closable: false
                }
            ])
            setActiveKey(currentTab?.key)
            history.replace({
                pathname: currentTab?.path,
                query: currentTab?.query
            })
        }
    }

    const handleIframAppSelected = () => {
        const currentTabList = allTab[selectedApp] || []
        if (currentTabList.length > 0) {
            tabListLocalChange(currentTabList)
            setActiveKey(currentTabList[0]?.key || "")
            history.replace({
                pathname: currentTabList[0]?.key
            })
        } else {
            const languageValueMappings =
                navigations[0].children[0].languageValueMappings
            setTabList([
                {
                    key: navigations[0].children[0].permissions || "",
                    label: languageValueMappings[store.locale] || "",
                    path: navigations[0].children[0].path,
                    languageValueMappings,
                    closable: false
                }
            ])
            setActiveKey(navigations[0].children[0].permissions || "")
            history.replace({
                pathname: navigations[0].children[0].permissions
            })
        }
    }

    const onChange = (newActiveKey: string) => {
        if (newActiveKey === routeKey) return
        const tab = getTab(newActiveKey) as TabList
        setActiveKey(newActiveKey)
        if (iframeShow) {
            history.replace({
                pathname: tab.key
            })
            onIframeTabChange(tab.path)
            return
        }
        history.replace({
            pathname: tab.path,
            query: tab.query
        })
    }

    const remove = (targetKey: string) => {
        const lastIndex = tabList.findIndex((item) => item.key === targetKey) // 当前删除标签的序号
        if (lastIndex > -1) {
            const newTabList = tabList.filter((item) => item.key !== targetKey)
            setTabList(newTabList)
            setAllTab({ ...allTab, [selectedApp]: newTabList })
            const isActive = activeKey === targetKey // 当前被删标签是否被激活
            if (isActive) {
                const beforeIndex = lastIndex - 1 < 0 ? 0 : lastIndex - 1 // 取的被删页签的前一条数据索引
                const tab = newTabList[beforeIndex] // 获取被删标签前一项的数据
                if (tab) {
                    onChange(tab.key) // 改变激活key
                }
            }
        }
        dropScopePage(targetKey) // 清除缓存
    }

    /**
     * 根据key清除页签的页面缓存
     * @param {String} value keys的字符串，传递多个可用逗号分割
     */
    const dropScopePage = (value: string) =>
        value.split(",").forEach((item) => dropScope(item))

    const onEdit = (targetKey: string, action: "add" | "remove") => {
        if (action == "remove") {
            remove(targetKey)
        }
    }

    return (
        <Tabs
            type="editable-card"
            size="small"
            tabBarGutter={0}
            hideAdd
            onChange={onChange}
            activeKey={activeKey}
            onEdit={onEdit}
            items={tabList}
        />
    )
}

export default TabsLayout
