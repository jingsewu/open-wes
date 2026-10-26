import React from "react"
import { Button, Input, Select, Space, Switch, Table } from "antd"
import { ColumnsType } from "antd/lib/table"
import { useTranslation } from "react-i18next"
import { CONFIG_FIELD_TYPES, ConfigRow, newConfigRowKey } from "./formModels"

interface ConfigSchemaTableProps {
    rows: ConfigRow[]
    onChange: (rows: ConfigRow[]) => void
    readOnly: boolean
}

const ConfigSchemaTable: React.FC<ConfigSchemaTableProps> = ({ rows, onChange, readOnly }) => {
    const { t } = useTranslation()

    const update = (rowKey: string, patch: Partial<ConfigRow>) => {
        onChange(rows.map((row) => (row.rowKey === rowKey ? { ...row, ...patch } : row)))
    }

    const addRow = () => {
        onChange([
            ...rows,
            { rowKey: newConfigRowKey(), field: "", type: "STRING", required: false, defaultValue: "" }
        ])
    }

    const columns: ColumnsType<ConfigRow> = [
        {
            title: t("pluginPlatform.configSchema.field"),
            dataIndex: "field",
            width: 220,
            render: (field: string, row) =>
                readOnly ? (
                    field
                ) : (
                    <Input value={field} onChange={(event) => update(row.rowKey, { field: event.target.value })} />
                )
        },
        {
            title: t("pluginPlatform.configSchema.type"),
            dataIndex: "type",
            width: 160,
            render: (type: string, row) =>
                readOnly ? (
                    type
                ) : (
                    <Select
                        style={{ width: "100%" }}
                        value={type}
                        onChange={(value: string) => update(row.rowKey, { type: value })}
                        options={CONFIG_FIELD_TYPES.map((option) => ({ label: option, value: option }))}
                    />
                )
        },
        {
            title: t("pluginPlatform.configSchema.required"),
            dataIndex: "required",
            width: 110,
            render: (required: boolean, row) =>
                readOnly ? (
                    required ? t("pluginPlatform.common.yes") : t("pluginPlatform.common.no")
                ) : (
                    <Switch checked={required} onChange={(checked) => update(row.rowKey, { required: checked })} />
                )
        },
        {
            title: t("pluginPlatform.configSchema.defaultValue"),
            dataIndex: "defaultValue",
            render: (defaultValue: string, row) =>
                readOnly ? (
                    defaultValue
                ) : (
                    <Input
                        value={defaultValue}
                        onChange={(event) => update(row.rowKey, { defaultValue: event.target.value })}
                    />
                )
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
            <Table<ConfigRow>
                rowKey="rowKey"
                size="small"
                pagination={false}
                columns={readOnly ? columns.filter((column) => column.key !== "actions") : columns}
                dataSource={rows}
                locale={{ emptyText: t("pluginPlatform.configSchema.empty") }}
            />
            {!readOnly && (
                <Button type="dashed" onClick={addRow}>
                    {t("pluginPlatform.scripts.addConfigField")}
                </Button>
            )}
        </Space>
    )
}

export default ConfigSchemaTable
