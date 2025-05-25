import schema2component from "@/utils/schema2component"
import {api_print_config_add, api_print_config_get} from "@/pages/wms/config_center/constants/api_constant"
import {create_update_columns, enable_options} from "@/utils/commonContants"
import {api_crud_search} from "@/pages/constantApi"
import {select_print_rule_code, work_station} from "@/pages/wms/constants/select_search_api_contant";

const form = [
    {
        type: "input-text",
        name: "id",
        label: "ID",
        hidden: true
    },
    {
        type: "input-text",
        name: "configCode",
        label: "printConfig.configCode",
        required: true
    },
    {
        name: "workStationId",
        label: "printConfig.workStationId",
        type: "select",
        source: work_station,
        required: true
    },
    {
        type: "combo",
        name: "printConfigDetails",
        label: "printConfig.details",
        multiple: true,
        required: true,
        items: [
            {
                type: "select",
                name: "ruleCode",
                label: "printConfig.ruleCode",
                source: select_print_rule_code,
                required: true
            },
            {
                type: "input-text",
                name: "printer",
                label: "printConfig.printer",
                required: true
            }
        ]
    },
    {
        type: "switch",
        name: "enabled",
        label: "printConfig.enabled",
        options: enable_options
    }
]

const add = {
    type: "button",
    actionType: "dialog",
    icon: "fa fa-plus",
    label: "button.add",
    target: "Table",
    dialog: {
        title: "button.add",
        closeOnEsc: true,
        size: "lg",
        body: {
            type: "form",
            api: api_print_config_add,
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
        name: "configCode",
        label: "printConfig.configCode",
        searchable: true
    },
    {
        name: "workStationId",
        label: "printConfig.workStationId",
        searchable: true
    },
    {
        name: "enabled",
        label: "printConfig.enabled",
        type: "switch",
        options: enable_options
    },
    ...create_update_columns
]

const searchIdentity = "PPrintConfig"
const showColumns = columns

const schema = {
    type: "page",
    title: "printConfig.title",
    toolbar: [],
    data: {
        dictionary: "${ls:dictionary}"
    },
    body: [
        {
            type: "crud",
            syncLocation: false,
            name: "PPrintConfig",
            api: api_crud_search,
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
                            actionType: "drawer",
                            drawer: {
                                title: "button.modify",
                                closeOnEsc: true,
                                closeOnOutside: true,
                                body: {
                                    type: "form",
                                    initApi: api_print_config_get,
                                    api: api_print_config_add,
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
