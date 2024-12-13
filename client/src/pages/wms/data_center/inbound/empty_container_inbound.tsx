import schema2component from "@/utils/schema2component"
import { container_spec } from "@/pages/wms/constants/select_search_api_contant"
import { create_update_columns } from "@/utils/commonContants"
import { api_empty_container_inbound_add } from "@/pages/wms/data_center/constants/api_constant"
import {
    api_getDictionary,
    api_crud_search_by_warehouseCode
} from "@/pages/constantApi"

let warehouseCode = localStorage.getItem("warehouseCode")

const form = [
    {
        label: "workLocationArea.containerSpecification",
        type: "select",
        name: "containerSpecCode",
        source: {
            ...container_spec,
            url:
                container_spec.url +
                "&containerType-op=il&containerType=SHELF,CONTAINER"
        },
        onEvent: {
            change: {
                actions: [{ actionType: "focus", componentId: "containerCode" }]
            }
        }
    },
    {
        id: "containerCode",
        label: "table.containerCode",
        type: "input-text",
        name: "containerCode",
        options: [{ label: "", value: "" }],
        onEvent: {
            enter: {
                actions: [{ actionType: "focus", componentId: "locationCode" }]
            }
        }
    },
    {
        id: "locationCode",
        label: "table.locationCode",
        type: "input-text",
        name: "locationCode"
    }
]

const add = {
    type: "button",
    actionType: "dialog",
    icon: "fa fa-plus",
    label: "button.add",
    target: "emptyContainerInboundOrderTable",
    dialog: {
        title: "menu.emptyContainerInboundOrder",
        closeOnEsc: true,
        body: {
            id: "inputForm",
            type: "form",
            api: api_empty_container_inbound_add + warehouseCode,
            preventEnterSubmit: true,
            body: form
        }
    }
}

const columns = [
    {
        name: "id",
        label: "ID",
        hidden: true
    },
    {
        name: "orderNo",
        label: "table.orderNo",
        searchable: true
    },
    {
        name: "inboundWay",
        label: "table.inboundMethod",
        source: "${EmptyContainerInboundWay}",
        searchable: {
            type: "select",
            source: "${EmptyContainerInboundWay}"
        }
    },
    {
        name: "planCount",
        label: "table.plannedQuantity",
        searchable: true
    },
    {
        name: "inboundStatus",
        // label: "状态",
        label: "table.status",
        type: "mapping",
        source: "${PutAwayTaskStatus}",
        searchable: {
            type: "select",
            source: "${PutAwayTaskStatus}"
        }
    },
    ...create_update_columns,
    {
        name: "warehouseCode",
        label: "table.warehouseCode",
        hidden: true
    }
]

const detailColumns = [
    {
        name: "emptyContainerInboundOrderId",
        label: "空箱入库单id",
        hidden: true
    },
    {
        name: "containerCode",
        label: "table.containerCode"
    },
    {
        name: "containerSpecCode",
        label: "workLocationArea.containerSpecification"
    },
    {
        name: "locationCode",
        label: "table.locationCode"
    },
    {
        name: "inboundStatus",
        label: "table.status",
        type: "mapping",
        source: "${PutAwayTaskStatus}"
    }
]

const searchIdentity = "WEmptyContainerInboundOrder"
const searchDetailIdentity = "WEmptyContainerInboundOrderDetail"
const showColumns = columns
const showDetailColumns = detailColumns

const detailDialog = {
    title: "emptyContainerInboundOrder.detail.modal.title",
    actions: [],
    closeOnEsc: true,
    closeOnOutside: true,
    size: "xl",
    body: [
        {
            type: "crud",
            syncLocation: false,
            name: "emptyContainerInboundOrderDetailTable",
            api: {
                method: "POST",
                url: "/search/search?page=${page}&perPage=${perPage}&emptyContainerInboundOrderId=${id}&emptyContainerInboundOrderId-op=eq",
                dataType: "application/json"
            },
            defaultParams: {
                searchIdentity: searchDetailIdentity,
                showColumns: showDetailColumns
            },
            footerToolbar: ["switch-per-page", "statistics", "pagination"],
            columns: detailColumns
        }
    ]
}

const schema = {
    type: "page",
    title: "menu.emptyContainerInboundOrder",
    toolbar: [],
    initApi: api_getDictionary,
    body: [
        {
            type: "crud",
            syncLocation: false,
            name: "emptyContainerInboundOrderTable",
            api: api_crud_search_by_warehouseCode,
            defaultParams: {
                searchIdentity: searchIdentity,
                showColumns: showColumns,
                searchObject: {
                    orderBy: "inbound_status, update_time desc"
                }
            },
            autoFillHeight: true,
            autoGenerateFilter: {
                columnsNum: 3,
                showBtnToolbar: true
            },
            headerToolbar: [add],
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
