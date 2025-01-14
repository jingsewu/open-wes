import schema2component from "@/utils/schema2component"
import {
    container_spec,
    warehouse_area_id,
    work_station
} from "@/pages/wms/constants/select_search_api_contant"
import { create_update_columns } from "@/utils/commonContants"
import {
    api_empty_container_outbound_add,
    api_empty_container_outbound_cancel,
    api_empty_container_outbound_execute
} from "@/pages/wms/data_center/constants/api_constant"
import { api_crud_search_by_warehouseCode } from "@/pages/constantApi"

let warehouseCode = localStorage.getItem("warehouseCode")

const form = [
    {
        type: "select",
        name: "warehouseAreaId",
        label: "workLocationArea.warehouseArea",
        selectFirst: true,
        source: {
            ...warehouse_area_id,
            url: warehouse_area_id.url + "&warehouseAreaWorkType=ROBOT"
        },
        required: true,
        className: "warehouseArea"
    },
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
        required: true
    },
    {
        id: "emptySlotNum",
        label: "table.emptySlotNum",
        type: "input-number",
        name: "emptySlotNum"
    },
    {
        id: "planCount",
        label: "table.planCount",
        type: "input-number",
        name: "planCount",
        required: true
    },
    {
        label: "table.workstationCoding",
        type: "select",
        name: "workStationId",
        source: work_station,
        required: true
    },
    {
        id: "warehouseCode",
        label: "table.warehouseCode",
        type: "input-text",
        name: "warehouseCode",
        value: warehouseCode,
        hidden: true
    }
]

const add = {
    type: "button",
    actionType: "dialog",
    icon: "fa fa-plus",
    label: "button.add",
    target: "emptyContainerOutboundOrderTable",
    dialog: {
        title: "menu.emptyContainerOutboundOrder",
        closeOnEsc: true,
        body: {
            id: "inputForm",
            type: "form",
            api: api_empty_container_outbound_add,
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
        name: "containerSpecCode",
        label: "table.containerSpecificationNumber",
        searchable: true
    },
    {
        name: "planCount",
        label: "table.plannedQuantity"
    },
    {
        name: "actualCount",
        label: "table.actualCountQuantity"
    },
    {
        name: "workStationId",
        label: "table.workstationName",
        source: work_station
    },
    {
        name: "emptyContainerOutboundStatus",
        label: "table.status",
        type: "mapping",
        source: "${dictionary.EmptyContainerOutboundOrderStatus}",
        searchable: {
            type: "select",
            source: "${dictionary.EmptyContainerOutboundOrderStatus}"
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
        name: "emptyContainerOutboundOrderId",
        label: "空箱出库单id",
        hidden: true
    },
    {
        name: "containerCode",
        label: "table.containerCode"
    },
    {
        name: "detailStatus",
        label: "table.status",
        type: "mapping",
        source: "${dictionary.EmptyContainerOutboundDetailStatus}"
    }
]

const searchIdentity = "WEmptyContainerOutboundOrder"
const searchDetailIdentity = "WEmptyContainerOutboundOrderDetail"
const showColumns = columns
const showDetailColumns = detailColumns

const detailDialog = {
    title: "emptyContainerOutboundOrder.detail.modal.title",
    actions: [],
    closeOnEsc: true,
    closeOnOutside: true,
    size: "xl",
    body: [
        {
            type: "crud",
            syncLocation: false,
            name: "emptyContainerOutboundOrderDetailTable",
            api: {
                method: "POST",
                url: "/search/search?page=${page}&perPage=${perPage}&emptyContainerOutboundOrderId=${id}&emptyContainerOutboundOrderId-op=eq",
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
    title: "menu.emptyContainerOutboundOrder",
    toolbar: [],
    data: {
        dictionary: "${ls:dictionary}"
    },
    body: [
        {
            type: "crud",
            syncLocation: false,
            name: "emptyContainerOutboundOrderTable",
            api: api_crud_search_by_warehouseCode,
            defaultParams: {
                searchIdentity: searchIdentity,
                showColumns: showColumns,
                searchObject: {
                    orderBy: "empty_container_outbound_status, update_time desc"
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
                    label: "button.emptyContainerOutbound.execute",
                    actionType: "ajax",
                    api: {
                        method: "post",
                        url: api_empty_container_outbound_execute,
                        data: {
                            orderIds:
                                "${ARRAYMAP(selectedItems, item => item.id)}"
                        }
                    },
                    confirmText: "confirm.title.emptyContainerOutbound.execute"
                },
                {
                    label: "button.emptyContainerOutbound.cancel",
                    actionType: "ajax",
                    api: {
                        method: "post",
                        url: api_empty_container_outbound_cancel,
                        data: {
                            orderIds:
                                "${ARRAYMAP(selectedItems, item => item.id)}"
                        }
                    },
                    confirmText: "confirm.title.emptyContainerOutbound.cancel"
                }
            ],
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
