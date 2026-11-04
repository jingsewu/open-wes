import React, { useState } from "react"
import { Alert, Button, Card, Space, Typography, message } from "antd"
import { useTranslation } from "react-i18next"
import { enablePlugin, validatePlugin } from "../../api"
import { ScriptPlugin, ValidationReport } from "../../types"
import ValidationReportView from "../../ValidationReportView"

interface FinishStepProps {
    plugin: ScriptPlugin
    canAdminister: boolean
    onRefresh: () => void
    onClose: () => void
}

const FinishStep: React.FC<FinishStepProps> = ({ plugin, canAdminister, onRefresh, onClose }) => {
    const { t } = useTranslation()
    const [report, setReport] = useState<ValidationReport | null>(null)
    const [validating, setValidating] = useState(false)
    const [enabling, setEnabling] = useState(false)

    const runValidation = () => {
        setValidating(true)
        validatePlugin(plugin.pluginKey)
            .then((loaded) => {
                setReport(loaded)
                message.success(t("pluginPlatform.overview.validationFinished"))
                onRefresh()
            })
            .catch(() => undefined)
            .then(() => setValidating(false))
    }

    const enable = () => {
        if (!report || !report.passed) {
            message.error(t("pluginPlatform.wizard.finish.validationRequired"))
            return
        }
        setEnabling(true)
        enablePlugin(plugin.pluginKey)
            .then(() => {
                message.success(t("pluginPlatform.wizard.finish.enabled"))
                onRefresh()
                onClose()
            })
            .catch(() => undefined)
            .then(() => setEnabling(false))
    }

    return (
        <Space direction="vertical" style={{ width: "100%" }} size="middle">
            <Typography.Title level={5} style={{ margin: 0 }}>
                {t("pluginPlatform.wizard.finish.title")}
            </Typography.Title>
            {!canAdminister && (
                <Alert type="info" showIcon message={t("pluginPlatform.wizard.finish.developerHint")} />
            )}
            <Space wrap>
                <Button type="primary" onClick={runValidation} loading={validating}>
                    {t("pluginPlatform.wizard.finish.runValidation")}
                </Button>
                <Button
                    type="primary"
                    onClick={enable}
                    loading={enabling}
                    disabled={!canAdminister || !report || !report.passed}
                >
                    {t("pluginPlatform.wizard.finish.enableNow")}
                </Button>
                <Button onClick={onClose}>{t("pluginPlatform.wizard.finish.finish")}</Button>
            </Space>
            {report && (
                <Card size="small" title={t("pluginPlatform.validation.reportTitle")}>
                    <ValidationReportView report={report} />
                </Card>
            )}
        </Space>
    )
}

export default FinishStep
