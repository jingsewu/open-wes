import schema2component from "@/utils/schema2component"
import { debounce } from "lodash"
import { DEBOUNCE_TIME } from "@/pages/wms/station/constant"
import request from "@/utils/requestInterceptor"

let warehouseCode = localStorage.getItem("warehouseCode")

const columns = [
    {
        name: "id",
        label: "ID",
        hidden: true
    },
    {
        name: "taskNo",
        label: "table.countOrderNumber",
        searchable: true
    },
    {
        name: "stocktakeTaskStatus",
        label: "table.status",
        source: "${StocktakeTaskStatus}",
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
        name: "stocktakeTaskDetailStatus",
        label: "table.status",
        type: "mapping",
        source: "${StocktakeTaskDetailStatus}"
    }
]

const detailDialog = {
    title: "inventoryCounting.detail.modal.title",
    actions: [],
    closeOnEsc: true,
    closeOnOutside: true,
    size: "xl",
    body: [
        {
            type: "crud",
            syncLocation: false,
            name: "stocktakeTaskDetailTable",
            api: {
                method: "POST",
                url: "/search/search?page=${page}&perPage=${perPage}&&stocktakeTaskId=${id}&stocktakeTaskId-op=eq",
                dataType: "application/json"
            },
            defaultParams: {
                searchIdentity: "WStocktakeTaskDetail",
                showColumns: detailColumns
            },
            footerToolbar: ["switch-per-page", "statistics", "pagination"],
            columns: detailColumns
        }
    ]
}

const schema = {
    type: "page",
    title: "modal.receiveInventoryList",
    toolbar: [],
    initApi: "post:/mdm/config/dictionary/getAll",
    body: [
        {
            type: "crud",
            syncLocation: false,
            name: "stocktakeTaskTable",
            api: {
                method: "POST",
                url:
                    "/search/search?page=${page}&perPage=${perPage}&warehouseCode-eq=" +
                    warehouseCode,
                dataType: "application/json"
            },
            defaultParams: {
                searchIdentity: "WStocktakeTask",
                showColumns: columns
            },
            autoFillHeight: true,
            autoGenerateFilter: {
                columnsNum: 3,
                showBtnToolbar: true
            },
            headerToolbar: ["reload", "bulkActions"],
            bulkActions: [
                {
                    type: "button",
                    label: "button.receiveInBatches",
                    level: "primary",
                    className: "batchTakeOrders",
                    onClick: debounce(
                        async (e: any, props: any) => {
                            request({
                                method: "post",
                                url: "/wms/stocktake/order/receive",
                                headers: {
                                    "Content-Type": "application/json"
                                },
                                data: {
                                    stocktakeTaskIds: props.data.ids.split(","),
                                    workStationId:
                                        props.workStationEvent.workStationId
                                }
                            })
                                .then((res: any) => {
                                    if (res.status === 200) {
                                        props.setModalVisible(false)
                                    }
                                })
                                .catch((error) => {
                                    console.log("error", error)
                                })
                        },
                        DEBOUNCE_TIME,
                        { leading: false }
                    )
                }
            ],
            footerToolbar: ["switch-per-page", "statistics", "pagination"],
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
    ]
}

export default schema2component(schema)
