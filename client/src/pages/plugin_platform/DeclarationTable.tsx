import React from "react"
import { Button, Input, InputNumber, Select, Space, Table, Tag } from "antd"
import { ColumnsType } from "antd/lib/table"
import { useTranslation } from "react-i18next"
import { ExtensionType } from "./types"
import { EXTENSION_TYPE_LABEL_KEYS } from "./labels"
import {
    DEFAULT_LIFECYCLE_ENTITY_NAMES,
    DEFAULT_METHOD_BY_TYPE,
    DeclarationRow,
    EXTENSION_TYPES,
    isRequestType,
    newDeclarationRowKey
} from "./formModels"

interface DeclarationTableProps {
    rows: DeclarationRow[]
    onChange: (rows: DeclarationRow[]) => void
    readOnly: boolean
    allowedTypes: ExtensionType[]
    lifecycleEntityNames: string[]
}

const HTTP_METHODS = ["GET", "POST", "PUT", "PATCH", "DELETE"]

const DeclarationTable: React.FC<DeclarationTableProps> = ({
    rows,
    onChange,
    readOnly,
    allowedTypes,
    lifecycleEntityNames
}) => {
    const { t } = useTranslation()
    const typeOptions = allowedTypes.length > 0 ? allowedTypes : EXTENSION_TYPES
    const entityOptions = lifecycleEntityNames.length > 0 ? lifecycleEntityNames : DEFAULT_LIFECYCLE_ENTITY_NAMES

    const update = (rowKey: string, patch: Partial<DeclarationRow>) => {
        onChange(rows.map((row) => (row.rowKey === rowKey ? { ...row, ...patch } : row)))
    }

    const changeType = (row: DeclarationRow, type: ExtensionType) => {
        const defaultMethod = DEFAULT_METHOD_BY_TYPE[type]
        const method = row.method.trim() === "" || row.method === DEFAULT_METHOD_BY_TYPE[row.type]
            ? defaultMethod
            : row.method
        update(row.rowKey, { type, method })
    }

    const addRow = () => {
        const type = typeOptions[0] || "HTTP_ENDPOINT"
        onChange([
            ...rows,
            {
                rowKey: newDeclarationRowKey(),
                type,
                priority: 100,
                script: "",
                method: DEFAULT_METHOD_BY_TYPE[type],
                apiType: "",
                entityName: type === "LIFECYCLE_LISTENER" ? entityOptions[0] || "" : "",
                customApiCode: "",
                httpMethod: type === "HTTP_ENDPOINT" ? "POST" : "",
                path: "",
                cron: ""
            }
        ])
    }

    const details = (row: DeclarationRow): React.ReactNode => {
        if (readOnly) {
            if (isRequestType(row.type)) {
                return row.apiType
            }
            if (row.type === "LIFECYCLE_LISTENER") {
                return row.entityName
            }
            if (row.type === "CUSTOM_API") {
                return row.customApiCode
            }
            if (row.type === "HTTP_ENDPOINT") {
                return (row.httpMethod || "") + " " + (row.path || "")
            }
            if (row.type === "CRON") {
                return row.cron
            }
            return "-"
        }
        if (isRequestType(row.type)) {
            return (
                <Input
                    value={row.apiType}
                    onChange={(event) => update(row.rowKey, { apiType: event.target.value })}
                    placeholder={t("pluginPlatform.declaration.apiType")}
                />
            )
        }
        if (row.type === "LIFECYCLE_LISTENER") {
            return (
                <Select
                    style={{ width: "100%" }}
                    value={row.entityName || undefined}
                    onChange={(value: string) => update(row.rowKey, { entityName: value })}
                    options={entityOptions.map((name) => ({ label: name, value: name }))}
                    placeholder={t("pluginPlatform.declaration.entityName")}
                />
            )
        }
        if (row.type === "CUSTOM_API") {
            return (
                <Input
                    value={row.customApiCode}
                    onChange={(event) => update(row.rowKey, { customApiCode: event.target.value })}
                    placeholder={t("pluginPlatform.declaration.customApiCode")}
                />
            )
        }
        if (row.type === "HTTP_ENDPOINT") {
            return (
                <Space.Compact style={{ width: "100%" }}>
                    <Select
                        style={{ width: 110 }}
                        value={row.httpMethod || "POST"}
                        onChange={(value: string) => update(row.rowKey, { httpMethod: value })}
                        options={HTTP_METHODS.map((method) => ({ label: method, value: method }))}
                    />
                    <Input
                        value={row.path}
                        onChange={(event) => update(row.rowKey, { path: event.target.value })}
                        placeholder={t("pluginPlatform.declaration.path")}
                    />
                </Space.Compact>
            )
        }
        if (row.type === "CRON") {
            return (
                <Input
                    value={row.cron}
                    onChange={(event) => update(row.rowKey, { cron: event.target.value })}
                    placeholder={t("pluginPlatform.declaration.cron")}
                />
            )
        }
        return "-"
    }

    const columns: ColumnsType<DeclarationRow> = [
        {
            title: t("pluginPlatform.declaration.type"),
            dataIndex: "type",
            width: 230,
            render: (type: ExtensionType, row) =>
                readOnly ? (
                    <Tag>{t(EXTENSION_TYPE_LABEL_KEYS[type])}</Tag>
                ) : (
                    <Select
                        style={{ width: "100%" }}
                        value={type}
                        onChange={(value: ExtensionType) => changeType(row, value)}
                        options={typeOptions.map((option) => ({
                            label: t(EXTENSION_TYPE_LABEL_KEYS[option]),
                            value: option
                        }))}
                    />
                )
        },
        {
            title: t("pluginPlatform.declaration.script"),
            dataIndex: "script",
            width: 170,
            render: (script: string, row) =>
                readOnly ? (
                    script
                ) : (
                    <Input
                        value={script}
                        onChange={(event) => update(row.rowKey, { script: event.target.value })}
                        placeholder={t("pluginPlatform.scripts.defaultFile")}
                    />
                )
        },
        {
            title: t("pluginPlatform.declaration.method"),
            dataIndex: "method",
            width: 150,
            render: (method: string, row) =>
                readOnly ? (
                    method
                ) : (
                    <Input
                        value={method}
                        onChange={(event) => update(row.rowKey, { method: event.target.value })}
                    />
                )
        },
        {
            title: t("pluginPlatform.declaration.priority"),
            dataIndex: "priority",
            width: 110,
            render: (priority: number, row) =>
                readOnly ? (
                    String(priority)
                ) : (
                    <InputNumber
                        min={0}
                        precision={0}
                        style={{ width: "100%" }}
                        value={priority}
                        onChange={(value) =>
                            update(row.rowKey, { priority: typeof value === "number" ? value : 0 })
                        }
                    />
                )
        },
        {
            title: t("pluginPlatform.declaration.details"),
            key: "details",
            render: (_value, row) => details(row)
        },
        {
            title: t("pluginPlatform.workbench.actions"),
            key: "actions",
            width: 90,
            render: (_value, row) =>
                readOnly ? null : (
                    <Button
                        type="link"
                        danger
                        onClick={() => onChange(rows.filter((candidate) => candidate.rowKey !== row.rowKey))}
                    >
                        {t("pluginPlatform.common.remove")}
                    </Button>
                )
        }
    ]

    return (
        <Space direction="vertical" style={{ width: "100%" }}>
            <Table<DeclarationRow>
                rowKey="rowKey"
                size="small"
                pagination={false}
                columns={readOnly ? columns.filter((column) => column.key !== "actions") : columns}
                dataSource={rows}
                scroll={{ x: 1100 }}
                locale={{ emptyText: t("pluginPlatform.declaration.empty") }}
            />
            {!readOnly && (
                <Button type="dashed" onClick={addRow}>
                    {t("pluginPlatform.scripts.addDeclaration")}
                </Button>
            )}
        </Space>
    )
}

export default DeclarationTable
