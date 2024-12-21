import schema2component from "@/utils/schema2component"
import { owner_code_barcode_analysis } from "@/pages/wms/constants/select_search_api_contant"
import {
    api_barcode_parse_rule_add,
    api_barcode_parse_rule_get,
    api_barcode_parse_rule_update
} from "@/pages/wms/config_center/constants/api_constant"
import {
    create_update_columns,
    true_false_options
} from "@/utils/commonContants"
import { api_crud_search } from "@/pages/constantApi"

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
        label: "table.ruleEncoding",
        type: "input-text",
        name: "code",
        required: true
    },
    {
        label: "table.ruleName",
        type: "input-text",
        name: "name",
        required: true
    },
    {
        label: "table.productOwner",
        type: "select",
        name: "ownerCode",
        source: owner_code_barcode_analysis
    },
    {
        label: "table.buriedPoint",
        type: "select",
        name: "executeTime",
        source: "${ls:dictionary|pick:ExecuteTime}",
        required: true
    },
    {
        label: "table.businessModules",
        type: "select",
        name: "businessFlow",
        source: "${ls:dictionary|pick:BusinessFlow}",
        required: true
    },
    {
        label: "table.brand",
        type: "select",
        name: "brand"
    },
    {
        label: "table.stitchingPosition",
        type: "select",
        name: "unionLocation",
        source: "${ls:dictionary|pick:UnionLocation}"
    },
    {
        type: "input-text",
        name: "unionStr",
        placeholder: "table.stitchingContent"
    },
    {
        label: "table.resolutionRules",
        type: "textarea",
        name: "regularExpression",
        required: true
    },
    {
        name: "resultFields",
        label: "table.resolveObjects",
        type: "input-array",
        required: true,
        items: {
            type: "select",
            clearable: false,
            source: "${ls:dictionary|pick:ParserObject}"
        }
    },
    {
        name: "enable",
        type: "switch",
        label: "table.whetherEnabled",
        onText: "启用",
        offText: "禁用",
        "en-US": {
            onText: "Enable",
            offText: "Disabled"
        }
        // offText: <Translation>{(t) => t("table.disabled")}</Translation>
    }
]

const add = {
    type: "button",
    actionType: "dialog",
    icon: "fa fa-plus",
    label: "button.add",
    dialog: {
        name: "addDialog",
        title: "button.add",
        closeOnEsc: true,
        size: "lg",
        body: {
            type: "form",
            api: api_barcode_parse_rule_add,
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
        name: "warehouseCode",
        label: "table.warehouseCode",
        hidden: true
    },
    {
        name: "version",
        label: "Version",
        hidden: true
    },
    {
        name: "code",
        label: "table.ruleEncoding",
        searchable: true
    },
    {
        name: "name",
        label: "table.ruleName",
        searchable: true
    },
    {
        name: "ownerCode",
        label: "table.productOwner",
        type: "mapping",
        source: owner_code_barcode_analysis,
        searchable: {
            type: "select",
            source: owner_code_barcode_analysis
        }
    },
    {
        name: "executeTime",
        label: "table.buriedPoint",
        type: "mapping",
        source: "${ls:dictionary|pick:ExecuteTime}",
        searchable: {
            type: "select",
            source: "${ls:dictionary|pick:ExecuteTime}"
        }
    },
    {
        name: "enable",
        label: "table.whetherEnabled",
        type: "mapping",
        map: true_false_options
    },
    ...create_update_columns
]

const searchIdentity = "MBarcodeParseRule"
const showColumns = columns

const schema = {
    type: "page",
    title: "barcodeParsingManagement.title",
    toolbar: [],
    body: [
        {
            type: "crud",
            syncLocation: false,
            name: "barcodeParseRule",
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
                            actionType: "dialog",
                            dialog: {
                                title: "button.modify",
                                closeOnEsc: true,
                                closeOnOutside: true,
                                size: "lg",
                                body: {
                                    type: "form",
                                    initApi: api_barcode_parse_rule_get,
                                    api: api_barcode_parse_rule_update,
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
