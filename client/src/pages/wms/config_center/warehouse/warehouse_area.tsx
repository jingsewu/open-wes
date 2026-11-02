import schema2component from "@/utils/schema2component"
import { enable_options } from "@/utils/commonContants"
import { warehouse_area_group } from "@/pages/wms/constants/select_search_api_contant"
import { api_warehouse_area_add } from "@/pages/wms/config_center/constants/api_constant"
import { api_crud_search_by_warehouseCode } from "@/pages/constantApi"
import {
    addButton,
    auditColumns,
    codeColumn,
    crudPage,
    editButton,
    formDrawer,
    numberColumn,
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
        label: "table.warehouseAreaGroupName",
        name: "warehouseGroupCode",
        type: "select",
        source: warehouse_area_group,
        required: true
    },
    {
        label: "table.reservoirAreaCode",
        name: "warehouseAreaCode",
        type: "input-text",
        required: true
    },
    {
        label: "table.reservoirAreaName",
        name: "warehouseAreaName",
        type: "input-text",
        required: true
    },
    {
        label: "table.reservoirAreaType",
        name: "warehouseAreaType",
        type: "select",
        source: "${dictionary.WarehouseAreaType}",
        required: true
    },
    {
        label: "table.reservoirAreaPurpose",
        name: "warehouseAreaUse",
        type: "select",
        source: "${dictionary.WarehouseAreaUse}",
        required: true
    },
    {
        label: "table.reservoirAreaWorkType",
        name: "warehouseAreaWorkType",
        type: "select",
        source: "${dictionary.WarehouseAreaWorkType}",
        required: true
    },
    {
        label: "table.floor",
        name: "level",
        type: "input-number"
    },
    {
        label: "table.temperatureLimits",
        name: "temperatureLimit",
        type: "input-number"
    },
    {
        label: "table.humidityLimitation",
        name: "wetLimit",
        type: "input-number"
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
    title: "reservoirAreaManagement.addTitle",
    size: "lg",
    body: form,
    api: api_warehouse_area_add,
    reload: "role"
})

const editDrawer = formDrawer({
    title: "reservoirAreaManagement.editTitle",
    size: "lg",
    context: "${warehouseAreaCode} · ${warehouseAreaName}",
    body: form,
    api: api_warehouse_area_add,
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
        name: "warehouseGroupCode",
        label: "table.warehouseAreaGroupName",
        type: "mapping",
        source: warehouse_area_group,
        searchable: {
            type: "select",
            source: warehouse_area_group
        }
    },
    codeColumn("warehouseAreaCode", "table.reservoirAreaCode", {
        searchable: true
    }),
    {
        name: "warehouseAreaName",
        label: "table.reservoirAreaName"
    },
    {
        name: "warehouseAreaType",
        label: "table.reservoirAreaType",
        type: "mapping",
        source: "${dictionary.WarehouseAreaType}"
    },
    {
        name: "warehouseAreaUse",
        label: "table.reservoirAreaPurpose",
        type: "mapping",
        source: "${dictionary.WarehouseAreaUse}"
    },
    {
        name: "warehouseAreaWorkType",
        label: "table.reservoirAreaWorkType",
        type: "mapping",
        source: "${dictionary.WarehouseAreaWorkType}",
        searchable: {
            type: "select",
            source: "${dictionary.WarehouseAreaWorkType}"
        }
    },
    numberColumn("level", "table.floor"),
    numberColumn("temperatureLimit", "table.temperatureLimits"),
    numberColumn("wetLimit", "table.humidityLimitation"),
    remarkColumn(),
    {
        name: "enable",
        label: "table.status",
        type: "mapping",
        map: enable_options,
        searchable: {
            type: "select",
            options: enable_options
        }
    },
    ...auditColumns()
]

const schema = crudPage({
    title: "reservoirAreaManagement.title",
    name: "role",
    searchIdentity: "WWarehouseArea",
    api: api_crud_search_by_warehouseCode,
    columns,
    orderBy: "update_time desc",
    pageActions: [addButton({ target: "role", drawer: addDrawer })],
    rowActions: [editButton(editDrawer)],
    empty: "emptyState.reservoirAreaManagement"
})

export default schema2component(schema)
