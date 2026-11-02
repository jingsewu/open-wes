import schema2component from "@/utils/schema2component"
import {
    container_spec,
    warehouse_area_id,
    work_station
} from "@/pages/wms/constants/select_search_api_contant"
import {
    api_empty_container_outbound_add,
    api_empty_container_outbound_cancel,
    api_empty_container_outbound_execute
} from "@/pages/wms/data_center/constants/api_constant"
import { api_crud_search_by_warehouseCode } from "@/pages/constantApi"
import {
    addButton,
    auditColumns,
    codeColumn,
    crudPage,
    detailButton,
    formDrawer,
    linkAction,
    nestedTable,
    numberColumn,
    statusColumn
} from "@/utils/amis/presets"
import {
    emptyContainerOutboundDetailStatus,
    emptyContainerOutboundStatus
} from "@/utils/amis/statusMaps"

const warehouseCode = localStorage.getItem("warehouseCode")

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

const addDrawer = formDrawer({
    title: "menu.emptyContainerOutboundOrder",
    body: form,
    api: api_empty_container_outbound_add,
    reload: "EmptyContainerOutboundOrderTable",
    preventEnterSubmit: true
})

const columns = [
    {
        name: "id",
        label: "ID",
        hidden: true
    },
    codeColumn("orderNo", "table.orderNo", { searchable: true }),
    codeColumn("containerSpecCode", "table.containerSpecificationNumber", {
        searchable: true
    }),
    numberColumn("planCount", "table.plannedQuantity"),
    numberColumn("actualCount", "table.actualCountQuantity"),
    {
        name: "workStationId",
        label: "table.workstationName",
        source: work_station
    },
    statusColumn("emptyContainerOutboundStatus", "table.status", {
        ...emptyContainerOutboundStatus,
        searchable: {
            type: "select",
            source: "${dictionary.EmptyContainerOutboundOrderStatus}"
        }
    }),
    ...auditColumns(),
    {
        name: "warehouseCode",
        label: "table.warehouseCode",
        hidden: true
    }
]

const detailColumns = [
    {
        name: "emptyContainerOutboundOrderId",
        hidden: true
    },
    codeColumn("containerCode", "table.containerCode"),
    statusColumn(
        "detailStatus",
        "table.status",
        emptyContainerOutboundDetailStatus
    )
]

const detailDrawer = {
    size: "xl",
    title: "emptyContainerOutboundOrder.detail.modal.title",
    closeOnEsc: true,
    closeOnOutside: true,
    actions: [
        {
            type: "button",
            actionType: "close",
            label: "button.close"
        }
    ],
    body: [
        {
            type: "tpl",
            tpl: "${orderNo}",
            className: "owes-drawer-context"
        },
        nestedTable({
            name: "EmptyContainerOutboundOrderDetailTable",
            api: {
                method: "POST",
                url: "/search/search?page=${page}&perPage=${perPage}&emptyContainerOutboundOrderId=${id}&emptyContainerOutboundOrderId-op=eq",
                dataType: "application/json"
            },
            searchIdentity: "WEmptyContainerOutboundOrderDetail",
            columns: detailColumns,
            empty: "emptyState.emptyContainerOutboundDetail"
        })
    ]
}

const bulkActions = [
    linkAction({
        label: "button.emptyContainerOutbound.execute",
        confirmText: "confirm.title.emptyContainerOutbound.execute",
        api: {
            method: "post",
            url: api_empty_container_outbound_execute,
            data: {
                orderIds: "${ARRAYMAP(selectedItems, item => item.id)}"
            }
        },
        reload: "EmptyContainerOutboundOrderTable"
    }),
    linkAction({
        label: "button.emptyContainerOutbound.cancel",
        confirmText: "confirm.title.emptyContainerOutbound.cancel",
        danger: true,
        api: {
            method: "post",
            url: api_empty_container_outbound_cancel,
            data: {
                orderIds: "${ARRAYMAP(selectedItems, item => item.id)}"
            }
        },
        reload: "EmptyContainerOutboundOrderTable"
    })
]

const schema = crudPage({
    title: "menu.emptyContainerOutboundOrder",
    name: "EmptyContainerOutboundOrderTable",
    searchIdentity: "WEmptyContainerOutboundOrder",
    api: api_crud_search_by_warehouseCode,
    columns,
    orderBy: "empty_container_outbound_status, update_time desc",
    pageActions: [
        addButton({
            target: "EmptyContainerOutboundOrderTable",
            drawer: addDrawer
        })
    ],
    bulkActions,
    rowActions: [detailButton(detailDrawer)],
    empty: "emptyState.emptyContainerOutbound"
})

export default schema2component(schema)
