import React, { useEffect, useState } from "react"
import { Button, Card, Col, Row, Select, Space, Statistic, Table, Tag, Tooltip, Typography } from "antd"
import { ColumnsType } from "antd/lib/table"
import { useTranslation } from "react-i18next"
import { ExecutionQuery, getExecutionStats, listExecutions } from "../../api"
import { formatDuration, formatMillis, formatNumber, formatPercent } from "../../format"
import { EXTENSION_TYPES } from "../../formModels"
import { EXTENSION_TYPE_LABEL_KEYS } from "../../labels"
import {
    ExecutionLog,
    ExecutionStats,
    ExecutionTypeStats,
    ExtensionType,
    PageResult
} from "../../types"
import { useWorkbench } from "../context"

const PAGE_SIZE = 20

type ResultFilter = "all" | "success" | "failure"

/**
 * Execution history and aggregate stats for the current plugin. The list is server-paged and
 * server-filtered; the stats window is independent of the list filters, matching the API.
 */
const ExecutionsTab: React.FC = () => {
    const { t } = useTranslation()
    const { pluginKey } = useWorkbench()
    const [hours, setHours] = useState(24)
    const [stats, setStats] = useState<ExecutionStats | null>(null)
    const [page, setPage] = useState(1)
    const [pageSize, setPageSize] = useState(PAGE_SIZE)
    const [resultFilter, setResultFilter] = useState<ResultFilter>("all")
    const [typeFilter, setTypeFilter] = useState<ExtensionType | "all">("all")
    const [data, setData] = useState<PageResult<ExecutionLog> | null>(null)
    const [loading, setLoading] = useState(false)
    const [reloadKey, setReloadKey] = useState(0)

    useEffect(() => {
        let active = true
        getExecutionStats(pluginKey, hours)
            .then((loaded) => {
                if (active) {
                    setStats(loaded)
                }
            })
            .catch(() => {
                if (active) {
                    setStats(null)
                }
            })
        return () => {
            active = false
        }
    }, [pluginKey, hours, reloadKey])

    useEffect(() => {
        let active = true
        setLoading(true)
        const query: ExecutionQuery = { page, pageSize }
        if (resultFilter !== "all") {
            query.success = resultFilter === "success"
        }
        if (typeFilter !== "all") {
            query.extensionType = typeFilter
        }
        listExecutions(pluginKey, query)
            .then((loaded) => {
                if (active) {
                    setData(loaded)
                }
            })
            .catch(() => {
                if (active) {
                    setData({ items: [], total: "0", page, pageSize })
                }
            })
            .then(() => {
                if (active) {
                    setLoading(false)
                }
            })
        return () => {
            active = false
        }
    }, [pluginKey, page, pageSize, resultFilter, typeFilter, reloadKey])

    const statsColumns: ColumnsType<ExecutionTypeStats> = [
        {
            title: t("pluginPlatform.executions.type"),
            dataIndex: "extensionType",
            render: (type: ExtensionType) => t(EXTENSION_TYPE_LABEL_KEYS[type])
        },
        {
            title: t("pluginPlatform.executions.total"),
            dataIndex: "total",
            width: 120,
            render: (value: string) => formatNumber(value)
        },
        {
            title: t("pluginPlatform.executions.failed"),
            dataIndex: "failed",
            width: 120,
            render: (value: string) => formatNumber(value)
        },
        {
            title: t("pluginPlatform.executions.avgDuration"),
            dataIndex: "avgDurationMs",
            width: 140,
            render: (value: string) => formatDuration(value)
        },
        {
            title: t("pluginPlatform.executions.maxDuration"),
            dataIndex: "maxDurationMs",
            width: 140,
            render: (value: string) => formatDuration(value)
        }
    ]

    const columns: ColumnsType<ExecutionLog> = [
        {
            title: t("pluginPlatform.executions.time"),
            dataIndex: "createdAt",
            width: 170,
            render: (value: string) => formatMillis(value)
        },
        {
            title: t("pluginPlatform.executions.type"),
            dataIndex: "extensionType",
            width: 180,
            render: (type: ExtensionType) => t(EXTENSION_TYPE_LABEL_KEYS[type])
        },
        {
            title: t("pluginPlatform.executions.result"),
            dataIndex: "success",
            width: 110,
            render: (success: boolean) => (
                <Tag color={success ? "success" : "error"}>
                    {t(
                        success
                            ? "pluginPlatform.executions.success"
                            : "pluginPlatform.executions.failure"
                    )}
                </Tag>
            )
        },
        {
            title: t("pluginPlatform.executions.duration"),
            dataIndex: "durationMs",
            width: 110,
            render: (value: string) => formatDuration(value)
        },
        {
            title: t("pluginPlatform.executions.revision"),
            dataIndex: "revisionId",
            width: 120,
            render: (value: string | null) => value || "-"
        },
        {
            title: t("pluginPlatform.executions.error"),
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

    const typeOptions: { label: string; value: string }[] = [
        { label: t("pluginPlatform.common.all"), value: "all" }
    ].concat(
        EXTENSION_TYPES.map((type) => ({ label: t(EXTENSION_TYPE_LABEL_KEYS[type]), value: type }))
    )
    const resultOptions = [
        { label: t("pluginPlatform.common.all"), value: "all" },
        { label: t("pluginPlatform.executions.success"), value: "success" },
        { label: t("pluginPlatform.executions.failure"), value: "failure" }
    ]
    const hourOptions = [
        { label: t("pluginPlatform.executions.lastHour"), value: 1 },
        { label: t("pluginPlatform.executions.last24Hours"), value: 24 },
        { label: t("pluginPlatform.executions.last7Days"), value: 168 }
    ]

    return (
        <Space direction="vertical" style={{ width: "100%" }} size="middle">
            <Space wrap>
                <Typography.Text type="secondary">{t("pluginPlatform.executions.hours")}</Typography.Text>
                <Select
                    style={{ width: 160 }}
                    value={hours}
                    onChange={(value: number) => setHours(value)}
                    options={hourOptions}
                />
                <Select
                    style={{ width: 180 }}
                    value={resultFilter}
                    onChange={(value: ResultFilter) => {
                        setResultFilter(value)
                        setPage(1)
                    }}
                    options={resultOptions}
                />
                <Select
                    style={{ width: 220 }}
                    value={typeFilter}
                    onChange={(value: ExtensionType | "all") => {
                        setTypeFilter(value)
                        setPage(1)
                    }}
                    options={typeOptions}
                />
                <Button onClick={() => setReloadKey(reloadKey + 1)} loading={loading}>
                    {t("pluginPlatform.common.refresh")}
                </Button>
            </Space>
            {stats && (
                <Row gutter={16}>
                    <Col span={5}>
                        <Card size="small">
                            <Statistic title={t("pluginPlatform.executions.total")} value={formatNumber(stats.total)} />
                        </Card>
                    </Col>
                    <Col span={5}>
                        <Card size="small">
                            <Statistic title={t("pluginPlatform.executions.failed")} value={formatNumber(stats.failed)} />
                        </Card>
                    </Col>
                    <Col span={5}>
                        <Card size="small">
                            <Statistic
                                title={t("pluginPlatform.executions.failureRate")}
                                value={formatPercent(stats.failureRate)}
                            />
                        </Card>
                    </Col>
                    <Col span={5}>
                        <Card size="small">
                            <Statistic
                                title={t("pluginPlatform.executions.avgDuration")}
                                value={formatDuration(stats.avgDurationMs)}
                            />
                        </Card>
                    </Col>
                    <Col span={4}>
                        <Card size="small">
                            <Statistic
                                title={t("pluginPlatform.executions.maxDuration")}
                                value={formatDuration(stats.maxDurationMs)}
                            />
                        </Card>
                    </Col>
                </Row>
            )}
            {stats && stats.byType && stats.byType.length > 0 && (
                <Card size="small" title={t("pluginPlatform.executions.byType")}>
                    <Table<ExecutionTypeStats>
                        rowKey="extensionType"
                        size="small"
                        pagination={false}
                        dataSource={stats.byType}
                        columns={statsColumns}
                    />
                </Card>
            )}
            <Card size="small" title={t("pluginPlatform.executions.title")}>
                <Table<ExecutionLog>
                    rowKey="id"
                    size="small"
                    loading={loading}
                    dataSource={data ? data.items : []}
                    columns={columns}
                    locale={{ emptyText: t("pluginPlatform.executions.empty") }}
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
        </Space>
    )
}

export default ExecutionsTab
