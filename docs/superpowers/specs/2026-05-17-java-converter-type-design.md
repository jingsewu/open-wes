# Java Converter Type — Design Spec

**Date:** 2026-05-17
**Status:** Approved

## Overview

Add a `JAVA` converter type to the API Platform's converter pipeline, allowing users to write Groovy/Java scripts (executed via `GroovyClassLoader`) as an alternative to the existing `JS` (GraalVM) and `TEMPLATE` (FreeMarker) converters.

As part of this work, the `ApiConfigPO` table is refactored to eliminate the redundant per-type script fields (`jsParamConverter`, `jsResponseConverter`, `templateParamConverter`, `templateResponseConverter`) in favour of two unified fields (`paramConverterScript`, `responseConverterScript`). The `converterType` enum already carries enough information to know how to execute the script.

---

## Scope of Changes

### 1. `ConverterTypeEnum`

**File:** `modules-api-platform/api-platform-api/.../constants/ConverterTypeEnum.java`

Add `JAVA`:

```java
NONE("NONE", "NONE"),
JS("JS", "javascript"),
JAVA("JAVA", "java"),
TEMPLATE("TEMPLATE", "template");
```

---

### 2. `ApiConfigPO` — field refactor

**File:** `modules-api-platform/api-platform/.../domain/entity/ApiConfigPO.java`

**Remove** the four type-specific script columns:
- `jsParamConverter`
- `jsResponseConverter`
- `templateParamConverter`
- `templateResponseConverter`

**Add** two unified script columns:

```java
@Column(columnDefinition = "text")
@Comment("参数转换脚本")
private String paramConverterScript;

@Column(columnDefinition = "text")
@Comment("响应转换脚本")
private String responseConverterScript;
```

---

### 3. `ApiConfigDTO` — field refactor

**File:** `modules-api-platform/api-platform-api/.../dto/api/ApiConfigDTO.java`

Same replacement as `ApiConfigPO`: remove 4 type-specific fields, add `paramConverterScript` + `responseConverterScript`.

`ApiConfigTransfer` (MapStruct) requires no explicit `@Mapping` annotations — field names match between `ApiConfigPO` and `ApiConfigDTO`.

---

### 4. `ApiConfigUpdateParam` — field refactor

**File:** `modules-api-platform/api-platform/.../controller/param/apiconfig/ApiConfigUpdateParam.java`

Remove:
- `jsParamConverter`
- `templateParamConverter`
- `jsResponseConverter`
- `templateResponseConverter`

Add:
```java
@Schema(title = "参数转换脚本")
private String paramConverterScript;

@Schema(title = "响应转换脚本")
private String responseConverterScript;
```

`ApiConfigServiceImpl.updateConfig()` uses `BeanUtils.copyProperties` — no mapper change needed.

---

### 5. `ApiConfigVO` — field refactor

**File:** `modules-api-platform/api-platform/.../controller/param/apiconfig/ApiConfigVO.java`

Same replacement as `ApiConfigUpdateParam`.

`ApiConfigManagementController.getConfigByCode()` uses `BeanUtils.copyProperties(apiConfigPO, apiConfigVO)` — no change needed.

---

### 6. `ConverterHelper` — add JAVA routing

**File:** `modules-api-platform/api-platform/.../utils/ConverterHelper.java`

Both `convertParam` and `convertResponse` updated to read from the unified script field and route on type:

```java
public static Object convertParam(ApiConfigPO apiConfigPO, Object dataObj) {
    if (apiConfigPO == null) return dataObj;
    ConverterTypeEnum type = apiConfigPO.getParamConverterType();
    if (type == null || type == ConverterTypeEnum.NONE) return dataObj;
    return convert(type, apiConfigPO.getParamConverterScript(), dataObj);
}

public static Object convertResponse(ApiConfigPO apiConfigPO, Object dataObj) {
    if (apiConfigPO == null) return dataObj;
    ConverterTypeEnum type = apiConfigPO.getResponseConverterType();
    if (type == null || type == ConverterTypeEnum.NONE) return dataObj;
    return convert(type, apiConfigPO.getResponseConverterScript(), dataObj);
}

private static Object convert(ConverterTypeEnum type, String script, Object dataObj) {
    return switch (type) {
        case JS -> convertWithJs(script, dataObj);
        case JAVA -> convertWithJava(script, dataObj);
        case TEMPLATE -> convertWithTemplate(script, dataObj);
        default -> dataObj;
    };
}

private static String convertWithJs(String script, Object obj) {
    try (Context context = Context.create()) {
        return JsonUtils.obj2String(JavaScriptUtils.executeJs(context, script, obj));
    }
}

private static Object convertWithJava(String script, Object obj) {
    return JavaScriptUtils.executeJava(script, obj);
}

private static Object convertWithTemplate(String script, Object dataObj) {
    return FreeMarkerHelper.convertByTemplate(
        script.getBytes(StandardCharsets.UTF_8), dataObj, (Map<String, Object>) null);
}
```

---

### 7. `ApiConfigTestConverterParam` — generalise

**File:** `modules-api-platform/api-platform/.../controller/param/apiconfig/ApiConfigTestConverterParam.java`

Remove `jsScript`, add `converterType` + `script`. Only `JS` and `JAVA` types are valid for this endpoint — `TEMPLATE` has its own test flow in `TemplateController`, and `NONE` requires no script.

