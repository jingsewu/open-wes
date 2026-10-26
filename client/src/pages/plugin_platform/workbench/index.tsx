import React from "react"
import { Card, Space, Typography } from "antd"
import { useTranslation } from "react-i18next"
import { useLocation } from "react-router"

const Workbench: React.FC = () => {
    const { t } = useTranslation()
    const location = useLocation()
    const params = new URLSearchParams(location.search)
    const pluginKey = params.get("pluginKey")
    const isNew = params.get("mode") === "new"
    return (
        <Card>
            <Space direction="vertical">
                <Typography.Title level={4}>{t("pluginPlatform.workbench.title")}</Typography.Title>
                <Typography.Text type="secondary">
                    {isNew
                        ? t("pluginPlatform.workbench.newPlugin")
                        : pluginKey || t("pluginPlatform.workbench.noPlugin")}
                </Typography.Text>
            </Space>
        </Card>
    )
}

export default Workbench
