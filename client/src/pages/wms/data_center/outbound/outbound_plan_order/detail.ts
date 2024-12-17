export const searchDetailIdentity = "WOutboundPlanOrderDetail"

export const detailColumns = [
    {
        name: "outboundPlanOrderId",
        dbField: "a.outbound_plan_order_id",
        label: "出库通知单ID",
        hidden: true
    },
    {
        name: "id",
        dbField: "a.id",
        label: "出库计划单明细ID",
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
        name: "ownerCode",
        dbField: "a.owner_code",
        label: "table.productOwner",
        searchable: true
    },
    {
        name: "batchAttributes",
        dbField: "a.batch_attributes",
        label: "table.batchAttributes"
    },
    {
        name: "qtyActual",
        dbField: "a.qty_actual",
        label: "table.actualOutboundQuantity"
    },
    {
        name: "qtyRequired",
        dbField: "a.qty_required",
        label: "table.plannedOutboundQuantity"
    },
    {
        name: "skuCode",
        dbField: "a.sku_code",
        label: "table.skuCode"
    },
    {
        name: "skuName",
        dbField: "a.sku_name",
        label: "table.skuName"
    }
]

export const detailDialog = {
    title: "outboundOrder.detail.modal.title",
    actions: [],
    closeOnEsc: true,
    closeOnOutside: true,
    size: "xl",
    body: [
        {
            type: "crud",
            syncLocation: false,
            name: "OutboundPlanOrderDetailTable",
            api: {
                method: "POST",
                url: "/search/search?page=${page}&perPage=${perPage}&outboundPlanOrderId=${id}&outboundPlanOrderId-op=eq",
                dataType: "application/json"
            },
            defaultParams: {
                searchIdentity: searchDetailIdentity,
                showColumns: detailColumns,
                searchObject: {
                    tables: "w_outbound_plan_order_detail a left join w_outbound_plan_order b on a.outbound_plan_order_id = b.id"
                }
            },
            footerToolbar: ["switch-per-page", "statistics", "pagination"],
            columns: detailColumns
        }
    ]
}
