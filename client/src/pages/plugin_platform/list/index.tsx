import schema2component from "@/utils/schema2component"
import { listRevisions } from "../api"
import { ScriptPlugin, ScriptPluginRevision } from "../types"
import {
    codeColumn,
    crudPage,
    dateTimeColumn,
    statusColumn
} from "@/utils/amis/presets"

interface PluginListRow extends ScriptPlugin {
    currentRevisionNo: number | null
}

/**
 * AMIS CRUD adaptor. The interceptor resolves business errors as HTTP 200 envelopes; returning an
 * empty page here keeps AMIS from rendering the envelope as a row and from toasting twice.
 */
async function adaptPluginList(payload: unknown): Promise<{ items: PluginListRow[]; total: number }> {
    const envelope = payload as { code?: string; status?: string; data?: ScriptPlugin[] } | null
    if (!envelope || envelope.status || envelope.code !== "0" || !Array.isArray(envelope.data)) {
        return { items: [], total: 0 }
    }
    const plugins = envelope.data
    const revisionNos: Record<string, number | null> = {}
    await Promise.all(
        plugins.map(async (plugin) => {
            if (!plugin.currentRevisionId) {
                revisionNos[plugin.pluginKey] = null
                return
            }
            const revisions = await listRevisions(plugin.pluginKey).catch(
                () => [] as ScriptPluginRevision[]
            )
            const current = revisions.filter((revision) => revision.id === plugin.currentRevisionId)[0]
            revisionNos[plugin.pluginKey] = current ? current.revisionNo : null
        })
    )
    const items: PluginListRow[] = plugins.map((plugin) => ({
        ...plugin,
        currentRevisionNo: revisionNos[plugin.pluginKey]
    }))
    return { items, total: items.length }
}

const newPluginButton = {
    type: "button",
    level: "primary",
    icon: "fa fa-plus",
    label: "pluginPlatform.list.newPlugin",
    // "link" navigates with history.push; this app's AMIS env opens "url" in a new window.
    actionType: "link",
    link: "/plugin-platform/plugin-workbench?mode=new"
}

const columns = [
    codeColumn("pluginKey", "pluginPlatform.list.column.pluginKey", {
        width: 200,
        searchable: true
    }),
    {
        name: "name",
        label: "pluginPlatform.list.column.name",
        width: 180,
        searchable: true
    },
    {
        name: "applySystem",
        label: "pluginPlatform.list.column.applySystem",
        type: "mapping",
        width: 120,
        map: {
            wes: "pluginPlatform.applySystem.wes",
            station: "pluginPlatform.applySystem.station"
        },
        searchable: {
            type: "select",
            options: [
                { label: "pluginPlatform.applySystem.wes", value: "wes" },
                { label: "pluginPlatform.applySystem.station", value: "station" }
            ]
        }
    },
    {
        name: "applyModule",
        label: "pluginPlatform.list.column.applyModule",
        width: 140,
        tpl: "${applyModule || '-'}"
    },
    statusColumn("status", "pluginPlatform.list.column.status", {
        labels: {
            DRAFT: "pluginPlatform.status.draft",
            ENABLED: "pluginPlatform.status.enabled",
            DISABLED: "pluginPlatform.status.disabled",
            ARCHIVED: "pluginPlatform.status.archived"
        },
        tones: {
            DRAFT: "info",
            ENABLED: "success",
            DISABLED: "muted",
            ARCHIVED: "muted"
        },
        width: 110,
        searchable: {
            type: "select",
            options: [
                { label: "pluginPlatform.status.draft", value: "DRAFT" },
                { label: "pluginPlatform.status.enabled", value: "ENABLED" },
                { label: "pluginPlatform.status.disabled", value: "DISABLED" },
                { label: "pluginPlatform.status.archived", value: "ARCHIVED" }
            ]
        }
    }),
    statusColumn(
        "lastValidationStatus",
        "pluginPlatform.list.column.lastValidation",
        {
            labels: {
                PASSED: "pluginPlatform.validation.passed",
                FAILED: "pluginPlatform.validation.failed"
            },
            tones: {
                PASSED: "success",
                FAILED: "danger"
            },
            placeholder: "pluginPlatform.validation.notValidated",
            width: 130
        }
    ),
    dateTimeColumn("lastValidationAt", "pluginPlatform.validation.lastAt", {
        seconds: true,
        width: 170
    }),
    {
        name: "currentRevisionNo",
        label: "pluginPlatform.list.column.currentRevision",
        width: 130,
        tpl: "${currentRevisionNo ? 'v' + currentRevisionNo : '-'}"
    },
    dateTimeColumn("updateTime", "pluginPlatform.list.column.updateTime", {
        seconds: true,
        width: 170
    })
]

const schema = crudPage({
    title: "pluginPlatform.list.title",
    name: "PluginTable",
    // Custom endpoint: it returns the whole collection, not a paged /search result.
    api: {
        method: "get",
        url: "/plugin-platform/scriptPlugin/list",
        adaptor: adaptPluginList
    },
    loadDataOnce: true,
    columns,
    operationWidth: 90,
    pageActions: [newPluginButton],
    rowActions: [
        {
            type: "button",
            level: "link",
            className: "owes-action",
            label: "pluginPlatform.list.open",
            actionType: "link",
            link: "/plugin-platform/plugin-workbench?pluginKey=${pluginKey}"
        }
    ],
    empty: "pluginPlatform.list.empty"
})

export default schema2component(schema)
