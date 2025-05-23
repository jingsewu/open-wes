import schema2component from "@/utils/schema2component"
import {api_print_template_add, api_print_template_get} from "@/pages/wms/config_center/constants/api_constant"
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
                        name: "templateCode",
                        label: "printTemplate.templateCode",
                        required: true
                    },
                    {
                        type: "input-text",
                        name: "templateName",
                        label: "printTemplate.templateName",
                        required: true
                    },
                    {
                        type: "switch",
                        name: "enabled",
                        label: "printTemplate.enabled",
                        options: enable_options
                    }
                ]
            },
            {
                title: "form.tab.templateContent",
                controls: [
                    {
                        type: "tabs",
                        tabs: [
                            {
                                title: "form.tab.editor",
                                controls: [
                                    {
                                        type: "editor",
                                        name: "templateContent",
                                        label: "printTemplate.templateContent",
                                        language: "html",
                                        required: true,
                                        options: {
                                            minimap: {
                                                enabled: false
                                            },
                                            lineNumbers: "on",
                                            theme: "vs-dark"
                                        },
                                        size: "xxl"
                                    }
                                ]
                            },
                            {
                                title: "form.tab.preview",
                                controls: [
                                    {
                                        type: "html",
                                        name: "templateContent",
                                        html: "<div>${templateContent}</div>"
                                    }
                                ]
                            }
                        ]
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
    target: "printTemplate",
    drawer: {
        title: "button.add",
        closeOnEsc: true,
        body: {
            type: "form",
            api: api_print_template_add,
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
        name: "templateCode",
        label: "printTemplate.templateCode",
        searchable: true
    },
    {
        name: "templateName",
        label: "printTemplate.templateName",
        searchable: true
    },
    {
        name: "enabled",
        label: "printTemplate.enabled",
        type: "switch",
        options: enable_options
    },
    ...create_update_columns
]

const searchIdentity = "PrintTemplate"
const showColumns = columns

const schema = {
    type: "page",
    title: "printTemplate.title",
    toolbar: [],
    data: {
        dictionary: "${ls:dictionary}"
    },
    body: [
        {
            type: "crud",
            syncLocation: false,
            name: "PPrintTemplate",
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
                                    initApi: api_print_template_get,
                                    api: api_print_template_add,
                                    controls: form
                                }
                            }
                        },
                        {
                            label: "button.preview",
                            type: "button",
                            actionType: "dialog",
                            dialog: {
                                title: "printTemplate.preview",
                                size: "lg",
                                body: {
                                    type: "html",
                                    html: "<div>${templateContent}</div>"
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
