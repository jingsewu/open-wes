import schema2component from "@/utils/schema2component"
import { api_crud_search } from "@/pages/constantApi"
import {success_or_fail} from "@/utils/commonContants";
const columns = [
    {
        name: "id",
        label: "ID"
    },
    {
        name: "account",
        label: "userCenter.userManagement.table.loginUsername",
        searchable: true
    },
    {
        name: "loginTime",
        label: "userCenter.loginLogs.table.loginTime",
        tpl: "${loginTime/1000|date:YYYY-MM-DD HH\\:mm\\:ss}",
        searchable: {
            type: "input-date-range",
            valueFormat: "x"
        }
    },
    {
        name: "loginResult",
        label: "userCenter.loginLogs.table.loginResults",
        type: "mapping",
        map: success_or_fail
    },
    {
        name: "loginAddress",
        label: "userCenter.loginLogs.table.loginAddress"
    },
    {
        name: "loginFailureMsg",
        label: "userCenter.loginLogs.table.loginFailureReason"
    }
]

const searchIdentity = "ULoginLog"
const showColumns = columns

const schema = {
    type: "page",
    title: "userCenter.loginLogs.title",
    toolbar: [],
    body: [
        {
            type: "crud",
            syncLocation: false,
            name: "LoginLogTable",
            api: api_crud_search,
            defaultParams: {
                searchIdentity: searchIdentity,
                showColumns: showColumns
            },
            autoFillHeight: true,
            autoGenerateFilter: {
                columnsNum: 3,
                showBtnToolbar: true
            },
            headerToolbar: ["reload"],
            footerToolbar: ["switch-per-page", "statistics", "pagination"],
            columns: columns
        }
    ]
}

export default schema2component(schema)
