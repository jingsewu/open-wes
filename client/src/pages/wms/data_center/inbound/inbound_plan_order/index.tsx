import schema2component from "@/utils/schema2component"
import { create_update_columns } from "@/utils/commonContants"
import { detailDialog } from "./detail"
import {
    api_crud_search_by_warehouseCode,
    api_crud_search_by_warehouseCode_total
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
        name: "lpnCode",
        label: "table.LPNNo",
        searchable: true
    },
    {
        name: "customerOrderNo",
        label: "table.customerOrderNo",
        searchable: true
    },
    {
        name: "inboundPlanOrderStatus",
        label: "table.status",
        type: "mapping",
        source: "${InboundPlanOrderStatus}",
        searchable: {
            type: "select",
            source: "${InboundPlanOrderStatus}"
        }
    },

    {
        name: "orderNo",
        label: "table.orderNo",
        searchable: true
    },
    {
        name: "sender",
        label: "table.shipper",
        searchable: true
    },
    {
        name: "skuKindNum",
        label: "table.skuTypes",
        searchable: true
    },
    {
        name: "storageType",
        label: "table.storageType",
        type: "mapping",
        source: "${StorageType}",
        searchable: {
            type: "select",
            source: "${StorageType}"
        }
    },
    {
        name: "totalBox",
        label: "table.boxesNumber"
    },
    {
        name: "totalQty",
        label: "table.totalQuantity"
    },
    {
        name: "trackingNumber",
        label: "table.theTrackingNumber",
        searchable: true
    },
    {
        name: "shippingMethod",
        label: "table.modeOfCarriage",
        searchable: true
    },
    ...create_update_columns,
    {
        type: "tpl",
        name: "remark",
        label: "table.remark",
        tpl: "${remark|truncate:30}",
        popOver: {
            trigger: "hover",
            position: "left-top",
            showIcon: false,
            body: {
                type: "tpl",
                tpl: "${remark}"
            }
        }
    }
]

const searchIdentity = "WInboundPlanOrder"
const showColumns = columns

const schema = {
    type: "page",
    title: "menu.inboundOrder",
    toolbar: [],
    initApi: "post:/mdm/config/dictionary/getAll",
    body: [
        {
            type: "crud",
            syncLocation: false,
            name: "inboundPlanOrderTable",
            silentPolling: true,
            api: api_crud_search_by_warehouseCode,
            defaultParams: {
                searchIdentity: searchIdentity,
                showColumns: showColumns,
                searchObject: {
                    orderBy: "inbound_plan_order_status, update_time desc"
                }
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
                    method: "POST",
                    api: api_crud_search_by_warehouseCode_total,
                    filename: "inbound_plan_order",
                    defaultParams: {
                        searchIdentity: searchIdentity,
                        showColumns: showColumns
                    }
                }
            ],
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
