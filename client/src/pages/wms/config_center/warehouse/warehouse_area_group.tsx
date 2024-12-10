import schema2component from "@/utils/schema2component"
import {
    create_update_columns,
    enable_options,
    true_false_options
} from "@/utils/commonContants"
import {api_warehouse_area_group_add} from "@/pages/wms/config_center/constants/api_constant";

let warehouseCode = localStorage.getItem("warehouseCode")

const form = {
    type: "form",
    api: api_warehouse_area_group_add,
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
            name: "warehouseAreaGroupCode",
            type: "input-text",
            required: true
        },
        {
            label: "table.warehouseAreaName",
            name: "warehouseAreaGroupName",
            type: "input-text",
            required: true
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
        name: "warehouseAreaGroupCode",
        label: "table.warehouseAreaEncoding",
        searchable: true
    },
    {
        name: "warehouseAreaGroupName",
        label: "table.warehouseAreaName"
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

const searchIdentity = "WWarehouseAreaGroup"
const showColumns = columns

const schema = {
    type: "page",
    title: "warehouseAreaManagement.title",
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
                                closeOnEsc: true,
                                closeOnOutside: true,
                                title: "button.modify",
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