```java
@NotNull(message = "转换类型不能为空")
@Schema(title = "转换类型（仅支持 JS / JAVA）", requiredMode = Schema.RequiredMode.REQUIRED)
private ConverterTypeEnum converterType;

@NotEmpty(message = "转换脚本不能为空")
@Schema(title = "转换脚本", requiredMode = Schema.RequiredMode.REQUIRED)
private String script;

@NotEmpty(message = "输入 JSON 不能为空")
@Schema(title = "输入 JSON 字符串", requiredMode = Schema.RequiredMode.REQUIRED)
private String inputJson;
```

---

### 8. `ApiConfigManagementController.testConverter` — update

**File:** `modules-api-platform/api-platform/.../controller/ApiConfigManagementController.java`

```java
@PostMapping("/test-converter")
@Operation(summary = "测试参数转换脚本")
public Response<String> testConverter(@RequestBody @Valid ApiConfigTestConverterParam param) {
    try {
        Object input = JsonUtils.string2MapObject(param.getInputJson());
        ApiConfigPO apiConfigPO = new ApiConfigPO();
        apiConfigPO.setParamConverterType(param.getConverterType());
        apiConfigPO.setParamConverterScript(param.getScript());
        Object result = ConverterHelper.convertParam(apiConfigPO, input);
        return Response.success(JsonUtils.obj2String(result));
    } catch (Exception e) {
        return Response.<String>builder().code("1").msg(e.getMessage()).build();
    }
}
```

---

### 9. DB Migration SQL

Run as a Flyway/Liquibase script or manual migration against `a_api_config`:

```sql
-- Step 1: add unified columns
ALTER TABLE a_api_config
  ADD COLUMN param_converter_script   TEXT COMMENT '参数转换脚本',
  ADD COLUMN response_converter_script TEXT COMMENT '响应转换脚本';

-- Step 2: migrate existing data (JS takes priority, fallback to TEMPLATE)
UPDATE a_api_config
SET param_converter_script    = COALESCE(js_param_converter, template_param_converter),
    response_converter_script = COALESCE(js_response_converter, template_response_converter);

-- Step 3: drop old columns
ALTER TABLE a_api_config
  DROP COLUMN js_param_converter,
  DROP COLUMN js_response_converter,
  DROP COLUMN template_param_converter,
  DROP COLUMN template_response_converter;
```

> **Note:** If this project uses Flyway, create a new versioned migration file under `server/initdb.d/` or the configured migration path.

---

### 10. Frontend — `api_management.tsx`

**File:** `client/src/pages/api_platform/api_management.tsx`

#### Field name changes in `configForm`

All references to the four old field names are replaced with the unified names:

| Old field | New field |
|-----------|-----------|
| `jsParamConverter` | `paramConverterScript` |
| `jsResponseConverter` | `responseConverterScript` |
| `templateParamConverter` | `paramConverterScript` |
| `templateResponseConverter` | `responseConverterScript` |

#### Per-type editor blocks (request tab, identical mirror for response tab)

**JS block** (`visibleOn: "${paramConverterType === 'JS'}"`)
- Editor `name`: `paramConverterScript`
- Editor `language`: `javascript`
- Placeholder:
```
// param contains the parsed input object
function convert(param) {
    return {
        result: param.name
    };
}
```

**JAVA block** (`visibleOn: "${paramConverterType === 'JAVA'}"`) — new
- Same grid layout as JS: editor (md=7, left) + test panel (md=5, right)
- Editor `name`: `paramConverterScript`
- Editor `language`: `java`
- Placeholder:
```
//java:convert
public class MyConverter {
    public Object convert(Object param) {
        Map<String, Object> input = (Map<String, Object>) param;
        return "Hello, " + input.get("name");
    }
}
```
- Test panel: same structure as JS (textarea input, test button, success/error output display)

**TEMPLATE block** (`visibleOn: "${paramConverterType === 'TEMPLATE'}"`)
- Textarea `name`: `paramConverterScript` (was `templateParamConverter`)
- No other changes

**NONE block** — no change

#### Test button payload update

Both request and response test buttons change `jsScript` → `script`, and add `converterType`:

```js
// request tab
data: {
    converterType: "${paramConverterType}",
    script: "${paramConverterScript}",
    inputJson: "${testParamInput}"
}

// response tab
data: {
    converterType: "${responseConverterType}",
    script: "${responseConverterScript}",
    inputJson: "${testResponseInput}"
}
```

---

## Files Changed Summary

| File | Change type |
|------|-------------|
| `ConverterTypeEnum.java` | Add `JAVA` enum value |
| `ApiConfigPO.java` | Replace 4 fields → 2 unified fields |
| `ApiConfigDTO.java` | Replace 4 fields → 2 unified fields |
| `ApiConfigUpdateParam.java` | Replace 4 fields → 2 unified fields |
| `ApiConfigVO.java` | Replace 4 fields → 2 unified fields |
| `ConverterHelper.java` | Add `JAVA` routing via `JavaScriptUtils.executeJava()` |
| `ApiConfigTestConverterParam.java` | Replace `jsScript` → `converterType` + `script` |
| `ApiConfigManagementController.java` | Update `testConverter` endpoint |
| `api_management.tsx` | Unified field names, add JAVA block, fix placeholders |
| DB migration SQL | Merge 4 columns → 2, drop old columns |

**No changes required:** `ApiConfigTransfer.java`, `ApiConfigServiceImpl.java`, `AbstractRequestHandler.java`, `AbstractCallbackHandler.java`
