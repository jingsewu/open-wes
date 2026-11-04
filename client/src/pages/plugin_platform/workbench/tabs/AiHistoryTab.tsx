import React, { useCallback, useEffect, useState } from "react"
import { Button, Card, Table, Tag, Tooltip } from "antd"
import { ColumnsType } from "antd/lib/table"
import { useTranslation } from "react-i18next"
import { listAiGenerations } from "../../api"
import { formatDuration, formatMillis, formatNumber } from "../../format"
import { AI_STATUS_COLORS, AI_STATUS_LABEL_KEYS } from "../../labels"
import { AiGeneration, AiGenerationStatus } from "../../types"
import { useWorkbench } from "../context"

const AiHistoryTab: React.FC = () => {
    const { t } = useTranslation()
    const { plugin } = useWorkbench()
    const [generations, setGenerations] = useState<AiGeneration[]>([])
    const [loading, setLoading] = useState(false)
    const pluginKey = plugin ? plugin.pluginKey : null

    const load = useCallback(() => {
        if (!pluginKey) {
            return
        }
        setLoading(true)
        listAiGenerations(pluginKey)
            .then((loaded) => setGenerations(loaded))
            .catch(() => undefined)
            .then(() => setLoading(false))
    }, [pluginKey])

    useEffect(() => {
        load()
    }, [load])

    if (!plugin) {
        return null
    }

    const columns: ColumnsType<AiGeneration> = [
        {
            title: t("pluginPlatform.aiHistory.createdAt"),
            dataIndex: "createdAt",
            width: 170,
            render: (value: string) => formatMillis(value)
        },
        {
            title: t("pluginPlatform.aiHistory.user"),
            dataIndex: "userName",
            width: 140
        },
        {
            title: t("pluginPlatform.aiHistory.model"),
            dataIndex: "model",
            width: 160
        },
        {
            title: t("pluginPlatform.aiHistory.promptVersion"),
            dataIndex: "promptVersion",
            width: 130
        },
        {
            title: t("pluginPlatform.aiHistory.status"),
            dataIndex: "status",
            width: 110,
            render: (status: AiGenerationStatus) => (
                <Tag color={AI_STATUS_COLORS[status]}>{t(AI_STATUS_LABEL_KEYS[status])}</Tag>
            )
        },
        {
            title: t("pluginPlatform.aiHistory.attempts"),
            dataIndex: "attempts",
            width: 90
        },
        {
            title: t("pluginPlatform.aiHistory.tokens"),
            width: 140,
            render: (generation: AiGeneration) =>
                formatNumber(generation.promptTokens) + " / " + formatNumber(generation.completionTokens)
        },
        {
            title: t("pluginPlatform.aiHistory.duration"),
            dataIndex: "durationMs",
            width: 110,
            render: (value: string | null) => formatDuration(value)
        },
        {
            title: t("pluginPlatform.aiHistory.finishedAt"),
            dataIndex: "finishedAt",
            width: 170,
            render: (value: string | null) => formatMillis(value)
        },
        {
            title: t("pluginPlatform.aiHistory.error"),
            dataIndex: "errorMessage",
            ellipsis: true,
            render: (value: string | null) =>
                value ? (
                    <Tooltip title={value}>
                        <span>{value}</span>
                    </Tooltip>
                ) : (
                    "-"
                )
        }
    ]

    return (
        <Card
            size="small"
            title={t("pluginPlatform.aiHistory.title")}
            extra={
                <Button onClick={load} loading={loading}>
                    {t("pluginPlatform.common.refresh")}
                </Button>
            }
        >
            <Table<AiGeneration>
                rowKey="id"
                size="small"
                loading={loading}
                dataSource={generations}
                columns={columns}
                pagination={false}
                locale={{ emptyText: t("pluginPlatform.aiHistory.empty") }}
            />
        </Card>
    )
}

export default AiHistoryTab
