import React, { useState } from "react"
import { Button, Form, Input, Select, Space, message } from "antd"
import { useTranslation } from "react-i18next"
import { createDraft } from "../../api"
import { CreateDraftRequest, ScriptPlugin } from "../../types"

interface BasicsStepProps {
    onCreated: (plugin: ScriptPlugin) => void
}

interface BasicsValues {
    pluginKey: string
    name: string
    description?: string
    applySystem: string
    applyModule?: string
}

const BasicsStep: React.FC<BasicsStepProps> = ({ onCreated }) => {
    const { t } = useTranslation()
    const [submitting, setSubmitting] = useState(false)

    const submit = (values: BasicsValues) => {
        const payload: CreateDraftRequest = {
            pluginKey: values.pluginKey.trim(),
            name: values.name.trim(),
            description: (values.description || "").trim(),
            applySystem: values.applySystem,
            applyModule: (values.applyModule || "").trim()
        }
        setSubmitting(true)
        createDraft(payload)
            .then((created) => {
                message.success(t("pluginPlatform.wizard.requirement.created"))
                onCreated(created)
            })
            .catch(() => undefined)
            .then(() => setSubmitting(false))
    }

    return (
        <Form<BasicsValues>
            layout="vertical"
            initialValues={{ applySystem: "wes" }}
            onFinish={submit}
        >
            <Form.Item
                name="pluginKey"
                label={t("pluginPlatform.wizard.basics.pluginKey")}
                extra={t("pluginPlatform.wizard.basics.pluginKeyHint")}
                rules={[
                    {
                        required: true,
                        whitespace: true,
                        message: t("pluginPlatform.wizard.basics.pluginKeyRequired")
                    },
                    {
                        pattern: /^[A-Za-z0-9._-]+$/,
                        message: t("pluginPlatform.wizard.basics.pluginKeyInvalid")
                    }
                ]}
            >
                <Input maxLength={64} />
            </Form.Item>
            <Form.Item
                name="name"
                label={t("pluginPlatform.wizard.basics.name")}
                rules={[
                    {
                        required: true,
                        whitespace: true,
                        message: t("pluginPlatform.wizard.basics.nameRequired")
                    }
                ]}
            >
                <Input maxLength={128} />
            </Form.Item>
            <Form.Item name="description" label={t("pluginPlatform.wizard.basics.description")}>
                <Input.TextArea rows={3} maxLength={512} />
            </Form.Item>
            <Form.Item
                name="applySystem"
                label={t("pluginPlatform.wizard.basics.applySystem")}
                rules={[
                    {
                        required: true,
                        message: t("pluginPlatform.wizard.basics.applySystemRequired")
                    }
                ]}
            >
                <Select
                    options={[
                        { label: t("pluginPlatform.applySystem.wes"), value: "wes" },
                        { label: t("pluginPlatform.applySystem.station"), value: "station" }
                    ]}
                />
            </Form.Item>
            <Form.Item name="applyModule" label={t("pluginPlatform.wizard.basics.applyModule")}>
                <Input maxLength={64} />
            </Form.Item>
            <Space>
                <Button type="primary" htmlType="submit" loading={submitting}>
                    {t("pluginPlatform.common.next")}
                </Button>
            </Space>
        </Form>
    )
}

export default BasicsStep
