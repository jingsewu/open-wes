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
import { api_crud_search_by_warehouseCode } from "@/pages/constantApi"

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
            source: "${dictionary.LocationType}"
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
    level: "primary",
    icon: "fa fa-plus",
    label: "button.add",
    target: "role",
    drawer: {
        size: "lg",
        title: "locationManagement.addTitle",
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
        source: warehouse_area_id
    },
    {
        name: "warehouseLogicId",
        label: "table.logicalAreaEncoding",
        type: "mapping",
        source: warehouse_logic_id
    },
    {
        name: "aisleCode",
        label: "table.roadwayCoding"
    },
    {
        name: "shelfCode",
        label: "table.shelfCoding"
    },
    {
        name: "locationCode",
        label: "table.locationCode"
    },
    {
        name: "locationType",
        label: "table.locationType",
        source: "${dictionary.LocationType}"
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
    toolbar: [add],
    data: {
        dictionary: "${ls:dictionary}"
    },
    body: [
        {
            type: "crud",
            syncLocation: false,
            name: "role",
            api: api_crud_search_by_warehouseCode,
            defaultParams: {
                searchIdentity: searchIdentity,
                showColumns: showColumns
            },
            autoFillHeight: true,
            filterTogglable: true,
            filterDefaultVisible: true,
            filter: {
                title: "common.filter",
                mode: "inline",
                body: [
                    {
                        label: "table.locationCode",
                        name: "locationCode",
                        type: "input-text"
                    },
                    {
                        label: "table.reservoirAreaCode",
                        name: "warehouseAreaId",
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
                        label: "table.shelfCoding",
                        name: "shelfCode",
                        type: "input-text"
                    }
                ],
                actions: [
                    { type: "reset", label: "button.reset" },
                    {
                        type: "submit",
                        level: "primary",
                        label: "common.search"
                    }
                ]
            },
            headerToolbar: ["reload"],
            footerToolbar: ["switch-per-page", "statistics", "pagination"],
            placeholder: "emptyState.locationManagement",
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
                                size: "lg",
                                title: "locationManagement.editTitle",
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
