import {
    owner_code,
    stock_sku_id_table,
    stock_sku_id_table_columns,
    warehouse_area_id,
    warehouse_logic_id
} from "@/pages/wms/constants/select_search_api_contant"
import {api_stocktake_order_add} from "@/pages/wms/data_center/constants/api_constant"
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
                    source: "${dictionary.StocktakeType}",
                    required: true,
                    className: "stocktakeType"
                },
                {
                    type: "button-group-select",
                    name: "stocktakeMethod",
                    selectFirst: true,
                    label: "table.countMethod",
                    source: "${dictionary.StocktakeMethod}",
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

export const add = {
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
