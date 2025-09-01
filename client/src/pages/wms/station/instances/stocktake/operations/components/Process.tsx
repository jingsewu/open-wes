import schema2component from "@/utils/schema2component"
import {debounce} from "lodash"
import {DEBOUNCE_TIME} from "@/pages/wms/station/constants/constant"
import {CustomActionType} from "@/pages/wms/station/instances/stocktake/customActionType"
import {api_crud_search_by_warehouseCode} from "@/pages/constantApi"

const columns = [
    {
        name: "id",
        label: "ID",
        hidden: true
    },
    {
        name: "orderNo",
        label: "table.countOrderNumber",
        searchable: true
    },
    {
        name: "stocktakeTaskStatus",
        label: "table.status",
        source: "${dictionary.StocktakeTaskStatus}",
        type: "mapping"
    },
    {
        label: "table.createdBy",
        name: "createUser"
    },
    {
        name: "createTime",
        label: "table.creationTime",
        tpl: "${createTime/1000|date:YYYY-MM-DD HH\\:mm\\:ss}",
        searchable: {
            type: "input-date-range",
            valueFormat: "x"
        }
    },
    {
        label: "table.modifiedBy",
        name: "updateUser"
    },
    {
        label: "table.updated",
        name: "updateTime",
        tpl: "${updateTime/1000|date:YYYY-MM-DD HH\\:mm\\:ss}"
    }
]

const detailColumns = [
    {
        name: "id",
        label: "明细ID",
        hidden: true
    },
    {
        name: "stocktakeTaskId",
        label: "任务ID",
        hidden: true
    },
    {
        name: "barCodeList",
        label: "skuArea.barcode"
    },
    {
        name: "skuName",
        label: "skuArea.productName"
    },
    {
        name: "warehouseCode",
        label: "table.warehouse"
    },
    {
        name: "containerCode",
        label: "table.containerCode"
    },
    {
        name: "containerFace",
        label: "workLocationArea.face"
    },
    {
        name: "containerSlotCode",
        label: "table.containerSlotCode"
    },
    {
        name: "totalQty",
        label: "table.inventoryQuantity"
    },
    {
        name: "stocktakeTaskDetailStatus",
        label: "table.status",
        type: "mapping",
        source: "${dictionary.StocktakeTaskDetailStatus}"
    }
]

const detailDialog = {
    title: "inventoryCounting.detail.modal.title",
    actions: [],
    closeOnEsc: true,
    closeOnOutside: true,
    size: "full",
    body: [
        {
            type: "crud",
            syncLocation: false,
            name: "stocktakeTaskDetailTable",
            api: {
                method: "POST",
                url: "/search/search?page=${page}&perPage=${perPage}&stocktakeTaskId-eq=${id}",
                dataType: "application/json"
            },
            defaultParams: {
                searchIdentity: "findStocktakeTaskDetailByTaskId"
            },
            footerToolbar: ["switch-per-page", "statistics", "pagination"],
            columns: detailColumns
        }
    ]
}

const schema = {
    type: "page",
    data: {
        dictionary: "${ls:dictionary}"
    },
    body: {
        type: "crud",
        syncLocation: false,
        name: "stocktakeTaskTable",
        api: api_crud_search_by_warehouseCode,
        defaultParams: {
            searchIdentity: "WStocktakeTaskDetail",
            showColumns: detailColumns
        },
        keepItemSelectionOnPageChange: true,
        autoFillHeight: true,
        autoGenerateFilter: {
            columnsNum: 3,
            showBtnToolbar: true
        },
        headerToolbar: ["reload"],
        bulkActions: [
            {
                label: "button.receiveInBatches",
                level: "primary",
                close: true,
                onClick: debounce(
                    async (e: any, props: any) => {
                        const {onActionDispatch} = props
                        const {code} = await onActionDispatch({
                            eventCode: CustomActionType.STOCKTAKE_EXECUTE_TASK,
                            data: {
                                taskIds: props.data.ids.split(",")
                            }
                        })
                    },
                    DEBOUNCE_TIME,
                    {leading: false}
                ),
                className: "batchTakeOrders"
            }
        ],
        footerToolbar: [
            "switch-per-page",
            "statistics",
            "pagination",
            {
                type: "bulkActions",
                align: "right"
            }
        ],
        rowClassName: "takeOrderTableRow",
        columns: [
            ...columns,
            {
                label: "button.detail",
                type: "button",
                level: "link",
                actionType: "dialog",
                dialog: detailDialog
            }
        ]
    }
}

export default schema2component(schema)
