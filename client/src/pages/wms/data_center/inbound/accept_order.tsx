import schema2component from "@/utils/schema2component"
import { create_update_columns } from "@/utils/commonContants"
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
        name: "identifyNo",
        label: "table.containerCode",
        searchable: true
    },
    {
        name: "acceptOrderStatus",
        label: "table.status",
        type: "mapping",
        source: "${ls:dictionary|pick:AcceptOrderStatus}",
        searchable: {
            type: "select",
            source: "${ls:dictionary|pick:AcceptOrderStatus}"
        }
    },
    {
        name: "orderNo",
        label: "table.orderNo",
        searchable: true
    },
    // {
    //     name: "acceptType",
    //     label: "table.acceptType",
    //     type: "mapping",
    //     source: "${AcceptType}",
    //     searchable: {
    //         type: "select",
    //         source: "${AcceptType}"
    //     }
    // },
    {
        name: "totalBox",
        label: "table.boxesNumber"
    },
    {
        name: "totalQty",
        label: "table.totalQuantity"
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
        name: "acceptOrderDetailId",
        dbField: "id",
        hidden: true
    },
    {
        name: "acceptOrderId",
        hidden: true
    },
    {
        name: "targetContainerSpecCode",
        label: "table.containerSpecificationNumber"
    },
    {
        name: "targetContainerSlotCode",
        label: "table.containerLatticeSlogan"
    },
    {
        name: "ownerCode",
        label: "table.productOwner",
        searchable: true
    },
    {
        name: "skuCode",
        label: "skuArea.skuCode",
        searchable: true
    },
    {
        name: "skuName",
        label: "skuArea.productName",
        searchable: true
    },
    {
        name: "qtyAccepted",
        label: "table.receivedQuantity"
    }
]

const searchIdentity = "WAcceptOrder"
const searchDetailIdentity = "WAcceptOrderDetail"
const showColumns = columns
const showDetailColumns = detailColumns

const detailDialog = {
    title: "table.acceptOrderDetails",
    actions: [],
    closeOnEsc: true,
    closeOnOutside: true,
    size: "xl",
    body: [
        {
            type: "crud",
            syncLocation: false,
            name: "acceptOrderDetailTable",
            api: {
                method: "POST",
                url: "/search/search?page=${page}&perPage=${perPage}&acceptOrderId=${id}&acceptOrderId-op=eq",
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
    title: "menu.acceptOrder",
    toolbar: [],
    body: [
        {
            type: "crud",
            syncLocation: false,
            name: "acceptOrderTable",
            api: api_crud_search_by_warehouseCode,
            defaultParams: {
                searchIdentity: searchIdentity,
                showColumns: showColumns,
                searchObject: {
                    orderBy: "accept_order_status, update_time desc"
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
                    filename: "accept_order",
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
