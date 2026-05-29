import * as React from "react"
import {Button, Checkbox, Form, Input, Typography} from "antd"
import {RouteComponentProps} from "react-router-dom"
import {UserOutlined, LockOutlined} from "@ant-design/icons"
import Message, {MessageType} from "@/pages/wms/station/widgets/message"

import {IMainStore} from "@/stores"
import {inject, observer} from "mobx-react"
import {withRouter} from "react-router"
import request from "@/utils/requestInterceptor"
import "@/scss/style.scss"
import {withTranslation} from "react-i18next"

const {Title, Text} = Typography

const FORM_LOGO_GRAD_ID = "login-form-logo-grad"

interface LoginProps extends RouteComponentProps<any> {
    store: IMainStore
}

@inject("store")
// @ts-ignore
@withRouter
@observer
class LoginForm extends React.Component<any> {
    handleFormSaved = (values: { username: string; password: string }) => {
        const history = this.props.history;
        const store = this.props.store;
        const {t} = this.props;

        request({
            method: "post",
            url: "/user/api/auth/signin",
            data: values,
            headers: {
                "content-type": "application/json",
            },
        }).then((res: any) => {
            if (res.data != null && res.status === 200 && res.data.token != undefined) {
                store.user.login(values.username, res.data.token);
                Message({
                    type: MessageType.SUCCESS,
                    content: t("toast.loginSuccess"),
                });
                history.replace(`/dashboard`);
            }
        });
    };

    render() {
        const {t} = this.props
        return (
            <div className="login-form-card">
                <div style={{textAlign: "center", marginBottom: 32}}>
                    <svg width="48" height="48" viewBox="0 0 34 34" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <defs>
                            <linearGradient id={FORM_LOGO_GRAD_ID} x1="0" y1="0" x2="1" y2="1">
                                <stop offset="0%" stopColor="#3b82f6" />
                                <stop offset="100%" stopColor="#1d4ed8" />
                            </linearGradient>
                        </defs>
                        <rect width="34" height="34" rx="9" fill={`url(#${FORM_LOGO_GRAD_ID})`} />
                        <text x="17" y="23.5" textAnchor="middle" fill="white" fontSize="17" fontWeight="900" fontFamily="Plus Jakarta Sans, Arial, sans-serif">W</text>
                    </svg>
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
                >
                    <Form.Item
                        label={<span style={{fontWeight: 500, color: "#334155"}}>{t("login.username")}</span>}
                        name="username"
                        rules={[
                            {
                                required: true,
                                message: "Please input your username!",
                            },
                        ]}
                    >
                        <Input
                            size="large"
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
                                message: "Please input your password!",
                            },
                            {type: "string", min: 6, message: "Password must be at least 6 characters long!"},
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
                            style={{
                                borderRadius: 8,
                                height: 44,
                                fontWeight: 600,
                                background: "#3b82f6",
                                borderColor: "#3b82f6",
                                boxShadow: "0 2px 8px rgba(59, 130, 246, 0.3)",
                            }}
                        >
                            {t("login.submitText")}
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
