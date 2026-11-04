import schema2component from "@/utils/schema2component"
import { true_false_options } from "@/utils/commonContants"
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
    numberColumn,
    statusColumn
} from "@/utils/amis/presets"
import { outboundPlanOrderStatus } from "@/utils/amis/statusMaps"
import {
    detailColumns,
    detailDrawer,
    searchDetailIdentity
} from "./detail"
import { add } from "./add"

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
    codeColumn("customerOrderNo", "table.customerOrderNo", {
        searchable: true
    }),
    {
        name: "customerOrderType",
        label: "table.orderType",
        type: "mapping",
        source: "${dictionary.CustomerOrderType}",
        searchable: {
            type: "select",
            source: "${dictionary.CustomerOrderType}"
        }
    },
    codeColumn("orderNo", "table.orderNo"),
    codeColumn("customerWaveNo", "table.customerWaveNumber"),
    codeColumn("waveNo", "table.waveNumber"),
    numberColumn("priority", "table.priority"),
    {
        name: "shortOutbound",
        label: "table.shortOut",
        type: "mapping",
        map: true_false_options
    },
    statusColumn("outboundPlanOrderStatus", "table.status", {
        ...outboundPlanOrderStatus,
        searchable: {
            type: "select",
            source: "${dictionary.OutboundPlanOrderStatus}"
        }
    }),
    dateTimeColumn("expiredTime", "table.cut-off_time"),
    numberColumn("skuKindNum", "table.skuTypes"),
    numberColumn("totalQty", "table.totalQuantity"),
    {
        name: "abnormal",
        label: "table.whetherAbnormal",
        type: "mapping",
        map: true_false_options
    },
    {
        name: "abnormalReason",
        label: "skuArea.abnormalCause"
    },
    {
        name: "origPlatformCode",
        label: "table.sourcePlatform"
    },
    {
        name: "carrierCode",
        label: "table.carriers"
    },
    codeColumn("waybillNo", "table.theTrackingNumber"),
    ...auditColumns()
]

const tableActions = [
    exportButton({
        type: "export-csv",
        api: api_crud_search_by_warehouseCode_total,
        filename: "outbound_plan_orders",
        label: "button.exportOrder",
        method: "POST"
    }),
    exportButton({
        type: "export-csv",
        api: {
            method: "POST",
            url: "/search/search?page=${1}&perPage=${100000}&warehouseCode-op=eq&warehouseCode=${ls:warehouseCode}",
            dataType: "application/json",
            data: {
                searchIdentity: searchDetailIdentity,
                showColumns: detailColumns,
                searchObject: {
                    tables: "w_outbound_plan_order_detail a left join w_outbound_plan_order b on a.outbound_plan_order_id = b.id"
                }
            }
        },
        filename: "outbound_plan_order_details",
        label: "button.exportDetail",
        method: "POST",
        exportColumns: detailColumns.map((column) => ({ ...column }))
    })
]

const schema = crudPage({
    title: "outboundOrder.title",
    name: "OutboundOrderTable",
    searchIdentity: "WOutboundPlanOrder",
    api: api_crud_search_by_warehouseCode,
    columns,
    orderBy: "update_time desc",
    pageActions: [add],
    tableActions,
    rowActions: [detailButton(detailDrawer)],
    empty: "emptyState.outboundPlanOrder"
})

export default schema2component(schema)
