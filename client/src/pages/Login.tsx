import * as React from "react"
import {RouteComponentProps} from "react-router-dom"
import {IMainStore} from "@/stores"
import {inject, observer} from "mobx-react"
import {withRouter} from "react-router"
import "@/scss/style.scss"
import LoginForm from "./components/LoginForm"
import {withTranslation} from "react-i18next"
import Language from "./components/Language"

interface LoginProps extends RouteComponentProps<any> {
    store: IMainStore
    t: any
}

@inject("store")
// @ts-ignore
@withRouter
@observer
class LoginRoute extends React.Component<LoginProps, any> {
    render() {
        const {t} = this.props
        return (
            <div className="login-page-container d-flex justify-center">
                <div className="w-1/2">
                    <div
                        className="relative h-full d-flex flex-col justify-center items-center"
                    >
                        <div className="flex-1 d-flex flex-col justify-center max-w-6xl">
                            <LoginForm/>
                        </div>
                    </div>
                </div>
                <div
                    className="flex-1 bg-blue-50"
                >
                    <div
                        className="d-flex justify-between items-center relative"
                        style={{height: "100%"}}
                    >
                        <div
                            className="flex-1 h-full"
                        >
                            <div
                                className="flex justify-end"
                                style={{
                                    paddingTop: "72px",
                                    paddingLeft: "79px",
                                    paddingRight: "75px"
                                }}
                            >
                                <Language/>
                            </div>
                            <div className="login-image"></div>
                        </div>
                    </div>
                </div>
            </div>
        )
    }
}

export default withTranslation()(LoginRoute)
