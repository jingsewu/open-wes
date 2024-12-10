import schema2component from "@/utils/schema2component"
import {put_wall_spec, work_station} from "@/pages/wms/constants/select_search_api_contant"
import {create_update_columns} from "@/utils/commonContants"
import {
    api_put_wall_add,
    api_put_wall_delete,
    api_put_wall_get
} from "@/pages/wms/config_center/constants/api_constant";

let warehouseCode = localStorage.getItem("warehouseCode")

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
    }];

const formApi = {
    url: api_put_wall_add ,
    requestAdaptor: (api: any, context: any) => {
        return {
            ...api,
            data: {
                ...api.data,
                "warehouseCode": warehouseCode,
                putWallSlots: api.data.putWallSlots.map((slot: any) => {
                    return {
                        ...slot,
                        workStationId: api.data.workStationId,
                        putWallCode: api.data.putWallCode,
                        putWallSlotCode: slot.putWallSlotCode ? slot.putWallSlotCode : slot.containerSlotSpecCode,
                        putWallSlotStatus: 'IDLE'
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
        source: "${PutWallLocation}",
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

const add = {
    type: "button",
    actionType: "dialog",
    icon: "fa fa-plus",
    label: "button.add",
    target: "PutWallTable",
    dialog: {
        title: "button.add",
        size: "lg",
        body: {
            type: "form",
            api: formApi,
            body: formBody
        }
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
        name: "workStationId",
        label: "table.workstationBelongs",
        type: "mapping",
        source: work_station
    },
    {
        name: "putWallCode",
        label: "table.seedingWallCoding",
        searchable: true
    },
    {
        name: "putWallName",
        label: "table.seedingWallName",
        searchable: true
    },
    {
        name: "containerSpecCode",
        label: "table.seedingWallSpecifications"
    },
    {
        name: "enable",
        label: "table.enable",
        type: "mapping",
        source: "${EnableStatus}",
    },
    ...create_update_columns
]

const searchIdentity = "WPutWall"
const showColumns = columns

const schema = {
    type: "page",
    title: "seedingWallManagement.title",
    toolbar: [],
    initApi: "post:/mdm/config/dictionary/getAll",
    body: [
        {
            type: "crud",
            syncLocation: false,
            name: "PutWallTable",
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
                    where: "deleted = false",
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
                            actionType: "dialog",
                            dialog: {
                                title: "button.modify",
                                size: "lg",
                                body: {
                                    type: "form",
                                    api: api_put_wall_add,
                                    initApi: api_put_wall_get,
                                    body: formBody
                                }
                            }
                        },
                        {
                            label: "button.delete",
                            type: "button",
                            actionType: "ajax",
                            level: "danger",
                            confirmText: "toast.sureDelete",
                            confirmTitle: "button.delete",
                            api: api_put_wall_delete,
                            reload: "PutWallTable"
                        }
                    ],
                    toggled: true
                }
            ]
        }
    ]
}

export default schema2component(schema)
