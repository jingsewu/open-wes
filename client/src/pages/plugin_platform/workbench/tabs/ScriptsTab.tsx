import React, { useEffect, useState } from "react"
import { Alert, Button, Card, Input, Space, Tag, message } from "antd"
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
import { Scripts } from "../../types"
import ConfigSchemaTable from "../../ConfigSchemaTable"
import DeclarationTable from "../../DeclarationTable"
import ScriptFileEditor from "../../ScriptFileEditor"
import { useWorkbench } from "../context"

/**
 * Manual revision editor. The seed is the current revision; saving appends a new revision and
 * makes it current (server rule). Enabled plugins must be disabled first, and archived plugins are
 * read-only — both are server rules mirrored here by disabling the editors.
 */
const ScriptsTab: React.FC = () => {
    const { t } = useTranslation()
    const { plugin, currentRevision, contract, capabilities, reload } = useWorkbench()
    const [scripts, setScripts] = useState<Scripts>({})
    const [declarations, setDeclarations] = useState<DeclarationRow[]>([])
    const [configRows, setConfigRows] = useState<ConfigRow[]>([])
    const [changeSummary, setChangeSummary] = useState("")
    const [saving, setSaving] = useState(false)

    const revisionId = currentRevision ? currentRevision.id : null

    useEffect(() => {
        if (currentRevision) {
            setScripts(currentRevision.scripts || {})
            setDeclarations(toDeclarationRows(currentRevision.extensionPoints || []))
            setConfigRows(toConfigRows(currentRevision.configSchema || []))
        } else {
            setScripts({})
            setDeclarations([])
            setConfigRows([])
        }
        setChangeSummary("")
    }, [revisionId])

    if (!plugin) {
        return null
    }

    const archived = plugin.status === "ARCHIVED"
    const enabled = plugin.status === "ENABLED"
    const canEdit = capabilities.canDevelop && !archived && !enabled
    const readOnlyReason = archived
        ? t("pluginPlatform.scripts.readOnlyArchived")
        : enabled
        ? t("pluginPlatform.scripts.readOnlyEnabled")
        : null

    const issueText = (issue: RowIssue): string => t(issue.key, issue.values)

    const handleSave = () => {
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
                message.success(t("pluginPlatform.scripts.revisionSaved"))
                return reload()
            })
            .catch(() => undefined)
            .then(() => setSaving(false))
    }

    return (
        <Space direction="vertical" style={{ width: "100%" }} size="middle">
            {readOnlyReason && <Alert type="info" showIcon message={readOnlyReason} />}
            {!capabilities.canDevelop && (
                <Alert type="warning" showIcon message={t("pluginPlatform.scripts.noPermission")} />
            )}
            <Card
                title={t("pluginPlatform.scripts.files")}
                size="small"
                extra={
                    currentRevision ? (
                        <Tag color="blue">{"v" + currentRevision.revisionNo}</Tag>
                    ) : (
                        <Tag>{t("pluginPlatform.overview.noRevisions")}</Tag>
                    )
                }
            >
                <ScriptFileEditor
                    scripts={scripts}
                    onChange={setScripts}
                    readOnly={!canEdit}
                    seedKey={revisionId ? "rev:" + revisionId : "new"}
                />
            </Card>
            <Card title={t("pluginPlatform.scripts.declarations")} size="small">
                <DeclarationTable
                    rows={declarations}
                    onChange={setDeclarations}
                    readOnly={!canEdit}
                    allowedTypes={contract ? contract.archetypes.map((archetype) => archetype.type) : []}
                    lifecycleEntityNames={contract ? contract.lifecycleEntityNames : []}
                />
            </Card>
            <Card title={t("pluginPlatform.scripts.configSchema")} size="small">
                <ConfigSchemaTable rows={configRows} onChange={setConfigRows} readOnly={!canEdit} />
            </Card>
            <Card size="small">
                <Space direction="vertical" style={{ width: "100%" }}>
                    <Input.TextArea
                        rows={2}
                        maxLength={512}
                        value={changeSummary}
                        onChange={(event) => setChangeSummary(event.target.value)}
                        placeholder={t("pluginPlatform.scripts.changeSummary")}
                        disabled={!canEdit}
                    />
                    <Button type="primary" onClick={handleSave} loading={saving} disabled={!canEdit}>
                        {t("pluginPlatform.scripts.saveRevision")}
                    </Button>
                </Space>
            </Card>
        </Space>
    )
}

export default ScriptsTab
