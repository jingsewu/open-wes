import schema2component from "@/utils/schema2component"
import {
    put_wall_spec,
    work_station
} from "@/pages/wms/constants/select_search_api_contant"
import { enable_options } from "@/utils/commonContants"
import {
    api_put_wall_add,
    api_put_wall_delete,
    api_put_wall_get
} from "@/pages/wms/config_center/constants/api_constant"
import { api_crud_search_by_warehouseCode } from "@/pages/constantApi"
import {
    addButton,
    auditColumns,
    codeColumn,
    crudPage,
    deleteButton,
    editButton,
    formDrawer
} from "@/utils/amis/presets"

const warehouseCode = localStorage.getItem("warehouseCode")

const putWallSlotColumns = [
    {
        name: "id",
        hidden: true
    },
    {
        name: "workStationId",
        hidden: true
    },
    {
        name: "putWallId",
        hidden: true
    },
    {
        label: "table.putWallSlotCode",
        name: "putWallSlotCode",
        value: "${putWallSlotCode == null ? containerSlotSpecCode : putWallSlotCode}"
    },
    {
        label: "table.ptlTag",
        name: "ptlTag",
        type: "input-text",
        required: true
    },
    {
        label: "face",
        name: "face",
        hidden: true
    },
    {
        label: "table.putWallLevel",
        name: "level"
    },
    {
        label: "table.putWallBay",
        name: "bay"
    },
    {
        label: "locLevel",
        name: "locLevel",
        hidden: true
    },
    {
        label: "locBay",
        name: "locBay",
        hidden: true
    },
    {
        label: "table.enable",
        name: "enable",
        value: true,
        type: "switch"
    }
]

const formApi = {
    url: api_put_wall_add,
    requestAdaptor: (api: any, context: any) => {
        return {
            ...api,
            data: {
                ...api.data,
                warehouseCode: warehouseCode,
                putWallSlots: api.data.putWallSlots.map((slot: any) => {
                    return {
                        ...slot,
                        workStationId: api.data.workStationId,
                        putWallCode: api.data.putWallCode,
                        putWallSlotCode:
                            slot.putWallSlotCode ?? slot.containerSlotSpecCode,
                        putWallSlotStatus: "IDLE"
                    }
                })
            }
        }
    }
}

const formBody = [
    {
        type: "hidden",
        name: "id"
    },
    {
        type: "hidden",
        name: "warehouseCode"
    },
    {
        type: "hidden",
        name: "version"
    },
    {
        type: "hidden",
        name: "putWallStatus",
        value: "IDLE"
    },
    {
        label: "table.seedingWallCoding",
        name: "putWallCode",
        type: "input-text",
        disabledOn: "${id != null}",
        required: true
    },
    {
        label: "table.seedingWallName",
        name: "putWallName",
        type: "input-text",
        disabledOn: "${id != null}",
        required: true
    },
    {
        label: "table.workstationBelongs",
        name: "workStationId",
        type: "select",
        disabledOn: "${id != null}",
        source: work_station,
        required: true
    },
    {
        label: "table.seedingWallSpecifications",
        name: "containerSpecCode",
        type: "select",
        source: put_wall_spec,
        required: true,
        onEvent: {
            change: {
                actions: [
                    {
                        componentId: "putWallSlots",
                        actionType: "setValue",
                        args: {
                            value: "${event.data.selectedItems.containerSlotSpecs|toJson}"
                        }
                    }
                ]
            }
        }
    },
    {
        label: "table.seedingWallLocation",
        name: "location",
        type: "select",
        source: "${dictionary.PutWallLocation}",
        required: true
    },
    {
        name: "enable",
        type: "switch",
        label: "table.whetherEnabled"
    },
    {
        id: "putWallSlots",
        name: "putWallSlots",
        type: "input-table",
        columnsTogglable: false,
        columns: putWallSlotColumns
    }
]

const addDrawer = formDrawer({
    title: "seedingWallManagement.addTitle",
    size: "lg",
    columnCount: 1,
    body: formBody,
    api: formApi,
    reload: "PutWallTable"
})

const editDrawer = formDrawer({
    title: "seedingWallManagement.editTitle",
    size: "lg",
    columnCount: 1,
    context: "${putWallCode} · ${putWallName}",
    body: formBody,
    initApi: api_put_wall_get,
    api: api_put_wall_add,
    reload: "PutWallTable"
})

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
        name: "workStationId",
        label: "table.workstationBelongs",
        type: "mapping",
        source: work_station
    },
    codeColumn("putWallCode", "table.seedingWallCoding", {
        searchable: true
    }),
    {
        name: "putWallName",
        label: "table.seedingWallName",
        searchable: true
    },
    codeColumn("containerSpecCode", "table.seedingWallSpecifications"),
    {
        name: "enable",
        label: "table.enable",
        type: "mapping",
        map: enable_options
    },
    ...auditColumns()
]

const schema = crudPage({
    title: "seedingWallManagement.title",
    name: "PutWallTable",
    searchIdentity: "WPutWall",
    api: api_crud_search_by_warehouseCode,
    columns,
    searchObject: {
        where: "deleted = false"
    },
    orderBy: "update_time desc",
    pageActions: [addButton({ target: "PutWallTable", drawer: addDrawer })],
    rowActions: [
        editButton(editDrawer),
        deleteButton({
            api: api_put_wall_delete,
            confirmText: "confirm.deletePutWall",
            reload: "PutWallTable"
        })
    ],
    empty: "emptyState.seedingWall"
})

export default schema2component(schema)
