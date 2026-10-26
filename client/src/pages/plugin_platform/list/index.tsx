import schema2component from "@/utils/schema2component"
import { listRevisions } from "../api"
import { ScriptPlugin, ScriptPluginRevision } from "../types"

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
    {
        name: "pluginKey",
        label: "pluginPlatform.list.column.pluginKey",
        width: 200
    },
    {
        name: "name",
        label: "pluginPlatform.list.column.name",
        width: 180
    },
    {
        name: "applySystem",
        label: "pluginPlatform.list.column.applySystem",
        type: "mapping",
        width: 120,
        map: {
            wes: "pluginPlatform.applySystem.wes",
            station: "pluginPlatform.applySystem.station"
        }
    },
    {
        name: "applyModule",
        label: "pluginPlatform.list.column.applyModule",
        width: 140,
        tpl: "${applyModule || '-'}"
    },
    {
        name: "status",
        label: "pluginPlatform.list.column.status",
        type: "mapping",
        width: 110,
        map: {
            DRAFT: "pluginPlatform.status.draft",
            ENABLED: "pluginPlatform.status.enabled",
            DISABLED: "pluginPlatform.status.disabled",
            ARCHIVED: "pluginPlatform.status.archived"
        }
    },
    {
        name: "lastValidationStatus",
        label: "pluginPlatform.list.column.lastValidation",
        type: "mapping",
        width: 130,
        map: {
            PASSED: "pluginPlatform.validation.passed",
            FAILED: "pluginPlatform.validation.failed",
            "*": "pluginPlatform.validation.notValidated"
        }
    },
    {
        name: "lastValidationAt",
        label: "pluginPlatform.validation.lastAt",
        width: 170,
        tpl: "${lastValidationAt|millis}"
    },
    {
        name: "currentRevisionNo",
        label: "pluginPlatform.list.column.currentRevision",
        width: 130,
        tpl: "${currentRevisionNo ? 'v' + currentRevisionNo : '-'}"
    },
    {
        name: "updateTime",
        label: "pluginPlatform.list.column.updateTime",
        width: 170,
        tpl: "${updateTime|millis}"
    },
    {
        type: "operation",
        label: "pluginPlatform.list.column.actions",
        width: 90,
        buttons: [
            {
                type: "button",
                level: "link",
                label: "pluginPlatform.list.open",
                actionType: "link",
                link: "/plugin-platform/plugin-workbench?pluginKey=${pluginKey}"
            }
        ],
        toggled: true
    }
]

const schema = {
    type: "page",
    title: "pluginPlatform.list.title",
    toolbar: [],
    body: [
        {
            type: "crud",
            name: "PluginTable",
            syncLocation: false,
            api: {
                method: "get",
                url: "/plugin-platform/scriptPlugin/list",
                adaptor: adaptPluginList
            },
            // One fetch; filter changes apply client-side because the endpoint is not paged.
            loadDataOnce: true,
            loadDataOnceFetchOnFilter: false,
            autoFillHeight: true,
            placeholder: "pluginPlatform.list.empty",
            filterTogglable: true,
            filterDefaultVisible: true,
            filter: {
                title: "pluginPlatform.list.filterTitle",
                mode: "inline",
                submitText: "pluginPlatform.list.search",
                body: [
                    {
                        type: "input-text",
                        name: "pluginKey",
                        label: "pluginPlatform.list.column.pluginKey",
                        clearable: true
                    },
                    {
                        type: "input-text",
                        name: "name",
                        label: "pluginPlatform.list.column.name",
                        clearable: true
                    },
                    {
                        type: "select",
                        name: "status",
                        label: "pluginPlatform.list.column.status",
                        clearable: true,
                        options: [
                            { label: "pluginPlatform.status.draft", value: "DRAFT" },
                            { label: "pluginPlatform.status.enabled", value: "ENABLED" },
                            { label: "pluginPlatform.status.disabled", value: "DISABLED" },
                            { label: "pluginPlatform.status.archived", value: "ARCHIVED" }
                        ]
                    },
                    {
                        type: "select",
                        name: "applySystem",
                        label: "pluginPlatform.list.column.applySystem",
                        clearable: true,
                        options: [
                            { label: "pluginPlatform.applySystem.wes", value: "wes" },
                            { label: "pluginPlatform.applySystem.station", value: "station" }
                        ]
                    }
                ]
            },
            headerToolbar: ["reload", newPluginButton],
            footerToolbar: ["switch-per-page", "statistics", "pagination"],
            columns: columns
        }
    ]
}

export default schema2component(schema)
