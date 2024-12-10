import schema2component from "@/utils/schema2component"
import {create_update_columns, true_false_options} from "@/utils/commonContants"
import {api_outbound_plan_order_add} from '@/pages/wms/data_center/constants/api_constant';
import {Translation} from "react-i18next"
import {
    available_stock_sku_code_table,
    owner_code,
    warehouse_area_id,
    work_station
} from '@/pages/wms/constants/select_search_api_contant';
import React from 'react';
import {toast} from 'amis';

let warehouseCode = localStorage.getItem("warehouseCode")

let areaConditions =
    "&warehouseAreaCode-op=eq&warehouseAreaId=${warehouseAreaId}" +
    "&warehouseLogicCode-op=eq&warehouseLogicCode=${warehouseLogicCode}" +
    "&ownerCode-op=eq&ownerCode=${ownerCode}"

let barCodeCondition = {
    "barCode-op": "il",
    barCode: "${barCode}"
}

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
        name: "customerOrderNo",
        label: "table.customerOrderNo",
        searchable: true
    },
    {
        name: "customerOrderType",
        label: "table.orderType",
        type: "mapping",
        source: "${CustomerOrderType}",
        searchable: {
            type: "select",
            source: "${CustomerOrderType}"
        }
    },
    {
        name: "orderNo",
        label: "table.orderNo"
    },
    {
        name: "customerWaveNo",
        label: "table.customerWaveNumber"
    },
    {
        name: "waveNo",
        label: "table.waveNumber"
    },
    {
        name: "priority",
        label: "table.priority"
    },
    {
        name: "shortOutbound",
        label: "table.shortOut",
        type: "mapping",
        map: true_false_options
    },
    {
        name: "outboundPlanOrderStatus",
        label: "table.status",
        type: "mapping",
        source: "${OutboundPlanOrderStatus}",
        searchable: {
            type: "select",
            source: "${OutboundPlanOrderStatus}"
        }
    },
    {
        name: "expiredTime",
        label: "table.cut-off_time",
        tpl: "${createTime/1000|date:YYYY-MM-DD HH\\:mm\\:ss}"
    },
    {
        name: "skuKindNum",
        label: "table.skuTypes"
    },
    {
        name: "totalQty",
        label: "table.totalQuantity"
    },
    {
        name: "abnormal",
        label: "table.whetherAbnormal",
        type: "mapping",
        map: true_false_options
    },
    {
        name: "abnormalReason",
        label: "skuArea.abnormalCause"
    },
    {
        name: "origPlatformCode",
        label: "table.sourcePlatform"
    },
    {
        name: "currierCode",
        label: "table.carriers"
    },
    {
        name: "waybillNo",
        label: "table.theTrackingNumber"
    },
    ...create_update_columns
]

const detailColumns = [
    {
        name: "outboundPlanOrderId",
        dbField: "a.outbound_plan_order_id",
        label: "出库通知单ID",
        hidden: true
    },
    {
        name: "id",
        dbField: "a.id",
        label: "出库计划单明细ID",
        hidden: true
    },
    {
        name: "customerOrderNo",
        dbField: "b.customer_order_no",
        label: "table.customerOrderNo",
        hidden: true
    },
    {
        name: "customerWaveNo",
        dbField: "b.customer_wave_no",
        label: "table.customerWaveNo",
        hidden: true
    },
    {
        name: "ownerCode",
        dbField: "a.owner_code",
        label: "table.productOwner",
        searchable: true
    },
    {
        name: "batchAttributes",
        dbField: "a.batch_attributes",
        label: "table.batchAttributes"
    },
    {
        name: "qtyActual",
        dbField: "a.qty_actual",
        label: "table.actualOutboundQuantity"
    },
    {
        name: "qtyRequired",
        dbField: "a.qty_required",
        label: "table.plannedOutboundQuantity"
    },
    {
        name: "skuCode",
        dbField: "a.sku_code",
        label: "table.skuCode"
    },
    {
        name: "skuName",
        dbField: "a.sku_name",
        label: "table.skuName"
    }
]

const searchIdentity = "WOutboundPlanOrder"
const searchDetailIdentity = "WOutboundPlanOrderDetail"
const showColumns = columns
const showDetailColumns = detailColumns

const detailDialog = {
    title: "outboundOrder.detail.modal.title",
    actions: [],
    closeOnEsc: true,
    closeOnOutside: true,
    size: "xl",
    body: [
        {
            type: "crud",
            syncLocation: false,
            name: "OutboundPlanOrderDetailTable",
            api: {
                method: "POST",
                url: "/search/search?page=${page}&perPage=${perPage}&outboundPlanOrderId=${id}&outboundPlanOrderId-op=eq",
                dataType: "application/json"
            },
            defaultParams: {
                searchIdentity: searchDetailIdentity,
                showColumns: showDetailColumns,
                searchObject: {
                    tables: "w_outbound_plan_order_detail a left join w_outbound_plan_order b on a.outbound_plan_order_id = b.id"
                }
            },
            footerToolbar: ["switch-per-page", "statistics", "pagination"],
            columns: detailColumns
        }
    ]
}

