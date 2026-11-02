import schema2component from "@/utils/schema2component"
import {create_update_columns} from "@/utils/commonContants"
import {api_crud_search} from "@/pages/constantApi"

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
    {
        name: "templateCode",
        label: "printRecord.templateCode"
    },
    {
        name: "templateName",
        label: "printRecord.templateName"
    },
    {
        name: "workStationId",
        label: "printRecord.workStationId"
    },
    {
        name: "printTime",
        label: "printRecord.printTime",
        type: "datetime",
        tpl: "${printTime/1000|date:YYYY-MM-DD HH\\:mm\\:ss}"
    },
    {
        name: "printer",
        label: "printRecord.printer"
    },
    {
        name: "printStatus",
        label: "printRecord.printStatus",
        type: "mapping",
        source: "${dictionary.PrintStatusEnum}"
    },
    {
        name: "errorMessage",
        label: "printRecord.errorMessage",
        type: "tpl",
        tpl: "${errorMessage}",
        className: "owes-text-danger"
    },
    ...create_update_columns
]

const searchIdentity = "PPrintRecord"
const showColumns = columns

const schema = {
    type: "page",
    title: "printRecord.title",
    toolbar: [],
    data: {
        dictionary: "${ls:dictionary}"
    },
    body: [
        {
            type: "crud",
            syncLocation: false,
            name: "PPrintRecord",
            api: api_crud_search,
            defaultParams: {
                searchIdentity: searchIdentity,
                showColumns: showColumns,
                searchObject: {
                    orderBy: "create_time desc"
                }
            },
            autoFillHeight: true,
            filterTogglable: true,
            filterDefaultVisible: true,
            filter: {
                title: "common.filter",
                mode: "inline",
                body: [
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
                        type: "input-text"
                    },
                    {
                        label: "printRecord.printTime",
                        name: "printTime",
                        type: "input-date-range",
                        valueFormat: "x"
                    }
                ],
                actions: [
                    { type: "reset", label: "button.reset" },
                    {
                        type: "submit",
                        level: "primary",
                        label: "common.search"
                    }
                ]
            },
            headerToolbar: ["reload"],
            footerToolbar: ["switch-per-page", "statistics", "pagination"],
            placeholder: "emptyState.printRecord",
            columns: [
                ...columns
            ]
        }
    ]
}

export default schema2component(schema)
