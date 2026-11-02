import schema2component from "@/utils/schema2component"
import { true_false_options } from "@/utils/commonContants"
import {
    api_dictionary_add,
    api_dictionary_get,
    api_dictionary_update
} from "@/pages/wms/config_center/constants/api_constant"
import {
    addButton,
    auditColumns,
    codeColumn,
    crudPage,
    editButton,
    formDrawer,
    remarkColumn
} from "@/utils/amis/presets"

const form = [
    {
        type: "hidden",
        name: "id"
    },
    {
        type: "hidden",
        name: "version"
    },
    {
        label: "table.enumerationEncoding",
        type: "input-text",
        name: "code",
        required: true
    },
    {
        label: "table.enumerationName",
        type: "input-text",
        name: "name",
        required: true
    },
    {
        type: "switch",
        name: "editable",
        label: "table.allowEditing"
    },
    {
        label: "table.description",
        type: "input-text",
        name: "description"
    },
    {
        type: "input-table",
        name: "items",
        addable: true,
        editable: true,
        minLength: 1,
        columns: [
            {
                name: "value",
                label: "table.value",
                type: "input-text"
            },
            {
                name: "systemContent",
                label: "dictionaryManagement.systemLabel",
                type: "static"
            },
            {
                name: "showContent",
                label: "dictionaryManagement.customLabel",
                type: "input-text"
            },
            {
                name: "order",
                label: "table.displayOrder",
                type: "input-number"
            },
            {
                name: "defaultItem",
                label: "table.whetherDefaultValue",
                type: "select",
                options: true_false_options
            },
            {
                name: "description",
                label: "table.description",
                type: "input-text"
            }
        ],
        removable: true,
        needConfirm: false,
        onEvent: {
            delete: {
                actions: [
                    {
                        actionType: "toast",
                        args: {
                            msgType: "info",
                            position: "top-right",
                            title: "button.delete",
                            msg: "success"
                        }
                    }
                ]
            }
        }
    }
]

const addDrawer = formDrawer({
    title: "dictionaryManagement.addTitle",
    size: "xl",
    columnCount: 1,
    body: form,
    api: api_dictionary_add,
    reload: "dictionaryTable"
})

const editDrawer = formDrawer({
    title: "dictionaryManagement.editTitle",
    size: "xl",
    columnCount: 1,
    context: "${code} · ${name}",
    body: form,
    initApi: api_dictionary_get,
    api: api_dictionary_update,
    reload: "dictionaryTable"
})

const columns = [
    {
        name: "id",
        label: "ID",
        hidden: true
    },
    {
        name: "version",
        label: "Version",
        hidden: true
    },
    codeColumn("code", "table.enumerationEncoding", { searchable: true }),
    {
        name: "name",
        label: "table.enumerationName",
        searchable: true
    },
    {
        name: "editable",
        label: "table.whetherDefaultValue",
        type: "mapping",
        map: true_false_options
    },
    remarkColumn("description", "table.description"),
    ...auditColumns()
]

const schema = crudPage({
    title: "dictionaryManagement.title",
    name: "dictionaryTable",
    searchIdentity: "MDictionary",
    api: {
        method: "POST",
        url: "/search/search?page=${page}&perPage=${perPage}&createTime-op=bt&code-op=ct",
        dataType: "application/json"
    },
    columns,
    orderBy: "update_time desc",
    pageActions: [addButton({ target: "dictionaryTable", drawer: addDrawer })],
    rowActions: [editButton(editDrawer)],
    empty: "emptyState.dictionaryManagement"
})

export default schema2component(schema)
