import schema2component from "@/utils/schema2component"
import {
    owner_code,
    stock_sku_id_table,
    stock_sku_id_table_columns,
    warehouse_area_id,
    warehouse_logic_id
} from "@/pages/wms/constants/select_search_api_contant"
import {api_stocktake_order_add} from "@/pages/wms/data_center/constants/api_constant"
import {create_update_columns, yes_no_options} from "@/utils/commonContants"
import {toast} from "amis"

let warehouseCode = localStorage.getItem("warehouseCode")

const dialog = {
    type: "wizard",
    actionFinishLabel: "modal.generateInventoryOrder",
    api: api_stocktake_order_add,
    preventEnterSubmit: true,
    reload: "stocktakeOrderTable",
    id: "wizardComponent",
    steps: [
        {
            title: "modal.selectInventoryArea",
            body: [
                {
                    type: "hidden",
                    name: "id"
                },
                {
                    type: "hidden",
                    name: "warehouseCode",
                    value: warehouseCode
                },
                {
                    type: "hidden",
                    name: "stocktakeCreateMethod",
                    value: "MANUAL"
                },
                {
                    type: "select",
                    name: "warehouseAreaId",
                    label: "workLocationArea.warehouseArea",
                    selectFirst: true,
                    source: {
                        ...warehouse_area_id,
                        url:
                            warehouse_area_id.url +
                            "&warehouseAreaWorkType=ROBOT"
                    },
                    required: true,
                    className: "warehouseArea"
                },
                {
                    type: "select",
                    name: "warehouseLogicId",
                    label: "table.logicArea",
                    clearable: true,
                    source: warehouse_logic_id,
                    className: "warehouseLogic"
                },
                {
                    type: "select",
                    name: "ownerCode",
                    label: "table.productOwner",
                    clearable: true,
                    source: owner_code,
                    className: "ownerCode"
                }
            ],
            actions: [
                {
                    label: "Next",
                    type: "button",
                    actionType: "next",
                    level: "primary",
                    className: "nextButton"
                }
            ]
        },
        {
            title: "modal.selectCountingRules",
            body: [
                {
                    type: "button-group-select",
                    name: "stocktakeType",
                    label: "table.countType",
                    selectFirst: true,
                    source: "${StocktakeType}",
                    required: true,
                    className: "stocktakeType"
                },
                {
                    type: "button-group-select",
                    name: "stocktakeMethod",
                    selectFirst: true,
                    label: "table.countMethod",
                    source: "${StocktakeMethod}",
                    required: true,
                    className: "stocktakeMethod"
                }
            ],
            actions: [
                {
                    label: "Previous",
                    type: "button",
                    actionType: "prev"
                },
                {
                    label: "Next",
                    type: "button",
                    actionType: "next",
                    level: "primary",
                    className: "nextButton"
                }
            ]
        },
        {
            title: "modal.selectCountingTarget",
            wrapperComponent: "div",
            body: {
                type: "tabs",
                name: "stocktakeUnitType",
                tabsMode: "strong",
                tabs: [
                    {
                        title: "modal.countByProduct",
                        value: "SKU",
                        hiddenOn: "${stocktakeType === 'DISCREPANCY_REVIEW'}",
                        body: {
                            type: "form",
                            wrapWithPanel: false,
                            id: "step2Form",
                            body: [
                                {
                                    name: "barCode",
                                    id: "barCode",
                                    type: "input-text",
                                    multiple: true,
                                    placeholder: "skuArea.scanBarcode",
                                    trimContents: true,
                                    clearable: true,
                                    className:
                                        "w-4/5	inline-block mr-3 align-top	",
                                    source: [],
                                    onEvent: {
                                        enter: {
                                            actions: [
                                                {
                                                    componentId:
                                                        "skuCount-service-reload",
                                                    actionType: "reload",
                                                    data: {
                                                        barCode:
                                                            "${event.data.value}"
                                                    }
                                                }
                                            ]
                                        },
                                        clear: {
                                            actions: [
                                                {
                                                    componentId:
                                                        "skuCount-service-reload",
                                                    actionType: "reload",
                                                    data: {
                                                        barCode:
                                                            "${event.data.value}"
                                                    }
                                                }
                                            ]
                                        }
                                    }
                                },
                                {
                                    type: "input-file",
                                    name: "file",
                                    id: "import_file",
                                    accept: ".xls,.xlsx,.csv",
                                    receiver: {
                                        url: "/mdm/common-import/parse"
                                    },
                                    className: "inline-block w-1/6 align-top",
                                    btnLabel: "button.import Excel",
                                    onEvent: {
                                        success: {
                                            actions: [
                                                {
                                                    actionType: "reload",
                                                    componentId:
                                                        "skuCount-service-reload",
                                                    data: {
                                                        barCode:
                                                            "${event.data.result.list | pick:barcode | join}"
                                                    }
                                                }
                                            ]
                                        },
                                        remove: {
                                            actions: [
                                                {
                                                    actionType: "reload",
                                                    componentId:
                                                        "service-reload",
                                                    data: {
                                                        barCode: ""
                                                    }
                                                }
                                            ]
                                        }
                                    }
                                },
                                {
                                    type: "service",
                                    id: "skuCount-service-reload",
                                    name: "skuCount-service-reload",
                                    api: stock_sku_id_table,
                                    body: {
                                        type: "transfer",
                                        name: "stockIds",
                                        joinValues: false,
                                        extractValue: true,
                                        selectMode: "table",
                                        affixHeader: true,
                                        resultListModeFollowSelect: true,
                                        id: "transferTable",
                                        virtualThreshold: 10,
                                        "en-US": {
                                            searchPlaceholder:
                                                "Please scan the product bar code"
                                        },
                                        source: "${options}",
                                        footerToolbar: [
                                            "switch-per-page",
                                            "statistics",
                                            "pagination"
                                        ],
                                        columns: stock_sku_id_table_columns,
                                        onEvent: {
                                            change: {
                                                actions: [
                                                    {
                                                        componentId:
                                                            "wizardComponent",
                                                        actionType: "setValue",
                                                        args: {
                                                            value: {
                                                                skuIds: "${event.data.value}"
                                                            }
                                                        }
                                                    }
                                                ]
                                            }
                                        }
                                    }
                                }
                            ]
                        }
                    }
                ]
            },
            actions: [
                {
                    label: "Prev",
                    type: "button",
                    onEvent: {
                        click: {
                            actions: [
                                {
                                    actionType: "clear",
                                    componentId: "stepForm"
                                },
                                {
                                    actionType: "prev",
                                    componentId: "wizardComponent"
                                }
                            ]
                        }
                    }
                },
                {
                    label: "modal.generateInventoryOrder",
                    type: "submit",
                    level: "primary",
                    actionType: "submit",
                    className: "generateInventoryOrder",
                    onClick: (_e: any, props: any) => {
                        if (
                            !props.scope.skuIds ||
                            props.scope.skuIds.length === 0
                        ) {
                            toast["error"](
                                "Please select the products you want to count",
                                "消息"
                            )
                            return false
                        }
                        return true
                    }
                }
            ]
        }
    ]
}

