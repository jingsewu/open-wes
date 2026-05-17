# API 参数转换配置弹框 UX 重构 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 重构接口管理页"修改参数转换配置"弹框，将单列滚动表单改为 Tab + 左右分栏布局，提升编辑器与测试面板的可用性。

**Architecture:** 纯 AMIS JSON schema 改动，无新 React 组件。`configForm` 改为 `tabs` 结构（请求/响应各一个 Tab），每个 Tab 内用 `grid` 实现编辑器（左60%）与测试面板（右40%）并排。测试输出用两个条件 `tpl` 替换原有 `static`，根据隐藏状态字段切换绿色成功/红色失败样式。

**Tech Stack:** React 17, TypeScript, AMIS (schema2component), Monaco Editor

---

## File Map

| 文件 | 改动 |
|---|---|
| `client/src/pages/api_platform/api_management.tsx` | 重写 `configForm` 常量 |
| `client/src/pages/api_platform/constants/api_constant.tsx` | 移除 2 处 `console.log` |

---

### Task 1: 移除 api_constant.tsx 中的 debug console.log

**Files:**
- Modify: `client/src/pages/api_platform/constants/api_constant.tsx`

- [ ] **Step 1: 移除两处 console.log**

将 `editorDidMount` 函数中的两行删除：

```typescript
// 删除这行（约第34行）：
console.log("Completion triggered");

// 删除这行（约第48行）：
console.log("Generated Code:", fullCode);
```

修改后 `editorDidMount` 完整内容：

```typescript
let debounceTimer: NodeJS.Timeout | null = null;

export const editorDidMount = (editor: any, monaco: any) => {

    if (debounceTimer) {
        clearTimeout(debounceTimer);
    }

    debounceTimer = setTimeout(async function () {

        monaco.languages.registerCompletionItemProvider("java", {
            provideCompletionItems: async function (model: any, position: any) {
                const codeContext = model.getValue();
                const lineContent = model.getLineContent(position.lineNumber);
                const language = "Java"

                const response: any = await request({
                    url: "/ai/ai/generateCode",
                    method: "POST",
                    headers: {"Content-Type": "application/json"},
                    data: JSON.stringify({codeContext, lineContent, language})
                });

                if (response.data && typeof response.data === "string") {
                    let fullCode: string = response.data.trim();
                    fullCode = fullCode.split('\n').join('\n');

                    return {
                        suggestions: fullCode.split("\n").filter(line => line.trim()).map(s => ({
                            label: s.trim(),
                            kind: monaco.languages.CompletionItemKind.Function,
                            insertText: s.trim(),
                            range: new monaco.Range(
                                position.lineNumber,
                                1,
                                position.lineNumber,
                                model.getLineMaxColumn(position.lineNumber)
                            )
                        }))
                    };
                }

                console.warn("No valid response received");
                return {suggestions: []};
            }
        });
    }, 3000);
}
```

- [ ] **Step 2: Commit**

```bash
git add client/src/pages/api_platform/constants/api_constant.tsx
git commit -m "fix: remove debug console.log from editorDidMount"
```

---

### Task 2: 重写 api_management.tsx 的 configForm

**Files:**
- Modify: `client/src/pages/api_platform/api_management.tsx`

- [ ] **Step 1: 用以下内容替换整个 `configForm` 常量**

找到文件中 `const configForm = [` 到其对应 `]` 的全部内容，替换为：

