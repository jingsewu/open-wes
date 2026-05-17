# API 参数转换配置弹框 UX 重构设计

**日期**: 2026-05-17
**文件**: `client/src/pages/api_platform/api_management.tsx`
**相关文件**: `client/src/pages/api_platform/constants/api_constant.tsx`

---

## 背景

接口管理页（`api_management.tsx`）中"修改参数转换配置"弹框（`configForm`）存在以下 UX 问题：

1. **单列垂直滚动**：请求转换和响应转换上下堆叠，内容过长，需大量滚动。
2. **测试区域无结构**：测试输入、按钮、输出看起来和普通表单字段没有区别，缺乏视觉层次。
3. **测试输出不直观**：使用 `static` 类型显示纯文本，无格式化、无成功/失败状态区分。
4. **无错误处理**：测试失败时无任何视觉反馈。
5. **NONE 类型无提示**：选择 NONE 时编辑器消失但没有友好的空状态说明。
6. **响应编辑器缺少 `size: "lg"`**：导致请求/响应两个编辑器高度不一致。
7. **`console.log` 未清理**：`api_constant.tsx` 中的 `editorDidMount` 有两处 `console.log`，违反项目规范。

---

## 目标

在不引入新 React 组件的前提下，纯在 AMIS JSON schema 内重构 `configForm`，实现：

- 请求/响应转换分 Tab 展示，消除滚动
- 编辑器与测试面板左右并排，操作流畅
- 测试输出格式化展示，成功/失败有明确视觉区分
- NONE 类型显示友好空状态

---

## 整体结构

```
form (initApi + api 不变)
 ├── hidden: id
 ├── hidden: version
 ├── input-text: code（readOnly，展示接口编码）
 └── tabs
      ├── Tab: "请求转换脚本"
      │    ├── select: paramConverterType  （脚本类型，带 description 说明）
      │    ├── [visibleOn: JS]      grid 60/40
      │    │    ├── col-7: editor jsParamConverter
      │    │    └── col-5: 测试面板
      │    ├── [visibleOn: TEMPLATE] textarea templateParamConverter
      │    └── [visibleOn: NONE]    tpl 空状态
      └── Tab: "响应转换脚本"
           └── （镜像结构，字段名前缀 Response）
```

弹框保持 `dialog` + `size: "xl"`，不改为 drawer。

---

## Section 1：Tab 布局

使用 AMIS `tabs` 组件，包含两个 tab：

```json
{
  "type": "tabs",
  "tabs": [
    { "title": "请求转换脚本", "body": [ ... ] },
    { "title": "响应转换脚本", "body": [ ... ] }
  ]
}
```

脚本类型选择器（`select`）放在 grid **上方**，独立占一行，附带 `description` 说明文字。

---

## Section 2：编辑器列（左侧 60%）

使用 AMIS `grid` 组件：

```json
{
  "type": "grid",
  "visibleOn": "${paramConverterType === 'JS'}",
  "columns": [
    { "md": 7, "body": [ /* editor */ ] },
    { "md": 5, "body": [ /* test panel */ ] }
  ]
}
```

编辑器配置（请求和响应统一）：
- `type: "editor"`
- `language: "java"`
- `size: "lg"`（响应编辑器补加，与请求编辑器一致）
- `options.automaticLayout: true`（Tab 切换后自动重算尺寸）
- `editorDidMount` 保留 AI 补全逻辑

---

## Section 3：测试面板（右侧 40%）

面板从上到下包含：

### 3.1 测试输入

```json
{ "type": "textarea", "name": "testParamInput", "label": "测试输入 JSON" }
```

### 3.2 运行测试按钮

`actionType: "ajax"`，调用现有 `api_api_config_test_converter`。

`onEvent` 同时处理两种结果：

```json
"onEvent": {
  "success": {
    "actions": [
      { "actionType": "setValue", "componentId": "testParamStatusId",
        "args": { "value": "success" } },
      { "actionType": "setValue", "componentId": "testParamOutputId",
        "args": { "value": "${event.data.data}" } }
    ]
  },
  "error": {
    "actions": [
      { "actionType": "setValue", "componentId": "testParamStatusId",
        "args": { "value": "error" } },
      { "actionType": "setValue", "componentId": "testParamOutputId",
        "args": { "value": "${event.data.msg|default:'未知错误'}" } }
    ]
  }
}
```

