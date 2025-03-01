import schema2component from "@/utils/schema2component"
import {api_crud_search} from "@/pages/constantApi"
import {api_api_keys_create, api_api_keys_delete} from "@/pages/api_platform/constants/api_constant";

const form = [
    {
        type: "hidden",
        name: "id"
    },
    {
        type: "input-text",
        name: "apiKeyName",
        label: "interfacePlatform.interfaceManagement.table.apiKeyName",
        require: true
    }
]

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
    {
        name: "apiKey",
        label: "API Key"
    },
    {
        label: "table.createdBy",
        name: "createUser"
    },
    {
        name: "createTime",
        label: "table.creationTime",
        tpl: "${createTime/1000|date:YYYY-MM-DD HH\\:mm\\:ss}",
        searchable: {
            type: "input-date-range",
            valueFormat: "x"
        }
    }
]

const add = {
    type: "button",
    actionType: "dialog",
    icon: "fa fa-plus",
    label: "button.add",
    target: "Table",
    dialog: {
        title: "interfacePlatform.apiKeys.title",
        closeOnEsc: true,
        body: {
            id: "inputForm",
            type: "form",
            api: api_api_keys_create,
            preventEnterSubmit: true,
            body: form
        }
    }
}

const searchIdentity = "AApiKey"
const showColumns = columns

const schema = {
    type: "page",
    title: "interfacePlatform.apiKeys.title",
    toolbar: [],
    data: {
        dictionary: "${ls:dictionary}"
    },
    body: [
        {
            type: "crud",
            syncLocation: false,
            name: "ApiKeysTable",
            api: api_crud_search,
            defaultParams: {
                searchIdentity: searchIdentity,
                showColumns: showColumns,
                searchObject: {
                    orderBy: "create_time desc"
                }
            },
            autoFillHeight: true,
            autoGenerateFilter: {
                columnsNum: 3,
                showBtnToolbar: true
            },
            headerToolbar: ["reload", add],
            footerToolbar: ["switch-per-page", "statistics", "pagination"],
            columns: [
                ...columns,
                {
                    type: "operation",
                    label: "table.operation",
                    width: 230,
                    buttons: [
                        {
                            label: "button.delete",
                            type: "button",
                            actionType: "ajax",
                            level: "danger",
                            confirmText: "toast.sureDelete",
                            confirmTitle: "button.delete",
                            api: api_api_keys_delete,
                            reload: "ApiKeysTable"
                        }
                    ],
                    toggled: true
                }
            ]
        }
    ]
}

export default schema2component(schema)
