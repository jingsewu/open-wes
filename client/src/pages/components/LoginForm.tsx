import * as React from "react"
import {Button, Checkbox, Form, Input} from "antd"
import Message, {MessageType} from "@/pages/wms/station/widgets/message"

import {inject, observer} from "mobx-react"
import {withRouter} from "react-router"
import request from "@/utils/requestInterceptor"
// Global app chrome (header, sidebar, AMIS overrides) ships from the login route — keep the import.
import "@/scss/style.scss"
import "@/scss/login.scss"
import {withTranslation} from "react-i18next"
import Language from "./Language"

interface LoginFormValues {
    username: string
    password: string
    remember?: boolean
}

interface LoginFormState {
    submitting: boolean
}

/**
 * Sign-in console docked on the right edge of the login page. The panel is
 * the lit surface of the machine hall defined in Login.tsx.
 */
@inject("store")
// @ts-ignore
@withRouter
@observer
class LoginForm extends React.Component<any, LoginFormState> {
    state: LoginFormState = {
        submitting: false
    }

    handleFormSaved = (values: LoginFormValues) => {
        const {history, store, t} = this.props

        this.setState({submitting: true})

        request({
            method: "post",
            url: "/user/api/auth/signin",
            data: values,
            headers: {
                "content-type": "application/json",
            },
        }).then(
            (res: any) => {
                if (res.data != null && res.status === 200 && res.data.token != undefined) {
                    store.user.login(values.username, res.data.token)
                    Message({
                        type: MessageType.SUCCESS,
                        content: t("toast.loginSuccess"),
                    })
                    history.replace(`/dashboard`)
                    return
                }
                this.setState({submitting: false})
            },
            (error: unknown) => {
                console.error("Sign-in request failed:", error)
                this.setState({submitting: false})
            }
        )
    }

    render() {
        const {t} = this.props

        return (
            <section className="login-console">
                <div className="login-console__top">
                    <Language/>
                </div>

                <div className="login-console__body">
                    <h2 className="login-console__title">{t("login.title")}</h2>
                    <p className="login-console__sub">{t("login.subtitle")}</p>

                    <Form
                        name="login"
                        layout="vertical"
                        requiredMark={false}
                        onFinish={this.handleFormSaved}
                    >
                        <Form.Item
                            label={t("login.username")}
                            name="username"
                            rules={[{required: true, message: t("login.usernameRequired")}]}
                        >
                            <Input
                                size="large"
                                autoComplete="username"
                                placeholder={t("login.usernamePlaceholder")}
                            />
                        </Form.Item>

                        <Form.Item
                            label={t("login.password")}
                            name="password"
                            rules={[
                                {required: true, message: t("login.passwordRequired")},
                                {type: "string", min: 6, message: t("login.passwordMin")},
                            ]}
                        >
                            <Input.Password
                                size="large"
                                autoComplete="current-password"
                                placeholder={t("login.passwordPlaceholder")}
                            />
                        </Form.Item>

                        <Form.Item name="remember" valuePropName="checked">
                            <Checkbox>{t("login.rememberMe")}</Checkbox>
                        </Form.Item>

                        <Form.Item>
                            <Button
                                type="primary"
                                htmlType="submit"
                                size="large"
                                block
                                loading={this.state.submitting}
                            >
                                {t("login.submitText")}
                            </Button>
                        </Form.Item>
                    </Form>

                    <p className="login-console__note">{t("login.contactAdmin")}</p>
                </div>
            </section>
        )
    }
}

export default withTranslation()(LoginForm)
