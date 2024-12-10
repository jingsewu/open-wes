import schema2component from "@/utils/schema2component"
import {
    api_api_add,
    api_api_config_get,
    api_api_config_update,
    api_api_delete,
    api_api_update
} from "@/pages/api_platform/constants/api_constant"
import {
    create_update_columns,
    true_false_options
} from "@/utils/commonContants"

const baseform = [
    {
        type: "hidden",
        name: "id"
    },
    {
        label: "interfacePlatform.interfaceManagement.table.interfaceCode",
        type: "input-text",
        name: "code",
        required: true
    },
    {
        label: "interfacePlatform.interfaceManagement.table.interfaceName",
        type: "input-text",
        name: "name",
        required: true
    },
    {
        label: "interfacePlatform.interfaceManagement.table.interfaceType",
        type: "select",
        name: "apiType",
        source: "${ApiType}",
        required: true
    },
    {
        label: "interfacePlatform.interfaceManagement.table.interfaceAddress",
        type: "input-text",
        name: "url",
        validations: "isUrl"
    },
    {
        label: "interfacePlatform.interfaceManagement.table.interfaceRequestMethod",
        type: "select",
        name: "method",
        source: "${HttpMethod}"
    },
    {
        label: "interfacePlatform.interfaceManagement.table.interfaceRequestEncoding",
        type: "input-text",
        name: "encoding"
    },
    {
        label: "interfacePlatform.interfaceManagement.table.API_request_header",
        type: "input-kv",
        name: "headers",
        value: "${DECODEJSON(headersStr)}"
    },
    {
        label: "interfacePlatform.interfaceManagement.table.interfaceRequestFormat",
        type: "select",
        name: "format",
        source: "${MediaType}",
        required: true
    },
    {
        label: "interfacePlatform.interfaceManagement.table.isCertificationRequired",
        type: "switch",
        name: "auth",
        required: true,
        value: false
    },
    {
        label: "interfacePlatform.interfaceManagement.table.authenticationServiceAddress",
        type: "input-text",
        name: "authUrl",
        visibleOn: "${auth}"
    },
    {
        label: "interfacePlatform.interfaceManagement.table.typeOfCertification",
        type: "input-text",
        name: "grantType",
        visibleOn: "${auth}"
    },
    {
        label: "interfacePlatform.interfaceManagement.table.authenticationServiceUsername",
        type: "input-text",
        name: "username",
        visibleOn: "${auth}"
    },
    {
        label: "interfacePlatform.interfaceManagement.table.authenticationServicePassword",
        type: "input-text",
        name: "password",
        visibleOn: "${auth}"
    },
    {
        label: "interfacePlatform.interfaceManagement.table.keysID",
        type: "input-text",
        name: "secretId",
        visibleOn: "${auth}"
    },
    {
        label: "interfacePlatform.interfaceManagement.table.keys",
        type: "input-text",
        name: "secretKey",
        visibleOn: "${auth}"
    },
    {
        label: "interfacePlatform.interfaceManagement.table.tokenName",
        description: "认证服务器响应的 token 的名称",
        type: "input-text",
        name: "tokenName",
        visibleOn: "${auth}"
    },
    {
        label: "table.whetherEnabled",
        type: "switch",
        name: "enabled",
        value: true
    },
    {
        label: "table.syncCallback",
        type: "switch",
        name: "syncCallback",
        value: false
    },
    {
        label: "interfacePlatform.interfaceManagement.form.interfaceDescription",
        type: "textarea",
        name: "description"
    }
]

const configForm = [
    {
        label: "interfacePlatform.interfaceManagement.table.interfaceCode",
        type: "input-text",
        name: "code",
        readOnly: true
    },
    {
        label: "interfacePlatform.interfaceManagement.form.converseScriptType",
        type: "select",
        name: "paramConverterType",
        source: "${ConverterType}",
        required: true
    },
    {
        label: "interfacePlatform.interfaceManagement.form.requestTransformationScript",
        type: "textarea",
        name: "jsParamConverter",
        description:
            "如果接口的请求是单条数据时，会执行一次脚本，如果是多条数据，则会为每条数据执行一次脚本，本次执行脚本时，可以使用 <strong>this</strong> 或 <strong>obj</strong> 获取当前操作的数据进行转换",
        visibleOn: "${paramConverterType == 'JS'}"
    },
    {
        label: "interfacePlatform.interfaceManagement.form.requestTransformationScript",
        type: "textarea",
        name: "templateParamConverter",
        visibleOn: "${paramConverterType == 'TEMPLATE'}"
    },
    {
        label: "interfacePlatform.interfaceManagement.form.responseTransformationScriptType",
        type: "select",
        name: "responseConverterType",
        source: "${ConverterType}",
        required: true
    },
    {
        label: "interfacePlatform.interfaceManagement.form.responseTransformationScripts",
        type: "textarea",
        name: "jsResponseConverter",
        visibleOn: "${responseConverterType == 'JS'}"
    },
    {
        label: "interfacePlatform.interfaceManagement.form.responseTransformationScripts",
        type: "textarea",
        name: "templateResponseConverter",
        visibleOn: "${responseConverterType == 'TEMPLATE'}"
    }
]