const add = {
    type: "button",
    actionType: "dialog",
    icon: "fa fa-plus",
    label: "button.add",
    dialog: {
        title: "button.add",
        actions: [],
        closeOnEsc: true,
        size: "xl",
        body: dialog
    },
    className: "inventoryCountAddButton"
}

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

const detailColumns = [
    {
        name: "stocktakeOrderId",
        label: "盘点单ID",
        hidden: true
    },
    {
        name: "stocktakeUnitType",
        label: "table.stocktakeUnitType"
    },
    {
        name: "unitCode",
        label: "table.unitCode"
    },
    {
        name: "unitId",
        label: "table.unitId"
    }
]

const recordColumns = [
    {
        name: "id",
        dbField: "k.id",
        label: "盘点记录ID",
        hidden: true
    },
    {
        dbField: "k.stocktake_order_id",
        name: "stocktakeOrderId",
        label: "盘点单ID",
        hidden: true
    },
    {
        dbField: "a.sku_code",
        name: "skuCode",
        label: "skuArea.skuCode"
    },
    {
        dbField: "a.sku_name",
        name: "skuName",
        label: "skuArea.productName"
    },
    {
        dbField: "k.container_code",
        name: "containerCode",
        label: "table.containerCode"
    },
    {
        dbField: "k.container_face",
        name: "containerFace",
        label: "workLocationArea.face"
    },
    {
        dbField: "k.container_slot_code",
        name: "containerSlotCode",
        label: "table.containerSlotCode"
    },
    {
        dbField: "k.qty_original",
        name: "qtyOriginal",
        label: "table.inventoryQuantity"
    },
    {
        dbField: "k.qty_stocktake",
        name: "qtyStocktake",
        label: "table.countQty"
    },
    {
        dbField: "k.stocktake_record_status",
        name: "stocktakeRecordStatus",
        label: "table.status",
        type: "mapping",
        source: "${StocktakeRecordStatus}"
    }
]

const searchIdentity = "WStocktakeOrder"
const searchDetailIdentity = "WStocktakeOrderDetail"
const searchRecordIdentity = "WStocktakeRecord"

const detailDialog = {
    title: "inventoryCounting.detail.modal.title",
    actions: [],
    closeOnEsc: true,
    closeOnOutside: true,
    size: "xl",
    body: [
        {
            type: "crud",
            syncLocation: false,
            name: "stocktakeOrderDetailTable",
            api: {
                method: "POST",
                url: "/search/search?page=${page}&perPage=${perPage}&stocktakeOrderId=${id}&stocktakeOrderId-op=eq",
                dataType: "application/json"
            },
            defaultParams: {
                searchIdentity: searchDetailIdentity,
                showColumns: detailColumns
            },
            footerToolbar: ["switch-per-page", "statistics", "pagination"],
            columns: detailColumns
        }
    ]
}

const recordDialog = {
    title: "button.inventoryRecord",
    actions: [],
    closeOnEsc: true,
    closeOnOutside: true,
    size: "xl",
    body: [
        {
            type: "crud",
            syncLocation: false,
            name: "stocktakeRecordTable",
            api: {
                method: "POST",
                url: "/search/search?page=${page}&perPage=${perPage}&stocktakeOrderId=${id}&stocktakeOrderId-op=eq",
                dataType: "application/json"
            },
            defaultParams: {
                searchIdentity: searchRecordIdentity,
                showColumns: recordColumns,
                searchObject: {
                    tables: "w_stocktake_record k  inner join m_sku_main_data a on k.sku_id = a.id"
                }
            },
            footerToolbar: ["switch-per-page", "statistics", "pagination"],
            columns: recordColumns
        }
    ]
}

const schema = {
    type: "page",
    title: "wms.menu.inventoryCheck",
    toolbar: [],
    initApi: "post:/mdm/config/dictionary/getAll",
    body: [
        {
            type: "crud",
            syncLocation: false,
            name: "stocktakeOrderTable",

            api: {
                method: "POST",
                url:
                    "/search/search?page=${page}&perPage=${perPage}&warehouseCode-op=eq&warehouseCode=" +
                    warehouseCode,
                dataType: "application/json",
                data: {
                    searchIdentity: searchIdentity,
                    showColumns: columns
                }
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