```typescript
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
                    // 状态字段：存储测试状态和输出，供 tpl 读取
                    {
                        type: "hidden",
                        name: "testParamStatus",
                        id: "testParamStatusComp"
                    },
                    {
                        type: "hidden",
                        name: "testParamOutput",
                        id: "testParamOutputComp"
                    },
                    // JS 模式：编辑器（左）+ 测试面板（右）
                    {
                        type: "grid",
                        visibleOn: "${paramConverterType === 'JS'}",
                        columns: [
                            {
                                md: 7,
                                body: [
                                    {
                                        label: "interfacePlatform.interfaceManagement.form.requestTransformationScript",
                                        type: "editor",
                                        size: "lg",
                                        name: "jsParamConverter",
                                        language: "java",
                                        placeholder: "Enter your java code here and named function as convert. for example: \n" +
                                            "                //java:convert \n" +
                                            "                public class MyClass { \n" +
                                            "                    public Object convert(Object param) {\n" +
                                            "                        Map<String, Object> input = (Map<String, Object>) param;\n" +
                                            "                        return \"Hello,  \"+ input.get(\"name\");\n" +
                                            "                    }\n" +
                                            "                }\n" +
                                            "                \"\"\"",
                                        options: {
                                            automaticLayout: true,
                                            lineNumbers: true,
                                            autofocus: true,
                                            lineHeight: 24,
                                            theme: "vs-dark",
                                            fontFamily: "'Courier New', monospace",
                                            fontSize: 14,
                                            wordWrap: "on"
                                        },
                                        editorDidMount: editorDidMount
                                    }
                                ]
                            },
                            {
                                md: 5,
                                body: [
                                    {
                                        type: "textarea",
                                        label: "interfacePlatform.interfaceManagement.form.testInputJson",
                                        name: "testParamInput",
                                        placeholder: "Enter input JSON to test the param converter script"
                                    },
                                    {
                                        type: "button",
                                        label: "interfacePlatform.interfaceManagement.button.testConverter",
                                        actionType: "ajax",
                                        api: {
                                            method: "post",
                                            url: api_api_config_test_converter,
                                            data: {
                                                jsScript: "${jsParamConverter}",
                                                inputJson: "${testParamInput}"
                                            }
                                        },
                                        onEvent: {
                                            success: {
                                                actions: [
                                                    {
                                                        actionType: "setValue",
                                                        componentId: "testParamStatusComp",
                                                        args: {value: "success"}
                                                    },
                                                    {
                                                        actionType: "setValue",
                                                        componentId: "testParamOutputComp",
                                                        args: {value: "${event.data.data}"}
                                                    }
                                                ]
                                            },
                                            error: {
                                                actions: [
                                                    {
                                                        actionType: "setValue",
                                                        componentId: "testParamStatusComp",
                                                        args: {value: "error"}
                                                    },
                                                    {
                                                        actionType: "setValue",
                                                        componentId: "testParamOutputComp",
                                                        args: {value: "${event.data.msg}"}
                                                    }
                                                ]
                                            }
                                        }
                                    },
                                    // 成功输出（绿色）
                                    {
                                        type: "tpl",
                                        visibleOn: "${testParamStatus === 'success'}",
                                        tpl: "<div style='font-size:11px;color:#64748b;margin-bottom:4px'>输出结果 <span style='background:#dcfce7;color:#166534;border-radius:10px;padding:1px 7px;font-size:10px'>✓ 成功</span></div><pre style='background:#f0fdf4;border:1px solid #bbf7d0;border-radius:6px;padding:10px;font-family:monospace;font-size:12px;white-space:pre-wrap;word-break:break-all;color:#15803d;min-height:60px'>${testParamOutput}</pre>"
                                    },
                                    // 失败输出（红色）
                                    {
                                        type: "tpl",
                                        visibleOn: "${testParamStatus === 'error'}",
                                        tpl: "<div style='font-size:11px;color:#64748b;margin-bottom:4px'>输出结果 <span style='background:#fee2e2;color:#dc2626;border-radius:10px;padding:1px 7px;font-size:10px'>✗ 失败</span></div><pre style='background:#fef2f2;border:1px solid #fecaca;border-radius:6px;padding:10px;font-family:monospace;font-size:12px;white-space:pre-wrap;word-break:break-all;color:#dc2626;min-height:60px'>${testParamOutput}</pre>"
                                    }
                                ]
                            }
                        ]
                    },
                    // TEMPLATE 模式：全宽 textarea
                    {
                        label: "interfacePlatform.interfaceManagement.form.requestTransformationScript",
                        type: "textarea",
                        name: "templateParamConverter",
                        visibleOn: "${paramConverterType === 'TEMPLATE'}"
                    },
                    // NONE 模式：空状态提示
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
                    // 状态字段
                    {
                        type: "hidden",
                        name: "testResponseStatus",
                        id: "testResponseStatusComp"
                    },
                    {
                        type: "hidden",
                        name: "testResponseOutput",
                        id: "testResponseOutputComp"
                    },
                    // JS 模式：编辑器（左）+ 测试面板（右）
                    {
                        type: "grid",
                        visibleOn: "${responseConverterType === 'JS'}",
                        columns: [
                            {
                                md: 7,
                                body: [
                                    {
                                        label: "interfacePlatform.interfaceManagement.form.responseTransformationScripts",
                                        type: "editor",
                                        size: "lg",
                                        name: "jsResponseConverter",
                                        language: "java",
                                        placeholder: "Enter your java code here and named function as convert. for example: \n" +
                                            "                //java:convert \n" +
                                            "                public class MyClass { \n" +
                                            "                    public Object convert(Object param) {\n" +
                                            "                        Map<String, Object> input = (Map<String, Object>) param;\n" +
                                            "                        return \"Hello,  \"+ input.get(\"name\");\n" +
                                            "                    }\n" +
                                            "                }\n" +
                                            "                \"\"\"",
                                        options: {
                                            automaticLayout: true,
                                            lineNumbers: true,
                                            autofocus: true,
                                            lineHeight: 24,
                                            theme: "vs-dark",
                                            fontFamily: "'Courier New', monospace",
                                            fontSize: 14,
                                            wordWrap: "on"
                                        },
                                        editorDidMount: editorDidMount
                                    }
                                ]
                            },
                            {
                                md: 5,
                                body: [
                                    {
                                        type: "textarea",
                                        label: "interfacePlatform.interfaceManagement.form.testInputJson",
                                        name: "testResponseInput",
                                        placeholder: "Enter input JSON to test the response converter script"
                                    },
                                    {
                                        type: "button",
                                        label: "interfacePlatform.interfaceManagement.button.testConverter",
                                        actionType: "ajax",
                                        api: {
                                            method: "post",
                                            url: api_api_config_test_converter,
                                            data: {
                                                jsScript: "${jsResponseConverter}",
                                                inputJson: "${testResponseInput}"
                                            }
                                        },
                                        onEvent: {
                                            success: {
                                                actions: [
                                                    {
                                                        actionType: "setValue",
                                                        componentId: "testResponseStatusComp",
                                                        args: {value: "success"}
                                                    },
                                                    {
                                                        actionType: "setValue",
                                                        componentId: "testResponseOutputComp",
                                                        args: {value: "${event.data.data}"}
                                                    }
                                                ]
                                            },
                                            error: {
                                                actions: [
                                                    {
                                                        actionType: "setValue",
                                                        componentId: "testResponseStatusComp",
                                                        args: {value: "error"}
                                                    },
                                                    {
                                                        actionType: "setValue",
                                                        componentId: "testResponseOutputComp",
                                                        args: {value: "${event.data.msg}"}
                                                    }
                                                ]
                                            }
                                        }
                                    },
                                    // 成功输出（绿色）
                                    {
                                        type: "tpl",
                                        visibleOn: "${testResponseStatus === 'success'}",
                                        tpl: "<div style='font-size:11px;color:#64748b;margin-bottom:4px'>输出结果 <span style='background:#dcfce7;color:#166534;border-radius:10px;padding:1px 7px;font-size:10px'>✓ 成功</span></div><pre style='background:#f0fdf4;border:1px solid #bbf7d0;border-radius:6px;padding:10px;font-family:monospace;font-size:12px;white-space:pre-wrap;word-break:break-all;color:#15803d;min-height:60px'>${testResponseOutput}</pre>"
                                    },
                                    // 失败输出（红色）
                                    {
                                        type: "tpl",
                                        visibleOn: "${testResponseStatus === 'error'}",
                                        tpl: "<div style='font-size:11px;color:#64748b;margin-bottom:4px'>输出结果 <span style='background:#fee2e2;color:#dc2626;border-radius:10px;padding:1px 7px;font-size:10px'>✗ 失败</span></div><pre style='background:#fef2f2;border:1px solid #fecaca;border-radius:6px;padding:10px;font-family:monospace;font-size:12px;white-space:pre-wrap;word-break:break-all;color:#dc2626;min-height:60px'>${testResponseOutput}</pre>"
                                    }
                                ]
                            }
                        ]
                    },
                    // TEMPLATE 模式：全宽 textarea
                    {
                        label: "interfacePlatform.interfaceManagement.form.responseTransformationScripts",
                        type: "textarea",
                        name: "templateResponseConverter",
                        visibleOn: "${responseConverterType === 'TEMPLATE'}"
                    },
                    // NONE 模式：空状态提示
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
```

