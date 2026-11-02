import schema2component from "@/utils/schema2component"
import {
    auditColumns,
    codeColumn,
    numberColumn,
    operationColumn
} from "@/utils/amis/presets"
import {detailDrawer} from "./detail"
import {
    api_crud_search_by_warehouseCode,
    api_crud_search_by_warehouseCode_total
} from "@/pages/constantApi"
import {api_inbound_plan_order_close} from "@/pages/wms/data_center/constants/api_constant";

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
    codeColumn("lpnCode", "table.LPNNo"),
    codeColumn("customerOrderNo", "table.customerOrderNo"),
    {
        name: "inboundPlanOrderStatus",
        label: "table.status",
        type: "mapping",
        source: "${dictionary.InboundPlanOrderStatus}"
    },

    codeColumn("orderNo", "table.orderNo"),
    {
        name: "sender",
        label: "table.shipper"
    },
    numberColumn("skuKindNum", "table.skuTypes"),
    {
        name: "storageType",
        label: "table.storageType",
        type: "mapping",
        source: "${dictionary.StorageType}"
    },
    numberColumn("totalBox", "table.boxesNumber"),
    numberColumn("totalQty", "table.totalQuantity"),
    codeColumn("trackingNumber", "table.theTrackingNumber"),
    {
        name: "shippingMethod",
        label: "table.modeOfCarriage"
    },
    ...auditColumns(),
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

const close = {
    label: "button.close",
    actionType: "ajax",
    api: {
        method: "post",
        url: api_inbound_plan_order_close,
        data: "${ARRAYMAP(selectedItems, item => item.id)}"
    },
    confirmText: "confirm.close"
}

const import_excel = {
    type: "button",
    label: "importExcel.label",
    icon: "fa fa-upload",
    actionType: "dialog",
    dialog: {
        title: "importExcel.dialogTitle",
        size: "lg",
        body: {
            type: "container",
            body: [
                {
                    type: "tpl",
                    tpl: "importExcel.chooseAction",
                    className: "text-lg font-bold mb-4"
                },
                {
                    type: "grid",
                    columns: [
                        {
                            md: 6,
                            body: [
                                {
                                    type: "card",
                                    className: "shadow-sm hover:shadow-md transition-shadow",
                                    header: {
                                        title: "importExcel.downloadTemplate",
                                        subTitle: "importExcel.ensureFormat",
                                        avatar: {
                                            type: "icon",
                                            icon: "fa fa-download",
                                            className: "text-success bg-success-light p-2 rounded-full"
                                        }
                                    },
                                    body: {
                                        type: "container",
                                        body: [
                                            {
                                                type: "tpl",
                                                tpl: "importExcel.templateDescription",
                                                className: "text-gray-600 mb-3"
                                            },
                                            {
                                                type: "action",
                                                label: "importExcel.downloadTemplate",
                                                actionType: "download",
                                                api: {
                                                    method: "POST",
                                                    url: "/wms/inbound/plan/download",
                                                    responseType: "blob",
                                                    silent: true,
                                                },
                                                icon: "fa fa-download",
                                                className: "w-full",
                                                feedback: {
                                                    enable: false
                                                },
                                                reload: "none"
                                            }
                                        ]
                                    }
                                }
                            ]
                        },
                        {
                            md: 6,
                            body: [
                                {
                                    type: "card",
                                    className: "shadow-sm hover:shadow-md transition-shadow",
                                    header: {
                                        title: "importExcel.uploadFile",
                                        subTitle: "importExcel.supportedFormats",
                                        avatar: {
                                            type: "icon",
                                            icon: "fa fa-upload",
                                            className: "text-info bg-info-light p-2 rounded-full"
                                        }
                                    },
                                    body: {
                                        type: "container",
                                        body: [
                                            {
                                                type: "tpl",
                                                tpl: "importExcel.uploadDescription",
                                                className: "text-gray-600 mb-3"
                                            },
                                            {
                                                type: "button",
                                                label: "importExcel.selectFile",
                                                actionType: "dialog",
                                                level: "primary",
                                                icon: "fa fa-upload",
                                                className: "w-full",
                                                dialog: {
                                                    title: "importExcel.uploadDialogTitle",
                                                    size: "md",
                                                    body: {
                                                        type: "form",
                                                        api: {
                                                            method: "post",
                                                            url: "/wms/inbound/plan/import",
                                                            data: {file: "${file}"},
                                                            messages: {
                                                                success: "importExcel.uploadSuccess",
                                                                failed: "importExcel.uploadFailed"
                                                            }
                                                        },
                                                        body: [
                                                            {
                                                                type: "alert",
                                                                level: "info",
                                                                body: "importExcel.fileRequirements",
                                                                className: "mb-4"
                                                            },
                                                            {
                                                                type: "input-file",
                                                                name: "file",
                                                                asBase64: false,
                                                                asBlob: true,
                                                                label: "importExcel.selectExcelFile",
                                                                accept: ".xlsx,.xls",
                                                                maxSize: 10485760,
                                                                required: true,
                                                                multiple: false,
                                                                drag: true,
                                                                description: "importExcel.fileRestrictions",
                                                                btnLabel: "importExcel.fileUploadCTA",
                                                                className: "mb-4"
                                                            }
                                                        ]
                                                    },
                                                    actions: [
                                                        {
                                                            type: "button",
                                                            actionType: "cancel",
                                                            label: "common.cancel",
                                                            className: "mr-2"
                                                        },
                                                        {
                                                            type: "button",
                                                            actionType: "submit",
                                                            label: "common.upload",
                                                            level: "primary",
                                                            icon: "fa fa-cloud-upload"
                                                        }
                                                    ]
                                                }
                                            }
                                        ]
                                    }
                                }
                            ]
                        }
                    ]
                },
                {
                    type: "divider",
                    className: "my-6"
                },
                {
                    type: "alert",
                    level: "warning",
                    body: [
                        {
                            type: "tpl",
                            tpl: "importExcel.importantNotes"
                        },
                        {
                            type: "tpl",
                            tpl: "importExcel.notesList",
                            className: "owes-note-list"
                        }
                    ],
                    className: "mt-4"
                }
            ]
        },
        actions: [
            {
                type: "button",
                actionType: "cancel",
                label: "common.close",
                icon: "fa fa-times"
            }
        ]
    }
};

