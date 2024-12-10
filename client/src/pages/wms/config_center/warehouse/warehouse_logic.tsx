import schema2component from "@/utils/schema2component"
import { create_update_columns, enable_options } from "@/utils/commonContants"
import { warehouse_area_id } from "@/pages/wms/constants/select_search_api_contant"
import {api_warehouse_area_logic_add} from "@/pages/wms/config_center/constants/api_constant";

let warehouseCode = localStorage.getItem("warehouseCode")

const form = {
    type: "form",
    api: api_warehouse_area_logic_add,
    body: [
        {
            type: "hidden",
            name: "id"
        },
        {
            type: "hidden",
            name: "version"
        },
        {
            label: "table.reservoirAreaCode",
            name: "warehouseAreaId",
            type: "select",
            source: warehouse_area_id,
            required: true
        },
        {
            label: "table.logicalAreaEncoding",
            name: "warehouseLogicCode",
            type: "input-text",
            required: true
        },
        {
            label: "table.logicalAreaName",
            name: "warehouseLogicName",
            type: "input-text",
            required: true
        },
        {
            label: "table.warehouseLogicType",
            name: "warehouseLogicType",
            type: "select",
            source: "${WarehouseLogicType}"
        },
        {
            label: "table.remark",
            name: "remark",
            type: "input-text"
        },
        {
            label: "table.status",
            name: "enable",
            type: "switch",
            value: true,
            options: enable_options,
            required: true
        },
        {
            label: "table.warehouseCode",
            name: "warehouseCode",
            type: "hidden",
            value: warehouseCode
        }
    ]
}
const add = {
    type: "button",
    actionType: "drawer",
    icon: "fa fa-plus",
    label: "button.add",
    target: "role",
    drawer: {
        title: "button.add",
        closeOnEsc: true,
        body: form
    }
}

const columns = [
    {
        name: "id",
        label: "ID",
        hidden: true
    },
    {
        name: "version",
        label: "Version",
        hidden: true
    },
    {
        name: "warehouseCode",
        label: "table.warehouseCode",
        hidden: true
    },
    {
        name: "warehouseAreaId",
        label: "table.reservoirAreaCode",
        type: "mapping",
        source: warehouse_area_id,
        searchable: {
            type: "select",
            source: warehouse_area_id
        }
    },
    {
        name: "warehouseLogicCode",
        label: "table.logicalAreaEncoding",
        searchable: true
    },
    {
        name: "warehouseLogicName",
        label: "table.logicalAreaName"
    },
    {
        name: "warehouseLogicType",
        label: "table.warehouseLogicType",
        type: "mapping",
        source: "${WarehouseLogicType}"
    },
    {
        name: "remark",
        label: "table.remark"
    },
    {
        name: "enable",
        label: "table.status",
        type: "mapping",
        map: enable_options
    },
    ...create_update_columns
]

const searchIdentity = "WWarehouseLogic"
const showColumns = columns

const schema = {
    type: "page",
    title: "logicalAreaManagement.title",
    toolbar: [],
    initApi: "post:/mdm/config/dictionary/getAll",
    body: [
        {
            type: "crud",
            syncLocation: false,
            name: "role",
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
            headerToolbar: [add],
            footerToolbar: ["switch-per-page", "statistics", "pagination"],
            columns: [
                ...columns,
                {
                    type: "operation",
                    label: "table.operation",
                    width: 100,
                    buttons: [
                        {
                            label: "button.modify",
                            type: "button",
                            actionType: "drawer",
                            drawer: {
                                title: "button.modify",
                                closeOnEsc: true,
                                closeOnOutside: true,
                                body: form
                            }
                        }
                    ],
                    toggled: true
                }
            ]
        }
    ]
}

export default schema2component(schema)
