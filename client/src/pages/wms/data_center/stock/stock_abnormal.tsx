import schema2component from "@/utils/schema2component"
import {
    api_crud_search_by_warehouseCode,
    api_crud_search_by_warehouseCode_total
} from "@/pages/constantApi"
import {
    auditColumns,
    codeColumn,
    crudPage,
    exportButton,
    linkAction,
    numberColumn,
    statusColumn
} from "@/utils/amis/presets"
import { stockAbnormalStatus } from "@/utils/amis/statusMaps"

const audit = auditColumns().map((column) => ({
    ...column,
    dbField: `r.${String(column.name).replace(
        /[A-Z]/g,
        (letter) => `_${letter.toLowerCase()}`
    )}`
}))

const columns = [
    {
        dbField: "r.id",
        name: "id",
        label: "ID",
        hidden: true
    },
    {
        ...codeColumn("abnormalOrderNo", "table.exceptionTrackingNumber"),
        dbField: "r.order_no"
    },
    {
        dbField: "r.stock_abnormal_type",
        name: "stockAbnormalType",
        label: "table.exceptionType",
        type: "mapping",
        source: "${dictionary.StockAbnormalType}"
    },
    {
        dbField: "r.abnormal_order_no",
        name: "orderNo",
        label: "原始单号",
        hidden: true
    },
    {
        ...codeColumn("replayNo", "table.reviewOrderNumber"),
        dbField: "r.replay_no"
    },
    {
        ...statusColumn(
            "stockAbnormalStatus",
            "table.status",
            stockAbnormalStatus
        ),
        dbField: "r.stock_abnormal_status"
    },
    {
        ...codeColumn("warehouseCode", "table.warehouse"),
        dbField: "r.warehouse_code"
    },
    {
        ...codeColumn("containerCode", "table.containerCode"),
        dbField: "r.container_code"
    },
    {
        ...codeColumn("containerSlotCode", "table.containerSlotCode"),
        dbField: "r.container_slot_code"
    },
    {
        dbField: "g.bar_code_list",
        name: "barCodeList",
        label: "skuArea.barcode"
    },
    {
        ...codeColumn("skuCode", "skuArea.skuCode"),
        dbField: "r.sku_code"
    },
    {
        dbField: "r.sku_id",
        name: "skuId",
        label: "商品id",
        hidden: true
    },
    {
        ...numberColumn("qtyAbnormal", "table.profit"),
        dbField: "r.qty_abnormal"
    },
    {
        dbField: "r.abnormal_reason",
        name: "abnormalReason",
        label: "table.differenceReason",
        type: "mapping",
        source: "${dictionary.StockAbnormalReason}"
    },
    {
        dbField: "r.reason_desc",
        name: "reasonDesc",
        label: "table.causeDescription"
    },
    {
        dbField: "r.container_stock_id",
        name: "containerStockId",
        label: "库存id",
        hidden: true
    },
    {
        dbField: "r.sku_batch_stock_id",
        name: "skuBatchStockId",
        label: "table.lotNumber"
    },
    {
        dbField: "r.sku_batch_attribute_id",
        name: "skuBatchAttributeId",
        label: "批次属性id",
        hidden: true
    },
    ...audit
]

const filters = [
    {
        label: "table.exceptionTrackingNumber",
        name: "abnormalOrderNo",
        type: "input-text"
    },
    {
        label: "table.containerCode",
        name: "containerCode",
        type: "input-text"
    },
    {
        label: "skuArea.barcode",
        name: "barCodeList",
        type: "input-text"
    },
    {
        label: "table.exceptionType",
        name: "stockAbnormalType",
        type: "select",
        source: "${dictionary.StockAbnormalType}"
    },
    {
        label: "table.status",
        name: "stockAbnormalStatus",
        type: "select",
        source: "${dictionary.StockAbnormalStatus}"
    },
    {
        label: "table.creationTime",
        name: "createTime",
        type: "input-date-range",
        valueFormat: "x"
    }
]

const schema = crudPage({
    title: "inventorAnomalies.title",
    name: "stockAbnormalTable",
    searchIdentity: "WStockAbnormalRecord",
    api: api_crud_search_by_warehouseCode,
    columns,
    searchObject: {
        tables:
            "w_stock_abnormal_record r left join (select sku_id, group_concat(distinct bar_code separator ' ') bar_code_list from m_sku_barcode_data group by sku_id) g on r.sku_id = g.sku_id",
        orderBy: "r.create_time desc"
    },
    filters,
    tableActions: [
        exportButton({
            api: api_crud_search_by_warehouseCode_total,
            filename: "stock_abnormal"
        })
    ],
    rowActions: [
        linkAction({
            label: "button.close",
            confirmTitle: "toast.prompt",
            confirmText: "toast.sureToCloseOrder",
            disabledOn: "${stockAbnormalStatus !== 'NEW'}",
            danger: true,
            api: {
                method: "post",
                url: "/wms/stock/abnormal/record/manualClose",
                data: ["${id}"]
            },
            reload: "stockAbnormalTable"
        }),
        linkAction({
            label: "button.adjustInventoryOrder",
            confirmTitle: "toast.prompt",
            confirmText: "toast.confirmGenerateOrder",
            disabledOn: "${stockAbnormalStatus !== 'NEW'}",
            api: {
                method: "post",
                url: "/wms/stock/abnormal/record/createAdjustmentOrder",
                data: ["${id}"]
            },
            reload: "stockAbnormalTable"
        })
    ],
    empty: "emptyState.stockAbnormal"
})

export default schema2component(schema)
