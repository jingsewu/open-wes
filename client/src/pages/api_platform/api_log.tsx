import schema2component from "@/utils/schema2component"
import { api_crud_search } from "@/pages/constantApi"
import { api_api_log_get } from "@/pages/api_platform/constants/api_constant"
import {
    auditColumns,
    codeColumn,
    crudPage,
    numberColumn,
    statusColumn
} from "@/utils/amis/presets"
import { apiLogStatus } from "@/utils/amis/statusMaps"

const detailForm = [
    {
        type: "hidden",
        name: "id"
    },
    {
        type: "static",
        name: "apiCode",
        label: "interfacePlatform.interfaceManagement.table.interfaceCode"
    },
    {
        type: "static",
        name: "costTime",
        label: "interfacePlatform.interfaceLogs.table.durationMs"
    },
    {
        type: "static",
        name: "retryCount",
        label: "interfacePlatform.interfaceLogs.table.numberOfRetries"
    },
    {
        type: "static",
        name: "status",
        label: "table.status"
    },
    {
        type: "textarea",
        name: "requestData",
        label: "interfacePlatform.interfaceLogs.from.requestPackets",
        maxRows: 10,
        readOnly: true
    },
    {
        type: "textarea",
        name: "responseData",
        label: "interfacePlatform.interfaceLogs.from.responsePackets",
        maxRows: 10,
        readOnly: true
    }
]

const detailDrawer = {
    size: "lg",
    title: "button.detail",
    closeOnEsc: true,
    closeOnOutside: true,
    // Read-only record: the only footer action is closing the drawer.
    actions: [
        {
            type: "button",
            actionType: "close",
            label: "button.close"
        }
    ],
    body: {
        type: "form",
        mode: "horizontal",
        labelWidth: 108,
        initApi: api_api_log_get,
        body: [
            {
                type: "tpl",
                tpl: "${apiCode}",
                className: "owes-drawer-context"
            },
            ...detailForm
        ]
    }
}

const columns = [
    {
        name: "id",
        label: "ID",
        hidden: true
    },
    codeColumn(
        "apiCode",
        "interfacePlatform.interfaceManagement.table.interfaceCode",
        {
            searchable: true
        }
    ),
    numberColumn(
        "costTime",
        "interfacePlatform.interfaceLogs.table.durationMs"
    ),
    numberColumn(
        "retryCount",
        "interfacePlatform.interfaceLogs.table.numberOfRetries"
    ),
    statusColumn("status", "table.status", {
        ...apiLogStatus,
        searchable: {
            type: "select",
            source: "${dictionary.ApiLogStatus}"
        }
    }),
    ...auditColumns({
        visible: true,
        seconds: true,
        createdOnly: true,
        createdSearchable: { type: "input-date-range", valueFormat: "x" }
    })
]

const schema = crudPage({
    title: "interfacePlatform.interfaceLogs.title",
    name: "ApiLogTable",
    searchIdentity: "AApiLog",
    api: api_crud_search,
    columns,
    orderBy: "create_time desc",
    pageActions: [],
    rowActions: [
        {
            type: "button",
            actionType: "drawer",
            level: "link",
            className: "owes-action",
            label: "button.detail",
            drawer: detailDrawer
        }
    ],
    empty: "emptyState.apiLog"
})

export default schema2component(schema)
