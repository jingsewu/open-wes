import { codeColumn, nestedTable, numberColumn } from "@/utils/amis/presets"

export const searchDetailIdentity = "WOutboundPlanOrderDetail"

export const detailColumns = [
    {
        name: "outboundPlanOrderId",
        dbField: "a.outbound_plan_order_id",
        label: "ID",
        hidden: true
    },
    {
        name: "id",
        dbField: "a.id",
        label: "ID",
        hidden: true
    },
    {
        name: "customerOrderNo",
        dbField: "b.customer_order_no",
        label: "table.customerOrderNo",
        hidden: true
    },
    {
        name: "customerWaveNo",
        dbField: "b.customer_wave_no",
        label: "table.customerWaveNo",
        hidden: true
    },
    {
        ...codeColumn("ownerCode", "table.productOwner"),
        dbField: "a.owner_code"
    },
    {
        name: "batchAttributes",
        dbField: "a.batch_attributes",
        label: "table.batchAttributes"
    },
    {
        ...numberColumn("qtyActual", "table.actualOutboundQuantity"),
        dbField: "a.qty_actual"
    },
    {
        ...numberColumn("qtyRequired", "table.plannedOutboundQuantity"),
        dbField: "a.qty_required"
    },
    {
        ...codeColumn("skuCode", "table.skuCode"),
        dbField: "a.sku_code"
    },
    {
        name: "skuName",
        dbField: "a.sku_name",
        label: "table.skuName"
    }
]

export const detailDrawer = {
    size: "xl",
    title: "outboundOrder.detail.modal.title",
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
            name: "OutboundPlanOrderDetailTable",
            api: {
                method: "POST",
                url: "/search/search?page=${page}&perPage=${perPage}&outboundPlanOrderId=${id}&outboundPlanOrderId-op=eq",
                dataType: "application/json"
            },
            searchIdentity: searchDetailIdentity,
            columns: detailColumns,
            searchObject: {
                tables: "w_outbound_plan_order_detail a left join w_outbound_plan_order b on a.outbound_plan_order_id = b.id"
            },
            empty: "emptyState.outboundPlanDetail"
        })
    ]
}
