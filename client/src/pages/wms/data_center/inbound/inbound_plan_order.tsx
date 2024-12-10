import schema2component from "@/utils/schema2component"
import { create_update_columns } from "@/utils/commonContants"

let warehouseCode = localStorage.getItem("warehouseCode")

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

const detailColumns = [
    {
        name: "inboundPlanOrderId",
        label: "入库通知单ID",
        hidden: true
    },
    {
        name: "batchAttributes",
        label: "table.batchAttributes"
    },
    {
        name: "boxNo",
        label: "table.lpnNumber"
    },
    {
        name: "brand",
        label: "table.brand"
    },
    {
        name: "color",
        label: "table.color"
    },
    {
        name: "qtyAbnormal",
        label: "skuArea.qtyAbnormal"
    },
    {
        name: "qtyAccepted",
        label: "table.acceptanceQuantity"
    },
    {
        name: "qtyRestocked",
        label: "table.plannedQuantity"
    },
    {
        name: "qtyUnreceived",
        label: "table.unreceivedQuantity"
    },
    {
        name: "responsibleParty",
        label: "table.responsibleParty"
    },
    {
        name: "size",
        label: "table.size"
    },
    {
        name: "skuCode",
        label: "table.skuCode"
    },
    {
        name: "skuName",
        label: "table.skuName"
    },
    {
        name: "ownerCode",
        label: "table.productOwner"
    },
    {
        name: "style",
        label: "table.style"
    }
]

const searchIdentity = "WInboundPlanOrder"
const searchDetailIdentity = "WInboundPlanOrderDetail"
const showColumns = columns
const showDetailColumns = detailColumns

const detailDialog = {
    title: "table.inboundPlanDetails",
    actions: [],
    closeOnEsc: true,
    closeOnOutside: true,
    size: "xl",
    body: [
        {
            type: "crud",
            syncLocation: false,
            name: "inboundPlanOrderDetailTable",
            api: {
                method: "POST",
                url: "/search/search?page=${page}&perPage=${perPage}&inboundPlanOrderId=${id}&inboundPlanOrderId-op=eq",
                dataType: "application/json"
            },
            defaultParams: {
                searchIdentity: searchDetailIdentity,
                showColumns: showDetailColumns
            },
            footerToolbar: ["switch-per-page", "statistics", "pagination"],
            columns: detailColumns
        }
    ]
}

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
                    api: {
                        method: "POST",
                        url:
                            "/search/search?page=${1}&perPage=${100000}&createTime-op=bt&warehouseCode-op=eq&warehouseCode=" +
                            warehouseCode,
                        dataType: "application/json"
                    },
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
