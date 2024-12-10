import * as React from "react"
import { Button, Checkbox, Form, Input, Typography } from "antd"
import { toast } from "amis"
import { RouteComponentProps } from "react-router-dom"
import Message, { MessageType } from "@/pages/wms/station/widgets/message"

import { IMainStore } from "@/stores"
import { inject, observer } from "mobx-react"
import { withRouter } from "react-router"
import request from "@/utils/requestInterceptor"
import "@/scss/style.scss"
import { withTranslation, Translation } from "react-i18next"

const { Title, Text } = Typography

interface LoginProps extends RouteComponentProps<any> {
    store: IMainStore
}

@inject("store")
// @ts-ignore
@withRouter
@observer
class LoginForm extends React.Component<any> {
    state = {
        username: "",
        password: ""
    }

    handleFormSaved = () => {
        const history = this.props.history
        const store = this.props.store
        const { t } = this.props
        // 这里可以进行登陆密码验证
        request({
            method: "post",
            url: "/user/api/auth/signin",
            data: this.state,
            headers: {
                "content-type": "application/json"
            }
        }).then((res: any) => {
            if (res.data != null && res.status === 200) {
                store.user.login(this.state.username, res.data.token)
                Message({
                    type: MessageType.SUCCESS,
                    content: t("toast.loginSuccess")
                })
                // 跳转到dashboard页面
                history.replace(`/dashboard`)
            } else {
                // toast["error"]("登录失败", "消息")
            }
        })
    }

    handleChangeForPassword = (e: any) => {
        this.setState({
            password: e.target.value
        })
    }

    handlePassWordEnter = (e: any) => {
        if (e.keyCode === 13) {
            this.handleFormSaved()
        }
    }

    componentDidMount() {
        const store = this.props.store
        console.log("store.user.name", store.user.name)
        console.log("store.user.isAuthenticated", store.user.isAuthenticated)
    }

    handleChangeForUsername = (e: any) => {
        this.setState({
            username: e.target.value
        })
    }

    render() {
        const { t } = this.props
        return (
            <div className="text-center mb-2.5 bg-white p-10 shadow-lg">
                {/* <h2 className="text-lg font-semibold text-gray-900 leading-none mb-4">
                    Sign in
                </h2> */}
                <Title level={3} className="text-gray-900 leading-none pb-6">
                    {t("login.submitText")}
                </Title>

                <Form
                    name="basic"
                    layout="vertical"
                    // wrapperCol={{ span: 24 }}
                    // initialValues={{ remember: true }}
                    onFinish={this.handleFormSaved}
                    // onFinishFailed={onFinishFailed}
                    autoComplete="off"
                    // size="large"
                    requiredMark={false}
                >
                    <Form.Item
                        label={t("login.username")}
                        name="username"
                        rules={[
                            {
                                required: true,
                                message: "Please input your username!"
                            }
                        ]}
                    >
                        <Input
                            // placeholder={t("login.username")}
                            // style={{ borderRadius: 10 }}
                            value={this.state.username}
                            size="large"
                            onChange={this.handleChangeForUsername}
                        />
                    </Form.Item>

                    <Form.Item
                        label={t("login.password")}
                        name="password"
                        rules={[
                            {
                                required: true,
                                message: "Please input your password!"
                            },
                            { type: "string", min: 6 }
                        ]}
                    >
                        <Input.Password
                            // placeholder={t("login.password")}
                            // style={{ borderRadius: 10 }}
                            value={this.state.password}
                            size="large"
                            onChange={this.handleChangeForPassword}
                        />
                    </Form.Item>
                    <Form.Item
                        name="remember"
                        valuePropName="checked"
                        className="text-left"
                    >
                        <Checkbox>Remember me</Checkbox>
                    </Form.Item>
                    <Form.Item>
                        <Button
                            type="primary"
                            htmlType="submit"
                            size="large"
                            block
                            // shape="round"
                        >
                            {t("login.submitText")}
                            {/* <i className="fa-solid fa-arrow-right m-l-xxl"></i> */}
                        </Button>
                    </Form.Item>
                </Form>
                <Text type="secondary" className="pb-8">
                    Need an account? Please contact the administrator.
                </Text>
            </div>
        )
    }
}

export default withTranslation()(LoginForm)
