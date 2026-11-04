import schema2component from "@/utils/schema2component"
import { enable_options } from "@/utils/commonContants"
import { api_warehouse_area_group_add } from "@/pages/wms/config_center/constants/api_constant"
import { api_crud_search_by_warehouseCode } from "@/pages/constantApi"
import {
    addButton,
    auditColumns,
    codeColumn,
    crudPage,
    editButton,
    formDrawer,
    remarkColumn
} from "@/utils/amis/presets"

const warehouseCode = localStorage.getItem("warehouseCode")

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
        label: "table.warehouseAreaEncoding",
        name: "warehouseAreaGroupCode",
        type: "input-text",
        required: true
    },
    {
        label: "table.warehouseAreaName",
        name: "warehouseAreaGroupName",
        type: "input-text",
        required: true
    },
    {
        label: "table.remark",
        name: "remark",
        type: "input-text"
    },
    {
        label: "table.status",
        name: "enable",
        type: "switch",
        options: enable_options
    },
    {
        label: "table.warehouseCode",
        name: "warehouseCode",
        type: "hidden",
        value: warehouseCode
    }
]

const addDrawer = formDrawer({
    title: "warehouseAreaManagement.addTitle",
    size: "md",
    columnCount: 1,
    body: form,
    api: api_warehouse_area_group_add,
    reload: "role"
})

const editDrawer = formDrawer({
    title: "warehouseAreaManagement.editTitle",
    size: "md",
    columnCount: 1,
    context: "${warehouseAreaGroupCode} · ${warehouseAreaGroupName}",
    body: form,
    api: api_warehouse_area_group_add,
    reload: "role"
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
    {
        name: "warehouseCode",
        label: "table.warehouseCode",
        hidden: true
    },
    codeColumn("warehouseAreaGroupCode", "table.warehouseAreaEncoding", {
        searchable: true
    }),
    {
        name: "warehouseAreaGroupName",
        label: "table.warehouseAreaName"
    },
    remarkColumn(),
    {
        name: "enable",
        label: "table.status",
        type: "mapping",
        map: enable_options
    },
    ...auditColumns()
]

const schema = crudPage({
    title: "warehouseAreaManagement.title",
    name: "role",
    searchIdentity: "WWarehouseAreaGroup",
    api: api_crud_search_by_warehouseCode,
    columns,
    orderBy: "update_time desc",
    pageActions: [addButton({ target: "role", drawer: addDrawer })],
    rowActions: [editButton(editDrawer)],
    empty: "emptyState.warehouseAreaManagement"
})

export default schema2component(schema)
