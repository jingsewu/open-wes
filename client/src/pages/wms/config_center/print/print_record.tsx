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
        source: "${dictionary.ModuleEnum}",
        searchable: true
    },
    {
        name: "printNode",
        label: "printRecord.printNode",
        type: "mapping",
        source: "${dictionary.PrintNodeEnum}",
        searchable: true
    },
    {
        name: "templateCode",
        label: "printRecord.templateCode",
        searchable: true
    },
    {
        name: "templateName",
        label: "printRecord.templateName"
    },
    {
        name: "workStationId",
        label: "printRecord.workStationId",
        searchable: true
    },
    {
        name: "printTime",
        label: "printRecord.printTime",
        type: "datetime",
        tpl: "${createTime/1000|date:YYYY-MM-DD HH\\:mm\\:ss}",
        searchable: {
            type: "input-date-range",
            valueFormat: "x"
        }
    },
    {
        name: "printer",
        label: "printRecord.printer",
        searchable: true
    },
    {
        name: "printStatus",
        label: "printRecord.printStatus",
        type: "mapping",
        source: "${dictionary.PrintStatusEnum}",
        searchable: true
    },
    {
        name: "errorMessage",
        label: "printRecord.errorMessage",
        type: "html",
        html: "<span class='text-danger'>${errorMessage}</span>"
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
            autoGenerateFilter: {
                columnsNum: 3,
                showBtnToolbar: true
            },
            footerToolbar: ["switch-per-page", "statistics", "pagination"],
            columns: [
                ...columns
            ]
        }
    ]
}

export default schema2component(schema)
