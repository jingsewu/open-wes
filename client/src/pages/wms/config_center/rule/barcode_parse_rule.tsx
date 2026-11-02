import schema2component from "@/utils/schema2component"
import { owner_code_barcode_analysis } from "@/pages/wms/constants/select_search_api_contant"
import {
    api_barcode_parse_rule_add,
    api_barcode_parse_rule_get,
    api_barcode_parse_rule_update
} from "@/pages/wms/config_center/constants/api_constant"
import {
    enable_options,
    true_false_options
} from "@/utils/commonContants"
import { api_crud_search } from "@/pages/constantApi"
import {
    addButton,
    auditColumns,
    codeColumn,
    crudPage,
    editButton,
    formDrawer
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
        source: "${dictionary.ExecuteTime}",
        required: true
    },
    {
        label: "table.businessModules",
        type: "select",
        name: "businessFlow",
        source: "${dictionary.BusinessFlow}",
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
        source: "${dictionary.UnionLocation}"
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
            source: "${dictionary.ParserObject}"
        }
    },
    {
        name: "enable",
        type: "switch",
        label: "table.whetherEnabled",
        options: enable_options
    }
]

const addDrawer = formDrawer({
    title: "barcodeParsingManagement.addTitle",
    size: "xl",
    body: form,
    api: api_barcode_parse_rule_add,
    reload: "barcodeParseRule"
})

const editDrawer = formDrawer({
    title: "barcodeParsingManagement.editTitle",
    size: "xl",
    context: "${code} · ${name}",
    body: form,
    initApi: api_barcode_parse_rule_get,
    api: api_barcode_parse_rule_update,
    reload: "barcodeParseRule"
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
    codeColumn("code", "table.ruleEncoding", { searchable: true }),
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
        source: "${dictionary.ExecuteTime}",
        searchable: {
            type: "select",
            source: "${dictionary.ExecuteTime}"
        }
    },
    {
        name: "enable",
        label: "table.whetherEnabled",
        type: "mapping",
        map: true_false_options
    },
    ...auditColumns()
]

const schema = crudPage({
    title: "barcodeParsingManagement.title",
    name: "barcodeParseRule",
    searchIdentity: "MBarcodeParseRule",
    api: api_crud_search,
    columns,
    orderBy: "update_time desc",
    pageActions: [addButton({ target: "barcodeParseRule", drawer: addDrawer })],
    rowActions: [editButton(editDrawer)],
    empty: "emptyState.barcodeParsingManagement"
})

export default schema2component(schema)
