import { codeColumn, nestedTable, numberColumn } from "@/utils/amis/presets"

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
    size: "xl",
    title: "table.inboundPlanDetails",
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
            name: "InboundPlanOrderDetailTable",
            api: {
                method: "POST",
                url: "/search/search?page=${page}&perPage=${perPage}&inboundPlanOrderId=${id}&inboundPlanOrderId-op=eq",
                dataType: "application/json"
            },
            searchIdentity: "WInboundPlanOrderDetail",
            columns: detailColumns,
            empty: "emptyState.inboundPlanDetail"
        })
    ]
}
