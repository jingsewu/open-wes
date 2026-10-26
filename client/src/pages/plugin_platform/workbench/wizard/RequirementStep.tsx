import React, { useState } from "react"
import { Alert, Button, Select, Space, Tag, Typography } from "antd"
import { useTranslation } from "react-i18next"
import { EXTENSION_TYPES } from "../../formModels"
import { EXTENSION_TYPE_LABEL_KEYS } from "../../labels"
import { AiDraftResult, ExtensionType, PluginContract, ScriptPlugin } from "../../types"
import AiDraftPanel from "../AiDraftPanel"

interface RequirementStepProps {
    plugin: ScriptPlugin
    contract: PluginContract | null
    aiResult: AiDraftResult | null
    onResult: (result: AiDraftResult) => void
    onNext: () => void
}

const RequirementStep: React.FC<RequirementStepProps> = ({
    plugin,
    contract,
    aiResult,
    onResult,
    onNext
}) => {
    const { t } = useTranslation()
    const [hints, setHints] = useState<ExtensionType[]>([])

    const types = contract
        ? contract.archetypes.map((archetype) => archetype.type)
        : EXTENSION_TYPES
    const suffix =
        hints.length > 0 ? "Suggested extension types: " + hints.join(", ") : undefined

    return (
        <Space direction="vertical" style={{ width: "100%" }} size="middle">
            <Typography.Title level={5} style={{ margin: 0 }}>
                {t("pluginPlatform.wizard.requirement.title")}
            </Typography.Title>
            <div>
                <Typography.Text>{t("pluginPlatform.wizard.requirement.hints")}</Typography.Text>
                <Select
                    mode="multiple"
                    style={{ width: "100%", marginTop: 4 }}
                    value={hints}
                    onChange={(value: ExtensionType[]) => setHints(value)}
                    placeholder={t("pluginPlatform.wizard.requirement.hintsHint")}
                    options={types.map((type) => ({
                        label: t(EXTENSION_TYPE_LABEL_KEYS[type]),
                        value: type
                    }))}
                />
            </div>
            <AiDraftPanel
                pluginKey={plugin.pluginKey}
                baseRevisionId={null}
                requirementSuffix={suffix}
                onCompleted={onResult}
            />
            {aiResult && (
                <Alert
                    type="success"
                    showIcon
                    message={t("pluginPlatform.wizard.requirement.result")}
                    description={
                        <Space direction="vertical">
                            <span>
                                {t("pluginPlatform.wizard.requirement.resultSummary", {
                                    no: aiResult.revisionNo
                                })}
                            </span>
                            {aiResult.changeSummary && <span>{aiResult.changeSummary}</span>}
                            <Tag color={aiResult.materializationVerified ? "success" : "warning"}>
                                {aiResult.materializationVerified
                                    ? t("pluginPlatform.wizard.review.materializationVerified")
                                    : t("pluginPlatform.wizard.review.materializationUnverified")}
                            </Tag>
                        </Space>
                    }
                />
            )}
            <Space wrap>
                {aiResult ? (
                    <Button type="primary" onClick={onNext}>
                        {t("pluginPlatform.common.next")}
                    </Button>
                ) : (
                    <Button onClick={onNext}>
                        {t("pluginPlatform.wizard.requirement.skipAi")}
                    </Button>
                )}
            </Space>
        </Space>
    )
}

export default RequirementStep
