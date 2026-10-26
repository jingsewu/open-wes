import { ConfigField, ExtensionDeclaration, ExtensionType } from "./types"

export interface RowIssue {
    key: string
    values?: Record<string, string>
}

export interface DeclarationRow {
    rowKey: string
    type: ExtensionType
    priority: number
    script: string
    method: string
    apiType: string
    entityName: string
    customApiCode: string
    httpMethod: string
    path: string
    cron: string
}

export interface ConfigRow {
    rowKey: string
    field: string
    type: string
    required: boolean
    defaultValue: string
}

export const EXTENSION_TYPES: ExtensionType[] = [
    "OUTBOUND_WAVE_WAVE_ACTION",
    "OUTBOUND_WAVE_SPLIT_ACTION",
    "REQUEST_VALIDATE",
    "REQUEST_SUPPLY",
    "REQUEST_AFTER_INVOKE",
    "CUSTOM_REQUEST_INVOKE",
    "CUSTOM_API",
    "LIFECYCLE_LISTENER",
    "HTTP_ENDPOINT",
    "CRON"
]

// Mirrors PluginContractCatalog.ARCHETYPES.scriptsMethod on the server.
export const DEFAULT_METHOD_BY_TYPE: Record<ExtensionType, string> = {
    OUTBOUND_WAVE_WAVE_ACTION: "wave",
    OUTBOUND_WAVE_SPLIT_ACTION: "split",
    REQUEST_VALIDATE: "validate",
    REQUEST_SUPPLY: "supply",
    REQUEST_AFTER_INVOKE: "afterInvoke",
    CUSTOM_REQUEST_INVOKE: "invoke",
    CUSTOM_API: "execute",
    LIFECYCLE_LISTENER: "afterStatusChange",
    HTTP_ENDPOINT: "handle",
    CRON: "run"
}

export const DEFAULT_LIFECYCLE_ENTITY_NAMES: string[] = [
    "OutboundPlanOrder",
    "OutboundWave",
    "ContainerTask"
]

// Mirrors ScriptPluginConfigValidator: STRING, INT, INTEGER, NUMBER, BOOLEAN.
export const CONFIG_FIELD_TYPES: string[] = ["STRING", "INT", "INTEGER", "NUMBER", "BOOLEAN"]

let declarationSequence = 0
let configSequence = 0

export function newDeclarationRowKey(): string {
    declarationSequence += 1
    return "declaration-row-" + declarationSequence
}

export function newConfigRowKey(): string {
    configSequence += 1
    return "config-row-" + configSequence
}

export function isRequestType(type: ExtensionType): boolean {
    return type === "REQUEST_VALIDATE" || type === "REQUEST_SUPPLY" || type === "REQUEST_AFTER_INVOKE"
}

export function toDeclarationRows(declarations: ExtensionDeclaration[]): DeclarationRow[] {
    return declarations.map((declaration) => ({
        rowKey: newDeclarationRowKey(),
        type: declaration.type,
        priority: declaration.priority,
        script: declaration.script || "",
        method: declaration.method || "",
        apiType: declaration.apiType || "",
        entityName: declaration.entityName || "",
        customApiCode: declaration.customApiCode || "",
        httpMethod: declaration.httpMethod || "",
        path: declaration.path || "",
        cron: declaration.cron || ""
    }))
}

function nullable(value: string): string | null {
    const trimmed = value.trim()
    return trimmed === "" ? null : trimmed
}

export function fromDeclarationRows(rows: DeclarationRow[]): ExtensionDeclaration[] {
    return rows.map((row) => ({
        type: row.type,
        priority: row.priority,
        script: row.script.trim(),
        method: row.method.trim(),
        apiType: isRequestType(row.type) ? nullable(row.apiType) : null,
        entityName: row.type === "LIFECYCLE_LISTENER" ? nullable(row.entityName) : null,
        customApiCode: row.type === "CUSTOM_API" ? nullable(row.customApiCode) : null,
        httpMethod: row.type === "HTTP_ENDPOINT" ? nullable(row.httpMethod) : null,
        path: row.type === "HTTP_ENDPOINT" ? nullable(row.path) : null,
        cron: row.type === "CRON" ? nullable(row.cron) : null
    }))
}

export function validateDeclarationRows(rows: DeclarationRow[], scriptNames: string[]): RowIssue | null {
    for (let index = 0; index < rows.length; index++) {
        const row = rows[index]
        const script = row.script.trim()
        if (script === "") {
            return { key: "pluginPlatform.declaration.scriptRequired" }
        }
        if (scriptNames.indexOf(script) < 0) {
            return { key: "pluginPlatform.scripts.missingScriptFile", values: { declaration: script } }
        }
        if (row.method.trim() === "") {
            return { key: "pluginPlatform.declaration.methodRequired" }
        }
        if (isRequestType(row.type) && row.apiType.trim() === "") {
            return { key: "pluginPlatform.declaration.apiTypeRequired" }
        }
        if (row.type === "LIFECYCLE_LISTENER" && row.entityName.trim() === "") {
            return { key: "pluginPlatform.declaration.entityRequired" }
        }
        if (row.type === "CUSTOM_API" && row.customApiCode.trim() === "") {
            return { key: "pluginPlatform.declaration.customApiRequired" }
        }
        if (row.type === "HTTP_ENDPOINT") {
            const path = row.path.trim()
            if (!path || path.charAt(0) !== "/") {
                return { key: "pluginPlatform.declaration.pathRequired" }
            }
        }
        if (row.type === "CRON" && row.cron.trim() === "") {
            return { key: "pluginPlatform.declaration.cronRequired" }
        }
    }
    return null
}

export function toConfigRows(fields: ConfigField[]): ConfigRow[] {
    return fields.map((field) => ({
        rowKey: newConfigRowKey(),
        field: field.field || "",
        type: field.type || "STRING",
        required: field.required,
        defaultValue: field.defaultValue === null || field.defaultValue === undefined ? "" : field.defaultValue
    }))
}

export function fromConfigRows(rows: ConfigRow[]): ConfigField[] {
    return rows.map((row) => ({
        field: row.field.trim(),
        type: row.type,
        required: row.required,
        defaultValue: nullable(row.defaultValue)
    }))
}

export function validateConfigRows(rows: ConfigRow[]): RowIssue | null {
    const seen: string[] = []
    for (let index = 0; index < rows.length; index++) {
        const row = rows[index]
        const field = row.field.trim()
        if (field === "") {
            return { key: "pluginPlatform.configSchema.fieldRequired" }
        }
        if (seen.indexOf(field) >= 0) {
            return { key: "pluginPlatform.configSchema.duplicate", values: { field } }
        }
        seen.push(field)
        if (CONFIG_FIELD_TYPES.indexOf(row.type) < 0) {
            return { key: "pluginPlatform.configSchema.typeRequired", values: { field } }
        }
    }
    return null
}
