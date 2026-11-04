import schema2component from "@/utils/schema2component"
import { volume } from "@/pages/wms/config_center/constants/form_constants"
import {
    api_container_spec_add,
    api_container_spec_delete,
    api_container_spec_get,
    api_container_spec_update
} from "@/pages/wms/config_center/constants/api_constant"
import {
    api_crud_search_by_warehouseCode,
    api_crud_search_by_warehouseCode_total
} from "@/pages/constantApi"
import {
    addButton,
    auditColumns,
    codeColumn,
    crudPage,
    deleteButton,
    editButton,
    exportButton,
    formDrawer,
    numberColumn
} from "@/utils/amis/presets"

const warehouseCode = localStorage.getItem("warehouseCode")

const formBody = [
    {
        type: "hidden",
        name: "id"
    },
    {
        type: "hidden",
        name: "version"
    },
    {
        label: "table.specificationCode",
        type: "input-text",
        name: "containerSpecCode",
        required: true
    },
    {
        label: "table.specificationName",
        type: "input-text",
        name: "containerSpecName",
        required: true
    },
    {
        label: "table.containerType",
        type: "select",
        name: "containerType",
        source: "${dictionary.ContainerType}",
        required: true
    },
    {
        type: "hidden",
        name: "warehouseCode",
        label: "table.warehouseCode",
        value: warehouseCode
    },
    ...volume,
    {
        type: "input-table",
        name: "containerSlotSpecs",
        addable: true,
        editable: true,
        removable: true,
        needConfirm: false,
        columns: [
            ...volume,
            {
                name: "containerSlotSpecCode",
                label: "table.containerLatticeSlogan",
                type: "input-text",
                required: true
            },
            {
                name: "face",
                label: "table.faceWallCoding",
                type: "input-text",
                value: "",
                hiddenOn:
                    "this.containerType == 'CONTAINER' || this.containerType == 'TRANSFER_CONTAINER' ",
                requiredOn: "this.containerType == 'SHELF'"
            },
            {
                name: "level",
                label: "workLocationArea.layer",
                type: "input-text",
                required: true
            },
            {
                name: "bay",
                label: "table.column",
                type: "input-number",
                required: true
            },
            {
                name: "locLevel",
                label: "table.locLevel",
                type: "input-number",
                required: true
            },
            {
                name: "locBay",
                label: "table.locBay",
                type: "input-number",
                required: true
            }
        ]
    }
]

const addDrawer = formDrawer({
    title: "containerSpec.addTitle",
    size: "xl",
    columnCount: 1,
    body: formBody,
    api: api_container_spec_add,
    reload: "ContainerSpecTable"
})

const editDrawer = formDrawer({
    title: "containerSpec.editTitle",
    size: "xl",
    columnCount: 1,
    context: "${containerSpecCode} · ${containerSpecName}",
    body: formBody,
    initApi: api_container_spec_get,
    api: api_container_spec_update,
    reload: "ContainerSpecTable"
})

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
    codeColumn("containerSpecCode", "table.specificationCode", {
        searchable: true
    }),
    {
        name: "containerSpecName",
        label: "table.specificationName",
        searchable: true
    },
    {
        name: "containerType",
        label: "table.containerType",
        type: "mapping",
        source: "${dictionary.ContainerType}",
        searchable: {
            type: "select",
            source: "${dictionary.ContainerType}"
        }
    },
    numberColumn("volume", "table.volumeMm3"),
    numberColumn("length", "table.lengthMm"),
    numberColumn("width", "table.widthMm"),
    numberColumn("height", "table.heightMm"),
    ...auditColumns()
]

const schema = crudPage({
    title: "containerSpec.title",
    name: "ContainerSpecTable",
    searchIdentity: "WContainerSpec",
    api: api_crud_search_by_warehouseCode,
    columns,
    orderBy: "update_time desc",
    pageActions: [
        addButton({ target: "ContainerSpecTable", drawer: addDrawer })
    ],
    tableActions: [
        exportButton({
            api: api_crud_search_by_warehouseCode_total,
            filename: "container_spec"
        })
    ],
    rowActions: [
        editButton(editDrawer),
        deleteButton({
            api: api_container_spec_delete,
            confirmText: "confirm.deleteContainerSpec",
            reload: "ContainerSpecTable"
        })
    ],
    empty: "emptyState.containerSpec"
})

export default schema2component(schema)