const add = {
    type: "button",
    actionType: "drawer",
    icon: "fa fa-plus",
    label: "button.add",
    target: "ApiTable",
    drawer: {
        size: "lg",
        title: "button.add",
        closeOnEsc: true,
        body: {
            type: "form",
            api: api_api_add,
            body: baseform
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
        name: "code",
        label: "interfacePlatform.interfaceManagement.table.interfaceCode",
        searchable: true
    },
    {
        name: "name",
        label: "interfacePlatform.interfaceManagement.table.interfaceName",
        searchable: true
    },
    {
        name: "apiType",
        label: "interfacePlatform.interfaceManagement.table.interfaceType",
        type: "mapping",
        source: "${ApiCallType}",
        searchable: {
            type: "select",
            source: "${ApiCallType}"
        }
    },
    {
        name: "method",
        label: "interfacePlatform.interfaceManagement.table.interfaceRequestMethod"
    },
    {
        name: "format",
        label: "interfacePlatform.interfaceManagement.table.interfaceRequestFormat"
    },
    {
        name: "encoding",
        label: "interfacePlatform.interfaceManagement.table.interfaceRequestEncoding"
    },
    {
        name: "headersStr",
        dbField: "headers",
        label: "interfacePlatform.interfaceManagement.table.API_request_header",
        hidden: true
    },
    {
        name: "auth",
        label: "interfacePlatform.interfaceManagement.table.isCertificationRequired",
        type: "mapping",
        map: true_false_options
    },
    {
        name: "enabled",
        label: "table.whetherEnabled",
        type: "mapping",
        map: true_false_options
    },
    {
        name: "syncCallback",
        label: "table.syncCallback",
        type: "mapping",
        map: true_false_options
    },
    {
        name: "url",
        label: "请求url",
        hidden: true
    },
    {
        name: "authUrl",
        label: "interfacePlatform.interfaceManagement.table.authenticationServiceAddress",
        hidden: true
    },
    {
        name: "grantType",
        label: "interfacePlatform.interfaceManagement.table.typeOfCertification",
        hidden: true
    },
    {
        name: "username",
        label: "interfacePlatform.interfaceManagement.table.authenticationServiceUsername",
        hidden: true
    },
    {
        name: "password",
        label: "interfacePlatform.interfaceManagement.table.authenticationServicePassword",
        hidden: true
    },
    {
        name: "secretId",
        label: "interfacePlatform.interfaceManagement.table.keysID",
        hidden: true
    },
    {
        name: "secretKey",
        label: "interfacePlatform.interfaceManagement.table.keys",
        hidden: true
    },
    {
        name: "description",
        label: "interfacePlatform.interfaceManagement.form.interfaceDescription",
        hidden: true
    },
    ...create_update_columns
]

const searchIdentity = "AApi"
const showColumns = columns

const schema = {
    type: "page",
    title: "interfacePlatform.interfaceManagement.title",
    toolbar: [],
    initApi: "post:/mdm/config/dictionary/getAll",
    body: [
        {
            type: "crud",
            syncLocation: false,
            name: "ApiTable",
            api: {
                method: "POST",
                url: "/search/search?page=${page}&perPage=${perPage}&createTime-op=bt",
                dataType: "application/json"
            },
            defaultParams: {
                searchIdentity: searchIdentity,
                showColumns: showColumns,
                searchObject: {
                    orderBy: "update_time desc"
                }
            },
            autoFillHeight: true,
            autoGenerateFilter: {
                columnsNum: 3,
                showBtnToolbar: true
            },
            headerToolbar: [
                "reload",
                add,
                {
                    type: "export-excel",
                    label: "button.export",
                    method: "POST",
                    api: {
                        method: "POST",
                        url: "/search/search?page=${1}&perPage=${100000}&createTime-op=bt",
                        dataType: "application/json"
                    },
                    columns: [
                        "code",
                        "name",
                        "apiType",
                        "method",
                        "format",
                        "encoding",
                        "auth",
                        "enabled"
                    ],
                    filename: "api",
                    defaultParams: {
                        searchIdentity: searchIdentity,
                        showColumns: showColumns
                    }
                }
            ],
            footerToolbar: ["switch-per-page", "statistics", "pagination"],
            columns: [
                ...columns,
                {
                    type: "operation",
                    label: "table.operation",
                    width: 230,
                    buttons: [
                        {
                            label: "button.modify",
                            type: "button",
                            actionType: "drawer",
                            drawer: {
                                title: "button.modify",
                                closeOnEsc: true,
                                closeOnOutside: true,
                                body: {
                                    type: "form",
                                    api: api_api_update,
                                    body: baseform
                                }
                            }
                        },
                        {
                            label: "interfacePlatform.interfaceManagement.button.parameterConversionConfiguration",
                            type: "button",
                            actionType: "dialog",
                            dialog: {
                                title: "interfacePlatform.interfaceManagement.dialog.modifyParameterConversionConfiguration",
                                closeOnEsc: true,
                                closeOnOutside: true,
                                size: "xl",
                                body: {
                                    type: "form",
                                    initApi: api_api_config_get,
                                    api: api_api_config_update,
                                    body: configForm
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
                            api: api_api_delete,
                            reload: "ApiTable"
                        }
                    ],
                    toggled: true
                }
            ]
        }
    ]
}

export default schema2component(schema)
