import schema2component from "@/utils/schema2component"
import {
    container_spec,
    warehouse_area_id,
    warehouse_logic_code
} from "@/pages/wms/constants/select_search_api_contant"
import { true_false_options } from "@/utils/commonContants"
import {
    addButton,
    auditColumns,
    codeColumn,
    crudPage,
    exportButton,
    formDrawer,
    requiredField,
    statusColumn
} from "@/utils/amis/presets"
import { api_container_batch_add } from "@/pages/wms/config_center/constants/api_constant"
import {
    api_crud_search_by_warehouseCode,
    api_crud_search_by_warehouseCode_total
} from "@/pages/constantApi"

const warehouseCode = localStorage.getItem("warehouseCode")

const formBody = [
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
        source: "${dictionary.ContainerType}",
        ...requiredField("validation.container.typeRequired")
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
        ...requiredField("validation.container.specRequired")
    },
    {
        label: "table.numberOfDigits",
        type: "input-text",
        name: "indexNumber",
        ...requiredField("validation.container.indexNumberRequired")
    },
    {
        label: "table.numberingPrefix",
        type: "input-text",
        name: "containerCodePrefix",
        ...requiredField("validation.container.prefixRequired")
    },
    {
        label: "table.startNumber",
        type: "input-number",
        name: "startIndex",
        ...requiredField("validation.container.startNumberRequired")
    },
    {
        label: "table.createdNumber",
        type: "input-text",
        name: "createNumber",
        ...requiredField("validation.container.countRequired")
    },
    {
        type: "hidden",
        name: "warehouseCode",
        label: "table.warehouseCode",
        value: warehouseCode
    }
]

const addDrawer = formDrawer({
    title: "containerManagement.batchCreateTitle",
    size: "lg",
    columnCount: 1,
    body: formBody,
    api: api_container_batch_add,
    reload: "ContainerTable"
})

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
    {
        name: "version",
        label: "Version",
        hidden: true
    },
    codeColumn("containerCode", "table.containerCode", { searchable: true }),
    {
        ...codeColumn(
            "containerSpecCode",
            "workLocationArea.containerSpecification"
        ),
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
    statusColumn("containerStatus", "table.containerStatus", {
        labels: {
            IN_SIDE: "status.containerInSide",
            OUT_SIDE: "status.containerOutSide"
        },
        tones: {
            IN_SIDE: "success",
            OUT_SIDE: "muted"
        }
    }),
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
    codeColumn("locationCode", "table.locationCode", { searchable: true }),
    ...auditColumns()
]

const schema = crudPage({
    title: "containerManagement.title",
    name: "ContainerTable",
    searchIdentity: "WContainer",
    api: api_crud_search_by_warehouseCode,
    columns,
    orderBy: "update_time desc",
    pageActions: [
        addButton({
            target: "ContainerTable",
            label: "button.batchCreation",
            drawer: addDrawer
        })
    ],
    tableActions: [
        exportButton({
            api: api_crud_search_by_warehouseCode_total,
            filename: "container"
        })
    ],
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
    empty: "emptyState.containerManagement"
})

export default schema2component(schema)
