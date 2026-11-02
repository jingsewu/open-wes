import schema2component from "@/utils/schema2component"
import {
    container_spec,
    warehouse_area_id
} from "@/pages/wms/constants/select_search_api_contant"
import { true_false_options } from "@/utils/commonContants"
import { api_transfer_container_release } from "@/pages/wms/config_center/constants/api_constant"
import {
    api_crud_search_by_warehouseCode,
    api_crud_search_by_warehouseCode_total
} from "@/pages/constantApi"
import {
    codeColumn,
    crudPage,
    dateTimeColumn,
    exportButton,
    linkAction,
    statusColumn,
    auditColumns
} from "@/utils/amis/presets"
import { transferContainerStatus } from "@/utils/amis/statusMaps"

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
    codeColumn("transferContainerCode", "table.containerCode", {
        searchable: true
    }),
    {
        ...codeColumn("containerSpecCode", "workLocationArea.containerSpecification"),
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
    statusColumn(
        "transferContainerStatus",
        "table.containerStatus",
        transferContainerStatus
    ),
    dateTimeColumn("lockedTime", "table.lockedTime"),
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
    codeColumn("locationCode", "table.locationCode", { searchable: true }),
    ...auditColumns()
]

const schema = crudPage({
    title: "transferContainerManagement.title",
    name: "TransferContainerTable",
    searchIdentity: "WTransferContainer",
    api: api_crud_search_by_warehouseCode,
    columns,
    orderBy: "update_time desc",
    tableActions: [
        exportButton({
            api: api_crud_search_by_warehouseCode_total,
            filename: "transferContainer"
        })
    ],
    bulkActions: [
        linkAction({
            label: "button.release",
            api: api_transfer_container_release,
            confirmText: "modal.release",
            reload: "TransferContainerTable"
        })
    ],
    empty: "emptyState.transferContainer"
})

export default schema2component(schema)
