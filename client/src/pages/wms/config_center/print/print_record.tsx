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
        source: "${dictionary.ModuleEnum}",
        searchable: { type: "select", source: "${dictionary.ModuleEnum}" }
    },
    {
        name: "printNode",
        label: "printRecord.printNode",
        type: "mapping",
        source: "${dictionary.PrintNodeEnum}",
        searchable: { type: "select", source: "${dictionary.PrintNodeEnum}" }
    },
    codeColumn("templateCode", "printRecord.templateCode", {
        searchable: true
    }),
    {
        name: "templateName",
        label: "printRecord.templateName"
    },
    {
        name: "workStationId",
        label: "printRecord.workStationId",
        type: "mapping",
        source: work_station,
        searchable: { type: "select", source: work_station }
    },
    dateTimeColumn("printTime", "printRecord.printTime", {
        seconds: true,
        searchable: { type: "input-date-range", valueFormat: "x" }
    }),
    {
        name: "printer",
        label: "printRecord.printer",
        searchable: true
    },
    statusColumn("printStatus", "printRecord.printStatus", {
        ...printStatus,
        searchable: {
            type: "select",
            source: "${dictionary.PrintStatusEnum}"
        }
    }),
    {
        name: "errorMessage",
        label: "printRecord.errorMessage",
        type: "tpl",
        tpl: "${errorMessage}",
        className: "owes-text-danger"
    },
    ...auditColumns({ visible: true, createdOnly: true })
]

const schema = crudPage({
    title: "printRecord.title",
    name: "PPrintRecord",
    searchIdentity: "PPrintRecord",
    api: api_crud_search,
    columns,
    orderBy: "create_time desc",
    empty: "emptyState.printRecord"
})

export default schema2component(schema)
