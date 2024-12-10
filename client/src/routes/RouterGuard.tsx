import * as React from "react"
import { Route } from "react-router-dom"
import path2components from "@/routes/path2Compoment"
import KeepAlive from "react-activation"
import { NotFound, Spinner } from "amis"
import { handleRouteData } from "@/pages/components/TabsLayout"

export default class RouterGuard extends React.Component<any, any> {
    componentDidMount() {
        this.refreshRoute()
    }

    state = {
        pathname: "",
        component: Spinner,
        routeWhen: false
    }

    componentDidUpdate(
        prevProps: Readonly<any>,
        prevState: Readonly<any>,
        snapshot?: any
    ) {
        this.refreshRoute()
        this.getRouteWhen()
    }

    refreshRoute = () => {
        const pathname = this.props.location.pathname
        if (this.state.pathname != pathname) {
            this.setState({ pathname: pathname })
            let path2ComponentItem = path2components.find((v) => {
                let path = v.path
                if (path != null && !path.startsWith("/")) {
                    path = "/" + path
                }
                return path === pathname
            })

            if (
                path2ComponentItem != null &&
                path2ComponentItem.component != null
            ) {
                this.setState({
                    component: path2ComponentItem.component
                })
            } else {
                this.setState({ component: NotFound })
            }
        }
    }

    getRouteWhen = () => {
        const { allowedData } = handleRouteData(path2components)
        const allowed = allowedData.find(
            (item) => item.path == this.props.location.pathname && item.cache
        )
        this.state.routeWhen = !!allowed
    }

    render() {
        return (
            <React.Suspense fallback={<p>loading</p>}>
                <Route
                    path={this.state.pathname}
                    exact
                    render={() => (
                        <KeepAlive
                            id={this.state.pathname}
                            name={this.state.pathname}
                            when={this.state.routeWhen}
                        >
                            {React.createElement(this.state.component)}
                        </KeepAlive>
                    )}
                    // render={() => React.createElement(this.state.component)}
                ></Route>
            </React.Suspense>
        )
    }
}
