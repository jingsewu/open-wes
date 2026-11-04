import React, { useState } from "react"
import { Alert, Button, Card, Input, Space, Typography, message } from "antd"
import { useTranslation } from "react-i18next"
import { saveRevision } from "../../api"
import {
    ConfigRow,
    DeclarationRow,
    RowIssue,
    fromConfigRows,
    fromDeclarationRows,
    toConfigRows,
    toDeclarationRows,
    validateConfigRows,
    validateDeclarationRows
} from "../../formModels"
import { AiDraftResult, PluginContract, ScriptPlugin, Scripts } from "../../types"
import ConfigSchemaTable from "../../ConfigSchemaTable"
import DeclarationTable from "../../DeclarationTable"
import ScriptFileEditor from "../../ScriptFileEditor"

interface ReviewStepProps {
    plugin: ScriptPlugin
    aiResult: AiDraftResult | null
    contract: PluginContract | null
    onSaved: () => void
    onNext: () => void
}

const ReviewStep: React.FC<ReviewStepProps> = ({ plugin, aiResult, contract, onSaved, onNext }) => {
    const { t } = useTranslation()
    const [scripts, setScripts] = useState<Scripts>(aiResult ? aiResult.scripts : {})
    const [declarations, setDeclarations] = useState<DeclarationRow[]>(
        toDeclarationRows(aiResult ? aiResult.extensions : [])
    )
    const [configRows, setConfigRows] = useState<ConfigRow[]>(
        toConfigRows(aiResult ? aiResult.configSchema : [])
    )
    const [changeSummary, setChangeSummary] = useState("")
    const [saving, setSaving] = useState(false)
    const [saved, setSaved] = useState(false)

    const hasRevision = !!plugin.currentRevisionId || !!aiResult || saved

    const issueText = (issue: RowIssue): string => t(issue.key, issue.values)

    const save = () => {
        const fileNames = Object.keys(scripts)
        if (fileNames.length === 0) {
            message.error(t("pluginPlatform.scripts.noScripts"))
            return
        }
        const declarationIssue = validateDeclarationRows(declarations, fileNames)
        if (declarationIssue) {
            message.error(issueText(declarationIssue))
            return
        }
        const configIssue = validateConfigRows(configRows)
        if (configIssue) {
            message.error(issueText(configIssue))
            return
        }
        setSaving(true)
        saveRevision(plugin.pluginKey, {
            scripts,
            extensionPoints: fromDeclarationRows(declarations),
            configSchema: fromConfigRows(configRows),
            changeSummary: changeSummary.trim()
        })
            .then(() => {
                setSaved(true)
                message.success(t("pluginPlatform.wizard.review.saved"))
                onSaved()
            })
            .catch(() => undefined)
            .then(() => setSaving(false))
    }

    return (
        <Space direction="vertical" style={{ width: "100%" }} size="middle">
            <Typography.Title level={5} style={{ margin: 0 }}>
                {t("pluginPlatform.wizard.review.title")}
            </Typography.Title>
            {aiResult ? (
                <Alert
                    type="success"
                    showIcon
                    message={t("pluginPlatform.wizard.requirement.resultSummary", {
                        no: aiResult.revisionNo
                    })}
                    description={
                        <span>
                            {aiResult.materializationVerified
                                ? t("pluginPlatform.wizard.review.materializationVerified")
                                : t("pluginPlatform.wizard.review.materializationUnverified")}
                        </span>
                    }
                />
            ) : (
                <Alert
                    type="warning"
                    showIcon
                    message={t("pluginPlatform.wizard.review.materializationUnverified")}
                />
            )}
            <Card size="small" title={t("pluginPlatform.scripts.files")}>
                <ScriptFileEditor
                    scripts={scripts}
                    onChange={setScripts}
                    readOnly={false}
                    seedKey={aiResult ? "ai:" + aiResult.revisionId : "manual"}
                />
            </Card>
            <Card size="small" title={t("pluginPlatform.scripts.declarations")}>
                <DeclarationTable
                    rows={declarations}
                    onChange={setDeclarations}
                    readOnly={false}
                    allowedTypes={contract ? contract.archetypes.map((archetype) => archetype.type) : []}
                    lifecycleEntityNames={contract ? contract.lifecycleEntityNames : []}
                />
            </Card>
            <Card size="small" title={t("pluginPlatform.scripts.configSchema")}>
                <ConfigSchemaTable rows={configRows} onChange={setConfigRows} readOnly={false} />
            </Card>
            <Card size="small">
                <Space direction="vertical" style={{ width: "100%" }}>
                    <Input.TextArea
                        rows={2}
                        maxLength={512}
                        value={changeSummary}
                        onChange={(event) => setChangeSummary(event.target.value)}
                        placeholder={t("pluginPlatform.scripts.changeSummary")}
                    />
                    <Button type="primary" onClick={save} loading={saving}>
                        {t("pluginPlatform.wizard.review.saveRevision")}
                    </Button>
                </Space>
            </Card>
            <Button type="primary" onClick={onNext} disabled={!hasRevision}>
                {t("pluginPlatform.wizard.review.next")}
            </Button>
        </Space>
    )
}

export default ReviewStep
