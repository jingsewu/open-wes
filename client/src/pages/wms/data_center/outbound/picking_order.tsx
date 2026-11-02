import schema2component from "@/utils/schema2component"
import { api_crud_search_by_warehouseCode } from "@/pages/constantApi"
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
import { pickingOrderStatus } from "@/utils/amis/statusMaps"

const columns = [
    {
        name: "id",
        label: "ID",
        hidden: true
    },
    codeColumn("warehouseCode", "table.warehouse"),
    codeColumn("pickingOrderNo", "table.pickOrderNumber", {
        searchable: true
    }),
    {
        name: "assignedStationSlot",
        label: "table.assignedLattice"
    },
    codeColumn("waveNo", "table.waveNumber"),
    statusColumn("pickingOrderStatus", "table.status", {
        ...pickingOrderStatus,
        searchable: {
            type: "select",
            source: "${dictionary.PickingOrderStatus}"
        }
    }),
    numberColumn("priority", "table.priority"),
    ...auditColumns()
]

const detailColumns = [
    {
        name: "pickingOrderId",
        label: "ID",
        hidden: true,
        dbField: "k.picking_order_id"
    },
    {
        name: "warehouseAreaCode",
        label: "workLocationArea.warehouseArea",
        dbField: "wa.warehouse_area_code"
    },
    {
        ...codeColumn("ownerCode", "table.productOwner"),
        dbField: "k.owner_code"
    },
    {
        ...codeColumn("skuCode", "table.skuCode"),
        dbField: "a.sku_code"
    },
    {
        name: "skuName",
        label: "table.skuName",
        dbField: "a.sku_name"
    },
    {
        ...numberColumn("requiredQty", "table.planCount"),
        dbField: "t.required_qty"
    },
    {
        ...numberColumn("operatedQty", "table.actualCountQuantity"),
        dbField: "t.operated_qty"
    },
    {
        ...numberColumn("abnormalQty", "skuArea.qtyAbnormal"),
        dbField: "t.abnormal_qty"
    },
    {
        name: "stationCode",
        label: "table.picking.outboundStation",
        dbField: "ws.station_code"
    },
    {
        ...codeColumn("targetLocationCode", "table.picking.outboundStationSlot"),
        dbField: "t.target_location_code"
    },
    {
        ...codeColumn("sourceContainerCode", "table.containerCode"),
        dbField: "t.source_container_code"
    },
    {
        ...codeColumn("sourceContainerSlot", "table.containerSlotCode"),
        dbField: "t.source_container_slot"
    },
    {
        name: "batchAttributes",
        label: "table.batchAttributes",
        dbField: "k.batch_attributes"
    },
    {
        name: "updateUser",
        label: "table.picking.operator",
        dbField: "t.update_user"
    },
    {
        ...dateTimeColumn("updateTime", "table.completionTime", {
            seconds: true
        }),
        dbField: "t.update_time"
    }
]

const searchDetailIdentity = "WPickingOperationTask"

const detailSearchObject = {
    tables: "w_operation_task t, w_picking_order_detail k, m_sku_main_data a, w_picking_order p, w_warehouse_area wa, w_work_station ws",
    where: "t.detail_id = k.id and t.sku_id = a.id and t.order_id = p.id and p.warehouse_area_id = wa.id and t.work_station_id = ws.id"
}

const detailDrawer = {
    size: "xl",
    title: "picking.detail.title",
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
            tpl: "${pickingOrderNo}",
            className: "owes-drawer-context"
        },
        nestedTable({
            name: "PickingOperationTaskTable",
            api: {
                method: "POST",
                url: "/search/search?page=${page}&perPage=${perPage}&pickingOrderId=${id}&pickingOrderId-op=eq",
                dataType: "application/json"
            },
            searchIdentity: searchDetailIdentity,
            columns: detailColumns,
            searchObject: detailSearchObject,
            empty: "emptyState.pickingTaskDetail"
        })
    ]
}

const schema = crudPage({
    title: "pickingTasks.title",
    name: "PickingOrderTable",
    searchIdentity: "WPickingOrder",
    api: api_crud_search_by_warehouseCode,
    columns,
    orderBy: "update_time desc",
    pageActions: [],
    tableActions: [
        exportButton({
            type: "export-csv",
            api: {
                method: "POST",
                url: "/search/search?page=${1}&perPage=${100000}&createTime-op=bt&warehouseCode-op=eq&warehouseCode=${ls:warehouseCode}",
                dataType: "application/json",
                data: {
                    searchIdentity: searchDetailIdentity,
                    showColumns: detailColumns,
                    searchObject: detailSearchObject
                }
            },
            filename: "picking_order_tasks",
            method: "POST",
            exportColumns: detailColumns.map((column) => ({ ...column }))
        })
    ],
    rowActions: [detailButton(detailDrawer)],
    empty: "emptyState.pickingOrder"
})

export default schema2component(schema)
