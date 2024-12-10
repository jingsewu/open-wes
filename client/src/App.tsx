import * as React from "react"
import { Provider } from "mobx-react"

import RootRoute from "./routes"
import "./utils/polyfill"
import store from "./stores"
import { AliveScope } from "react-activation"

// css
import "@fortawesome/fontawesome-free/css/all.min.css"
import "bootstrap/dist/css/bootstrap.css"
import "amis/lib/themes/cxd.css"
import "amis/lib/helper.css"
import "amis/sdk/iconfont.css"
import "antd/dist/antd.css"

export default function (): JSX.Element {
    return (
        <Provider store={store}>
            <AliveScope>
                <RootRoute store={store} />
            </AliveScope>
        </Provider>
    )
}
