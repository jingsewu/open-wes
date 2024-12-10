import schema2component from "@/utils/schema2component"

import { Translation } from "react-i18next"
import { api_stocktake_order_add } from "@/pages/wms/data_center/constants/api_constant"
import { stock_sku_code_table } from "@/pages/wms/constants/select_search_api_contant"
import React from "react"
import { toast } from "amis"
import {
    owner_code,
    stock_id_table,
    warehouse_area_id,
    warehouse_logic_id
} from "@/pages/wms/constants/select_search_api_contant"
import { CustomActionType } from "@/pages/wms/station/instances/stocktake/customActionType"

let warehouseCode = localStorage.getItem("warehouseCode")

const schema = {
    type: "page",
    title: "modal.receiveInventoryList",
    initApi: "post:/mdm/config/dictionary/getAll",
    body: {
        type: "wizard",
        actionFinishLabel: "modal.generateInventoryOrder",
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
                    },
                    {
                        type: "switch",
                        name: "includeZeroStock",
                        label: "modal.inventory0ItemsInStock"
                    }
                ],
                actions: [
                    {
                        label: "Prev",
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
                            value: "STOCK",
                            body: {
                                type: "form",
                                wrapWithPanel: false,
                                id: "stepForm",
                                body: [
                                    {
                                        name: "barCode",
                                        id: "barCode",
                                        type: "input-text",
                                        multiple: true,
                                        placeholder:
                                            "skuArea.scanBarcode",
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
                                                            "service-reload",
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
                                                            "service-reload",
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
                                        className:
                                            "inline-block w-1/6 align-top",
                                        btnLabel:
                                            "button.import Excel",
                                        onEvent: {
                                            success: {
                                                actions: [
                                                    {
                                                        actionType: "reload",
                                                        componentId:
                                                            "service-reload",
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
                                        id: "service-reload",
                                        name: "service-reload",
                                        api: stock_sku_code_table,
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
                                            pagination: {
                                                enable: true,
                                                layout: [
                                                    "page",
                                                    "perpage",
                                                    "total"
                                                ]
                                            },
                                            columns: [
                                                {
                                                    name: "barCodeList",
                                                    label: "skuArea.barcode"
                                                },
                                                {
                                                    name: "totalQty",
                                                    label: "table.inventoryQuantity"
                                                },
                                                {
                                                    name: "ownerCode",
                                                    label: "table.productOwner"
                                                },
                                                {
                                                    name: "containerCode",
                                                    label: "table.containerCode"
                                                },
                                                {
                                                    name: "containerFace",
                                                    label: "workLocationArea.face"
                                                },
                                                {
                                                    name: "containerSlotCode",
                                                    label: "table.containerSlotCode"
                                                }
                                            ],
                                            onEvent: {
                                                change: {
                                                    actions: [
                                                        {
                                                            componentId:
                                                                "wizardComponent",
                                                            actionType:
                                                                "setValue",
                                                            args: {
                                                                value: {
                                                                    stockIds:
                                                                        "${event.data.value}"
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
                        onClick: async (_e: any, props: any) => {
                            console.log("props", props)
                            if (
                                !props.scope.stockIds ||
                                props.scope.stockIds.length === 0
                            ) {
                                toast["error"](
                                    "Please select the products you want to count",
                                    "消息"
                                )
                                return
                                // return false
                            }
                            const params = { ...props.scope, warehouseCode }
                            delete params.options
                            delete params.file
                            // return true
                            const { code } = await props.onCustomActionDispatch(
                                {
                                    eventCode:
                                        CustomActionType.STOCKTAKE_CREATE_ORDER,
                                    data: params
                                }
                            )
                            if (code !== "-1") {
                                props.setModalVisible(false)
                            }
                        }
                    }
                ]
            }
        ]
    }
}

export default schema2component(schema)
