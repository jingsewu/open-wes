import { codeColumn } from "@/utils/amis/presets"

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
    title: "inventoryCounting.detail.modal.title",
    actions: [],
    closeOnEsc: true,
    closeOnOutside: true,
    size: "xl",
    body: [
        {
            type: "tpl",
            tpl: "${orderNo}",
            className: "owes-drawer-context"
        },
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
