const detailColumns = [
    {
        name: "stocktakeOrderId",
        label: "盘点单ID",
        hidden: true
    },
    {
        name: "stocktakeUnitType",
        label: "table.stocktakeUnitType"
    },
    {
        name: "unitCode",
        label: "table.unitCode"
    },
    {
        name: "unitId",
        label: "table.unitId"
    }
]

export const detailDialog = {
    title: "inventoryCounting.detail.modal.title",
    actions: [],
    closeOnEsc: true,
    closeOnOutside: true,
    size: "xl",
    body: [
        {
            type: "crud",
            syncLocation: false,
            name: "stocktakeOrderDetailTable",
            api: {
                method: "POST",
                url: "/search/search?page=${page}&perPage=${perPage}&stocktakeOrderId=${id}&stocktakeOrderId-op=eq",
                dataType: "application/json"
            },
            defaultParams: {
                searchIdentity: "WStocktakeOrderDetail",
                showColumns: detailColumns
            },
            footerToolbar: ["switch-per-page", "statistics", "pagination"],
            columns: detailColumns
        }
    ]
}
