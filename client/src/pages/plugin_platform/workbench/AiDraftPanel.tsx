import React, { useEffect, useRef, useState } from "react"
import { Alert, Button, Input, Space, Tag, Typography, message } from "antd"
import { useTranslation } from "react-i18next"
import { errorMessage } from "../api"
import { isAbortError, streamAiDraft } from "../sse"
import { AiDraftResult } from "../types"

interface StageEntry {
    id: number
    name: string
}

interface AiDraftPanelProps {
    pluginKey: string
    baseRevisionId: string | null
    /** Appended to the requirement, separated by a blank line (e.g. archetype hints). */
    requirementSuffix?: string
    disabled?: boolean
    onCompleted: (result: AiDraftResult) => void
}

const MAX_REQUIREMENT_LENGTH = 4000

/**
 * Requirement input plus SSE generation. The server owns quota, role checks, retries and revision
 * creation; this panel only renders stream progress and hands the result back to its parent.
 */
const AiDraftPanel: React.FC<AiDraftPanelProps> = ({
    pluginKey,
    baseRevisionId,
    requirementSuffix,
    disabled,
    onCompleted
}) => {
    const { t } = useTranslation()
    const [requirement, setRequirement] = useState("")
    const [streaming, setStreaming] = useState(false)
    const [output, setOutput] = useState("")
    const [stages, setStages] = useState<StageEntry[]>([])
    const [error, setError] = useState<string | null>(null)
    const controllerRef = useRef<AbortController | null>(null)
    const stageSeqRef = useRef(0)

    useEffect(() => {
        return () => {
            if (controllerRef.current) {
                controllerRef.current.abort()
            }
        }
    }, [])

    const formatStage = (stage: string): string => {
        if (stage.indexOf("retry#") === 0) {
            return t("pluginPlatform.wizard.requirement.stage.retry", { n: stage.substring(6) })
        }
        const key = "pluginPlatform.wizard.requirement.stage." + stage
        const translated = t(key)
        return translated === key ? stage : translated
    }

    const stop = () => {
        if (controllerRef.current) {
            controllerRef.current.abort()
            controllerRef.current = null
        }
        setStreaming(false)
    }

    const generate = () => {
        const trimmed = requirement.trim()
        if (trimmed === "") {
            message.error(t("pluginPlatform.chat.requirementRequired"))
            return
        }
        const fullRequirement = requirementSuffix ? trimmed + "\n\n" + requirementSuffix : trimmed
        if (fullRequirement.length > MAX_REQUIREMENT_LENGTH) {
            message.error(t("pluginPlatform.chat.requirementTooLong"))
            return
        }
        const controller = new AbortController()
        controllerRef.current = controller
        setStreaming(true)
        setOutput("")
        setStages([])
        setError(null)
        stageSeqRef.current = 0
        streamAiDraft(
            { pluginKey, requirement: fullRequirement, baseRevisionId },
            {
                signal: controller.signal,
                onChunk: (text) => setOutput((previous) => previous + text),
                onStage: (stage) => {
                    stageSeqRef.current += 1
                    const entry: StageEntry = { id: stageSeqRef.current, name: stage }
                    setStages((previous) => previous.concat(entry))
                },
                onResult: (result) => {
                    controllerRef.current = null
                    setStreaming(false)
                    message.success(t("pluginPlatform.chat.completed", { no: result.revisionNo }))
                    onCompleted(result)
                },
                onError: (code, description) => {
                    controllerRef.current = null
                    setStreaming(false)
                    setError(description)
                }
            }
        ).catch((streamError) => {
            controllerRef.current = null
            setStreaming(false)
            if (isAbortError(streamError)) {
                message.info(t("pluginPlatform.chat.cancelled"))
                return
            }
            setError(errorMessage(streamError, "The AI request failed."))
        })
    }

    return (
        <Space direction="vertical" style={{ width: "100%" }} size="middle">
            <Input.TextArea
                rows={6}
                maxLength={MAX_REQUIREMENT_LENGTH}
                showCount
                value={requirement}
                onChange={(event) => setRequirement(event.target.value)}
                placeholder={t("pluginPlatform.chat.requirementPlaceholder")}
                disabled={disabled || streaming}
            />
            <Space wrap>
                <Button
                    type="primary"
                    onClick={generate}
                    loading={streaming}
                    disabled={disabled || streaming}
                >
                    {streaming ? t("pluginPlatform.chat.streaming") : t("pluginPlatform.chat.generate")}
                </Button>
                {streaming && (
                    <Button danger onClick={stop}>
                        {t("pluginPlatform.chat.cancel")}
                    </Button>
                )}
            </Space>
            {stages.length > 0 && (
                <div>
                    <Typography.Text type="secondary">{t("pluginPlatform.chat.stage")}</Typography.Text>
                    <div style={{ marginTop: 4 }}>
                        <Space wrap>
                            {stages.map((stage) => (
                                <Tag key={stage.id}>{formatStage(stage.name)}</Tag>
                            ))}
                        </Space>
                    </div>
                </div>
            )}
            {output && (
                <div>
                    <Typography.Text type="secondary">{t("pluginPlatform.chat.output")}</Typography.Text>
                    <Typography.Paragraph
                        style={{
                            whiteSpace: "pre-wrap",
                            background: "#fafafa",
                            padding: 8,
                            maxHeight: 240,
                            overflow: "auto"
                        }}
                    >
                        {output}
                    </Typography.Paragraph>
                </div>
            )}
            {error && <Alert type="error" showIcon message={error} />}
        </Space>
    )
}

export default AiDraftPanel
