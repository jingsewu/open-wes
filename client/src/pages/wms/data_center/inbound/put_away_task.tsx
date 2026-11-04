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
    codeColumn("containerCode", "table.containerNumber", {
        searchable: true
    }),
    {
        name: "containerSpecCode",
        label: "table.containerSpecificationNumber",
        searchable: true
    },
    codeColumn("locationCode", "table.locationCode"),
    codeColumn("taskNo", "table.putAwayTaskNo", { searchable: true }),
    {
        name: "taskType",
        label: "table.putAwayMethod",
        type: "mapping",
        source: "${dictionary.PutAwayTaskType}",
        searchable: {
            type: "select",
            source: "${dictionary.PutAwayTaskType}"
        }
    },
    statusColumn("taskStatus", "table.putAwayStatus", {
        ...putAwayTaskStatus,
        searchable: {
            type: "select",
            source: "${dictionary.PutAwayTaskStatus}"
        }
    }),
    {
        name: "workStationId",
        label: "station.operatingStation",
        searchable: true
    },
    ...auditColumns({
        seconds: true,
        createdOnly: true,
        createdSearchable: {
            type: "input-date-range",
            valueFormat: "x"
        }
    }),
    dateTimeColumn("updateTime", "table.completionTime", {
        seconds: true,
        toggled: true,
        searchable: {
            type: "input-date-range",
            valueFormat: "x"
        }
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

const schema = crudPage({
    title: "wms.menu.putAwayTasks",
    name: "PutAwayTaskTable",
    searchIdentity: "WPutAwayTask",
    api: api_crud_search_by_warehouseCode,
    columns,
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
