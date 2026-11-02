import schema2component from "@/utils/schema2component"
import {
    auditColumns,
    codeColumn,
    crudPage,
    detailButton,
    exportButton,
    importButton,
    linkAction,
    numberColumn,
    remarkColumn,
    statusColumn
} from "@/utils/amis/presets"
import { detailDrawer } from "./detail"
import {
    api_crud_search_by_warehouseCode,
    api_crud_search_by_warehouseCode_total
} from "@/pages/constantApi"
import { api_inbound_plan_order_close } from "@/pages/wms/data_center/constants/api_constant"
import { inboundPlanOrderStatus } from "@/utils/amis/statusMaps"

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
    codeColumn("lpnCode", "table.LPNNo", { searchable: true }),
    codeColumn("customerOrderNo", "table.customerOrderNo", {
        searchable: true
    }),
    statusColumn(
        "inboundPlanOrderStatus",
        "table.status",
        {
            ...inboundPlanOrderStatus,
            searchable: {
                type: "select",
                source: "${dictionary.InboundPlanOrderStatus}"
            }
        }
    ),
    codeColumn("orderNo", "table.orderNo", { searchable: true }),
    {
        name: "sender",
        label: "table.shipper",
        searchable: true
    },
    numberColumn("skuKindNum", "table.skuTypes", { searchable: true }),
    {
        name: "storageType",
        label: "table.storageType",
        type: "mapping",
        source: "${dictionary.StorageType}",
        searchable: {
            type: "select",
            source: "${dictionary.StorageType}"
        }
    },
    numberColumn("totalBox", "table.boxesNumber"),
    numberColumn("totalQty", "table.totalQuantity"),
    codeColumn("trackingNumber", "table.theTrackingNumber", {
        searchable: true
    }),
    {
        name: "shippingMethod",
        label: "table.modeOfCarriage",
        searchable: true
    },
    ...auditColumns(),
    remarkColumn()
]

const closeAction = linkAction({
    label: "button.close",
    danger: true,
    confirmText: "confirm.close",
    api: {
        method: "post",
        url: api_inbound_plan_order_close,
        data: "${ARRAYMAP(selectedItems, item => item.id)}"
    },
    reload: "InboundPlanOrderTable"
})

const importDialog = {
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
                                className:
                                    "shadow-sm hover:shadow-md transition-shadow",
                                header: {
                                    title: "importExcel.downloadTemplate",
                                    subTitle: "importExcel.ensureFormat",
                                    avatar: {
                                        type: "icon",
                                        icon: "fa fa-download",
                                        className:
                                            "text-success bg-success-light p-2 rounded-full"
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
                                                silent: true
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
                                className:
                                    "shadow-sm hover:shadow-md transition-shadow",
                                header: {
                                    title: "importExcel.uploadFile",
                                    subTitle: "importExcel.supportedFormats",
                                    avatar: {
                                        type: "icon",
                                        icon: "fa fa-upload",
                                        className:
                                            "text-info bg-info-light p-2 rounded-full"
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
                                                        data: {
                                                            file: "${file}"
                                                        },
                                                        messages: {
                                                            success:
                                                                "importExcel.uploadSuccess",
                                                            failed:
                                                                "importExcel.uploadFailed"
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
                                                            description:
                                                                "importExcel.fileRestrictions",
                                                            btnLabel:
                                                                "importExcel.fileUploadCTA",
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

const schema = crudPage({
    title: "menu.inboundOrder",
    name: "InboundPlanOrderTable",
    searchIdentity: "WInboundPlanOrder",
    api: api_crud_search_by_warehouseCode,
    columns,
    orderBy: "id ,inbound_plan_order_status desc",
    pageActions: [],
    tableActions: [
        importButton({
            label: "importExcel.label",
            dialog: importDialog
        }),
        exportButton({
            api: api_crud_search_by_warehouseCode_total,
            filename: "inbound_plan_order",
            method: "POST",
            defaultParams: {
                searchIdentity: "WInboundPlanOrder",
                showColumns: columns
            }
        })
    ],
    bulkActions: [closeAction],
    rowActions: [detailButton(detailDrawer)],
    operationWidth: 80,
    empty: "emptyState.inboundPlanOrder"
})

export default schema2component(schema)
