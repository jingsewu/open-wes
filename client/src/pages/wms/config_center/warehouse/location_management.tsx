import schema2component from "@/utils/schema2component"
import {
    warehouse_area_id,
    warehouse_logic_id
} from "@/pages/wms/constants/select_search_api_contant"
import {
    create_update_columns,
    enable_options,
    true_false_options
} from "@/utils/commonContants"

let warehouseCode = localStorage.getItem("warehouseCode")

const form = {
    type: "form",
    api: {
        url: "/wms/warehouseLogic/create"
    },
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
            name: "warehouseAreaCode",
            type: "select",
            source: warehouse_area_id
        },
        {
            label: "table.warehouseLogicName",
            name: "warehouseLogicId",
            type: "select",
            source: warehouse_logic_id
        },
        {
            label: "table.roadwayCoding",
            name: "aisleCode",
            type: "input-text"
        },
        {
            label: "table.locationType",
            name: "locationType",
            type: "select",
            source: "${LocationType}"
        },
        {
            label: "table.locationCode",
            name: "locationCode",
            type: "input-text"
        },
        {
            label: "table.locationHeat",
            name: "heat",
            type: "input-text"
        },
        {
            label: "table.occupation",
            name: "occupied",
            type: "switch",
            options: true_false_options
        },
        {
            label: "table.remark",
            name: "remark",
            type: "input-text"
        },
        {
            label: "table.locationStatus",
            name: "locationStatus",
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
        name: "warehouseAreaId",
        label: "table.warehouseLogicName",
        type: "mapping",
        source: warehouse_area_id,
        searchable: {
            type: "select",
            source: warehouse_area_id
        }
    },
    {
        name: "warehouseLogicId",
        label: "table.logicalAreaEncoding",
        type: "mapping",
        source: warehouse_logic_id,
        searchable: {
            type: "select",
            source: warehouse_logic_id
        }
    },
    {
        name: "aisleCode",
        label: "table.roadwayCoding",
        searchable: true
    },
    {
        name: "shelfCode",
        label: "table.shelfCoding",
        searchable: true
    },
    {
        name: "locationCode",
        label: "table.locationCode",
        searchable: true
    },
    {
        name: "locationType",
        label: "table.locationType",
        source: "${LocationType}"
    },
    {
        name: "heat",
        label: "table.locationHeat"
    },
    {
        name: "occupied",
        label: "table.occupation"
    },
    {
        name: "position",
        hidden: true
    },
    {
        name: "locationStatus",
        label: "table.locationStatus",
        type: "mapping",
        map: enable_options
    },
    ...create_update_columns
]

const searchIdentity = "WLocation"
const showColumns = columns

const schema = {
    type: "page",
    title: "locationManagement.title",
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
                showColumns: showColumns
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
                    label: "x",
                    tpl: "<%=JSON.parse(data.position)?.x || '-'%>"
                },
                {
                    label: "y",
                    tpl: "<%=JSON.parse(data.position)?.y || '-'%>"
                },
                {
                    label: "z",
                    tpl: "<%=JSON.parse(data.position)?.z || '-'%>"
                },
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
