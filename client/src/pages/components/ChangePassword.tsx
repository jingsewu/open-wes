import { Form, Input, Modal } from "antd"
import React from "react"
import request from "@/utils/requestInterceptor"
import Message, { MessageType } from "@/pages/wms/station/widgets/message"
import { useTranslation } from "react-i18next"

interface ChangePasswordFormProps {
    isModalOpen: boolean
    onModalCancel: () => void
}

const ChangePasswordForm: React.FC<ChangePasswordFormProps> = ({
    isModalOpen,
    onModalCancel
}) => {
    const { t } = useTranslation()
    const [form] = Form.useForm()
    const onFinish = async () => {
        try {
            const values = await form.validateFields()

            request({
                method: "post",
                url: "/user/api/currentUser/password",
                data: values
            })
                .then((res: any) => {
                    Message({
                        type: MessageType.SUCCESS,
                        content: res.data.msg
                    })
                    handleCancel()
                })
                .catch((error) => {
                    console.log("error", error)
                })
        } catch (errorInfo) {
            console.log("Failed:", errorInfo)
        }
    }

    const handleCancel = () => {
        form.resetFields()
        onModalCancel()
    }

    return (
        <Modal
            title={t("button.changePassword")}
            open={isModalOpen}
            maskClosable={false}
            onOk={onFinish}
            onCancel={handleCancel}
            destroyOnClose={true}
        >
            <Form
                form={form}
                name="basic"
                labelCol={{ span: 8 }}
                wrapperCol={{ span: 16 }}
                onFinish={onFinish}
                autoComplete="off"
            >
                <Form.Item
                    label={t("form.changePassword.oldPassword")}
                    name="oldPassword"
                    rules={[
                        {
                            required: true,
                            message: "Please input your old password!"
                        },
                        { type: "string", min: 6 }
                    ]}
                    getValueFromEvent={(e) => {
                        return e.target.value.trim()
                    }}
                >
                    <Input.Password />
                </Form.Item>

                <Form.Item
                    label={t("form.changePassword.newPassword")}
                    name="newPassword"
                    rules={[
                        {
                            required: true,
                            message: "Please input your new password!"
                        },
                        { type: "string", min: 6 }
                    ]}
                    getValueFromEvent={(e) => {
                        return e.target.value.trim()
                    }}
                >
                    <Input.Password />
                </Form.Item>
                <Form.Item
                    label={t("form.changePassword.confirmPassword")}
                    name="confirmNewPassword"
                    rules={[
                        {
                            required: true,
                            message: "Please confirm your password!"
                        },
                        // {
                        //     type: "string",
                        //     min: 6
                        // },
                        {
                            validator: (rule, value, callback) => {
                                if (
                                    value &&
                                    value !== form.getFieldValue("newPassword")
                                ) {
                                    callback(t("toast.passwordNotMatch"))
                                } else {
                                    callback()
                                }
                            }
                        }
                    ]}
                    getValueFromEvent={(e) => {
                        return e.target.value.trim()
                    }}
                >
                    <Input.Password />
                </Form.Item>
            </Form>
        </Modal>
    )
}

export default ChangePasswordForm
