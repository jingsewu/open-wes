import {
    codeColumn,
    nestedTable,
    numberColumn,
    statusColumn
} from "@/utils/amis/presets"
import { stocktakeRecordStatus } from "@/utils/amis/statusMaps"

const recordColumns = [
    {
        name: "id",
        dbField: "k.id",
        label: "ID",
        hidden: true
    },
    {
        name: "stocktakeOrderId",
        dbField: "k.stocktake_order_id",
        label: "ID",
        hidden: true
    },
    {
        ...codeColumn("skuCode", "skuArea.skuCode"),
        dbField: "a.sku_code"
    },
    {
        name: "skuName",
        dbField: "a.sku_name",
        label: "skuArea.productName"
    },
    {
        ...codeColumn("containerCode", "table.containerCode"),
        dbField: "k.container_code"
    },
    {
        name: "containerFace",
        dbField: "k.container_face",
        label: "workLocationArea.face"
    },
    {
        name: "containerSlotCode",
        dbField: "k.container_slot_code",
        label: "table.containerSlotCode"
    },
    {
        ...numberColumn("qtyOriginal", "table.inventoryQuantity"),
        dbField: "k.qty_original"
    },
    {
        ...numberColumn("qtyStocktake", "table.countQty"),
        dbField: "k.qty_stocktake"
    },
    {
        ...statusColumn(
            "stocktakeRecordStatus",
            "table.status",
            stocktakeRecordStatus
        ),
        dbField: "k.stocktake_record_status"
    }
]

export const recordDrawer = {
    size: "xl",
    title: "button.inventoryRecord",
    closeOnEsc: true,
    closeOnOutside: true,
    actions: [
        {
            type: "button",
            actionType: "close",
            label: "button.close"
        }
    ],
    body: [
        {
            type: "tpl",
            tpl: "${orderNo}",
            className: "owes-drawer-context"
        },
        nestedTable({
            name: "stocktakeRecordTable",
            api: {
                method: "POST",
                url: "/search/search?page=${page}&perPage=${perPage}&stocktakeOrderId=${id}&stocktakeOrderId-op=eq",
                dataType: "application/json"
            },
            searchIdentity: "WStocktakeRecord",
            columns: recordColumns,
            searchObject: {
                tables:
                    "w_stocktake_record k inner join m_sku_main_data a on k.sku_id = a.id"
            },
            empty: "emptyState.stocktakeRecord"
        })
    ]
}
