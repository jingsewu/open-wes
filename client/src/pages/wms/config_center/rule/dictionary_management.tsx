import schema2component from "@/utils/schema2component"
import {
    create_update_columns,
    true_false_options
} from "@/utils/commonContants"
import {
    api_dictionary_add,
    api_dictionary_get,
    api_dictionary_update
} from "@/pages/wms/config_center/constants/api_constant"

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
        columns: [
            {
                name: "value",
                label: "table.value",
                type: "input-text"
            },
            {
                name: "showContent",
                label: "table.value",
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
        ]
    }
]

const add = {
    type: "button",
    actionType: "dialog",
    icon: "fa fa-plus",
    label: "button.add",
    dialog: {
        title: "button.add",
        size: "lg",
        closeOnEsc: true,
        body: {
            type: "form",
            api: api_dictionary_add,
            body: form
        }
    }
}

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
    {
        name: "code",
        label: "table.enumerationEncoding",
        searchable: true
    },
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
    {
        name: "description",
        label: "table.description"
    },
    ...create_update_columns
]

const searchIdentity = "MDictionary"
const showColumns = columns

const schema = {
    type: "page",
    title: "dictionaryManagement.title",
    toolbar: [],
    initApi: "post:/mdm/config/dictionary/getAll",
    body: [
        {
            type: "crud",
            syncLocation: false,
            name: "dictionaryTable",
            api: {
                method: "POST",
                url: "/search/search?page=${page}&perPage=${perPage}&createTime-op=bt&code-op=ct",
                dataType: "application/json"
            },
            defaultParams: {
                searchIdentity: searchIdentity,
                showColumns: showColumns,
                searchObject: {
                    orderBy: "update_time desc"
                }
            },
            autoFillHeight: true,
            autoGenerateFilter: {
                columnsNum: 3,
                showBtnToolbar: true
            },
            headerToolbar: [add],
            footerToolbar: ["switch-per-page", "statistics", "pagination"],
            columns: [
                ...columns,
                {
                    type: "operation",
                    label: "table.operation",
                    width: 100,
                    buttons: [
                        {
                            label: "button.modify",
                            type: "button",
                            actionType: "dialog",
                            dialog: {
                                title: "button.modify",
                                size: "lg",
                                closeOnEsc: true,
                                closeOnOutside: true,
                                body: {
                                    type: "form",
                                    initApi: api_dictionary_get,
                                    api: api_dictionary_update,
                                    controls: form
                                }
                            }
                        }
                    ],
                    toggled: true
                }
            ]
        }
    ]
}

export default schema2component(schema)
