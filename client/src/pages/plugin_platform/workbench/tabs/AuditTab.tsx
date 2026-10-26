import React, { useCallback, useEffect, useState } from "react"
import { Button, Card, Table, Tag, Tooltip } from "antd"
import { ColumnsType } from "antd/lib/table"
import { useTranslation } from "react-i18next"
import { listPluginAudit } from "../../api"
import { formatMillis } from "../../format"
import { AUDIT_ACTION_LABEL_KEYS } from "../../labels"
import { AuditAction, PageResult, PluginAudit } from "../../types"
import { useWorkbench } from "../context"

const PAGE_SIZE = 20

const AuditTab: React.FC = () => {
    const { t } = useTranslation()
    const { plugin } = useWorkbench()
    const [data, setData] = useState<PageResult<PluginAudit> | null>(null)
    const [page, setPage] = useState(1)
    const [pageSize, setPageSize] = useState(PAGE_SIZE)
    const [loading, setLoading] = useState(false)
    const pluginKey = plugin ? plugin.pluginKey : null

    const load = useCallback(() => {
        if (!pluginKey) {
            return
        }
        setLoading(true)
        listPluginAudit(pluginKey, page, pageSize)
            .then((loaded) => setData(loaded))
            .catch(() => undefined)
            .then(() => setLoading(false))
    }, [pluginKey, page, pageSize])

    useEffect(() => {
        load()
    }, [load])

    if (!plugin) {
        return null
    }

    const columns: ColumnsType<PluginAudit> = [
        {
            title: t("pluginPlatform.audit.createdAt"),
            dataIndex: "createdAt",
            width: 170,
            render: (value: string) => formatMillis(value)
        },
        {
            title: t("pluginPlatform.audit.action"),
            dataIndex: "action",
            width: 150,
            render: (action: AuditAction) => t(AUDIT_ACTION_LABEL_KEYS[action])
        },
        {
            title: t("pluginPlatform.audit.operator"),
            dataIndex: "operator",
            width: 140
        },
        {
            title: t("pluginPlatform.audit.success"),
            dataIndex: "success",
            width: 110,
            render: (success: boolean) => (
                <Tag color={success ? "success" : "error"}>
                    {t(
                        success
                            ? "pluginPlatform.audit.successValue"
                            : "pluginPlatform.audit.failureValue"
                    )}
                </Tag>
            )
        },
        {
            title: t("pluginPlatform.audit.revision"),
            dataIndex: "revisionId",
            width: 120,
            render: (value: string | null) => value || "-"
        },
        {
            title: t("pluginPlatform.audit.detail"),
            dataIndex: "detail",
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
            title={t("pluginPlatform.audit.title")}
            extra={
                <Button onClick={load} loading={loading}>
                    {t("pluginPlatform.common.refresh")}
                </Button>
            }
        >
            <Table<PluginAudit>
                rowKey="id"
                size="small"
                loading={loading}
                dataSource={data ? data.items : []}
                columns={columns}
                locale={{ emptyText: t("pluginPlatform.audit.empty") }}
                pagination={{
                    current: page,
                    pageSize,
                    total: data ? Number(data.total) : 0,
                    showSizeChanger: true,
                    onChange: (nextPage, nextPageSize) => {
                        setPage(nextPage)
                        setPageSize(nextPageSize || PAGE_SIZE)
                    }
                }}
            />
        </Card>
    )
}

export default AuditTab