function genUUID() {
    return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(
        /[xy]/g,
        function (c) {
            let r = (Math.random() * 16) | 0,
                v = c === "x" ? r : (r & 0x3) | 0x8
            return v.toString(16)
        }
    )
}

const dialog = {
    type: "wizard",
    actionFinishLabel: "modal.generateOutboundPlanOrder",
    api: {
        url: api_outbound_plan_order_add,
        requestAdaptor: (api: any, context: any) => {
            if (context.oneLineOneOrder) {
                return {
                    ...api,
                    data: context.details.map((detail: any) => {
                        let uuid = genUUID()
                        return {
                            ...api.data,
                            customerOrderNo: uuid,
                            customerWaveNo: uuid,
                            destinations: [api.data.destinations],
                            details: [detail]
                        }
                    })
                }
            }
            return {
                ...api,
                data: [api.data]
            }
        }
    },
    preventEnterSubmit: true,
    reload: "OutboundOrderTable",
    id: "wizardComponent",
    steps: [
        {
            title: "modal.selectOutboundRules",
            columnCount: 2,
            body: [
                {
                    type: "hidden",
                    name: "id"
                },
                {
                    type: "uuid",
                    name: "customerWaveNo"
                },
                {
                    type: "uuid",
                    name: "customerOrderNo"
                },
                {
                    label: "table.warehouseCode",
                    name: "warehouseCode",
                    type: "hidden",
                    value: warehouseCode
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
                    label: "table.orderType",
                    name: "customerOrderType",
                    type: "select",
                    value: "${CustomerOrderType|filter:defaultValue:isTrue|pick:value|join}",
                    source: "${CustomerOrderType}"
                },
                {
                    type: "select",
                    name: "ownerCode",
                    label: "table.productOwner",
                    clearable: true,
                    source: owner_code,
                    className: "ownerCode"
                },
                {
                    label: "table.priority",
                    type: "input-number",
                    min: 1,
                    max: 100,
                    name: "priority"
                },
                {
                    type: "select",
                    name: "targetWorkStationIds",
                    label: "table.targetPickWorkStation",
                    source: work_station,
                    searchable: true,
                    checkAll: true,
                    multiple: true,
                    clearable: true,
                    joinValues: false,
                    extractValue: true,
                },
                {
                    type: "select",
                    name: "destinations",
                    label: "table.destinationManualAreas",
                    source: {
                        ...warehouse_area_id,
                        url:
                            warehouse_area_id.url +
                            "&warehouseAreaWorkType-op=eq&warehouseAreaWorkType=MANUAL"
                    },
                    // multiple: true,
                    required: true,
                    visibleOn: "${customerOrderType == 'REPLENISH_OUTBOUND_ORDER'}",
                    joinValues: false,
                    extractValue: true
                },
                {
                    type: "group",
                    label: "",
                    body: [
                        {
                            label: "table.shortOut",
                            type: "switch",
                            name: "shortOutbound",
                            columnRatio: 6,
                            value: true
                        },
                        // {
                        //     label: "table.shortOut",
                        //     type: "switch",
                        //     name: "shortWaiting"
                        // },
                        {
                            label: "modal.oneLineOneOrder",
                            type: "switch",
                            name: "oneLineOneOrder",
                            columnRatio: 6,
                            value: true
                        }
                    ]
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
            title: "modal.selectOutboundDetails",
            wrapperComponent: "div",
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
                        placeholder: "skuArea.scanBarcode",
                        trimContents: true,
                        clearable: true,
                        className: "w-4/5	inline-block mr-3 align-top	",
                        source: [],
                        onEvent: {
                            enter: {
                                actions: [
                                    {
                                        componentId: "service-reload",
                                        actionType: "reload",
                                        data: {
                                            barCode: "${event.data.value}"
                                        }
                                    }
                                ]
                            },
                            clear: {
                                actions: [
                                    {
                                        componentId: "service-reload",
                                        actionType: "reload",
                                        data: {
                                            barCode: "${event.data.value}"
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
                                        componentId: "service-reload",
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
                                        componentId: "service-reload",
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
                        // body: "当前时间：${options}",
                        api: {
                            ...available_stock_sku_code_table,
                            url:
                                available_stock_sku_code_table.url +
                                areaConditions,
                            data: {
                                ...available_stock_sku_code_table.data,
                                ...barCodeCondition
                            },
                            responseData: {
                                options: "${items}"
                            }
                        },
                        body: {
                            type: "transfer",
                            name: "details",
                            joinValues: false,
                            extractValue: false,
                            selectMode: "table",
                            affixHeader: true,
                            // resultListModeFollowSelect: true,
                            id: "transferTable",
                            virtualThreshold: 10,
                            // valueTpl: "${ownerCode} - ${skuCode}, qty: <input name='qty' value='${qty}'>",
                            valueTpl: {
                                type: "group",
                                body: [
                                    {
                                        type: "tpl",
                                        tpl: "${ownerCode} - ${skuCode} - ${skuName}",
                                        columnRatio: 10
                                    },
                                    {
                                        type: "input-number",
                                        displayMode: "enhance",
                                        min: 1,
                                        max: "${totalQty}",
                                        value: 1,
                                        name: "qtyRequired",
                                        columnRatio: 2
                                    }
                                ]
                            },
                            // searchable: true,
                            // searchPlaceholder: "请扫描商品条码",
                            "en-US": {
                                searchPlaceholder:
                                    "Please scan the product bar code"
                            },
                            source: "${options}",
                            // source: "${transferOptions}",
                            pagination: {
                                enable: false,
                                layout: ["pager", "perpage", "total"]
                            },
                            columns: [
                                {
                                    name: "ownerCode",
                                    label: (
                                        <Translation>
                                            {(t) => t("table.productOwner")}
                                        </Translation>
                                    )
                                },
                                {
                                    name: "skuCode",
                                    label: (
                                        <Translation>
                                            {(t) => t("skuArea.skuCode")}
                                        </Translation>
                                    )
                                },
                                {
                                    name: "skuName",
                                    label: (
                                        <Translation>
                                            {(t) => t("table.skuName")}
                                        </Translation>
                                    )
                                },
                                {
                                    name: "barCodeList",
                                    label: (
                                        <Translation>
                                            {(t) =>
                                                t(
                                                    "skuArea.barcode"
                                                )
                                            }
                                        </Translation>
                                    )
                                },
                                {
                                    name: "totalQty",
                                    label: (
                                        <Translation>
                                            {(t) =>
                                                t(
                                                    "table.totalQuantity"
                                                )
                                            }
                                        </Translation>
                                    )
                                }
                            ],
                            onEvent: {
                                change: {
                                    actions: [
                                        {
                                            componentId: "wizardComponent",
                                            actionType: "setValue",
                                            args: {
                                                value: {
                                                    details:
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
                    label: "modal.generateOutboundPlanOrder",
                    type: "submit",
                    level: "primary",
                    actionType: "submit",
                    onClick: (_e: any, props: any) => {
                        if (
                            !props.scope.details ||
                            props.scope.details.length === 0
                        ) {
                            toast["error"](
                                "Please select the products you want to outbound",
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
    }
}

const schema = {
    type: "page",
    title: "outboundOrder.title",
    toolbar: [],
    initApi: "post:/mdm/config/dictionary/getAll",
    body: [
        {
            type: "crud",
            syncLocation: false,
            name: "OutboundOrderTable",
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
                    orderBy: "update_time desc"
                }
            },
            autoFillHeight: true,
            autoGenerateFilter: {
                columnsNum: 3,
                showBtnToolbar: true
            },
            headerToolbar: [
                {
                    type: "columns-toggler",
                    draggable: true,
                    overlay: true,
                    icon: "fas fa-cog",
                    hideExpandIcon: false,
                    size: "sm"
                },
                "reload",
                {
                    type: "export-csv",
                    label: "button.exportOrder",
                    method: "POST",
                    api: {
                        method: "POST",
                        url:
                            "/search/search?page=${1}&perPage=${100000}&warehouseCode-op=eq&warehouseCode=" +
                            warehouseCode,
                        dataType: "application/json"
                    },
                    filename: "outbound_plan_orders"
                },
                {
                    type: "export-csv",
                    label: "button.exportDetail",
                    method: "POST",
                    api: {
                        method: "POST",
                        url:
                            "/search/search?page=${1}&perPage=${100000}&warehouseCode-op=eq&warehouseCode=" +
                            warehouseCode,
                        dataType: "application/json",
                        data: {
                            searchIdentity: searchDetailIdentity,
                            showColumns: showDetailColumns,
                            searchObject: {
                                tables: "w_outbound_plan_order_detail a left join w_outbound_plan_order b on a.outbound_plan_order_id = b.id"
                            }
                        }
                    },
                    filename: "outbound_plan_order_details",
                    exportColumns: JSON.parse(JSON.stringify(showDetailColumns))
                }
                // add
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
