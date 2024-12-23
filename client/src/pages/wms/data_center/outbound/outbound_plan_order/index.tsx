import schema2component from "@/utils/schema2component"
import {
    create_update_columns,
    true_false_options
} from "@/utils/commonContants"

import {
    api_crud_search_by_warehouseCode,
    api_crud_search_by_warehouseCode_total
} from "@/pages/constantApi"
import { detailDialog, detailColumns, searchDetailIdentity } from "./detail"

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
        name: "customerOrderNo",
        label: "table.customerOrderNo",
        searchable: true
    },
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
    {
        name: "orderNo",
        label: "table.orderNo"
    },
    {
        name: "customerWaveNo",
        label: "table.customerWaveNumber"
    },
    {
        name: "waveNo",
        label: "table.waveNumber"
    },
    {
        name: "priority",
        label: "table.priority"
    },
    {
        name: "shortOutbound",
        label: "table.shortOut",
        type: "mapping",
        map: true_false_options
    },
    {
        name: "outboundPlanOrderStatus",
        label: "table.status",
        type: "mapping",
        source: "${dictionary.OutboundPlanOrderStatus}",
        searchable: {
            type: "select",
            source: "${dictionary.OutboundPlanOrderStatus}"
        }
    },
    {
        name: "expiredTime",
        label: "table.cut-off_time",
        tpl: "${createTime/1000|date:YYYY-MM-DD HH\\:mm\\:ss}"
    },
    {
        name: "skuKindNum",
        label: "table.skuTypes"
    },
    {
        name: "totalQty",
        label: "table.totalQuantity"
    },
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
        name: "currierCode",
        label: "table.carriers"
    },
    {
        name: "waybillNo",
        label: "table.theTrackingNumber"
    },
    ...create_update_columns
]

const headerToolbar = [
    {
        type: "columns-toggler",
        draggable: true,
        overlay: true,
        icon: "fas fa-cog",
        hideExpandIcon: false,
        size: "sm"
    },
    "reload",
    {
        type: "export-csv",
        label: "button.exportOrder",
        method: "POST",
        api: api_crud_search_by_warehouseCode_total,
        filename: "outbound_plan_orders"
    },
    {
        type: "export-csv",
        label: "button.exportDetail",
        method: "POST",
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
        exportColumns: JSON.parse(JSON.stringify(detailColumns))
    }
    // add
]

const searchIdentity = "WOutboundPlanOrder"
const showColumns = columns

const schema = {
    type: "page",
    title: "outboundOrder.title",
    toolbar: [],
    data: {
        dictionary: "${ls:dictionary}"
    },
    body: [
        {
            type: "crud",
            syncLocation: false,
            name: "OutboundOrderTable",
            api: api_crud_search_by_warehouseCode,
            defaultParams: {
                searchIdentity: searchIdentity,
                showColumns: showColumns,
                searchObject: {
                    orderBy: "update_time desc"
                }
            },
            autoFillHeight: true,
            autoGenerateFilter: {
                columnsNum: 3,
                showBtnToolbar: true
            },
            headerToolbar: headerToolbar,
            footerToolbar: ["switch-per-page", "statistics", "pagination"],
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
