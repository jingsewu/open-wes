const recordColumns = [
    {
        name: "id",
        dbField: "k.id",
        label: "盘点记录ID",
        hidden: true
    },
    {
        dbField: "k.stocktake_order_id",
        name: "stocktakeOrderId",
        label: "盘点单ID",
        hidden: true
    },
    {
        dbField: "a.sku_code",
        name: "skuCode",
        label: "skuArea.skuCode"
    },
    {
        dbField: "a.sku_name",
        name: "skuName",
        label: "skuArea.productName"
    },
    {
        dbField: "k.container_code",
        name: "containerCode",
        label: "table.containerCode"
    },
    {
        dbField: "k.container_face",
        name: "containerFace",
        label: "workLocationArea.face"
    },
    {
        dbField: "k.container_slot_code",
        name: "containerSlotCode",
        label: "table.containerSlotCode"
    },
    {
        dbField: "k.qty_original",
        name: "qtyOriginal",
        label: "table.inventoryQuantity"
    },
    {
        dbField: "k.qty_stocktake",
        name: "qtyStocktake",
        label: "table.countQty"
    },
    {
        dbField: "k.stocktake_record_status",
        name: "stocktakeRecordStatus",
        label: "table.status",
        type: "mapping",
        source: "${StocktakeRecordStatus}"
    }
]

export const recordDialog = {
    title: "button.inventoryRecord",
    actions: [],
    closeOnEsc: true,
    closeOnOutside: true,
    size: "xl",
    body: [
        {
            type: "crud",
            syncLocation: false,
            name: "stocktakeRecordTable",
            api: {
                method: "POST",
                url: "/search/search?page=${page}&perPage=${perPage}&stocktakeOrderId=${id}&stocktakeOrderId-op=eq",
                dataType: "application/json"
            },
            defaultParams: {
                searchIdentity: "WStocktakeRecord",
                showColumns: recordColumns,
                searchObject: {
                    tables: "w_stocktake_record k  inner join m_sku_main_data a on k.sku_id = a.id"
                }
            },
            footerToolbar: ["switch-per-page", "statistics", "pagination"],
            columns: recordColumns
        }
    ]
}