- [ ] **Step 2: TypeScript 类型检查**

```bash
cd client && npx tsc --noEmit 2>&1 | grep "api_management\|api_constant" || echo "No type errors in target files"
```

预期：无报错输出（或只有无关模块的现有错误）。

- [ ] **Step 3: Commit**

```bash
git add client/src/pages/api_platform/api_management.tsx
git commit -m "feat: redesign param converter config dialog with tab layout and improved test panel"
```

---

### Task 3: 手动验证

**Files:** 无代码改动，验证步骤

- [ ] **Step 1: 启动前端开发服务器**

```bash
cd client && npm run dev
```

- [ ] **Step 2: 验证 Tab 布局**

打开接口管理页 → 点击某条记录的"参数转换配置"按钮：

| 检查项 | 预期结果 |
|---|---|
| 弹框顶部显示 Tab | 出现"请求转换脚本"和"响应转换脚本"两个 Tab |
| 接口编码显示 | 弹框内 code 字段只读展示当前接口编码 |
| 切换 Tab 正常 | 点击"响应转换脚本"Tab 能正常切换 |

- [ ] **Step 3: 验证 JS 模式布局**

选择脚本类型为 JS：

| 检查项 | 预期结果 |
|---|---|
| 左右分栏 | 编辑器在左，测试面板在右，并排显示 |
| 编辑器高度 | 请求和响应编辑器高度一致（`size: "lg"`） |
| 编辑器 Tab 切换后重渲染 | 切换 Tab 再切回，编辑器正常显示（不变形） |

