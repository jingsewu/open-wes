import { codeColumn, nestedTable } from "@/utils/amis/presets"

const detailColumns = [
    {
        name: "stocktakeOrderId",
        label: "ID",
        hidden: true
    },
    {
        name: "stocktakeUnitType",
        label: "table.stocktakeUnitType"
    },
    codeColumn("unitCode", "table.unitCode"),
    {
        name: "unitId",
        label: "table.unitId"
    }
]

export const detailDrawer = {
    size: "xl",
    title: "inventoryCounting.detail.modal.title",
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
            name: "stocktakeOrderDetailTable",
            api: {
                method: "POST",
                url: "/search/search?page=${page}&perPage=${perPage}&stocktakeOrderId=${id}&stocktakeOrderId-op=eq",
                dataType: "application/json"
            },
            searchIdentity: "WStocktakeOrderDetail",
            columns: detailColumns,
            empty: "emptyState.stocktakeDetail"
        })
    ]
}
