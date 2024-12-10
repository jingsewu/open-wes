import React, {lazy} from "react"
import {Route, Switch} from "react-router-dom"
import Index, {WORK_STATION_PATH_PREFIX} from "./WorkStationCard"
// import WorkStation from "./workStation"
const WorkStation = lazy(() => import("@/pages/wms/station"))

const WORK_STATION_ROUTES = [
    {
        /* 收货 */
        path: "/receiving",
        component: (props: any) => <WorkStation {...props} station="receiving"/>
    },
    // {
    //     /* 入库 */
    //     path: "/inbound",
    //     component: (props) => (
    //         <WorkStation {...props} station="ArtificialRobot" />
    //     )
    // },
    {
        /* 拣货 */
        path: "/outbound",
        component: (props: any) => <WorkStation {...props} station="outbound"/>
        // component: lazy(() => import("@/pages/wms/station/workStation"))
        // component: WorkStation
    },
    // {
    //     /* 拣货-斯凯奇 */
    //     path: "/outboundSKQ",
    //     component: (props) => <WorkStation {...props} station="outboundSKQ" />
    // },
    // {
    //     /* 选择容器出库 */
    //     path: "/choiceContainerOutbound",
    //     component: (props) => (
    //         <WorkStation {...props} station="choiceContainerOutbound" />
    //     )
    // },
    // {
    //     /* 一步式理库 */
    //     path: "/oneStepInventory",
    //     component: (props) => (
    //         <WorkStation {...props} station="oneStepInventoryManage" />
    //     )
    // },
    // {
    //     /* 两步式理库 */
    //     path: "/twoStepInventory",
    //     component: (props) => (
    //         <WorkStation {...props} station="twoStepInventoryManage" />
    //     )
    // },
    // {
    //     /* 盘点 */
    //     path: "/count",
    //     component: (props) => <WorkStation {...props} station="stocktake" />
    // },
    {
        /* 选择容器上架 */
        path: "/inventory",
        component: (props: any) => (
            <WorkStation {...props} station="replenish"/>
        )
    },
    {
        /* 盘点 */
        path: "/replenish",
        component: (props: any) => (
            <WorkStation {...props} station="inventory"/>
        )
    },
    // {
    //     /* 推荐容器上架 */
    //     path: "/select-replenish",
    //     component: (props) => <WorkStation {...props} station="replenish" />
    // },
    // {
    //     /* 空箱出库 */
    //     path: "/emptyContainerOutbound",
    //     component: (props) => <WorkStation {...props} station="FullDelivery" />
    // }
    {
        /* 卡片选择页面 */
        path: "/",
        component: lazy(() => import("@/pages/wms/station"))
    }
]

function WorkStationRouter() {
    return (
        <Switch>
            {WORK_STATION_ROUTES.map((route, index) => {
                return (
                    <Route
                        key={route.path}
                        path={`${WORK_STATION_PATH_PREFIX}${route.path}`}
                        exact
                        component={route.component}
                    />
                )
            })}
        </Switch>
    )
}

export default WorkStationRouter
