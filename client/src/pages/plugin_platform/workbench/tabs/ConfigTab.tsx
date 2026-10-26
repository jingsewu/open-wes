import React, { useEffect, useState } from "react"
import { Alert, Button, Card, Input, InputNumber, Space, Spin, Switch, Typography, message } from "antd"
import { useTranslation } from "react-i18next"
import { getPluginConfig, savePluginConfig } from "../../api"
import { ConfigField, ScriptPluginConfig } from "../../types"
import { useWorkbench } from "../context"

const SUPPORTED_TYPES = ["STRING", "INT", "INTEGER", "NUMBER", "BOOLEAN"]

function normaliseValue(field: ConfigField, raw: unknown): unknown {
    if (field.type === "BOOLEAN") {
        return raw === true || raw === "true"
    }
    if (field.type === "INT" || field.type === "INTEGER" || field.type === "NUMBER") {
        if (typeof raw === "number") {
            return raw
        }
        if (typeof raw === "string" && raw.trim() !== "") {
            const numeric = Number(raw)
            return isFinite(numeric) ? numeric : undefined
        }
        return undefined
    }
    return raw === null || raw === undefined ? undefined : String(raw)
}

function defaultValue(field: ConfigField): unknown {
    if (field.type === "BOOLEAN") {
        return field.defaultValue === "true"
    }
    if (field.defaultValue === null || field.defaultValue === "") {
        return undefined
    }
    return normaliseValue(field, field.defaultValue)
}

/**
 * The server rejects unknown fields, so the form state and the save payload contain only the
 * fields declared by the current config schema. Booleans always have a value (a switch cannot be
 * "undefined"), which also makes required booleans saveable without touching them.
 */
function initialValues(config: ScriptPluginConfig): Record<string, unknown> {
    const source = config.values || {}
    const values: Record<string, unknown> = {}
    ;(config.schema || []).forEach((field) => {
        if (!field.field) {
            return
        }
        const present = Object.prototype.hasOwnProperty.call(source, field.field)
        const normalised = present ? normaliseValue(field, source[field.field]) : defaultValue(field)
        if (normalised !== undefined) {
            values[field.field] = normalised
        } else if (field.type === "BOOLEAN") {
            values[field.field] = false
        }
    })
    return values
}

const ConfigTab: React.FC = () => {
    const { t } = useTranslation()
    const { plugin, capabilities } = useWorkbench()
    const [config, setConfig] = useState<ScriptPluginConfig | null>(null)
    const [values, setValues] = useState<Record<string, unknown>>({})
    const [loading, setLoading] = useState(false)
    const [saving, setSaving] = useState(false)

    const pluginKey = plugin ? plugin.pluginKey : null

    const load = () => {
        if (!pluginKey || !capabilities.canAdminister) {
            return
        }
        setLoading(true)
        getPluginConfig(pluginKey)
            .then((loaded) => {
                setConfig(loaded)
                setValues(initialValues(loaded))
            })
            .catch(() => undefined)
            .then(() => setLoading(false))
    }

    useEffect(() => {
        load()
    }, [pluginKey, capabilities.canAdminister])

    if (!plugin) {
        return null
    }
    if (capabilities.loading) {
        return <Spin />
    }
    if (!capabilities.canAdminister) {
        return <Alert type="info" showIcon message={t("pluginPlatform.config.adminOnly")} />
    }

    const archived = plugin.status === "ARCHIVED"
    const fields = config ? config.schema || [] : []
    const supportedFields = fields.filter((field) => SUPPORTED_TYPES.indexOf(field.type) >= 0)

    const setField = (field: ConfigField, value: unknown) => {
        setValues({ ...values, [field.field]: value })
    }

    const save = () => {
        if (!config) {
            return
        }
        const payload: Record<string, unknown> = {}
        supportedFields.forEach((field) => {
            if (Object.prototype.hasOwnProperty.call(values, field.field)) {
                const value = values[field.field]
                if (value !== undefined) {
                    payload[field.field] = value
                }
            }
        })
        setSaving(true)
        savePluginConfig(plugin.pluginKey, payload, config.lockVersion)
            .then((saved) => {
                setConfig(saved)
                setValues(initialValues(saved))
                message.success(t("pluginPlatform.config.saved"))
            })
            .catch(() => {
                // A stale lockVersion means another user saved first. Refetch so the next save
                // carries the current lockVersion; the interceptor already toasted the reason.
                load()
            })
            .then(() => setSaving(false))
    }

    const renderInput = (field: ConfigField): React.ReactNode => {
        const value = values[field.field]
        if (field.type === "BOOLEAN") {
            return (
                <Switch
                    checked={value === true}
                    onChange={(checked) => setField(field, checked)}
                    disabled={archived}
                />
            )
        }
        if (field.type === "INT" || field.type === "INTEGER" || field.type === "NUMBER") {
            return (
                <InputNumber
                    style={{ width: 240 }}
                    value={typeof value === "number" ? value : undefined}
                    precision={field.type === "NUMBER" ? undefined : 0}
                    onChange={(next) => setField(field, normaliseValue(field, next))}
                    disabled={archived}
                />
            )
        }
        if (field.type === "STRING") {
            return (
                <Input
                    style={{ width: 360 }}
                    value={typeof value === "string" ? value : ""}
                    onChange={(event) => setField(field, event.target.value)}
                    disabled={archived}
                />
            )
        }
        return (
            <Alert
                type="warning"
                showIcon
                message={t("pluginPlatform.config.unsupportedType", { type: field.type })}
            />
        )
    }

    return (
        <Space direction="vertical" style={{ width: "100%" }} size="middle">
            {archived && <Alert type="info" showIcon message={t("pluginPlatform.scripts.readOnlyArchived")} />}
            {loading ? (
                <Spin />
            ) : fields.length === 0 ? (
                <Alert type="info" showIcon message={t("pluginPlatform.config.empty")} />
            ) : (
                <Card size="small" title={t("pluginPlatform.config.title")}>
                    <Space direction="vertical" style={{ width: "100%" }} size="middle">
                        {fields.map((field) => (
                            <div key={field.field}>
                                <Typography.Text>
                                    {field.field}
                                    {field.required ? " *" : ""}
                                </Typography.Text>
                                <div style={{ marginTop: 4 }}>{renderInput(field)}</div>
                            </div>
                        ))}
                        <Button type="primary" onClick={save} loading={saving} disabled={archived}>
                            {t("pluginPlatform.config.save")}
                        </Button>
                    </Space>
                </Card>
            )}
        </Space>
    )
}

export default ConfigTab
