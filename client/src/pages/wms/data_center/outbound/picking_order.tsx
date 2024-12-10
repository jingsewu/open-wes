import schema2component from "@/utils/schema2component"
import { create_update_columns } from "@/utils/commonContants"
import {Translation} from 'react-i18next';
import React from 'react';

let warehouseCode = localStorage.getItem("warehouseCode")

const columns = [
    {
        name: "id",
        label: "ID",
        hidden: true
    },
    {
        name: "warehouseCode",
        label: "table.warehouse"
    },
    {
        name: "pickingOrderNo",
        label: "table.pickOrderNumber",
        searchable: true
    },
    {
        name: "assignedStationSlot",
        label: "table.assignedLattice"
    },
    {
        name: "waveNo",
        label: "table.waveNumber"
    },
    {
        name: "pickingOrderStatus",
        label: "table.status",
        type: "mapping",
        source: "${PickingOrderStatus}",
        searchable: {
            type: "select",
            source: "${PickingOrderStatus}"
        }
    },
    {
        name: "priority",
        label: "table.priority"
    },
    ...create_update_columns
]

const detailColumns = [
    {
        name: "pickingOrderId",
        label: "拣货单ID",
        hidden: true,
        dbField: "k.picking_order_id"
    },
    {
        name: "warehouseAreaCode",
        label: "库区",
        dbField: "wa.warehouse_area_code"
    },
    {
        name: "ownerCode",
        label: "货主",
        dbField: "k.owner_code"
    },
    {
        name: "skuCode",
        label: "sku编码",
        dbField: "a.sku_code"
    },
    {
        name: "skuName",
        label: "sku名称",
        dbField: "a.sku_name"
    },
    {
        name: "requiredQty",
        label: "计划数量",
        dbField: "t.required_qty"
    },
    {
        name: "operatedQty",
        label: "实际出库数量",
        dbField: "t.operated_qty"
    },
    {
        name: "abnormalQty",
        label: "异常数量",
        dbField: "t.abnormal_qty"
    },
    {
        name: "stationCode",
        label: "出库工作站",
        dbField: "ws.station_code"
    },
    {
        name: "targetLocationCode",
        label: "出库播种墙格口",
        dbField: "t.target_location_code"
    },
    {
        name: "sourceContainerCode",
        label: "容器编码",
        dbField: "t.source_container_code"
    },
    {
        name: "sourceContainerSlot",
        label: "容器格口",
        dbField: "t.source_container_slot"
    },
    {
        name: "batchAttributes",
        label: "批次属性",
        dbField: "k.batch_attributes"
    },
    {
        name: "updateUser",
        label: "操作人",
        dbField: "t.update_user"
    },
    {
        name: "updateTime",
        label: "完成时间",
        dbField: "t.update_time",
        tpl: "${updateTime/1000|date:YYYY-MM-DD HH\\:mm\\:ss}"
    }
]

const searchIdentity = "WPickingOrder"
const searchDetailIdentity = "WPickingOperationTask"
const showColumns = columns
const showDetailColumns = detailColumns

const detailDialog = {
    title: "拣货任务详情",
    actions: [],
    closeOnEsc: true,
    closeOnOutside: true,
    size: "xl",
    body: [
        {
            type: "crud",
            syncLocation: false,
            name: "PickingOperationTaskTable",
            api: {
                method: "POST",
                url: "/search/search?page=${page}&perPage=${perPage}&pickingOrderId=${id}&pickingOrderId-op=eq",
                dataType: "application/json"
            },
            defaultParams: {
                searchIdentity: searchDetailIdentity,
                showColumns: showDetailColumns,
                searchObject: {
                    tables: "w_operation_task t, w_picking_order_detail k, m_sku_main_data a, w_picking_order p, w_warehouse_area wa, w_work_station ws",
                    where: "t.detail_id = k.id and t.sku_id = a.id and t.order_id = p.id and p.warehouse_area_id = wa.id and t.work_station_id = ws.id"
                }
            },
            footerToolbar: ["switch-per-page", "statistics", "pagination"],
            columns: detailColumns
        }
    ]
}

const schema = {
    type: "page",
    title: "pickingTasks.title",
    toolbar: [],
    initApi: "post:/mdm/config/dictionary/getAll",
    body: [
        {
            type: "crud",
            syncLocation: false,
            name: "PickingOrderTable",
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
                "reload",
                {
                    type: "export-csv",
                    label: "button.export",
                    method: "POST",
                    api: {
                        method: "POST",
                        url:
                            "/search/search?page=${1}&perPage=${100000}&createTime-op=bt&warehouseCode-op=eq&warehouseCode=" +
                            warehouseCode,
                        dataType: "application/json",
                        data: {
                            searchIdentity: searchDetailIdentity,
                            showColumns: showDetailColumns,
                            searchObject: {
                                tables: "w_operation_task t, w_picking_order_detail k, m_sku_main_data a, w_picking_order p, w_warehouse_area wa, w_work_station ws",
                                where: "t.detail_id = k.id and t.sku_id = a.id and t.order_id = p.id and p.warehouse_area_id = wa.id and t.work_station_id = ws.id"
                            }
                        }
                    },
                    filename: "picking_order_tasks",
                    exportColumns: JSON.parse(JSON.stringify(showDetailColumns))
                }
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
