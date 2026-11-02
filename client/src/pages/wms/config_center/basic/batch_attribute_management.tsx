import schema2component from "@/utils/schema2component"
import {
    enable_options,
    true_false_options
} from "@/utils/commonContants"
import { owner_code } from "@/pages/wms/constants/select_search_api_contant"
import {
    api_batch_attribute_add,
    api_batch_attribute_get,
    api_batch_attribute_update
} from "@/pages/wms/config_center/constants/api_constant"
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
        type: "select",
        name: "ownerCode",
        label: "table.productOwner",
        source: owner_code
    },
    {
        label: "table.SKUCategories",
        type: "select",
        name: "skuFirstCategory",
        source: "${dictionary.SkuFirstCategory}"
    },
    {
        name: "enable",
        type: "switch",
        label: "table.whetherEnabled"
    },
    {
        type: "input-table",
        name: "batchAttributeFieldConfigs",
        addable: true,
        editable: true,
        removable: true,
        showFooterAddBtn: false,
        value: [
            {
                fieldCode: "INBOUND_DATE",
                fieldName: "table.inboundDate"
            },
            {
                fieldCode: "PRODUCT_DATE",
                fieldName: "table.dateOfManufacture"
            },
            {
                fieldCode: "EXPIRED_DATE",
                fieldName: "table.ExpirationDate"
            }
        ],
        columns: [
            {
                name: "fieldCode",
                label: "table.batchAttributeCode",
                type: "input-text",
                required: true
            },
            {
                name: "fieldName",
                label: "table.batchAttributes",
                type: "input-text"
            },
            {
                name: "required",
                label: "table.required",
                type: "switch",
                options: true_false_options
            },
            {
                name: "keyAttribute",
                label: "table.keyAttributes",
                type: "switch",
                options: true_false_options
            },
            {
                name: "exactMatch",
                label: "table.preciseMatch",
                type: "switch",
                options: true_false_options
            },
            {
                name: "enable",
                label: "table.enable",
                type: "switch",
                options: enable_options
            }
        ]
    }
]

const addDrawer = formDrawer({
    title: "batchManagement.addTitle",
    size: "lg",
    columnCount: 1,
    body: form,
    api: api_batch_attribute_add,
    reload: "batchAttributeTable"
})

const editDrawer = formDrawer({
    title: "batchManagement.editTitle",
    size: "lg",
    columnCount: 1,
    context: "${code} · ${name}",
    body: form,
    initApi: api_batch_attribute_get,
    api: api_batch_attribute_update,
    reload: "batchAttributeTable"
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
        source: owner_code,
        searchable: {
            type: "select",
            source: owner_code
        }
    },
    {
        name: "skuFirstCategory",
        label: "table.SKUCategories",
        type: "mapping",
        source: "${dictionary.SkuFirstCategory}",
        searchable: {
            type: "select",
            source: "${dictionary.SkuFirstCategory}"
        }
    },
    {
        name: "enable",
        label: "table.enable",
        type: "mapping",
        map: enable_options
    },
    ...auditColumns()
]

const schema = crudPage({
    title: "batchManagement.title",
    name: "batchAttributeTable",
    searchIdentity: "MBatchAttributeConfig",
    api: api_crud_search,
    columns,
    orderBy: "update_time desc",
    pageActions: [addButton({ target: "batchAttributeTable", drawer: addDrawer })],
    rowActions: [editButton(editDrawer)],
    empty: "emptyState.batchAttribute"
})

export default schema2component(schema)
