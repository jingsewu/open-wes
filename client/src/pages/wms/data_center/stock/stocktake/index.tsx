import schema2component from "@/utils/schema2component"
import {
    warehouse_area_id,
    warehouse_logic_id
} from "@/pages/wms/constants/select_search_api_contant"
import { api_crud_search_by_warehouseCode } from "@/pages/constantApi"
import { create_update_columns, yes_no_options } from "@/utils/commonContants"
import { add } from "./add"
import { detailDialog } from "./detail"
import { recordDialog } from "./record"
import { api_getDictionary } from "@/pages/constantApi"

let warehouseCode = localStorage.getItem("warehouseCode")

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
    {
        name: "orderNo",
        label: "table.countOrderNumber",
        searchable: true
    },
    {
        name: "stocktakeType",
        label: "table.orderType",
        type: "mapping",
        source: "${StocktakeType}",
        searchable: {
            type: "select",
            source: "${StocktakeType}"
        }
    },
    {
        name: "stocktakeCreateMethod",
        label: "table.howItWasCreated",
        type: "mapping",
        source: "${StocktakeCreateMethod}",
        searchable: {
            type: "select",
            source: "${StocktakeCreateMethod}"
        }
    },
    {
        name: "stocktakeUnitType",
        label: "table.creationType",
        type: "mapping",
        source: "${StocktakeUnitType}",
        searchable: {
            type: "select",
            source: "${StocktakeUnitType}"
        }
    },
    {
        name: "stocktakeOrderStatus",
        label: "table.status",
        type: "mapping",
        source: "${StocktakeOrderStatus}",
        searchable: {
            type: "select",
            source: "${StocktakeOrderStatus}"
        },
        classNameExpr:
            "${ stocktakeOrderStatus === 'STARTED' ? 'startStatus' : '' }"
    },
    {
        name: "warehouseAreaId",
        label: "table.warehouseAreaBelongs",
        type: "mapping",
        source: warehouse_area_id,
        searchable: {
            type: "select",
            source: warehouse_area_id
        }
    },
    {
        name: "warehouseLogicId",
        label: "table.logicalAreaName",
        type: "mapping",
        source: warehouse_logic_id,
        searchable: {
            type: "select",
            source: warehouse_logic_id
        }
    },
    {
        name: "abnormal",
        label: "table.exceptionIdentification",
        type: "mapping",
        map: yes_no_options,
        searchable: {
            type: "select",
            options: yes_no_options
        }
    },
    ...create_update_columns
]

const searchIdentity = "WStocktakeOrder"

const schema = {
    type: "page",
    title: "wms.menu.inventoryCheck",
    toolbar: [],
    initApi: api_getDictionary,
    body: [
        {
            type: "crud",
            syncLocation: false,
            name: "stocktakeOrderTable",
            api: api_crud_search_by_warehouseCode,
            defaultParams: {
                searchIdentity: searchIdentity,
                showColumns: columns
            },
            autoFillHeight: true,
            autoGenerateFilter: {
                columnsNum: 3,
                showBtnToolbar: true
            },
            headerToolbar: ["reload", add, "bulkActions"],
            footerToolbar: ["switch-per-page", "statistics", "pagination"],
            bulkActions: [
                {
                    label: "执行盘点单",
                    actionType: "ajax",
                    api: {
                        method: "post",
                        url: "/wms/stocktake/order/execute",
                        data: {
                            warehouseCode,
                            orderNos:
                                "${ARRAYMAP(selectedItems, item => item.orderNo)}",
                            taskCount: "${COUNT(selectedItems)}"
                        }
                    },
                    confirmText: "确定要执行盘点单?"
                }
            ],
            columns: [
                ...columns,
                {
                    type: "operation",
                    label: "table.operation",
                    // width: 130,
                    buttons: [
                        {
                            label: "button.detail",
                            type: "button",
                            actionType: "dialog",
                            dialog: detailDialog
                        },
                        {
                            label: "button.inventoryRecord",
                            type: "button",
                            actionType: "dialog",
                            dialog: recordDialog
                        }
                    ]
                }
            ]
        }
    ]
}

export default schema2component(schema)
