import schema2component from "@/utils/schema2component"
import { api_crud_search } from "@/pages/constantApi"
import { work_station } from "@/pages/wms/constants/select_search_api_contant"
import {
    auditColumns,
    codeColumn,
    crudPage,
    dateTimeColumn,
    statusColumn
} from "@/utils/amis/presets"
import { printStatus } from "@/utils/amis/statusMaps"

const columns = [
    {
        name: "id",
        label: "ID",
        hidden: true
    },
    {
        name: "module",
        label: "printRecord.module",
        type: "mapping",
        source: "${dictionary.ModuleEnum}"
    },
    {
        name: "printNode",
        label: "printRecord.printNode",
        type: "mapping",
        source: "${dictionary.PrintNodeEnum}"
    },
    codeColumn("templateCode", "printRecord.templateCode"),
    {
        name: "templateName",
        label: "printRecord.templateName"
    },
    {
        name: "workStationId",
        label: "printRecord.workStationId",
        type: "mapping",
        source: work_station
    },
    dateTimeColumn("printTime", "printRecord.printTime", { seconds: true }),
    {
        name: "printer",
        label: "printRecord.printer"
    },
    statusColumn("printStatus", "printRecord.printStatus", printStatus),
    {
        name: "errorMessage",
        label: "printRecord.errorMessage",
        type: "tpl",
        tpl: "${errorMessage}",
        className: "owes-text-danger"
    },
    ...auditColumns({ visible: true, createdOnly: true })
]

const filters = [
    {
        label: "printRecord.templateCode",
        name: "templateCode",
        type: "input-text"
    },
    {
        label: "printRecord.printStatus",
        name: "printStatus",
        type: "select",
        source: "${dictionary.PrintStatusEnum}"
    },
    {
        label: "printRecord.module",
        name: "module",
        type: "select",
        source: "${dictionary.ModuleEnum}"
    },
    {
        label: "printRecord.printNode",
        name: "printNode",
        type: "select",
        source: "${dictionary.PrintNodeEnum}"
    },
    {
        label: "printRecord.printer",
        name: "printer",
        type: "input-text"
    },
    {
        label: "printRecord.workStationId",
        name: "workStationId",
        type: "select",
        source: work_station
    },
    {
        label: "printRecord.printTime",
        name: "printTime",
        type: "input-date-range",
        valueFormat: "x"
    }
]

const schema = crudPage({
    title: "printRecord.title",
    name: "PPrintRecord",
    searchIdentity: "PPrintRecord",
    api: api_crud_search,
    columns,
    filters,
    orderBy: "create_time desc",
    empty: "emptyState.printRecord"
})

export default schema2component(schema)
