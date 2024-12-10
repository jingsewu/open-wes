import schema2component from "@/utils/schema2component"
import {create_update_columns} from "@/utils/commonContants"

let warehouseCode = localStorage.getItem("warehouseCode")

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
        dbField: "msbd.bar_code",
        name: "barcode",
        label: "skuArea.barcode",
        searchable: true
    },
    {
        dbField: "msmd.sku_code",
        name: "skuCode",
        label: "table.skuCode"
    },
    {
        dbField: "msmd.sku_name",
        name: "skuName",
        label: "table.skuName"
    },
    {
        dbField: "wcst.order_no",
        name: "orderNo",
        label: "table.orderNo",
        searchable: true
    },
    {
        dbField: "wcst.sku_batch_stock_id",
        name: "skuBatchStockId",
        label: "table.lotNumber",
        hidden: true
    },
    {
        dbField: "wcst.container_code",
        name: "containerCode",
        label: "table.containerCode",
        searchable: true,
        hidden: true
    },
    {
        dbField: "wcst.source_container_code",
        name: "sourceContainerCode",
        label: "table.sourceContainerNumber"
    },
    {
        dbField: "wcst.source_container_slot_code",
        name: "sourceContainerSlotCode",
        label: "table.sourceContainerLattice"
    },
    {
        dbField: "wcst.target_container_code",
        name: "targetContainerCode",
        label: "table.targetContainerNumber"
    },
    {
        dbField: "wcst.target_container_slot_code",
        name: "targetContainerSlotCode",
        label: "table.targetContainerLattice"
    },
    {
        dbField: "wcst.task_id",
        name: "taskId",
        label: "任务id",
        hidden: true
    },
    {
        dbField: "wcst.transfer_qty",
        name: "transferQty",
        label: "table.quantity"
    },
    {
        dbField: "wcst.operation_task_type",
        name: "operationTaskType",
        label: "table.currentOperationType",
        type: "mapping",
        source: "${OperationTaskType}"
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
    {
        dbField: "wcst.create_user",
        label: "table.createdBy",
        name: "createUser"
    },
    {
        dbField: "wcst.create_time",
        name: "createTime",
        label: "table.creationTime",
        tpl: "${createTime/1000|date:YYYY-MM-DD HH\\:mm\\:ss}",
        searchable: {
            type: "input-date-range",
            valueFormat: "x"
        }
    },
    {
        dbField: "wcst.update_user",
        label: "table.modifiedBy",
        name: "updateUser"
    },
    {
        dbField: "wcst.update_time",
        label: "table.updated",
        name: "updateTime",
        tpl: "${updateTime/1000|date:YYYY-MM-DD HH\\:mm\\:ss}"
    }
]

const searchIdentity = "WContainerStockTransaction"
const showColumns = columns

const searchObject = {
    tables: "w_container_stock_transaction wcst " +
        "left join m_sku_barcode_data msbd on msbd.sku_id = wcst.sku_id " +
        "left join m_sku_main_data msmd on msmd.id = wcst.sku_id",
    orderBy: "wcst.create_time desc"
}

const schema = {
    type: "page",
    title: "wms.menu.inventoryRecords",
    toolbar: [],
    initApi: "post:/mdm/config/dictionary/getAll",
    body: [
        {
            type: "crud",
            syncLocation: false,
            name: "stockRecordsTable",
            api: {
                method: "POST",
                url:
                    "/search/search?page=${page}&perPage=${perPage}&createTime-op=bt&warehouseCode-op=eq&warehouseCode=" +
                    warehouseCode,
                dataType: "application/json"
            },
            defaultParams: {
                searchIdentity: searchIdentity,
                showColumns: showColumns,
                searchObject: searchObject
            },
            autoFillHeight: true,
            autoGenerateFilter: {
                columnsNum: 3,
                showBtnToolbar: true
            },
            headerToolbar: [
                "reload",
                {
                    type: "export-excel",
                    label: "button.export",
                    api:
                        "post:/search/search?page=${1}&perPage=${100000}&createTime-op=bt&warehouseCode-op=eq&warehouseCode=" +
                        warehouseCode,
                    fileName: "stock_records"
                }
            ],
            footerToolbar: ["switch-per-page", "statistics", "pagination"],
            columns: showColumns
        }
    ]
}

export default schema2component(schema)
