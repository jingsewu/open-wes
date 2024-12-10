import * as React from "react"
import { Redirect, RouteComponentProps, Switch } from "react-router-dom"
import { Layout, toast } from "amis"
import { IMainStore } from "@/stores"
import { inject, observer } from "mobx-react"
import request from "@/utils/requestInterceptor"
import RouterGuard from "@/routes/RouterGuard"
import TabsLayout from "./components/TabsLayout"
import type { MenuProps } from "antd"
import { languageList } from "@/pages/components/Language"
import LayoutAside from "@/components/LayoutAside"
import LayoutHeader from "@/components/LayoutHeader"

export interface NavChildren {
    path: string
    permissions?: string
    title?: string
    languageValueMappings?: any
}

export interface Navigations {
    children: NavChildren[]
    path: string
    permission: string
    languageValueMappings: any
}

export interface State {
    menus: Object
    selectedWarehouse: string
    selectedApp: string
    applications: MenuProps["items"]
    warehouses: []
    hasLoadMenu: boolean
    navigations: Navigations[]
    iframeShow: number
    iframeUrl: string
}

export interface AdminProps extends RouteComponentProps<any> {
    store: IMainStore
}

@inject("store")
@observer
export default class Admin extends React.Component<AdminProps, State> {
    state: State = {
        menus: Object,
        selectedWarehouse: "",
        selectedApp: "",
        applications: [],
        warehouses: [],
        hasLoadMenu: false,
        navigations: [],
        iframeShow: 0,
        iframeUrl: ""
    }

    onApplicationChange = (value: any) => {
        let menus: any = this.state.menus
        this.setState({
            selectedApp: value.key,
            navigations: [menus[value.key]] || [],
            iframeShow: menus[value.key].iframeShow,
            iframeUrl: menus[value.key].children?.[0]?.path
        })
    }

    onWarehouseChange = (value: any) => {
        this.props.store.warehouse.setWarehouseCode(value.value)
        this.setState({
            selectedWarehouse: value.value
        })
    }

    onLanguageChange = (e: any) => {
        let menuList: string[] = Object.keys(this.state.menus)
        const options = menuList.map((menu) => {
            // return { key: value, label: menus[value].title }
            return {
                key: menu,
                label: (this.state.menus as any)[menu].title
            }
        })
        if (this.state.iframeShow) {
            const postUrl = this.state.iframeUrl.split("#")[0]
            window?.frames[0]?.postMessage(e.locale, postUrl)
        }

        this.setState({
            ...this.state,
            applications: options || []
        })
    }

    logout = () => {
        const store = this.props.store
        store.user.logout()
        const history = this.props.history
        history.replace(`/login`)
    }

    componentDidMount() {
        const store = this.props.store
        const history = this.props.history
        if (!store.user.isAuthenticated) {
            toast["error"]("用户未登陆，请先登陆！", "消息")
            history.replace(`/login`)
        }
        this.refreshMenu()
    }

    componentDidUpdate() {
        this.refreshMenu()
    }

    refreshMenu = () => {
        const store = this.props.store
        let pathname = this.props.location.pathname
        if (
            pathname !== "login" &&
            pathname !== "/" &&
            !this.state.hasLoadMenu &&
            store.user.isAuthenticated
        ) {
            request({
                method: "get",
                headers: {
                    terminalType: "PC"
                },
                url: "/user/api/currentUser/getAuth"
            }).then((res: any) => {
                let menus = res.data.menus
                localStorage.setItem("permissions", res.data.permissions)
                let applications: string[] = Object.keys(menus)
                let selectedApp = applications[0]
                let navigations = [menus[selectedApp]] || []
                const options = applications.map((value) => {
                    // return { key: value, label: menus[value].title }
                    return {
                        key: value,
                        label: menus[value].title
                    }
                })

                this.setState({
                    menus: menus,
                    navigations: navigations,
                    selectedApp: selectedApp,
                    applications: options,
                    hasLoadMenu: true,
                    iframeShow: menus[selectedApp].iframeShow,
                    iframeUrl: menus[selectedApp].children?.[0]?.path
                })

                // 角色的仓库权限为空时，不初始化仓库
                if (res.data.warehouses) {
                    this.initWarehouseSelect(res.data.warehouses)
                }
            })
        }
    }

    private initWarehouseSelect(warehouses: Array<string>) {
        request({
            method: "post",
            url:
                "/search/search/searchSelectResult?perPage=1000&activePage=1&value-op=il&value=" +
                warehouses.join(","),
            data: {
                searchIdentity: "SearchWarehouseMainData",
                searchObject: {
                    tables: "m_warehouse_main_data"
                },
                showColumns: [
                    {
                        dbField: "warehouse_code",
                        name: "value",
                        javaType: "java.lang.String"
                    },
                    {
                        dbField: "warehouse_name",
                        name: "label",
                        javaType: "java.lang.String"
                    }
                ]
            }
        }).then((res: any) => {
            let selectedWarehouse = res.data.options[0]?.value
            this.setState({
                selectedWarehouse: selectedWarehouse,
                warehouses: res.data.options
            })
            this.props.store.warehouse.setWarehouseCode(selectedWarehouse)
        })
    }

    iframeMenuClick = (path: string, permissions: string, e: any) => {
        e.stopPropagation()
        this.setState({
            ...this.state,
            iframeUrl: path
        })
        const history = this.props.history
        history.replace({
            pathname: permissions
        })
    }

    onIframeTabChange = (path: string) => {
        this.setState({
            ...this.state,
            iframeUrl: path
        })
    }

    iFrameLoadTest = async () => {
        const locale = languageList.find(
            (item) => item.value === this.props.store.locale
        )?.locale
        const postUrl = this.state.iframeUrl.split("#")[0]
        await window?.frames[0].postMessage(locale, postUrl)
    }

    render() {
        const store = this.props.store
        let pathname = this.props.location.pathname
        if (pathname == "login" || pathname == "/") {
            return (
                <Switch>
                    <RouterGuard />
                    <Redirect to={`/404`} />
                </Switch>
            )
        } else {
            return (
                <Layout
                    aside={
                        <LayoutAside
                            navigations={this.state.navigations}
                            iframeShow={this.state.iframeShow}
                            iframeMenuClick={this.iframeMenuClick}
                        />
                    }
                    header={
                        <LayoutHeader
                            selectedApp={this.state.selectedApp}
                            applications={this.state.applications}
                            selectedWarehouse={this.state.selectedWarehouse}
                            warehouses={this.state.warehouses}
                            onApplicationChange={this.onApplicationChange}
                            onWarehouseChange={this.onWarehouseChange}
                            onLanguageChange={this.onLanguageChange}
                        />
                    }
                    folded={store.asideFolded}
                    offScreen={store.offScreen}
                >
                    <TabsLayout
                        selectedApp={this.state.selectedApp}
                        navigations={this.state.navigations}
                        iframeShow={this.state.iframeShow}
                        onIframeTabChange={this.onIframeTabChange}
                        {...this.props}
                    />
                    {this.state.iframeShow ? (
                        <iframe
                            id="microFrontIframe"
                            src={this.state.iframeUrl}
                            height="100%"
                            onLoad={this.iFrameLoadTest}
                            allowFullScreen={true}
                            scrolling="no"
                        />
                    ) : (
                        <Switch>
                            <RouterGuard />
                            <Redirect to={`/404`} />
                        </Switch>
                    )}
                </Layout>
            )
        }
    }
}
