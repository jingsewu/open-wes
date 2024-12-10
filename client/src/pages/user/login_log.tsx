import schema2component from "@/utils/schema2component"

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
        name: "gmtLoginTime",
        label: "userCenter.loginLogs.table.loginTime",
        tpl: "${gmtLoginTime/1000|date:YYYY-MM-DD HH\\:mm\\:ss}",
        searchable: {
            type: "input-date-range",
            valueFormat: "x"
        }
    },
    {
        name: "loginResult",
        label: "userCenter.loginLogs.table.loginResults"
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
            api: {
                method: "POST",
                url: "/search/search?page=${page}&perPage=${perPage}",
                dataType: "application/json"
            },
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
