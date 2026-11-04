import schema2component from "@/utils/schema2component"
import {
    owner_code,
    warehouse_area_code
} from "@/pages/wms/constants/select_search_api_contant"
import {
    api_crud_search_by_warehouseCode,
    api_crud_search_by_warehouseCode_total
} from "@/pages/constantApi"
import {
    auditColumns,
    codeColumn,
    crudPage,
    exportButton,
    numberColumn
} from "@/utils/amis/presets"

const permissions = localStorage.getItem("permissions")?.split(",")

const form = [
    {
        type: "hidden",
        name: "id"
    },
    {
        type: "hidden",
        name: "version"
    }
]

const audit = auditColumns({
    seconds: true,
    createdSearchable: {
        type: "input-date-range",
        valueFormat: "x"
    }
}).map((column) => ({
    ...column,
    dbField: `k.${String(column.name).replace(
        /[A-Z]/g,
        (letter) => `_${letter.toLowerCase()}`
    )}`
}))

const columns = [
    {
        name: "id",
        label: "ID",
        hidden: true,
        dbField: "k.id"
    },
    {
        name: "version",
        label: "Version",
        hidden: true,
        dbField: "k.version"
    },
    {
        ...codeColumn("skuCode", "skuArea.skuCode", { searchable: true }),
        dbField: "a.sku_code"
    },
    {
        ...codeColumn("barcode", "skuArea.barcode", { searchable: true }),
        dbField: "f.bar_code"
    },
    {
        name: "ownerCode",
        label: "skuArea.ownerCode",
        dbField: "a.owner_code",
        source: owner_code,
        searchable: { type: "select", source: owner_code }
    },
    {
        name: "brand",
        label: "table.brand",
        dbField: "a.brand"
    },
    {
        name: "style",
        label: "table.style",
        dbField: "a.style"
    },
    {
        name: "color",
        label: "table.color",
        dbField: "a.color"
    },
    {
        name: "size",
        label: "table.size",
        dbField: "a.size"
    },
    {
        name: "skuFirstCategory",
        label: "table.levelOneClassification",
        dbField: "a.sku_first_category"
    },
    {
        name: "skuAttributeCategory",
        label: "table.first-level_attributes",
        dbField: "a.sku_attribute_category"
    },
    {
        name: "skuAttributes",
        label: "table.batchAttributes",
        dbField: "e.sku_attributes"
    },
    {
        name: "warehouseAreaCode",
        label: "workLocationArea.warehouseArea",
        dbField: "wa.warehouse_area_code",
        source: warehouse_area_code,
        searchable: { type: "select", source: warehouse_area_code }
    },
    {
        ...codeColumn("containerCode", "table.containerCode", {
            searchable: true
        }),
        dbField: "k.container_code"
    },
    {
        ...codeColumn("containerSlotCode", "table.containerSlotCode"),
        dbField: "k.container_slot_code"
    },
    {
        ...numberColumn("totalQty", "table.inventoryQuantity"),
        dbField: "k.total_qty"
    },
    {
        ...numberColumn("availableQty", "table.availableQuantity"),
        dbField: "k.available_qty"
    },
    {
        ...numberColumn("noOutboundLockedQty", "table.libraryOccupancy"),
        dbField: "k.no_outbound_locked_qty"
    },
    {
        ...numberColumn("outboundLockedQty", "table.pickingOccupancy"),
        dbField: "k.outbound_locked_qty"
    },
    {
        ...numberColumn("frozenQty", "table.frozenQuantity"),
        dbField: "k.frozen_qty"
    },
    ...audit,
    {
        name: "warehouseCode",
        label: "table.warehouseCode",
        dbField: "k.warehouse_code",
        hidden: true
    }
]

const freezeButton = {
    label: "button.freeze",
    type: "button",
    level: "link",
    className: "owes-action",
    actionType: "dialog",
    dialog: {
        title: "button.freeze",
        size: "sm",
        closeOnEsc: true,
        closeOnOutside: true,
        body: {
            type: "form",
            api: "post:/wms/containerStock/freezeContainerStock",
            controls: [
                ...form,
                {
                    label: "table.quantity",
                    type: "input-number",
                    name: "qty",
                    min: 1,
                    max: "${availableQty}",
                    required: true
                }
            ]
        }
    }
}

const thawButton = {
    label: "button.thaw",
    type: "button",
    level: "link",
    className: "owes-action",
    actionType: "dialog",
    disabledOn: "${frozenQty === 0}",
    dialog: {
        title: "button.thaw",
        size: "sm",
        closeOnEsc: true,
        closeOnOutside: true,
        body: {
            type: "form",
            api: "post:/wms/containerStock/unfreezeContainerStock",
            controls: [
                ...form,
                {
                    label: "table.quantity",
                    type: "input-number",
                    name: "qty",
                    min: 1,
                    max: "${frozenQty}",
                    required: true
                }
            ]
        }
    }
}

const rowActions = [
    ...(permissions?.includes("freeze") ? [freezeButton] : []),
    ...(permissions?.includes("thaw") ? [thawButton] : [])
]

const schema = crudPage({
    title: "inventoryDetails.title",
    name: "containerStockTable",
    searchIdentity: "WContainerStock",
    api: api_crud_search_by_warehouseCode,
    columns,
    searchObject: {
        tables:
            "w_container_stock k left join w_sku_batch_stock b on k.sku_batch_stock_id = b.id  left join w_sku_batch_attribute e on k.sku_batch_attribute_id = e.id  left join w_warehouse_area wa on b.warehouse_area_id = wa.id  left join m_sku_main_data a on e.sku_id = a.id left join m_sku_barcode_data f  on a.id = f.sku_id",
        where: "k.total_qty > 0 and k.container_id is not null"
    },
    tableActions: [
        exportButton({
            api: api_crud_search_by_warehouseCode_total,
            filename: "container_stock"
        })
    ],
    ...(rowActions.length ? { rowActions } : {}),
    empty: "emptyState.containerStock"
})

export default schema2component(schema)
