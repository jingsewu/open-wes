import schema2component from "@/utils/schema2component"
import {
    api_api_add,
    api_api_config_get,
    api_api_config_test_converter,
    api_api_config_update,
    api_api_delete,
    api_api_update,
    editorDidMount
} from "@/pages/api_platform/constants/api_constant"
import {create_update_columns, true_false_options} from "@/utils/commonContants"
import {api_crud_search, api_crud_search_total} from "@/pages/constantApi"

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
        source: "${dictionary.ApiType}",
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
        source: "${dictionary.HttpMethod}"
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
        source: "${dictionary.MediaType}",
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
        description: "interfacePlatform.interfaceManagement.table.tokenName.description",
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
        type: "hidden",
        name: "id"
    },
    {
        type: "hidden",
        name: "version"
    },
    {
        label: "interfacePlatform.interfaceManagement.table.interfaceCode",
        type: "input-text",
        name: "code",
        readOnly: true
    },
    {
        type: "tabs",
        tabs: [
            // ── Tab 1: 请求转换脚本 ──────────────────────────────────────
            {
                title: "interfacePlatform.interfaceManagement.form.requestTransformationScript",
                body: [
                    {
                        label: "interfacePlatform.interfaceManagement.form.converseScriptType",
                        type: "select",
                        name: "paramConverterType",
                        source: "${dictionary.ConverterType}",
                        required: true,
                        description: "interfacePlatform.interfaceManagement.form.requestTransformationScript.description"
                    },
                    {type: "hidden", name: "testParamStatus", id: "testParamStatusComp"},
                    {type: "hidden", name: "testParamOutput", id: "testParamOutputComp"},

                    // JS 模式
                    {
                        type: "grid",
                        visibleOn: "${paramConverterType === 'JS'}",
                        columns: [
                            {
                                md: 7,
                                body: [{
                                    label: "interfacePlatform.interfaceManagement.form.requestTransformationScript",
                                    type: "editor",
                                    size: "lg",
                                    name: "paramConverterScript",
                                    language: "javascript",
                                    placeholder: "// param contains the parsed input object\nfunction convert(param) {\n    return {\n        result: param.name\n    };\n}",
                                    options: {automaticLayout: true, lineNumbers: true, autofocus: true, lineHeight: 24, theme: "vs-dark", fontFamily: "'Courier New', monospace", fontSize: 14, wordWrap: "on"},
                                    editorDidMount: editorDidMount
                                }]
                            },
                            {
                                md: 5,
                                body: [
                                    {type: "textarea", label: "interfacePlatform.interfaceManagement.form.testInputJson", name: "testParamInput", placeholder: "Enter input JSON to test the param converter script"},
                                    {
                                        type: "button",
                                        label: "interfacePlatform.interfaceManagement.button.testConverter",
                                        onEvent: {click: {actions: [
                                            {actionType: "ajax", outputVar: "paramTestResult", api: {method: "post", url: api_api_config_test_converter, data: {converterType: "${paramConverterType}", script: "${paramConverterScript}", inputJson: "${testParamInput}"}, silent: true}},
                                            {actionType: "setValue", componentId: "testParamStatusComp", args: {value: "${paramTestResult.status === 0 ? 'success' : 'error'}"}},
                                            {actionType: "setValue", componentId: "testParamOutputComp", args: {value: "${paramTestResult.status === 0 ? paramTestResult.data : paramTestResult.msg}"}}
                                        ]}}
                                    },
                                    {type: "tpl", visibleOn: "${testParamStatus === 'success'}", tpl: "<div style='font-size:11px;color:#64748b;margin-bottom:4px'>输出结果 <span style='background:#dcfce7;color:#166534;border-radius:10px;padding:1px 7px;font-size:10px'>✓ 成功</span></div><pre style='background:#f0fdf4;border:1px solid #bbf7d0;border-radius:6px;padding:10px;font-family:monospace;font-size:12px;white-space:pre-wrap;word-break:break-all;color:#15803d;min-height:60px'>${testParamOutput}</pre>"},
                                    {type: "tpl", visibleOn: "${testParamStatus === 'error'}", tpl: "<div style='font-size:11px;color:#64748b;margin-bottom:4px'>输出结果 <span style='background:#fee2e2;color:#dc2626;border-radius:10px;padding:1px 7px;font-size:10px'>✗ 失败</span></div><pre style='background:#fef2f2;border:1px solid #fecaca;border-radius:6px;padding:10px;font-family:monospace;font-size:12px;white-space:pre-wrap;word-break:break-all;color:#dc2626;min-height:60px'>${testParamOutput}</pre>"}
                                ]
                            }
                        ]
                    },

                    // JAVA 模式
                    {
                        type: "grid",
                        visibleOn: "${paramConverterType === 'JAVA'}",
                        columns: [
                            {
                                md: 7,
                                body: [{
                                    label: "interfacePlatform.interfaceManagement.form.requestTransformationScript",
                                    type: "editor",
                                    size: "lg",
                                    name: "paramConverterScript",
                                    language: "java",
                                    placeholder: "//java:convert\npublic class MyConverter {\n    public Object convert(Object param) {\n        Map<String, Object> input = (Map<String, Object>) param;\n        return \"Hello, \" + input.get(\"name\");\n    }\n}",
                                    options: {automaticLayout: true, lineNumbers: true, autofocus: true, lineHeight: 24, theme: "vs-dark", fontFamily: "'Courier New', monospace", fontSize: 14, wordWrap: "on"},
                                    editorDidMount: editorDidMount
                                }]
                            },
                            {
                                md: 5,
                                body: [
                                    {type: "textarea", label: "interfacePlatform.interfaceManagement.form.testInputJson", name: "testParamInput", placeholder: "Enter input JSON to test the param converter script"},
                                    {
                                        type: "button",
                                        label: "interfacePlatform.interfaceManagement.button.testConverter",
                                        onEvent: {click: {actions: [
                                            {actionType: "ajax", outputVar: "paramTestResult", api: {method: "post", url: api_api_config_test_converter, data: {converterType: "${paramConverterType}", script: "${paramConverterScript}", inputJson: "${testParamInput}"}, silent: true}},
                                            {actionType: "setValue", componentId: "testParamStatusComp", args: {value: "${paramTestResult.status === 0 ? 'success' : 'error'}"}},
                                            {actionType: "setValue", componentId: "testParamOutputComp", args: {value: "${paramTestResult.status === 0 ? paramTestResult.data : paramTestResult.msg}"}}
                                        ]}}
                                    },
                                    {type: "tpl", visibleOn: "${testParamStatus === 'success'}", tpl: "<div style='font-size:11px;color:#64748b;margin-bottom:4px'>输出结果 <span style='background:#dcfce7;color:#166534;border-radius:10px;padding:1px 7px;font-size:10px'>✓ 成功</span></div><pre style='background:#f0fdf4;border:1px solid #bbf7d0;border-radius:6px;padding:10px;font-family:monospace;font-size:12px;white-space:pre-wrap;word-break:break-all;color:#15803d;min-height:60px'>${testParamOutput}</pre>"},
                                    {type: "tpl", visibleOn: "${testParamStatus === 'error'}", tpl: "<div style='font-size:11px;color:#64748b;margin-bottom:4px'>输出结果 <span style='background:#fee2e2;color:#dc2626;border-radius:10px;padding:1px 7px;font-size:10px'>✗ 失败</span></div><pre style='background:#fef2f2;border:1px solid #fecaca;border-radius:6px;padding:10px;font-family:monospace;font-size:12px;white-space:pre-wrap;word-break:break-all;color:#dc2626;min-height:60px'>${testParamOutput}</pre>"}
                                ]
                            }
                        ]
                    },

                    // TEMPLATE 模式
                    {
                        label: "interfacePlatform.interfaceManagement.form.requestTransformationScript",
                        type: "textarea",
                        name: "paramConverterScript",
                        visibleOn: "${paramConverterType === 'TEMPLATE'}"
                    },

                    // NONE 模式
                    {
                        type: "tpl",
                        visibleOn: "${paramConverterType === 'NONE' || !paramConverterType}",
                        tpl: "<div style='padding:40px;text-align:center;color:#9ca3af;font-size:13px;border:1px dashed #e2e8f0;border-radius:8px;margin-top:8px'>无需配置转换脚本</div>"
                    }
                ]
            },

            // ── Tab 2: 响应转换脚本 ──────────────────────────────────────
            {
                title: "interfacePlatform.interfaceManagement.form.responseTransformationScriptType",
                body: [
                    {
                        label: "interfacePlatform.interfaceManagement.form.responseTransformationScriptType",
                        type: "select",
                        name: "responseConverterType",
                        source: "${dictionary.ConverterType}",
                        required: true
                    },
                    {type: "hidden", name: "testResponseStatus", id: "testResponseStatusComp"},
                    {type: "hidden", name: "testResponseOutput", id: "testResponseOutputComp"},

                    // JS 模式
                    {
                        type: "grid",
                        visibleOn: "${responseConverterType === 'JS'}",
                        columns: [
                            {
                                md: 7,
                                body: [{
                                    label: "interfacePlatform.interfaceManagement.form.responseTransformationScripts",
                                    type: "editor",
                                    size: "lg",
                                    name: "responseConverterScript",
                                    language: "javascript",
                                    placeholder: "// param contains the parsed input object\nfunction convert(param) {\n    return {\n        result: param.code\n    };\n}",
                                    options: {automaticLayout: true, lineNumbers: true, autofocus: true, lineHeight: 24, theme: "vs-dark", fontFamily: "'Courier New', monospace", fontSize: 14, wordWrap: "on"},
                                    editorDidMount: editorDidMount
                                }]
                            },
                            {
                                md: 5,
                                body: [
                                    {type: "textarea", label: "interfacePlatform.interfaceManagement.form.testInputJson", name: "testResponseInput", placeholder: "Enter input JSON to test the response converter script"},
                                    {
                                        type: "button",
                                        label: "interfacePlatform.interfaceManagement.button.testConverter",
                                        onEvent: {click: {actions: [
                                            {actionType: "ajax", outputVar: "responseTestResult", api: {method: "post", url: api_api_config_test_converter, data: {converterType: "${responseConverterType}", script: "${responseConverterScript}", inputJson: "${testResponseInput}"}, silent: true}},
                                            {actionType: "setValue", componentId: "testResponseStatusComp", args: {value: "${responseTestResult.status === 0 ? 'success' : 'error'}"}},
                                            {actionType: "setValue", componentId: "testResponseOutputComp", args: {value: "${responseTestResult.status === 0 ? responseTestResult.data : responseTestResult.msg}"}}
                                        ]}}
                                    },
                                    {type: "tpl", visibleOn: "${testResponseStatus === 'success'}", tpl: "<div style='font-size:11px;color:#64748b;margin-bottom:4px'>输出结果 <span style='background:#dcfce7;color:#166534;border-radius:10px;padding:1px 7px;font-size:10px'>✓ 成功</span></div><pre style='background:#f0fdf4;border:1px solid #bbf7d0;border-radius:6px;padding:10px;font-family:monospace;font-size:12px;white-space:pre-wrap;word-break:break-all;color:#15803d;min-height:60px'>${testResponseOutput}</pre>"},
                                    {type: "tpl", visibleOn: "${testResponseStatus === 'error'}", tpl: "<div style='font-size:11px;color:#64748b;margin-bottom:4px'>输出结果 <span style='background:#fee2e2;color:#dc2626;border-radius:10px;padding:1px 7px;font-size:10px'>✗ 失败</span></div><pre style='background:#fef2f2;border:1px solid #fecaca;border-radius:6px;padding:10px;font-family:monospace;font-size:12px;white-space:pre-wrap;word-break:break-all;color:#dc2626;min-height:60px'>${testResponseOutput}</pre>"}
                                ]
                            }
                        ]
                    },

                    // JAVA 模式
                    {
                        type: "grid",
                        visibleOn: "${responseConverterType === 'JAVA'}",
                        columns: [
                            {
                                md: 7,
                                body: [{
                                    label: "interfacePlatform.interfaceManagement.form.responseTransformationScripts",
                                    type: "editor",
                                    size: "lg",
                                    name: "responseConverterScript",
                                    language: "java",
                                    placeholder: "//java:convert\npublic class MyConverter {\n    public Object convert(Object param) {\n        Map<String, Object> input = (Map<String, Object>) param;\n        return input.get(\"code\");\n    }\n}",
                                    options: {automaticLayout: true, lineNumbers: true, autofocus: true, lineHeight: 24, theme: "vs-dark", fontFamily: "'Courier New', monospace", fontSize: 14, wordWrap: "on"},
                                    editorDidMount: editorDidMount
                                }]
                            },
                            {
                                md: 5,
                                body: [
                                    {type: "textarea", label: "interfacePlatform.interfaceManagement.form.testInputJson", name: "testResponseInput", placeholder: "Enter input JSON to test the response converter script"},
                                    {
                                        type: "button",
                                        label: "interfacePlatform.interfaceManagement.button.testConverter",
                                        onEvent: {click: {actions: [
                                            {actionType: "ajax", outputVar: "responseTestResult", api: {method: "post", url: api_api_config_test_converter, data: {converterType: "${responseConverterType}", script: "${responseConverterScript}", inputJson: "${testResponseInput}"}, silent: true}},
                                            {actionType: "setValue", componentId: "testResponseStatusComp", args: {value: "${responseTestResult.status === 0 ? 'success' : 'error'}"}},
                                            {actionType: "setValue", componentId: "testResponseOutputComp", args: {value: "${responseTestResult.status === 0 ? responseTestResult.data : responseTestResult.msg}"}}
                                        ]}}
                                    },
                                    {type: "tpl", visibleOn: "${testResponseStatus === 'success'}", tpl: "<div style='font-size:11px;color:#64748b;margin-bottom:4px'>输出结果 <span style='background:#dcfce7;color:#166534;border-radius:10px;padding:1px 7px;font-size:10px'>✓ 成功</span></div><pre style='background:#f0fdf4;border:1px solid #bbf7d0;border-radius:6px;padding:10px;font-family:monospace;font-size:12px;white-space:pre-wrap;word-break:break-all;color:#15803d;min-height:60px'>${testResponseOutput}</pre>"},
                                    {type: "tpl", visibleOn: "${testResponseStatus === 'error'}", tpl: "<div style='font-size:11px;color:#64748b;margin-bottom:4px'>输出结果 <span style='background:#fee2e2;color:#dc2626;border-radius:10px;padding:1px 7px;font-size:10px'>✗ 失败</span></div><pre style='background:#fef2f2;border:1px solid #fecaca;border-radius:6px;padding:10px;font-family:monospace;font-size:12px;white-space:pre-wrap;word-break:break-all;color:#dc2626;min-height:60px'>${testResponseOutput}</pre>"}
                                ]
                            }
                        ]
                    },

                    // TEMPLATE 模式
                    {
                        label: "interfacePlatform.interfaceManagement.form.responseTransformationScripts",
                        type: "textarea",
                        name: "responseConverterScript",
                        visibleOn: "${responseConverterType === 'TEMPLATE'}"
                    },

                    // NONE 模式
                    {
                        type: "tpl",
                        visibleOn: "${responseConverterType === 'NONE' || !responseConverterType}",
                        tpl: "<div style='padding:40px;text-align:center;color:#9ca3af;font-size:13px;border:1px dashed #e2e8f0;border-radius:8px;margin-top:8px'>无需配置转换脚本</div>"
                    }
                ]
            }
        ]
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
        source: "${dictionary.ApiCallType}",
        searchable: {
            type: "select",
            source: "${dictionary.ApiCallType}"
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
    data: {
        dictionary: "${ls:dictionary}"
    },
    body: [
        {
            type: "crud",
            syncLocation: false,
            name: "ApiTable",
            api: api_crud_search,
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
                    api: api_crud_search_total,
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
                                size: "xl",
                                style: {height: "90vh"},
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
