import schema2component from "@/utils/schema2component"
import { api_crud_search } from "@/pages/constantApi"
import {
    api_api_keys_create,
    api_api_keys_delete
} from "@/pages/api_platform/constants/api_constant"
import {
    addButton,
    auditColumns,
    codeColumn,
    crudPage,
    deleteButton,
    formDrawer
} from "@/utils/amis/presets"

const form = [
    {
        type: "hidden",
        name: "id"
    },
    {
        type: "input-text",
        name: "apiKeyName",
        label: "interfacePlatform.interfaceManagement.table.apiKeyName",
        required: true
    }
]

const addDrawer = formDrawer({
    title: "interfacePlatform.apiKeys.addTitle",
    body: form,
    api: api_api_keys_create,
    reload: "ApiKeysTable",
    preventEnterSubmit: true
})

const columns = [
    {
        name: "id",
        label: "ID",
        hidden: true
    },
    {
        name: "apiKeyName",
        label: "interfacePlatform.interfaceManagement.table.apiKeyName",
        searchable: true
    },
    codeColumn("apiKey", "interfacePlatform.apiKeys.table.apiKey"),
    ...auditColumns({
        visible: true,
        createdOnly: true,
        seconds: true,
        createdSearchable: {
            type: "input-date-range",
            valueFormat: "x"
        }
    })
]

const schema = crudPage({
    title: "interfacePlatform.apiKeys.title",
    name: "ApiKeysTable",
    searchIdentity: "AApiKey",
    api: api_crud_search,
    columns,
    orderBy: "create_time desc",
    pageActions: [
        addButton({
            target: "ApiKeysTable",
            drawer: addDrawer
        })
    ],
    rowActions: [
        deleteButton({
            api: api_api_keys_delete,
            confirmText: "confirm.deleteApiKey",
            reload: "ApiKeysTable"
        })
    ],
    empty: "emptyState.apiKey"
})

export default schema2component(schema)
