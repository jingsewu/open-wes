import schema2component from "@/utils/schema2component"
import {
    warehouse_area_id,
    warehouse_logic_id
} from "@/pages/wms/constants/select_search_api_contant"
import { api_crud_search_by_warehouseCode } from "@/pages/constantApi"
import { yes_no_options } from "@/utils/commonContants"
import {
    auditColumns,
    codeColumn,
    crudPage,
    detailButton,
    linkAction,
    statusColumn
} from "@/utils/amis/presets"
import { stocktakeOrderStatus } from "@/utils/amis/statusMaps"
import { add } from "./add"
import { detailDrawer } from "./detail"
import { recordDrawer } from "./record"

const columns = [
    {
        name: "id",
        label: "ID",
        hidden: true
    },
    {
        name: "warehouseCode",
        label: "table.warehouse",
        hidden: true
    },
    codeColumn("orderNo", "table.countOrderNumber"),
    {
        name: "stocktakeType",
        label: "table.orderType",
        type: "mapping",
        source: "${dictionary.StocktakeType}"
    },
    {
        name: "stocktakeCreateMethod",
        label: "table.howItWasCreated",
        type: "mapping",
        source: "${dictionary.StocktakeCreateMethod}"
    },
    {
        name: "stocktakeUnitType",
        label: "table.creationType",
        type: "mapping",
        source: "${dictionary.StocktakeUnitType}"
    },
    statusColumn("stocktakeOrderStatus", "table.status", stocktakeOrderStatus),
    {
        name: "warehouseAreaId",
        label: "table.warehouseAreaBelongs",
        type: "mapping",
        source: warehouse_area_id
    },
    {
        name: "warehouseLogicId",
        label: "table.logicalAreaName",
        type: "mapping",
        source: warehouse_logic_id
    },
    {
        name: "abnormal",
        label: "table.exceptionIdentification",
        type: "mapping",
        map: yes_no_options
    },
    ...auditColumns()
]

const filters = [
    {
        label: "table.countOrderNumber",
        name: "orderNo",
        type: "input-text"
    },
    {
        label: "table.status",
        name: "stocktakeOrderStatus",
        type: "select",
        source: "${dictionary.StocktakeOrderStatus}"
    },
    {
        label: "table.warehouseAreaBelongs",
        name: "warehouseAreaId",
        type: "select",
        source: warehouse_area_id
    },
    {
        label: "table.logicalAreaName",
        name: "warehouseLogicId",
        type: "select",
        source: warehouse_logic_id
    },
    {
        label: "table.orderType",
        name: "stocktakeType",
        type: "select",
        source: "${dictionary.StocktakeType}"
    },
    {
        label: "table.creationType",
        name: "stocktakeUnitType",
        type: "select",
        source: "${dictionary.StocktakeUnitType}"
    },
    {
        label: "table.howItWasCreated",
        name: "stocktakeCreateMethod",
        type: "select",
        source: "${dictionary.StocktakeCreateMethod}"
    },
    {
        label: "table.exceptionIdentification",
        name: "abnormal",
        type: "select",
        options: yes_no_options
    }
]

const schema = crudPage({
    title: "wms.menu.inventoryCheck",
    name: "stocktakeOrderTable",
    searchIdentity: "WStocktakeOrder",
    api: api_crud_search_by_warehouseCode,
    columns,
    orderBy: "id, stocktake_order_status desc",
    filters,
    pageActions: [add],
    bulkActions: [
        linkAction({
            label: "button.stocktake.order.execute",
            confirmText: "confirm.stocktake.order.execute",
            api: {
                method: "post",
                url: "/wms/stocktake/order/execute",
                data: {
                    warehouseCode: localStorage.getItem("warehouseCode"),
                    orderNos: "${ARRAYMAP(selectedItems, item => item.orderNo)}",
                    taskCount: "${COUNT(selectedItems)}"
                }
            },
            reload: "stocktakeOrderTable"
        })
    ],
    rowActions: [
        detailButton(detailDrawer),
        detailButton(recordDrawer, "button.inventoryRecord")
    ],
    operationWidth: 130,
    empty: "emptyState.stocktake"
})

export default schema2component(schema)
