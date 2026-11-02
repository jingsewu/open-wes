import schema2component from "@/utils/schema2component"
import {
    api_crud_search_by_warehouseCode_total,
    api_crud_search_by_warehouseCode
} from "@/pages/constantApi"

const columns = [
    {
        name: "id",
        label: "ID",
        hidden: true
    },
    {
        name: "warehouseCode",
        label: "仓库",
        hidden: true
    },
    {
        name: "containerCode",
        label: "table.containerNumber"
    },
    {
        name: "containerSpecCode",
        label: "table.containerSpecificationNumber"
    },
    {
        name: "locationCode",
        label: "table.locationCode"
    },
    {
        name: "taskNo",
        label: "table.putAwayTaskNo"
    },
    {
        name: "taskType",
        label: "table.putAwayMethod",
        type: "mapping",
        source: "${dictionary.PutAwayTaskType}"
    },
    {
        name: "taskStatus",
        label: "table.putAwayStatus",
        type: "mapping",
        source: "${dictionary.PutAwayTaskStatus}"
    },
    {
        name: "workStationId",
        label: "station.operatingStation"
    },
    {
        label: "table.createdBy",
        name: "createUser"
    },
    {
        name: "createTime",
        label: "table.creationTime",
        tpl: "${createTime/1000|date:YYYY-MM-DD HH\\:mm\\:ss}"
    },
    {
        name: "updateTime",
        label: "table.completionTime",
        // tpl: "${auditTime/1000|date:YYYY-MM-DD HH\\:mm\\:ss}",
        tpl: "${updateTime/1000|date:YYYY-MM-DD HH\\:mm\\:ss}"
    }
]

const detailColumns = [
    {
        name: "ownerCode",
        label: "table.productOwner"
    },
    {
        name: "putAwayTaskId",
        label: "上架单ID",
        hidden: true
    },
    {
        name: "skuCode",
        label: "skuArea.skuCode"
    },
    {
        name: "skuName",
        label: "skuArea.productName"
    },
    {
        name: "qtyPutAway",
        label: "table.qtyPutAway"
    },
    {
        name: "containerCode",
        label: "table.containerCode"
    },
    {
        name: "containerSlotCode",
        label: "table.containerLatticeSlogan"
    }
]

const searchIdentity = "WPutAwayTask"
const searchDetailIdentity = "WPutAwayTaskDetail"
const showColumns = columns

const showDetailColumns = detailColumns

const detailDialog = {
    // title: "table.inboundPlanDetails",
    title: "putAwayTask.detail.modal.title",
    actions: [],
    closeOnEsc: true,
    closeOnOutside: true,
    size: "xl",
    body: [
        {
            type: "crud",
            syncLocation: false,
            name: "putAwayTaskDetailTable",
            api: {
                method: "POST",
                url: "/search/search?page=${page}&perPage=${perPage}&putAwayTaskId=${id}&putAwayTaskId-op=eq",
                dataType: "application/json"
            },
            defaultParams: {
                searchIdentity: searchDetailIdentity,
                showColumns: showDetailColumns,
                searchObject: {
                    tables: "w_put_away_task_detail"
                }
            },
            footerToolbar: ["switch-per-page", "statistics", "pagination"],
            columns: detailColumns
        }
    ]
}

const schema = {
    type: "page",
    title: "wms.menu.putAwayTasks",
    toolbar: [],
    data: {
        dictionary: "${ls:dictionary}"
    },
    body: [
        {
            type: "crud",
            syncLocation: false,
            name: "putAwayTaskTable",
            api: api_crud_search_by_warehouseCode,
            defaultParams: {
                searchIdentity: searchIdentity,
                showColumns: showColumns,
                searchObject: {
                    tables: "w_put_away_task",
                    orderBy: "task_status, update_time desc"
                }
            },
            autoFillHeight: true,
            filterTogglable: true,
            filterDefaultVisible: true,
            filter: {
                title: "common.filter",
                mode: "inline",
                body: [
                    {
                        label: "table.putAwayTaskNo",
                        name: "taskNo",
                        type: "input-text"
                    },
                    {
                        label: "table.containerNumber",
                        name: "containerCode",
                        type: "input-text"
                    },
                    {
                        label: "table.containerSpecificationNumber",
                        name: "containerSpecCode",
                        type: "input-text"
                    },
                    {
                        label: "station.operatingStation",
                        name: "workStationId",
                        type: "input-text"
                    },
                    {
                        label: "table.putAwayStatus",
                        name: "taskStatus",
                        type: "select",
                        source: "${dictionary.PutAwayTaskStatus}"
                    },
                    {
                        label: "table.putAwayMethod",
                        name: "taskType",
                        type: "select",
                        source: "${dictionary.PutAwayTaskType}"
                    },
                    {
                        label: "table.creationTime",
                        name: "createTime",
                        type: "input-date-range",
                        valueFormat: "x"
                    },
                    {
                        label: "table.completionTime",
                        name: "updateTime",
                        type: "input-date-range",
                        valueFormat: "x"
                    }
                ],
                actions: [
                    { type: "reset", label: "button.reset" },
                    {
                        type: "submit",
                        level: "primary",
                        label: "common.search"
                    }
                ]
            },
            headerToolbar: [
                "reload",
                {
                    type: "export-excel",
                    label: "button.export",
                    method: "POST",
                    api: api_crud_search_by_warehouseCode_total,
                    filename: "put_away_task",
                    defaultParams: {
                        searchIdentity: searchIdentity,
                        showColumns: showColumns
                    }
                }
            ],
            footerToolbar: ["switch-per-page", "statistics", "pagination"],
            placeholder: "emptyState.putAwayTask",
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
