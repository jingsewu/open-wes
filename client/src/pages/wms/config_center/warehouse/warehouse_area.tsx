import schema2component from "@/utils/schema2component"
import {create_update_columns, enable_options} from "@/utils/commonContants"
import {warehouse_area_group} from "@/pages/wms/constants/select_search_api_contant"
import {api_warehouse_area_add} from "@/pages/wms/config_center/constants/api_constant";

let warehouseCode = localStorage.getItem("warehouseCode")

const form = {
    type: "form",
    api: api_warehouse_area_add,
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
            label: "table.warehouseAreaEncoding",
            name: "warehouseGroupCode",
            type: "select",
            source: warehouse_area_group,
            required: true
        },
        {
            label: "table.reservoirAreaCode",
            name: "warehouseAreaCode",
            type: "input-text",
            required: true
        },
        {
            label: "table.reservoirAreaName",
            name: "warehouseAreaName",
            type: "input-text",
            required: true
        },
        {
            label: "table.reservoirAreaType",
            name: "warehouseAreaType",
            type: "select",
            source: "${WarehouseAreaType}",
            required: true
        },
        {
            label: "table.reservoirAreaPurpose",
            name: "warehouseAreaUse",
            type: "select",
            source: "${WarehouseAreaUse}",
            required: true
        },
        {
            label: "table.reservoirAreaWorkType",
            name: "warehouseAreaWorkType",
            type: "select",
            source: "${WarehouseAreaWorkType}",
            required: true
        },
        {
            label: "table.floor",
            name: "level",
            type: "input-number"
        },
        {
            label: "table.temperatureLimits",
            name: "temperatureLimit",
            type: "input-number"
        },
        {
            label: "table.humidityLimitation",
            name: "wetLimit",
            type: "input-number"
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
            options: enable_options
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
        name: "warehouseGroupCode",
        label: "table.warehouseAreaEncoding",
        type: "mapping",
        source: warehouse_area_group,
        searchable: {
            type: "select",
            source: warehouse_area_group
        }
    },
    {
        name: "warehouseAreaCode",
        label: "table.reservoirAreaCode",
        searchable: true
    },
    {
        name: "warehouseAreaName",
        label: "table.reservoirAreaName"
    },
    {
        name: "warehouseAreaType",
        label: "table.reservoirAreaType",
        type: "mapping",
        source: "${WarehouseAreaType}"
    },
    {
        name: "warehouseAreaUse",
        label: "table.reservoirAreaPurpose",
        type: "mapping",
        source: "${WarehouseAreaUse}"
    },
    {
        name: "warehouseAreaWorkType",
        label: "table.reservoirAreaWorkType",
        type: "mapping",
        source: "${WarehouseAreaWorkType}",
        searchable: {
            type: "select",
            source: "${WarehouseAreaWorkType}"
        }
    },
    {
        name: "level",
        label: "table.floor"
    },
    {
        name: "temperatureLimit",
        label: "table.temperatureLimits"
    },
    {
        name: "wetLimit",
        label: "table.humidityLimitation"
    },
    {
        name: "remark",
        label: "table.remark"
    },
    {
        name: "enable",
        label: "table.status",
        type: "mapping",
        map: enable_options,
        searchable: {
            type: "select",
            options: enable_options
        }
    },
    ...create_update_columns
]

const searchIdentity = "WWarehouseArea"
const showColumns = columns

const schema = {
    type: "page",
    title: "reservoirAreaManagement.title",
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
