import schema2component from "@/utils/schema2component"
import { api_crud_search_by_warehouseCode } from "@/pages/constantApi"
import {
    auditColumns,
    codeColumn,
    crudPage,
    detailButton,
    linkAction,
    nestedTable,
    numberColumn,
    statusColumn
} from "@/utils/amis/presets"
import { stockAdjustmentOrderStatus } from "@/utils/amis/statusMaps"

const columns = [
    {
        name: "id",
        label: "ID",
        hidden: true
    },
    codeColumn("orderNo", "table.adjustmentOrderNo", { searchable: true }),
    statusColumn("status", "table.status", {
        ...stockAdjustmentOrderStatus,
        searchable: {
            type: "select",
            source: "${dictionary.StockAdjustmentOrderStatus}"
        }
    }),
    ...auditColumns()
]

const detailColumns = [
    {
        dbField: "d.id",
        name: "id",
        label: "ID",
        hidden: true
    },
    {
        ...codeColumn("containerCode", "table.containerNumber"),
        dbField: "d.container_code"
    },
    {
        ...codeColumn("containerSlotCode", "table.containerLatticeSlogan"),
        dbField: "d.container_slot_code"
    },
    {
        ...codeColumn("skuCode", "skuArea.skuCode"),
        dbField: "d.sku_code"
    },
    {
        dbField: "g.bar_code_list",
        name: "barCodeList",
        label: "skuArea.barcode"
    },
    {
        ...numberColumn("qtyAdjustment", "table.adjustQuantity"),
        dbField: "d.qty_adjustment"
    },
    {
        dbField: "d.sku_id",
        name: "skuId",
        label: "商品id",
        hidden: true
    },
    {
        dbField: "d.stock_abnormal_record_id",
        name: "stockAbnormalRecordId",
        label: "库存异常单ID",
        hidden: true
    },
    {
        dbField: "d.stock_adjustment_order_id",
        name: "stockAdjustmentOrderId",
        label: "库存调整单ID",
        hidden: true
    },
    {
        dbField: "d.container_stock_id",
        name: "containerStockId",
        label: "库存id",
        hidden: true
    }
]

const detailDrawer = {
    size: "xl",
    title: "modal.stockAdjustmentOrderDetails",
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
            name: "stockAdjustmentDetailTable",
            api: {
                method: "POST",
                url: "/search/search?page=${page}&perPage=${perPage}&createTime-op=bt&stockAdjustmentOrderId=${id}&stockAdjustmentOrderId-op=eq",
                dataType: "application/json"
            },
            searchIdentity: "WStockAdjustmentDetail",
            columns: detailColumns,
            searchObject: {
                tables:
                    "w_stock_adjustment_detail d left join (select sku_id, group_concat(distinct bar_code separator ' ') bar_code_list from m_sku_barcode_data group by sku_id) g on d.sku_id = g.sku_id"
            },
            empty: "emptyState.stockAdjustDetail"
        })
    ]
}

const schema = crudPage({
    title: "wms.menu.inventoryAdjustment",
    name: "stockAdjustmentTable",
    searchIdentity: "WStockAdjustmentOrder",
    api: api_crud_search_by_warehouseCode,
    columns,
    orderBy: "create_time desc",
    pageActions: [],
    rowActions: [
        detailButton(detailDrawer),
        linkAction({
            label: "button.adjustInventory",
            confirmTitle: "toast.prompt",
            confirmText: "toast.confirmAjust",
            disabledOn: "${status !== 'NEW'}",
            api: {
                method: "post",
                url: "/wms/stock/adjustment/adjust",
                data: ["${id}"]
            },
            reload: "stockAdjustmentTable"
        }),
        linkAction({
            label: "button.close",
            confirmTitle: "toast.prompt",
            confirmText: "toast.confirmToClose",
            disabledOn: "${status !== 'NEW'}",
            danger: true,
            api: {
                method: "post",
                url: "/wms/stock/adjustment/close",
                data: ["${id}"]
            },
            reload: "stockAdjustmentTable"
        })
    ],
    empty: "emptyState.stockAdjust"
})

export default schema2component(schema)
