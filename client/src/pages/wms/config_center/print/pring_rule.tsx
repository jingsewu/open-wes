import schema2component from "@/utils/schema2component"
import {api_print_rule_add, api_print_rule_get} from "@/pages/wms/config_center/constants/api_constant"
import {create_update_columns, enable_options} from "@/utils/commonContants"
import {api_crud_search} from "@/pages/constantApi"

const form = [
    {
        type: "tabs",
        tabs: [
            {
                title: "form.tab.basicInformation",
                controls: [
                    {
                        type: "input-text",
                        name: "id",
                        label: "ID",
                        hidden: true
                    },
                    {
                        type: "input-text",
                        name: "ruleName",
                        label: "printRule.ruleName",
                        required: true
                    },
                    {
                        type: "input-text",
                        name: "ruleCode",
                        label: "printRule.ruleCode",
                        required: true
                    },
                    {
                        type: "select",
                        name: "module",
                        label: "printRule.module",
                        required: true,
                        source: "/api/enum/module-enum"
                    },
                    {
                        type: "select",
                        name: "printNode",
                        label: "printRule.printNode",
                        required: true,
                        source: "/api/enum/print-node-enum"
                    },
                    {
                        type: "input-number",
                        name: "printCount",
                        label: "printRule.printCount",
                        value: 1
                    },
                    {
                        type: "input-text",
                        name: "templateCode",
                        label: "printRule.templateCode",
                        required: true
                    },
                    {
                        type: "textarea",
                        name: "sqlScript",
                        label: "printRule.sqlScript"
                    },
                    {
                        type: "switch",
                        name: "enabled",
                        label: "printRule.enabled",
                        options: enable_options
                    }
                ]
            },
            {
                title: "form.tab.conditionSettings",
                controls: [
                    {
                        type: "checkboxes",
                        name: "ownerCodes",
                        label: "printRule.ownerCodes",
                        source: "/api/owners",
                        multiple: true
                    },
                    {
                        type: "checkboxes",
                        name: "salesPlatforms",
                        label: "printRule.salesPlatforms",
                        source: "/api/sales-platforms",
                        multiple: true
                    },
                    {
                        type: "checkboxes",
                        name: "carrierCodes",
                        label: "printRule.carrierCodes",
                        source: "/api/carriers",
                        multiple: true
                    },
                    {
                        type: "checkboxes",
                        name: "inboundOrderTypes",
                        label: "printRule.inboundOrderTypes",
                        source: "/api/order-types/inbound",
                        multiple: true
                    },
                    {
                        type: "checkboxes",
                        name: "outboundOrderTypes",
                        label: "printRule.outboundOrderTypes",
                        source: "/api/order-types/outbound",
                        multiple: true
                    }
                ]
            }
        ]
    }
]

const add = {
    type: "button",
    actionType: "drawer",
    icon: "fa fa-plus",
    label: "button.add",
    target: "printRule",
    drawer: {
        title: "button.add",
        closeOnEsc: true,
        body: {
            type: "form",
            api: api_print_rule_add,
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
        name: "ruleName",
        label: "printRule.ruleName",
        searchable: true
    },
    {
        name: "ruleCode",
        label: "printRule.ruleCode",
        searchable: true
    },
    {
        name: "module",
        label: "printRule.module",
        type: "mapping",
        source: "${dictionary.Module}"
    },
    {
        name: "printNode",
        label: "printRule.printNode",
        type: "mapping",
        source: "${dictionary.PrintNode}"

    },
    {
        name: "enabled",
        label: "printRule.enabled",
        type: "switch",
        options: enable_options
    },
    ...create_update_columns
]

const searchIdentity = "PrintRule"
const showColumns = columns

const schema = {
    type: "page",
    title: "printRule.title",
    toolbar: [],
    data: {
        dictionary: "${ls:dictionary}"
    },
    body: [
        {
            type: "crud",
            syncLocation: false,
            name: "PPrintRule",
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
                                    initApi: api_print_rule_get,
                                    api: api_print_rule_add,
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
