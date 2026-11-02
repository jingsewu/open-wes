import schema2component from "@/utils/schema2component"
import {
    warehouse_area_id,
    warehouse_logic_id
} from "@/pages/wms/constants/select_search_api_contant"
import { true_false_options } from "@/utils/commonContants"
import { api_crud_search_by_warehouseCode } from "@/pages/constantApi"
import {
    addButton,
    auditColumns,
    codeColumn,
    crudPage,
    editButton,
    formDrawer,
    statusColumn
} from "@/utils/amis/presets"
import { locationStatus } from "@/utils/amis/statusMaps"

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
        name: "warehouseAreaCode",
        type: "select",
        source: warehouse_area_id
    },
    {
        label: "table.warehouseLogicName",
        name: "warehouseLogicId",
        type: "select",
        source: warehouse_logic_id
    },
    {
        label: "table.roadwayCoding",
        name: "aisleCode",
        type: "input-text"
    },
    {
        label: "table.locationType",
        name: "locationType",
        type: "select",
        source: "${dictionary.LocationType}"
    },
    {
        label: "table.locationCode",
        name: "locationCode",
        type: "input-text"
    },
    {
        label: "table.locationHeat",
        name: "heat",
        type: "input-text"
    },
    {
        label: "table.occupation",
        name: "occupied",
        type: "switch",
        options: true_false_options
    },
    {
        label: "table.locationStatus",
        name: "locationStatus",
        type: "select",
        source: "${dictionary.LocationStatus}"
    },
    {
        label: "table.warehouseCode",
        name: "warehouseCode",
        type: "hidden",
        value: warehouseCode
    }
]

const addDrawer = formDrawer({
    title: "locationManagement.addTitle",
    size: "lg",
    body: form,
    api: { url: "/wms/warehouseLogic/create" },
    reload: "role"
})

const editDrawer = formDrawer({
    title: "locationManagement.editTitle",
    size: "lg",
    context: "${locationCode}",
    body: form,
    api: { url: "/wms/warehouseLogic/create" },
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
        searchable: { type: "select", source: warehouse_area_id }
    },
    {
        name: "warehouseLogicId",
        label: "table.logicalAreaName",
        type: "mapping",
        source: warehouse_logic_id,
        searchable: { type: "select", source: warehouse_logic_id }
    },
    codeColumn("aisleCode", "table.roadwayCoding", { searchable: true }),
    codeColumn("shelfCode", "table.shelfCoding", { searchable: true }),
    codeColumn("locationCode", "table.locationCode", { searchable: true }),
    {
        name: "locationType",
        label: "table.locationType",
        type: "mapping",
        source: "${dictionary.LocationType}"
    },
    {
        name: "heat",
        label: "table.locationHeat"
    },
    {
        name: "occupied",
        label: "table.occupation",
        type: "mapping",
        map: true_false_options
    },
    {
        name: "position",
        hidden: true
    },
    statusColumn("locationStatus", "table.locationStatus", locationStatus),
    ...auditColumns()
]

const schema = crudPage({
    title: "locationManagement.title",
    name: "role",
    searchIdentity: "WLocation",
    api: api_crud_search_by_warehouseCode,
    columns,
    pageActions: [addButton({ target: "role", drawer: addDrawer })],
    rowActions: [editButton(editDrawer)],
    empty: "emptyState.locationManagement"
})

export default schema2component(schema)
