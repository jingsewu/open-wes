import schema2component from "@/utils/schema2component"
import {
    api_crud_search_by_warehouseCode,
    api_crud_search_by_warehouseCode_total
} from "@/pages/constantApi"
import {
    auditColumns,
    codeColumn,
    crudPage,
    dateTimeColumn,
    detailButton,
    exportButton,
    nestedTable,
    numberColumn,
    statusColumn
} from "@/utils/amis/presets"
import { putAwayTaskStatus } from "@/utils/amis/statusMaps"

const columns = [
    {
        name: "id",
        label: "ID",
        hidden: true
    },
    {
        name: "warehouseCode",
        label: "table.warehouseCode",
        hidden: true
    },
    codeColumn("containerCode", "table.containerNumber"),
    {
        name: "containerSpecCode",
        label: "table.containerSpecificationNumber"
    },
    codeColumn("locationCode", "table.locationCode"),
    codeColumn("taskNo", "table.putAwayTaskNo"),
    {
        name: "taskType",
        label: "table.putAwayMethod",
        type: "mapping",
        source: "${dictionary.PutAwayTaskType}"
    },
    statusColumn("taskStatus", "table.putAwayStatus", putAwayTaskStatus),
    {
        name: "workStationId",
        label: "station.operatingStation"
    },
    ...auditColumns({ seconds: true, createdOnly: true }),
    dateTimeColumn("updateTime", "table.completionTime", {
        seconds: true,
        toggled: true
    })
]

const detailColumns = [
    {
        name: "putAwayTaskId",
        hidden: true
    },
    codeColumn("ownerCode", "table.productOwner"),
    codeColumn("skuCode", "skuArea.skuCode"),
    {
        name: "skuName",
        label: "skuArea.productName"
    },
    numberColumn("qtyPutAway", "table.qtyPutAway"),
    codeColumn("containerCode", "table.containerCode"),
    codeColumn("containerSlotCode", "table.containerLatticeSlogan")
]

const detailDrawer = {
    size: "xl",
    title: "putAwayTask.detail.modal.title",
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
            tpl: "${taskNo}",
            className: "owes-drawer-context"
        },
        nestedTable({
            name: "PutAwayTaskDetailTable",
            api: {
                method: "POST",
                url: "/search/search?page=${page}&perPage=${perPage}&putAwayTaskId=${id}&putAwayTaskId-op=eq",
                dataType: "application/json"
            },
            searchIdentity: "WPutAwayTaskDetail",
            columns: detailColumns,
            empty: "emptyState.putAwayTaskDetail"
        })
    ]
}

const filters = [
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
]

const schema = crudPage({
    title: "wms.menu.putAwayTasks",
    name: "PutAwayTaskTable",
    searchIdentity: "WPutAwayTask",
    api: api_crud_search_by_warehouseCode,
    columns,
    filters,
    orderBy: "task_status, update_time desc",
    pageActions: [],
    tableActions: [
        exportButton({
            api: api_crud_search_by_warehouseCode_total,
            filename: "put_away_task",
            method: "POST",
            defaultParams: {
                searchIdentity: "WPutAwayTask",
                showColumns: columns
            }
        })
    ],
    rowActions: [detailButton(detailDrawer)],
    empty: "emptyState.putAwayTask"
})

export default schema2component(schema)
