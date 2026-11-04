import schema2component from "@/utils/schema2component"
import {
    api_crud_search_by_warehouseCode,
    api_crud_search_by_warehouseCode_total
} from "@/pages/constantApi"
import {
    auditColumns,
    codeColumn,
    crudPage,
    detailButton,
    exportButton,
    nestedTable,
    numberColumn,
    remarkColumn,
    statusColumn
} from "@/utils/amis/presets"
import { acceptOrderStatus } from "@/utils/amis/statusMaps"

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
    codeColumn("ownerCode", "table.productOwner"),
    codeColumn("skuCode", "skuArea.skuCode"),
    {
        name: "skuName",
        label: "skuArea.productName"
    },
    numberColumn("qtyAccepted", "table.receivedQuantity")
]

const detailDrawer = {
    size: "xl",
    title: "table.acceptOrderDetails",
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
            tpl: "${identifyNo} · ${orderNo}",
            className: "owes-drawer-context"
        },
        nestedTable({
            name: "AcceptOrderDetailTable",
            api: {
                method: "POST",
                url: "/search/search?page=${page}&perPage=${perPage}&acceptOrderId=${id}&acceptOrderId-op=eq",
                dataType: "application/json"
            },
            searchIdentity: "WAcceptOrderDetail",
            columns: detailColumns,
            empty: "emptyState.acceptOrderDetail"
        })
    ]
}

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
    codeColumn("identifyNo", "table.containerCode", { searchable: true }),
    statusColumn("acceptOrderStatus", "table.status", {
        ...acceptOrderStatus,
        searchable: {
            type: "select",
            source: "${dictionary.AcceptOrderStatus}"
        }
    }),
    codeColumn("orderNo", "table.orderNo", { searchable: true }),
    numberColumn("totalBox", "table.boxesNumber"),
    numberColumn("totalQty", "table.totalQuantity"),
    ...auditColumns(),
    remarkColumn()
]

const schema = crudPage({
    title: "menu.acceptOrder",
    name: "AcceptOrderTable",
    searchIdentity: "WAcceptOrder",
    api: api_crud_search_by_warehouseCode,
    columns,
    orderBy: "accept_order_status, update_time desc",
    pageActions: [],
    tableActions: [
        exportButton({
            api: api_crud_search_by_warehouseCode_total,
            filename: "accept_order",
            defaultParams: {
                searchIdentity: "WAcceptOrder",
                showColumns: columns
            }
        })
    ],
    rowActions: [detailButton(detailDrawer)],
    empty: "emptyState.acceptOrder"
})

export default schema2component(schema)
