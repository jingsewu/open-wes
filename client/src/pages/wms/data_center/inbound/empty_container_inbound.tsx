import schema2component from "@/utils/schema2component"
import { container_spec } from "@/pages/wms/constants/select_search_api_contant"
import { api_empty_container_inbound_add } from "@/pages/wms/data_center/constants/api_constant"
import { api_crud_search_by_warehouseCode } from "@/pages/constantApi"
import {
    addButton,
    auditColumns,
    codeColumn,
    crudPage,
    detailButton,
    formDrawer,
    nestedTable,
    numberColumn,
    statusColumn
} from "@/utils/amis/presets"
import { putAwayTaskStatus } from "@/utils/amis/statusMaps"

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

const addDrawer = formDrawer({
    title: "menu.emptyContainerInboundOrder",
    body: form,
    api: api_empty_container_inbound_add,
    reload: "EmptyContainerInboundOrderTable",
    preventEnterSubmit: true
})

const detailColumns = [
    {
        name: "emptyContainerInboundOrderId",
        hidden: true
    },
    codeColumn("containerCode", "table.containerCode"),
    {
        name: "containerSpecCode",
        label: "workLocationArea.containerSpecification"
    },
    codeColumn("locationCode", "table.locationCode"),
    statusColumn("inboundStatus", "table.status", putAwayTaskStatus)
]

const detailDrawer = {
    size: "xl",
    title: "emptyContainerInboundOrder.detail.modal.title",
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
            name: "EmptyContainerInboundOrderDetailTable",
            api: {
                method: "POST",
                url: "/search/search?page=${page}&perPage=${perPage}&emptyContainerInboundOrderId=${id}&emptyContainerInboundOrderId-op=eq",
                dataType: "application/json"
            },
            searchIdentity: "WEmptyContainerInboundOrderDetail",
            columns: detailColumns,
            empty: "emptyState.emptyContainerInboundDetail"
        })
    ]
}

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
    codeColumn("orderNo", "table.orderNo", { searchable: true }),
    {
        name: "inboundWay",
        label: "table.inboundMethod",
        source: "${dictionary.EmptyContainerInboundWay}",
        searchable: {
            type: "select",
            source: "${dictionary.EmptyContainerInboundWay}"
        }
    },
    numberColumn("planCount", "table.plannedQuantity", { searchable: true }),
    statusColumn("inboundStatus", "table.status", {
        ...putAwayTaskStatus,
        searchable: {
            type: "select",
            source: "${dictionary.PutAwayTaskStatus}"
        }
    }),
    ...auditColumns()
]

const schema = crudPage({
    title: "menu.emptyContainerInboundOrder",
    name: "EmptyContainerInboundOrderTable",
    searchIdentity: "WEmptyContainerInboundOrder",
    api: api_crud_search_by_warehouseCode,
    columns,
    orderBy: "inbound_status, update_time desc",
    pageActions: [
        addButton({
            target: "EmptyContainerInboundOrderTable",
            drawer: addDrawer
        })
    ],
    rowActions: [detailButton(detailDrawer)],
    empty: "emptyState.emptyContainerInbound"
})

export default schema2component(schema)
