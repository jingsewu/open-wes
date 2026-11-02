import { codeColumn, numberColumn } from "@/utils/amis/presets"

const detailColumns = [
    {
        name: "inboundPlanOrderId",
        label: "ID",
        hidden: true
    },
    codeColumn("skuCode", "table.skuCode"),
    {
        name: "skuName",
        label: "table.skuName"
    },
    codeColumn("ownerCode", "table.productOwner"),
    {
        name: "batchAttributes",
        label: "table.batchAttributes"
    },
    codeColumn("boxNo", "table.lpnNumber"),
    {
        name: "brand",
        label: "table.brand"
    },
    {
        name: "color",
        label: "table.color"
    },
    {
        name: "size",
        label: "table.size"
    },
    {
        name: "style",
        label: "table.style"
    },
    numberColumn("qtyAbnormal", "skuArea.qtyAbnormal"),
    numberColumn("qtyAccepted", "table.acceptanceQuantity"),
    numberColumn("qtyRestocked", "table.plannedQuantity"),
    {
        name: "responsibleParty",
        label: "table.responsibleParty"
    }
]

export const detailDrawer = {
    title: "table.inboundPlanDetails",
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
            name: "inboundPlanOrderDetailTable",
            api: {
                method: "POST",
                url: "/search/search?page=${page}&perPage=${perPage}&inboundPlanOrderId=${id}&inboundPlanOrderId-op=eq",
                dataType: "application/json"
            },
            defaultParams: {
                searchIdentity: "WInboundPlanOrderDetail",
                showColumns: detailColumns
            },
            footerToolbar: ["switch-per-page", "statistics", "pagination"],
            columns: detailColumns
        }
    ]
}
