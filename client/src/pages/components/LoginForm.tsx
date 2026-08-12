import * as React from "react"
import {Button, Checkbox, Form, Input, Typography} from "antd"
import {UserOutlined, LockOutlined} from "@ant-design/icons"
import Message, {MessageType} from "@/pages/wms/station/widgets/message"

import {inject, observer} from "mobx-react"
import {withRouter} from "react-router"
import request from "@/utils/requestInterceptor"
import "@/scss/style.scss"
import {withTranslation} from "react-i18next"
import BrandLogo from "@/components/BrandLogo"

const {Title, Text} = Typography

// Remember-me stores only the username — never the password
const LOGIN_USERNAME_KEY = "login_username"
const FORM_LOGO_GRAD_ID = "openwes-login-form-logo-grad"

interface LoginFormState {
    loading: boolean
}

@inject("store")
// @ts-ignore
@withRouter
@observer
class LoginForm extends React.Component<any, LoginFormState> {
    state: LoginFormState = {loading: false}

    handleFormSaved = async (values: {
        username: string
        password: string
        remember?: boolean
    }) => {
        const {history, store, t} = this.props
        this.setState({loading: true})

        try {
            const res: any = await request({
                method: "post",
                url: "/user/api/auth/signin",
                data: {username: values.username, password: values.password},
                headers: {
                    "content-type": "application/json",
                },
                silentError: true,
            })

            if (res?.data?.token) {
                if (values.remember) {
                    localStorage.setItem(LOGIN_USERNAME_KEY, values.username)
                } else {
                    localStorage.removeItem(LOGIN_USERNAME_KEY)
                }
                store.user.login(values.username, res.data.token)
                Message({
                    type: MessageType.SUCCESS,
                    content: t("toast.loginSuccess"),
                })
                history.replace("/wms/dashboard")
            } else {
                Message({
                    type: MessageType.ERROR,
                    content: t("login.invalidCredentials"),
                })
            }
        } catch (err) {
            Message({
                type: MessageType.ERROR,
                content: t("login.invalidCredentials"),
            })
        } finally {
            this.setState({loading: false})
        }
    }

    render() {
        const {t} = this.props
        const savedUsername = localStorage.getItem(LOGIN_USERNAME_KEY) || ""

        return (
            <div className="login-form-card">
                <div style={{textAlign: "center", marginBottom: 32}}>
                    <BrandLogo size={48} gradientId={FORM_LOGO_GRAD_ID} />
                    <Title level={3} style={{
                        color: "#1e293b",
                        marginTop: 16,
                        marginBottom: 4,
                        fontWeight: 700,
                    }}>
                        {t("login.submitText")}
                    </Title>
                    <Text style={{color: "#64748b", fontSize: 14}}>
                        {t("login.subtitle")}
                    </Text>
                </div>

                <Form
                    name="basic"
                    layout="vertical"
                    onFinish={this.handleFormSaved}
                    autoComplete="off"
                    requiredMark={false}
                    initialValues={{username: savedUsername, remember: !!savedUsername}}
                >
                    <Form.Item
                        label={<span style={{fontWeight: 500, color: "#334155"}}>{t("login.username")}</span>}
                        name="username"
                        rules={[
                            {
                                required: true,
                                message: t("login.usernameRequired"),
                            },
                        ]}
                    >
                        <Input
                            size="large"
                            autoFocus
                            prefix={<UserOutlined style={{color: "#94a3b8"}} />}
                            placeholder={t("login.usernamePlaceholder")}
                            style={{borderRadius: 8, height: 44}}
                        />
                    </Form.Item>

                    <Form.Item
                        label={<span style={{fontWeight: 500, color: "#334155"}}>{t("login.password")}</span>}
                        name="password"
                        rules={[
                            {
                                required: true,
                                message: t("login.passwordRequired"),
                            },
                            {type: "string", min: 6, message: t("login.passwordMinLength")},
                        ]}
                    >
                        <Input.Password
                            size="large"
                            prefix={<LockOutlined style={{color: "#94a3b8"}} />}
                            placeholder={t("login.passwordPlaceholder")}
                            style={{borderRadius: 8, height: 44}}
                        />
                    </Form.Item>

                    <Form.Item name="remember" valuePropName="checked" className="text-left">
                        <Checkbox>{t("login.rememberMe")}</Checkbox>
                    </Form.Item>

                    <Form.Item>
                        <Button
                            type="primary"
                            htmlType="submit"
                            size="large"
                            block
                            loading={this.state.loading}
                            style={{
                                borderRadius: 8,
                                height: 44,
                                fontWeight: 600,
                                background: "#3b82f6",
                                borderColor: "#3b82f6",
                                boxShadow: "0 2px 8px rgba(59, 130, 246, 0.3)",
                            }}
                        >
                            {this.state.loading ? t("login.submitting") : t("login.submitText")}
                        </Button>
                    </Form.Item>
                </Form>
                <Text style={{color: "#94a3b8", fontSize: 13, display: "block", textAlign: "center"}}>
                    {t("login.contactAdmin")}
                </Text>
            </div>
        )
    }
}

export default withTranslation()(LoginForm)
