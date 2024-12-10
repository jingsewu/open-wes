import schema2component from "@/utils/schema2component"
import {container_spec, warehouse_area_id, warehouse_logic_code} from "@/pages/wms/constants/select_search_api_contant"
import {create_update_columns, true_false_options} from "@/utils/commonContants"
import {api_container_batch_add} from "@/pages/wms/config_center/constants/api_constant"

let warehouseCode = localStorage.getItem("warehouseCode")

const fromBody = [
    {
        type: "hidden",
        name: "id"
    },
    {
        type: "hidden",
        name: "version"
    },
    {
        label: "table.containerType",
        type: "select",
        name: "containerType",
        source: "${ContainerType}",
        required: true
    },
    {
        label: "workLocationArea.containerSpecification",
        type: "select",
        name: "containerSpecCode",
        initFetchOn: "data.containerType",
        source: {
            ...container_spec,
            url:
                container_spec.url +
                "&containerType-op=eq&containerType=${containerType}"
        },
        required: true
    },
    {
        label: "table.numberOfDigits",
        type: "input-text",
        name: "indexNumber",
        required: true
    },
    {
        label: "table.numberingPrefix",
        type: "input-text",
        name: "containerCodePrefix",
        required: true
    },
    {
        label: "table.startNumber",
        type: "input-number",
        name: "startIndex",
        required: true
    },
    {
        label: "table.createdNumber",
        type: "input-text",
        name: "createNumber",
        required: true
    },
    {
        type: "hidden",
        name: "warehouseCode",
        label: "table.warehouseCode",
        value: warehouseCode
    }
]

const form = {
    type: "form",
    api: api_container_batch_add,
    body: fromBody
}

const add = {
    type: "button",
    actionType: "drawer",
    icon: "fa fa-plus",
    label: "button.batchCreation",
    target: "ContainerTable",
    drawer: {
        size: "lg",
        title: "button.batchCreation",
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
        name: "containerCode",
        label: "table.containerCode",
        searchable: true
    },
    {
        name: "containerSpecCode",
        label: "workLocationArea.containerSpecification",
        type: "mapping",
        source: container_spec,
        searchable: {
            type: "select",
            source: {
                ...container_spec,
                url:
                    container_spec.url +
                    "&containerType-op=il&containerType=SHELF,CONTAINER"
            }
        }
    },
    {
        name: "containerStatus",
        label: "table.containerStatus",
        type: "mapping",
        source: "${ContainerStatus}"
    },
    {
        name: "warehouseAreaId",
        label: "table.warehouseAreaName",
        type: "mapping",
        source: warehouse_area_id
    },
    {
        name: "warehouseLogicCode",
        label: "table.warehouseLogicName",
        type: "mapping",
        source: warehouse_logic_code
    },
    {
        name: "emptyContainer",
        label: "table.emptyContainers",
        type: "mapping",
        map: true_false_options,
        searchable: {
            type: "select",
            options: true_false_options
        }
    },
    {
        name: "locked",
        label: "table.lock",
        type: "mapping",
        map: true_false_options
    },
    {
        name: "locationCode",
        label: "table.locationCode",
        searchable: true
    },
    ...create_update_columns
]

const searchIdentity = "WContainer"
const showColumns = columns

const schema = {
    type: "page",
    title: "containerManagement.title",
    toolbar: [],
    initApi: "post:/mdm/config/dictionary/getAll",
    body: [
        {
            type: "crud",
            name: "ContainerTable",
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
            autoGenerateFilter: {
                columnsNum: 3,
                showBtnToolbar: true
            },
            bulkActions: [
                {
                    label: "button.batchChangeWarehouseLogic",
                    type: "button",
                    onEvent: {
                        click: {
                            actions: [
                                {
                                    actionType: "dialog",
                                    dialog: {
                                        title: "batchChangeWarehouseArea.title",
                                        body: {
                                            type: "form",
                                            api: "post:/wms/container/changeWarehouseLogicArea",
                                            reload: "ContainerTable",
                                            body: [
                                                {
                                                    type: "select",
                                                    label: "table.warehouseLogicName",
                                                    name: "warehouseLogicCode",
                                                    source: warehouse_logic_code
                                                },
                                                {
                                                    type: "hidden",
                                                    name: "containerIds",
                                                    value: '${SPLIT(ids, ",")}'
                                                }
                                            ]
                                        }
                                    }
                                }
                            ]
                        }
                    }
                }
            ],
            headerToolbar: [
                "reload",
                add,
                "bulkActions",
                {
                    type: "export-excel",
                    label: "button.export",
                    api: {
                        method: "POST",
                        url:
                            "/search/search?page=${1}&perPage=${100000}&createTime-op=bt&warehouseCode-op=eq&warehouseCode=" +
                            warehouseCode,
                        dataType: "application/json"
                    },
                    filename: "container"
                }
            ],
            footerToolbar: ["switch-per-page", "statistics", "pagination"],
            columns: [
                ...columns
                // {
                //     type: "operation",
                //     label: "table.operation",
                //     width: 100,
                //     buttons: [],
                //     toggled: true
                // }
            ]
        }
    ]
}

export default schema2component(schema)
