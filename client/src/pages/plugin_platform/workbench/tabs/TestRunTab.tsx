import React, { useEffect, useState } from "react"
import { Alert, Button, Card, Descriptions, Input, Select, Space, Typography } from "antd"
import { useTranslation } from "react-i18next"
import { runTest } from "../../api"
import { formatDuration } from "../../format"
import { EXTENSION_TYPE_LABEL_KEYS } from "../../labels"
import { ExtensionType, TestRunResult } from "../../types"
import { useWorkbench } from "../context"

/**
 * Starting points for the argument editor, matching TestRunArgumentBuilder on the server.
 * Values are placeholders: the user must replace them with data the host type accepts.
 */
const ARGUMENT_TEMPLATES: Record<ExtensionType, Record<string, unknown>> = {
    OUTBOUND_WAVE_WAVE_ACTION: { orders: [] },
    OUTBOUND_WAVE_SPLIT_ACTION: { wave: {} },
    REQUEST_VALIDATE: { context: {} },
    REQUEST_SUPPLY: { context: {} },
    REQUEST_AFTER_INVOKE: { context: {} },
    CUSTOM_REQUEST_INVOKE: { context: {} },
    CUSTOM_API: { parameter: { workStationId: 1, requestParameter: {} } },
    LIFECYCLE_LISTENER: { entityId: 1, entityOrderNo: "", newStatus: "" },
    HTTP_ENDPOINT: { body: {} },
    CRON: {}
}

/**
 * Test runs execute the declared script against real host services through the server's test
 * runner. The server re-checks the developer role, the applySystem and that the selected
 * extension is declared by the current revision; the UI mirrors those rules to avoid dead ends.
 */
const TestRunTab: React.FC = () => {
    const { t } = useTranslation()
    const { plugin, currentRevision, capabilities } = useWorkbench()
    const declarations = currentRevision ? currentRevision.extensionPoints || [] : []
    const [selectedIndex, setSelectedIndex] = useState(0)
    const [argumentsText, setArgumentsText] = useState("{}")
    const [argumentsError, setArgumentsError] = useState<string | null>(null)
    const [running, setRunning] = useState(false)
    const [result, setResult] = useState<TestRunResult | null>(null)

    const revisionId = currentRevision ? currentRevision.id : null
    const declaration =
        declarations.length === 0
            ? null
            : selectedIndex < declarations.length
            ? declarations[selectedIndex]
            : declarations[0]
    const declarationKey = declaration
        ? declaration.type + "|" + declaration.script + "|" + declaration.method
        : ""

    useEffect(() => {
        setSelectedIndex(0)
        setArgumentsError(null)
        setResult(null)
    }, [revisionId])

    // Seed the editor with the selected archetype's expected top-level keys.
    useEffect(() => {
        if (declaration) {
            setArgumentsText(JSON.stringify(ARGUMENT_TEMPLATES[declaration.type], null, 2))
        }
    }, [declarationKey])

    if (!plugin) {
        return null
    }
    if (!capabilities.canDevelop) {
        return <Alert type="warning" showIcon message={t("pluginPlatform.testRun.noPermission")} />
    }
    if (plugin.applySystem !== "wes") {
        return <Alert type="info" showIcon message={t("pluginPlatform.testRun.wesOnly")} />
    }
    if (plugin.status === "ARCHIVED") {
        return <Alert type="info" showIcon message={t("pluginPlatform.scripts.readOnlyArchived")} />
    }
    if (!currentRevision) {
        return <Alert type="info" showIcon message={t("pluginPlatform.testRun.noRevision")} />
    }
    if (declarations.length === 0) {
        return <Alert type="info" showIcon message={t("pluginPlatform.testRun.noDeclarations")} />
    }

    const activeDeclaration = declaration || declarations[0]

    const run = () => {
        let parsed: unknown
        try {
            parsed = JSON.parse(argumentsText || "{}")
        } catch (ignored) {
            setArgumentsError(t("pluginPlatform.testRun.invalidArguments"))
            return
        }
        if (parsed === null || typeof parsed !== "object" || Array.isArray(parsed)) {
            setArgumentsError(t("pluginPlatform.testRun.invalidArguments"))
            return
        }
        setArgumentsError(null)
        setRunning(true)
        setResult(null)
        runTest(plugin.pluginKey, {
            extensionType: activeDeclaration.type,
            script: activeDeclaration.script,
            method: activeDeclaration.method,
            arguments: parsed as Record<string, unknown>
        })
            .then((testResult) => setResult(testResult))
            .catch(() => undefined)
            .then(() => setRunning(false))
    }

    return (
        <Space direction="vertical" style={{ width: "100%" }} size="middle">
            <Alert type="warning" showIcon message={t("pluginPlatform.testRun.warning")} />
            <Card size="small" title={t("pluginPlatform.testRun.title")}>
                <Space direction="vertical" style={{ width: "100%" }} size="middle">
                    <div>
                        <Typography.Text>{t("pluginPlatform.testRun.declaration")}</Typography.Text>
                        <Select
                            style={{ width: "100%", marginTop: 4 }}
                            value={selectedIndex}
                            onChange={(value: number) => setSelectedIndex(value)}
                            options={declarations.map((item, index) => ({
                                label:
                                    t(EXTENSION_TYPE_LABEL_KEYS[item.type]) +
                                    " · " +
                                    item.script +
                                    "#" +
                                    item.method,
                                value: index
                            }))}
                        />
                    </div>
                    <div>
                        <Typography.Text>{t("pluginPlatform.testRun.arguments")}</Typography.Text>
                        <Input.TextArea
                            style={{ marginTop: 4 }}
                            rows={6}
                            value={argumentsText}
                            onChange={(event) => setArgumentsText(event.target.value)}
                            disabled={running}
                        />
                    </div>
                    {argumentsError && <Alert type="error" showIcon message={argumentsError} />}
                    <Button type="primary" onClick={run} loading={running}>
                        {running ? t("pluginPlatform.testRun.running") : t("pluginPlatform.testRun.run")}
                    </Button>
                </Space>
            </Card>
            {result && (
                <Card size="small" title={result.success ? t("pluginPlatform.testRun.success") : t("pluginPlatform.testRun.failure")}>
                    <Space direction="vertical" style={{ width: "100%" }} size="middle">
                        <Alert
                            type={result.success ? "success" : "error"}
                            showIcon
                            message={
                                result.success ? t("pluginPlatform.testRun.success") : t("pluginPlatform.testRun.failure")
                            }
                            description={result.error || undefined}
                        />
                        <Descriptions size="small" column={2}>
                            <Descriptions.Item label={t("pluginPlatform.testRun.duration")}>
                                {formatDuration(result.durationMs)}
                            </Descriptions.Item>
                        </Descriptions>
                        {result.resultTruncated && (
                            <Alert type="info" showIcon message={t("pluginPlatform.testRun.truncated")} />
                        )}
                        {result.result && (
                            <Typography.Paragraph
                                style={{
                                    whiteSpace: "pre-wrap",
                                    fontFamily: "monospace",
                                    background: "#fafafa",
                                    padding: 8,
                                    marginBottom: 0,
                                    maxHeight: 320,
                                    overflow: "auto"
                                }}
                            >
                                {result.result}
                            </Typography.Paragraph>
                        )}
                    </Space>
                </Card>
            )}
        </Space>
    )
}

export default TestRunTab
