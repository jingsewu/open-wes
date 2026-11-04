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
    numberColumn
} from "@/utils/amis/presets"

const audit = auditColumns({
    visible: true,
    seconds: true,
    createdSearchable: {
        type: "input-date-range",
        valueFormat: "x"
    }
}).map((column) => ({
    ...column,
    dbField: `wcst.${String(column.name).replace(
        /[A-Z]/g,
        (letter) => `_${letter.toLowerCase()}`
    )}`
}))

const columns = [
    {
        dbField: "wcst.id",
        name: "id",
        label: "ID",
        hidden: true
    },
    {
        dbField: "wcst.container_stock_id",
        name: "containerStockId",
        label: "明细库存ID",
        hidden: true
    },
    {
        ...codeColumn("barcode", "skuArea.barcode", { searchable: true }),
        dbField: "msbd.bar_code"
    },
    {
        ...codeColumn("skuCode", "table.skuCode"),
        dbField: "msmd.sku_code"
    },
    {
        dbField: "msmd.sku_name",
        name: "skuName",
        label: "table.skuName"
    },
    {
        ...codeColumn("orderNo", "table.orderNo", { searchable: true }),
        dbField: "wcst.order_no"
    },
    {
        dbField: "wcst.sku_batch_stock_id",
        name: "skuBatchStockId",
        label: "table.lotNumber",
        hidden: true
    },
    {
        ...codeColumn("containerCode", "table.containerCode"),
        dbField: "wcst.container_code",
        hidden: true
    },
    {
        ...codeColumn("sourceContainerCode", "table.sourceContainerNumber"),
        dbField: "wcst.source_container_code"
    },
    {
        ...codeColumn(
            "sourceContainerSlotCode",
            "table.sourceContainerLattice"
        ),
        dbField: "wcst.source_container_slot_code"
    },
    {
        ...codeColumn("targetContainerCode", "table.targetContainerNumber"),
        dbField: "wcst.target_container_code"
    },
    {
        ...codeColumn(
            "targetContainerSlotCode",
            "table.targetContainerLattice"
        ),
        dbField: "wcst.target_container_slot_code"
    },
    {
        dbField: "wcst.task_id",
        name: "taskId",
        label: "任务id",
        hidden: true
    },
    {
        ...numberColumn("transferQty", "table.quantity"),
        dbField: "wcst.transfer_qty"
    },
    {
        dbField: "wcst.operation_task_type",
        name: "operationTaskType",
        label: "table.currentOperationType",
        type: "mapping",
        source: "${dictionary.OperationTaskType}"
    },
    {
        dbField: "wcst.version",
        name: "version",
        label: "版本号",
        hidden: true
    },
    {
        dbField: "wcst.warehouse_code",
        name: "warehouseCode",
        label: "table.warehouseCode",
        hidden: true
    },
    ...audit
]

const schema = crudPage({
    title: "wms.menu.inventoryRecords",
    name: "stockRecordsTable",
    searchIdentity: "WContainerStockTransaction",
    api: api_crud_search_by_warehouseCode,
    columns,
    searchObject: {
        tables:
            "w_container_stock_transaction wcst " +
            "left join m_sku_barcode_data msbd on msbd.sku_id = wcst.sku_id " +
            "left join m_sku_main_data msmd on msmd.id = wcst.sku_id",
        orderBy: "wcst.update_time desc"
    },
    tableActions: [
        exportButton({
            api: api_crud_search_by_warehouseCode_total,
            filename: "stock_records"
        })
    ],
    empty: "emptyState.stockRecords"
})

export default schema2component(schema)
