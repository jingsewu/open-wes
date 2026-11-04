import schema2component from "@/utils/schema2component"
import {
    api_print_config_add,
    api_print_config_get
} from "@/pages/wms/config_center/constants/api_constant"
import { enable_options } from "@/utils/commonContants"
import { api_crud_search } from "@/pages/constantApi"
import {
    select_print_rule_code,
    work_station
} from "@/pages/wms/constants/select_search_api_contant"
import {
    addButton,
    auditColumns,
    codeColumn,
    crudPage,
    editButton,
    formDrawer
} from "@/utils/amis/presets"

const form = [
    {
        type: "input-text",
        name: "id",
        label: "ID",
        hidden: true
    },
    {
        type: "input-text",
        name: "configCode",
        label: "printConfig.configCode",
        required: true
    },
    {
        name: "workStationId",
        label: "printConfig.workStationId",
        type: "select",
        source: work_station,
        required: true
    },
    {
        type: "combo",
        name: "printConfigDetails",
        label: "printConfig.details",
        multiple: true,
        required: true,
        items: [
            {
                type: "select",
                name: "ruleCode",
                label: "printConfig.ruleCode",
                source: select_print_rule_code,
                required: true
            },
            {
                type: "input-text",
                name: "printer",
                label: "printConfig.printer",
                required: true
            }
        ]
    },
    {
        type: "switch",
        name: "enabled",
        label: "printConfig.enabled",
        options: enable_options
    }
]

const addDrawer = formDrawer({
    title: "printConfig.addTitle",
    size: "lg",
    columnCount: 1,
    body: form,
    api: api_print_config_add,
    reload: "PPrintConfig"
})

const editDrawer = formDrawer({
    title: "printConfig.editTitle",
    size: "md",
    columnCount: 1,
    context: "${configCode}",
    body: form,
    initApi: api_print_config_get,
    api: api_print_config_add,
    reload: "PPrintConfig"
})

const columns = [
    {
        name: "id",
        label: "ID",
        hidden: true
    },
    codeColumn("configCode", "printConfig.configCode", { searchable: true }),
    {
        name: "workStationId",
        label: "printConfig.workStationId",
        type: "mapping",
        source: work_station,
        searchable: {
            type: "select",
            source: work_station
        }
    },
    {
        name: "enabled",
        label: "printConfig.enabled",
        type: "mapping",
        map: enable_options
    },
    ...auditColumns()
]

const schema = crudPage({
    title: "printConfig.title",
    name: "PPrintConfig",
    searchIdentity: "PPrintConfig",
    api: api_crud_search,
    columns,
    orderBy: "update_time desc",
    pageActions: [addButton({ target: "PPrintConfig", drawer: addDrawer })],
    rowActions: [editButton(editDrawer)],
    empty: "emptyState.printConfig"
})

export default schema2component(schema)
