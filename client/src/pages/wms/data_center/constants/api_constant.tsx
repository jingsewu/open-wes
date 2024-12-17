// data center - outbound - outbound plan order module
export const api_outbound_plan_order_add = "/wms/outbound/order/create"

export const api_empty_container_inbound_add =
    "post:/wms/inbound/empty/container/create?warehouseCode=${ls:warehouseCode}"

// data center - stock - stocktake module
export const api_stocktake_order_add = "post:/wms/stocktake/order/create"
export const api_stocktake_order_cancel = "post:/wms/stocktake/order/cancel"
export const api_stocktake_order_execute = "post:/wms/stocktake/order/execute"
export const api_stocktake_task_execute = "post:/wms/stocktake/task/execute"


export const api_empty_container_outbound_add = "post:/wms/outbound/empty/container/create"
export const api_empty_container_outbound_execute = "post:/wms/outbound/empty/container/execute"
