import * as React from "react"
import {Button, Checkbox, Form, Input, Typography} from "antd"
import {RouteComponentProps} from "react-router-dom"
import Message, {MessageType} from "@/pages/wms/station/widgets/message"

import {IMainStore} from "@/stores"
import {inject, observer} from "mobx-react"
import {withRouter} from "react-router"
import request from "@/utils/requestInterceptor"
import "@/scss/style.scss"
import {withTranslation} from "react-i18next"

const {Title, Text} = Typography

interface LoginProps extends RouteComponentProps<any> {
    store: IMainStore
}

@inject("store")
// @ts-ignore
@withRouter
@observer
class LoginForm extends React.Component<any> {
    state = {
        username: "admin",
        password: "123456"
    }

    handleFormSaved = (values: { username: string; password: string }) => {
        const history = this.props.history;
        const store = this.props.store;
        const {t} = this.props;

        request({
            method: "post",
            url: "/user/api/auth/signin",
            data: values, // Use form values directly
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
                // Navigate to the dashboard
                history.replace(`/dashboard`);
            } else {
                // toast["error"]("Login failed", "Message");
            }
        });
    };

    componentDidMount() {
        const store = this.props.store
        console.log("store.user.name", store.user.name)
        console.log("store.user.isAuthenticated", store.user.isAuthenticated)
    }

    render() {
        const {t} = this.props
        return (
            <div className="text-center mb-2.5 bg-white p-10 shadow-lg">
                <Title level={3} className="text-gray-900 leading-none pb-6">
                    {t("login.submitText")}
                </Title>

                <Form
                    name="basic"
                    layout="vertical"
                    onFinish={this.handleFormSaved}
                    autoComplete="off"
                    requiredMark={false}
                    initialValues={{
                        username: this.state.username,
                        password: this.state.password,
                        remember: true, // Default for the "Remember me" checkbox
                    }}
                >
                    <Form.Item
                        label={t("login.username")}
                        name="username"
                        rules={[
                            {
                                required: true,
                                message: "Please input your username!",
                            },
                        ]}
                    >
                        <Input size="large"/>
                    </Form.Item>

                    <Form.Item
                        label={t("login.password")}
                        name="password"
                        rules={[
                            {
                                required: true,
                                message: "Please input your password!",
                            },
                            {type: "string", min: 6, message: "Password must be at least 6 characters long!"},
                        ]}
                    >
                        <Input.Password size="large"/>
                    </Form.Item>

                    <Form.Item name="remember" valuePropName="checked" className="text-left">
                        <Checkbox>Remember me</Checkbox>
                    </Form.Item>

                    <Form.Item>
                        <Button type="primary" htmlType="submit" size="large" block>
                            {t("login.submitText")}
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
