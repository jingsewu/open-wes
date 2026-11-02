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
    operationColumn,
    statusColumn
} from "@/utils/amis/presets"
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
    statusColumn("stocktakeOrderStatus", "table.status", {
        labels: {
            NEW: "status.stocktakeNew",
            STARTED: "status.stocktakeStarted",
            PARTIAL_DONE: "status.stocktakePartialDone",
            DONE: "status.stocktakeDone",
            CANCELED: "status.stocktakeCanceled"
        },
        tones: {
            NEW: "info",
            STARTED: "warning",
            PARTIAL_DONE: "warning",
            DONE: "success",
            CANCELED: "muted"
        }
    }),
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

const searchIdentity = "WStocktakeOrder"

const schema = {
    type: "page",
    title: "wms.menu.inventoryCheck",
    toolbar: [add],
    data: {
        dictionary: "${ls:dictionary}"
    },
    body: [
        {
            type: "crud",
            syncLocation: false,
            name: "stocktakeOrderTable",
            api: api_crud_search_by_warehouseCode,
            defaultParams: {
                searchIdentity: searchIdentity,
                showColumns: columns,
                searchObject: {
                    orderBy: "id ,stocktake_order_status desc"
                }
            },
            autoFillHeight: true,
            filterTogglable: true,
            filterDefaultVisible: true,
            filter: {
                title: "common.filter",
                mode: "inline",
                body: [
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
                ],
                actions: [
                    { type: "reset", label: "button.reset" },
                    {
                        type: "submit",
                        level: "primary",
                        label: "common.search"
                    }
                ]
            },
            headerToolbar: ["reload", "bulkActions"],
            footerToolbar: ["switch-per-page", "statistics", "pagination"],
            bulkActions: [
                {
                    label: "button.stocktake.order.execute",
                    actionType: "ajax",
                    api: {
                        method: "post",
                        url: "/wms/stocktake/order/execute",
                        data: {
                            warehouseCode:
                                localStorage.getItem("warehouseCode"),
                            orderNos:
                                "${ARRAYMAP(selectedItems, item => item.orderNo)}",
                            taskCount: "${COUNT(selectedItems)}"
                        }
                    },
                    confirmText: "confirm.stocktake.order.execute"
                }
            ],
            placeholder: "emptyState.stocktake",
            columns: [
                ...columns,
                operationColumn(
                    [
                        {
                            label: "button.detail",
                            type: "button",
                            actionType: "drawer",
                            level: "link",
                            className: "owes-action",
                            drawer: detailDrawer
                        },
                        {
                            label: "button.inventoryRecord",
                            type: "button",
                            actionType: "drawer",
                            level: "link",
                            className: "owes-action",
                            drawer: recordDrawer
                        }
                    ],
                    { width: 130 }
                )
            ]
        }
    ]
}

export default schema2component(schema)
