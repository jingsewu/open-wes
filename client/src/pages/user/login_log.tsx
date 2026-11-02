import schema2component from "@/utils/schema2component"
import { api_crud_search } from "@/pages/constantApi"
import {
    crudPage,
    dateTimeColumn,
    statusColumn
} from "@/utils/amis/presets"
import { loginResultStatus } from "@/utils/amis/statusMaps"

const columns = [
    {
        name: "id",
        label: "ID",
        hidden: true
    },
    {
        name: "account",
        label: "userCenter.userManagement.table.loginUsername",
        searchable: true
    },
    dateTimeColumn("loginTime", "userCenter.loginLogs.table.loginTime", {
        seconds: true,
        searchable: {
            type: "input-date-range",
            valueFormat: "x"
        }
    }),
    statusColumn(
        "loginResult",
        "userCenter.loginLogs.table.loginResults",
        loginResultStatus
    ),
    {
        name: "loginAddress",
        label: "userCenter.loginLogs.table.loginAddress"
    },
    {
        name: "loginFailureMsg",
        label: "userCenter.loginLogs.table.loginFailureReason"
    }
]

const schema = crudPage({
    title: "userCenter.loginLogs.title",
    name: "LoginLogTable",
    searchIdentity: "ULoginLog",
    api: api_crud_search,
    columns,
    pageActions: [],
    empty: "emptyState.loginLog"
})

export default schema2component(schema)
