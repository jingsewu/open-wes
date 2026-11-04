import schema2component from "@/utils/schema2component"
import { enable_options } from "@/utils/commonContants"
import { warehouse_area_id } from "@/pages/wms/constants/select_search_api_contant"
import { api_warehouse_area_logic_add } from "@/pages/wms/config_center/constants/api_constant"
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
        label: "table.reservoirAreaName",
        name: "warehouseAreaId",
        type: "select",
        source: warehouse_area_id,
        required: true
    },
    {
        label: "table.logicalAreaEncoding",
        name: "warehouseLogicCode",
        type: "input-text",
        required: true
    },
    {
        label: "table.logicalAreaName",
        name: "warehouseLogicName",
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
        value: true,
        options: enable_options,
        required: true
    },
    {
        label: "table.warehouseCode",
        name: "warehouseCode",
        type: "hidden",
        value: warehouseCode
    }
]

const addDrawer = formDrawer({
    title: "logicalAreaManagement.addTitle",
    size: "md",
    columnCount: 1,
    body: form,
    api: api_warehouse_area_logic_add,
    reload: "role"
})

const editDrawer = formDrawer({
    title: "logicalAreaManagement.editTitle",
    size: "md",
    columnCount: 1,
    context: "${warehouseLogicCode} · ${warehouseLogicName}",
    body: form,
    api: api_warehouse_area_logic_add,
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
    {
        name: "warehouseAreaId",
        label: "table.reservoirAreaName",
        type: "mapping",
        source: warehouse_area_id,
        searchable: {
            type: "select",
            source: warehouse_area_id
        }
    },
    codeColumn("warehouseLogicCode", "table.logicalAreaEncoding", {
        searchable: true
    }),
    {
        name: "warehouseLogicName",
        label: "table.logicalAreaName"
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
    title: "logicalAreaManagement.title",
    name: "role",
    searchIdentity: "WWarehouseLogic",
    api: api_crud_search_by_warehouseCode,
    columns,
    orderBy: "update_time desc",
    pageActions: [addButton({ target: "role", drawer: addDrawer })],
    rowActions: [editButton(editDrawer)],
    empty: "emptyState.logicalAreaManagement"
})

export default schema2component(schema)
