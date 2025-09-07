import schema2component from "@/utils/schema2component"
import {
    container_spec,
    warehouse_area_id,
    warehouse_logic_code
} from "@/pages/wms/constants/select_search_api_contant"
import {
    create_update_columns,
    true_false_options
} from "@/utils/commonContants"
import {
    api_crud_search_by_warehouseCode,
} from "@/pages/constantApi"

let warehouseCode = localStorage.getItem("warehouseCode")

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
        source: "${dictionary.ContainerStatus}"
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

const call_container = {
    type: "button",
    label: "button.callContainer",
    actionType: "ajax",
    api: {
        url: "/station/api?apiCode=CALL_CONTAINER",
        method: "put",
        data: {
            containerCodes: "${ARRAYMAP(selectedItems, item => item.containerCode)}",
            warehouseCode: warehouseCode
        }
    },
    confirmText: "confirm.callContainer",
    disabledOn: "!selectedItems || selectedItems.length === 0"
}

const searchIdentity = "WContainer"
const showColumns = columns

const schema = {
    type: "page",
    title: "containerManagement.title",
    toolbar: [],
    data: {
        dictionary: "${ls:dictionary}"
    },
    body: [
        {
            type: "crud",
            name: "ContainerTable",
            api: api_crud_search_by_warehouseCode,
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
            headerToolbar: [
                "reload",
                call_container
            ],
            multiple: true,
            selectable: true,
            keepItemSelectionOnPageChange: false,
            primaryField: "id",
            footerToolbar: ["switch-per-page", "statistics", "pagination"],
            columns: [
                ...columns
            ]
        }
    ]
}

export default schema2component(schema)