- [ ] **Step 4: 验证测试功能（成功路径）**

在测试输入框输入合法 JSON，点击"测试转换器"：

| 检查项 | 预期结果 |
|---|---|
| 成功后绿色输出 | 出现绿色"✓ 成功"徽章 + 绿色背景 `<pre>` 输出 |
| 输出内容正确 | 显示脚本转换后的结果 |

- [ ] **Step 5: 验证测试功能（失败路径）**

在测试输入框输入非法 JSON 或编写语法错误的脚本，点击"测试转换器"：

| 检查项 | 预期结果 |
|---|---|
| 失败后红色输出 | 出现红色"✗ 失败"徽章 + 红色背景 `<pre>` 错误信息 |

- [ ] **Step 6: 验证 NONE / TEMPLATE 模式**

| 操作 | 预期结果 |
|---|---|
| 脚本类型选 NONE | 编辑器和测试面板隐藏，显示"无需配置转换脚本"虚线框提示 |
| 脚本类型选 TEMPLATE | 显示全宽 textarea，无测试面板 |

- [ ] **Step 7: 验证保存功能**

修改脚本内容后点击"保存"：

| 检查项 | 预期结果 |
|---|---|
| 保存成功 | 弹框关闭，无报错 |
| 重新打开 | 重新打开弹框，之前保存的内容正确回显 |