const searchIdentity = "WInboundPlanOrder"
const showColumns = columns

const schema = {
    type: "page",
    title: "menu.inboundOrder",
    toolbar: [],
    data: {
        dictionary: "${ls:dictionary}"
    },
    body: [
        {
            type: "crud",
            syncLocation: false,
            name: "inboundPlanOrderTable",
            silentPolling: true,
            api: api_crud_search_by_warehouseCode,
            defaultParams: {
                searchIdentity: searchIdentity,
                showColumns: showColumns,
                searchObject: {
                    orderBy: "id ,inbound_plan_order_status desc"
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
                        label: "table.orderNo",
                        name: "orderNo",
                        type: "input-text"
                    },
                    {
                        label: "table.customerOrderNo",
                        name: "customerOrderNo",
                        type: "input-text"
                    },
                    {
                        label: "table.LPNNo",
                        name: "lpnCode",
                        type: "input-text"
                    },
                    {
                        label: "table.theTrackingNumber",
                        name: "trackingNumber",
                        type: "input-text"
                    },
                    {
                        label: "table.status",
                        name: "inboundPlanOrderStatus",
                        type: "select",
                        source: "${dictionary.InboundPlanOrderStatus}"
                    },
                    {
                        label: "table.storageType",
                        name: "storageType",
                        type: "select",
                        source: "${dictionary.StorageType}"
                    },
                    {
                        label: "table.shipper",
                        name: "sender",
                        type: "input-text"
                    },
                    {
                        label: "table.modeOfCarriage",
                        name: "shippingMethod",
                        type: "input-text"
                    },
                    {
                        label: "table.skuTypes",
                        name: "skuKindNum",
                        type: "input-text"
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
            multiple: true,
            headerToolbar: [
                "reload",
                import_excel,
                {
                    type: "export-excel",
                    label: "button.export",
                    method: "POST",
                    api: api_crud_search_by_warehouseCode_total,
                    filename: "inbound_plan_order",
                    defaultParams: {
                        searchIdentity: searchIdentity,
                        showColumns: showColumns
                    }
                },
                "bulkActions"
            ],
            bulkActions: [
                close
            ],
            footerToolbar: ["switch-per-page", "statistics", "pagination"],
            placeholder: "emptyState.inboundPlanOrder",
            columns: [
                ...columns,
                operationColumn(
                    [
                        {
                            label: "button.detail",
                            type: "button",
                            level: "link",
                            className: "owes-action",
                            actionType: "drawer",
                            drawer: detailDrawer
                        }
                    ],
                    { width: 80 }
                )
            ]
        }
    ]
}

export default schema2component(schema)
