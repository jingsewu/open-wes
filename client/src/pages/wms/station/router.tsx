import React, {lazy} from "react"
import {Route, Switch} from "react-router-dom"
import {WORK_STATION_PATH_PREFIX} from "./WorkStationCard"
const WorkStation = lazy(() => import("@/pages/wms/station"))

const WORK_STATION_ROUTES = [
    {
        /* 收货 */
        path: "/receiving",
        component: (props: any) => <WorkStation {...props} station="receiving"/>
    },
    {
        /* 拣货 */
        path: "/outbound",
        component: (props: any) => <WorkStation {...props} station="outbound"/>
    },
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
