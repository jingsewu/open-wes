import schema2component from "@/utils/schema2component"
import {container_spec, warehouse_area_id} from "@/pages/wms/constants/select_search_api_contant"
import {create_update_columns, true_false_options} from "@/utils/commonContants"
import {api_transfer_container_release} from "@/pages/wms/config_center/constants/api_constant"
import {api_crud_search_by_warehouseCode, api_crud_search_by_warehouseCode_total} from "@/pages/constantApi"

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
        name: "transferContainerCode",
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
        name: "transferContainerStatus",
        label: "table.containerStatus",
        type: "mapping",
        source: "${ls:dictionary|pick:TransferContainerStatus}"
    },
    {
        name: "lockedTime",
        label: "table.lockedTime",
        tpl: "${lockedTime/1000|date:YYYY-MM-DD HH\\:mm\\:ss}"
    },
    {
        name: "warehouseAreaId",
        label: "table.warehouseAreaName",
        type: "mapping",
        source: warehouse_area_id
    },
    {
        name: "virtualContainer",
        label: "table.virtualContainer",
        type: "mapping",
        map: true_false_options,
        searchable: {
            type: "select",
            options: true_false_options
        }
    },
    {
        name: "locationCode",
        label: "table.locationCode",
        searchable: true
    },
    ...create_update_columns
]

const searchIdentity = "WTransferContainer"
const schema = {
    type: "page",
    title: "transferContainerManagement.title",
    toolbar: [],
    body: [
        {
            type: "crud",
            name: "TransferContainerTable",
            api: api_crud_search_by_warehouseCode,
            defaultParams: {
                searchIdentity: searchIdentity,
                showColumns: columns,
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
                    label: "button.release",
                    actionType: "ajax",
                    api: api_transfer_container_release,
                    confirmText: "modal.release"
                }
            ],
            headerToolbar: [
                "reload",
                "bulkActions",
                {
                    type: "export-excel",
                    label: "button.export",
                    api: api_crud_search_by_warehouseCode_total,
                    filename: "transferContainer"
                }
            ],
            footerToolbar: ["switch-per-page", "statistics", "pagination"],
            columns: [
                ...columns
            ]
        }
    ]
}

export default schema2component(schema)
