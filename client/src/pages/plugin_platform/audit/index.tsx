import schema2component from "@/utils/schema2component"
import { PluginAudit } from "../types"
import {
    codeColumn,
    crudPage,
    dateTimeColumn,
    statusColumn
} from "@/utils/amis/presets"

interface AuditPagePayload {
    items?: PluginAudit[]
    total?: number | string
}

/**
 * AMIS CRUD adaptor. The interceptor resolves business errors as HTTP 200 envelopes; returning an
 * empty page here keeps AMIS from rendering the envelope as a row and from toasting twice.
 */
async function adaptAuditPage(payload: unknown): Promise<{ items: PluginAudit[]; total: number }> {
    const envelope = payload as { code?: string; status?: string; data?: AuditPagePayload } | null
    if (!envelope || envelope.status || envelope.code !== "0" || !envelope.data) {
        return { items: [], total: 0 }
    }
    const items = Array.isArray(envelope.data.items) ? envelope.data.items : []
    return { items, total: Number(envelope.data.total || 0) }
}

const columns = [
    dateTimeColumn("createdAt", "pluginPlatform.audit.createdAt", {
        seconds: true,
        width: 170
    }),
    codeColumn("pluginKey", "pluginPlatform.audit.plugin", {
        width: 220
    }),
    {
        name: "action",
        label: "pluginPlatform.audit.action",
        type: "mapping",
        width: 150,
        map: {
            CREATE_DRAFT: "pluginPlatform.auditAction.createDraft",
            SAVE_REVISION: "pluginPlatform.auditAction.saveRevision",
            VALIDATE: "pluginPlatform.auditAction.validate",
            ENABLE: "pluginPlatform.auditAction.enable",
            DISABLE: "pluginPlatform.auditAction.disable",
            ARCHIVE: "pluginPlatform.auditAction.archive",
            ROLLBACK: "pluginPlatform.auditAction.rollback",
            CONFIG_SAVE: "pluginPlatform.auditAction.configSave",
            METADATA_UPDATE: "pluginPlatform.auditAction.metadataUpdate",
            TEST_RUN: "pluginPlatform.auditAction.testRun"
        }
    },
    {
        name: "operator",
        label: "pluginPlatform.audit.operator",
        width: 160
    },
    statusColumn("success", "pluginPlatform.audit.success", {
        labels: {
            "1": "pluginPlatform.audit.successValue",
            "0": "pluginPlatform.audit.failureValue"
        },
        tones: {
            "1": "success",
            "0": "danger"
        },
        width: 110
    }),
    {
        name: "revisionId",
        label: "pluginPlatform.audit.revision",
        width: 120,
        tpl: "${revisionId || '-'}"
    },
    {
        name: "detail",
        label: "pluginPlatform.audit.detail",
        tpl: "${detail || '-'}"
    }
]

const schema = crudPage({
    title: "pluginPlatform.audit.title",
    name: "PluginAuditTable",
    // Custom endpoint: it returns its own paged payload, not a /search result.
    api: {
        method: "get",
        url: "/plugin-platform/scriptPlugin/audit",
        adaptor: adaptAuditPage
    },
    // AMIS CRUD names the page-size field perPage; the endpoint expects pageSize.
    perPageField: "pageSize",
    columns,
    pageActions: [],
    empty: "pluginPlatform.audit.empty"
})

export default schema2component(schema)
