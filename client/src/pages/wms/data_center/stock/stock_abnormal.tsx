import schema2component from "@/utils/schema2component"

let warehouseCode = localStorage.getItem("warehouseCode")

const columns = [
    {
        dbField: "r.id",
        name: "id",
        label: "ID",
        hidden: true
    },
    {
        dbField: "r.order_no",
        name: "abnormalOrderNo",
        label: "table.exceptionTrackingNumber",
        searchable: true
    },
    {
        dbField: "r.stock_abnormal_type",
        name: "stockAbnormalType",
        label: "table.exceptionType",
        type: "mapping",
        source: "${StockAbnormalType}",
        searchable: {
            type: "select",
            source: "${StockAbnormalType}"
        }
    },
    {
        dbField: "r.abnormal_order_no",
        name: "orderNo",
        label: "原始单号",
        hidden: true
    },
    {
        dbField: "r.replay_no",
        name: "replayNo",
        label: "table.reviewOrderNumber"
    },
    {
        dbField: "r.stock_abnormal_status",
        name: "stockAbnormalStatus",
        label: "table.status",
        type: "mapping",
        source: "${StockAbnormalStatus}",
        searchable: {
            type: "select",
            source: "${StockAbnormalStatus}"
        }
    },
    {
        dbField: "r.warehouse_code",
        name: "warehouseCode",
        label: "table.warehouse"
    },

    {
        dbField: "r.container_code",
        name: "containerCode",
        label: "table.containerCode",
        searchable: true
    },
    {
        dbField: "r.container_slot_code",
        name: "containerSlotCode",
        label: "table.containerSlotCode"
    },
    {
        dbField: "g.bar_code_list",
        name: "barCodeList",
        label: "skuArea.barcode",
        searchable: true
    },
    {
        dbField: "r.sku_code",
        name: "skuCode",
        label: "skuArea.skuCode"
    },

    {
        dbField: "r.sku_id",
        name: "skuId",
        label: "商品id",
        hidden: true
    },
    {
        dbField: "r.qty_abnormal",
        name: "qtyAbnormal",
        label: "table.profit"
    },
    {
        dbField: "r.abnormal_reason",
        name: "abnormalReason",
        label: "table.differenceReason",
        type: "mapping",
        source: "${StockAbnormalReason}"
    },
    {
        dbField: "r.reason_desc",
        name: "reasonDesc",
        label: "table.causeDescription"
    },
    {
        dbField: "r.container_stock_id",
        name: "containerStockId",
        label: "库存id",
        hidden: true
    },

    {
        dbField: "r.sku_batch_stock_id",
        name: "skuBatchStockId",
        label: "table.lotNumber"
    },

    {
        dbField: "r.sku_batch_attribute_id",
        name: "skuBatchAttributeId",
        label: "批次属性id",
        hidden: true
    },
    {
        dbField: "r.create_time",
        name: "createTime",
        label: "table.creationTime",
        tpl: "${createTime/1000|date:YYYY-MM-DD HH\\:mm\\:ss}",
        searchable: {
            type: "input-date-range",
            valueFormat: "x"
        }
    },
    {
        dbField: "r.update_time",
        name: "updateTime",
        label: "table.updated",
        tpl: "${updateTime/1000|date:YYYY-MM-DD HH\\:mm\\:ss}"
    }
]

const searchIdentity = "WStockAbnormalRecord"
const showColumns = columns

const schema = {
    type: "page",
    title: "inventorAnomalies.title",
    toolbar: [],
    initApi: "post:/mdm/config/dictionary/getAll",
    body: [
        {
            type: "crud",
            syncLocation: false,
            name: "paramConfigTable",
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
                    tables: "w_stock_abnormal_record r left join (select sku_id, group_concat(distinct bar_code separator ' ') bar_code_list from m_sku_barcode_data group by sku_id) g on r.sku_id = g.sku_id",
                    orderBy: "r.create_time desc"
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
                    api:
                        "post:/search/search?page=${1}&perPage=${100000}&createTime-op=bt&warehouseCode-op=eq&warehouseCode=" +
                        warehouseCode,
                    fileName: "stock_abnormal"
                }
            ],
            footerToolbar: ["switch-per-page", "statistics", "pagination"],
            columns: [
                ...columns,
                {
                    type: "operation",
                    label: "table.operation",
                    width: 130,
                    buttons: [
                        {
                            label: "button.close",
                            type: "button",
                            level: "link",
                            disabledOn: "${stockAbnormalStatus !== 'NEW'}",
                            actionType: "dialog",
                            dialog: {
                                title: "toast.prompt",
                                body: "toast.sureToCloseOrder",
                                actions: [
                                    {
                                        label: "button.cancel",
                                        actionType: "cancel",
                                        type: "button"
                                    },
                                    {
                                        label: "button.confirm",
                                        actionType: "ajax",
                                        primary: true,
                                        type: "button",
                                        api: {
                                            method: "post",
                                            url: "/wms/stock/abnormal/record/manualClose",
                                            data: ["${id}"]
                                        },
                                        close: true,
                                        reload: "paramConfigTable"
                                    }
                                ]
                            }
                        },
                        {
                            label: "button.adjustInventoryOrder",
                            type: "button",
                            level: "link",
                            disabledOn: "${stockAbnormalStatus !== 'NEW'}",
                            actionType: "dialog",
                            dialog: {
                                title: "toast.prompt",
                                body: "toast.confirmGenerateOrder",
                                actions: [
                                    {
                                        label: "button.cancel",
                                        actionType: "cancel",
                                        type: "button"
                                    },
                                    {
                                        label: "button.confirm",
                                        actionType: "ajax",
                                        primary: true,
                                        type: "button",
                                        api: {
                                            method: "post",
                                            url: "/wms/stock/abnormal/record/createAdjustmentOrder",
                                            data: ["${id}"]
                                        },
                                        close: true,
                                        reload: "paramConfigTable"
                                    }
                                ]
                            }
                        },
                        // {
                        //     label: "button.takeInventoryAgain",
                        //     type: "button",
                        //     level: "link",
                        //     disabledOn: "${stockAbnormalStatus !== 'NEW'}",
                        //     actionType: "dialog",
                        //     dialog: {
                        //         title: "toast.prompt",
                        //         body: "toast.confirmRecount",
                        //         actions: [
                        //             {
                        //                 label: "button.cancel",
                        //                 actionType: "cancel",
                        //                 // primary: true,
                        //                 type: "button"
                        //             },
                        //             {
                        //                 label: "button.confirm",
                        //                 actionType: "ajax",
                        //                 primary: true,
                        //                 type: "button",
                        //                 api: {
                        //                     method: "post",
                        //                     url: "/wms/stock/abnormal/record/createRecheckOrder",
                        //                     data: ["${id}"]
                        //                 },
                        //                 close: true,
                        //                 reload: "paramConfigTable"
                        //             }
                        //         ]
                        //     }
                        // }
                    ],
                    toggled: true
                }
            ]
        }
    ]
}

export default schema2component(schema)