### 3.3 格式化输出区域

用两个 `tpl` 替换原有 `static`，根据 `testParamStatus` 字段切换显示：

**成功状态**（绿色）：
```json
{
  "type": "tpl",
  "visibleOn": "${testParamStatus === 'success'}",
  "tpl": "<div style='font-size:11px;color:#64748b;margin-bottom:4px'>输出结果 <span style='background:#dcfce7;color:#166534;border-radius:10px;padding:1px 7px;font-size:10px'>✓ 成功</span></div><pre style='background:#f0fdf4;border:1px solid #bbf7d0;border-radius:6px;padding:10px;font-family:monospace;font-size:12px;white-space:pre-wrap;word-break:break-all;color:#15803d;min-height:60px'>${testParamOutput}</pre>"
}
```

**失败状态**（红色）：
```json
{
  "type": "tpl",
  "visibleOn": "${testParamStatus === 'error'}",
  "tpl": "<div style='font-size:11px;color:#64748b;margin-bottom:4px'>输出结果 <span style='background:#fee2e2;color:#dc2626;border-radius:10px;padding:1px 7px;font-size:10px'>✗ 失败</span></div><pre style='background:#fef2f2;border:1px solid #fecaca;border-radius:6px;padding:10px;font-family:monospace;font-size:12px;white-space:pre-wrap;word-break:break-all;color:#dc2626;min-height:60px'>${testParamOutput}</pre>"
}
```

`testParamStatus` 和 `testParamOutput` 需声明为 `type: "hidden"` 的表单字段，使其存在于 AMIS 表单数据域（form scope）中：

```json
{ "type": "hidden", "name": "testParamStatus" },
{ "type": "hidden", "name": "testParamOutput" }
```

这样：
1. `setValue` action 可以通过 `componentId`（字段对应的 id）向它们写入值
2. `tpl` 的 `${testParamStatus}` / `${testParamOutput}` 能从 form scope 读取并响应式更新
3. `visibleOn` 的条件判断也可以读到这两个字段

这两个字段会随表单一起提交，但后端 `api_api_config_update` 接口只读取已知字段，额外字段被忽略，无需特殊过滤。

响应转换测试面板字段名为 `testResponseInput`、`testResponseStatus`、`testResponseOutput`（镜像，同样需声明为 hidden）。

---

## Section 4：类型条件显示

| paramConverterType | 显示内容 |
|---|---|
| `JS` | grid（编辑器 + 测试面板） |
| `TEMPLATE` | 全宽 textarea（templateParamConverter），无测试面板 |
| `NONE` 或未选择 | 空状态提示 tpl |

**NONE 空状态**：
```json
{
  "type": "tpl",
  "visibleOn": "${paramConverterType === 'NONE' || !paramConverterType}",
  "tpl": "<div style='padding:40px;text-align:center;color:#9ca3af;font-size:13px;border:1px dashed #e2e8f0;border-radius:8px;margin-top:8px'>无需配置转换脚本</div>"
}
```

---

## Section 5：代码质量修复

在 `api_constant.tsx` 的 `editorDidMount` 中移除以下两行（违反项目前端规范"不提交 debug console.log"）：

```typescript
// 删除：
console.log("Completion triggered");
console.log("Generated Code:", fullCode);
```

保留 `console.warn("No valid response received")` — 这是有意义的警告，不是调试日志。

---

## 不在本次范围内

- 输出区域复制按钮（未选择）
- 改为 Drawer 布局（保持 dialog）
- 后端接口变更
- i18n 新增 key（现有 key 全部复用，无新增文案）
- 其他页面的改动

---

## 文件改动清单

| 文件 | 改动类型 |
|---|---|
| `client/src/pages/api_platform/api_management.tsx` | 重写 `configForm` |
| `client/src/pages/api_platform/constants/api_constant.tsx` | 移除 2 处 `console.log` |
