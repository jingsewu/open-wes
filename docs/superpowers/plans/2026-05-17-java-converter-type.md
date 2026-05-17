# Java Converter Type Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a `JAVA` converter type that executes Groovy/Java scripts via `GroovyClassLoader`, and refactor the four type-specific script columns in `ApiConfigPO` into two unified `paramConverterScript` / `responseConverterScript` columns.

**Architecture:** The `ConverterTypeEnum` gains a `JAVA` value; all data-layer classes (PO, DTO, UpdateParam, VO) drop the four old script fields in favour of two unified ones; `ConverterHelper` routes the new type to the already-existing `JavaScriptUtils.executeJava()`; the test endpoint accepts a `converterType` field so any script type can be tested; a DB migration merges the old columns; the frontend adds a JAVA editor block and fixes the JS placeholder.

**Tech Stack:** Java 17, Spring Boot 3.2.2, Lombok, MapStruct, GraalVM Polyglot, Groovy, JPA/Hibernate, React 17, AMIS schema

---

## File Map

| File | Action | Purpose |
|------|--------|---------|
| `server/modules-api-platform/api-platform-api/src/main/java/org/openwes/api/platform/api/constants/ConverterTypeEnum.java` | Modify | Add `JAVA` enum value |
| `server/modules-api-platform/api-platform/src/main/java/org/openwes/api/platform/domain/entity/ApiConfigPO.java` | Modify | Replace 4 script fields → 2 unified fields |
| `server/modules-api-platform/api-platform-api/src/main/java/org/openwes/api/platform/api/dto/api/ApiConfigDTO.java` | Modify | Replace 4 script fields → 2 unified fields |
| `server/modules-api-platform/api-platform/src/main/java/org/openwes/api/platform/controller/param/apiconfig/ApiConfigUpdateParam.java` | Modify | Replace 4 script fields → 2 unified fields |
| `server/modules-api-platform/api-platform/src/main/java/org/openwes/api/platform/controller/param/apiconfig/ApiConfigVO.java` | Modify | Replace 4 script fields → 2 unified fields |
| `server/modules-api-platform/api-platform/src/main/java/org/openwes/api/platform/utils/ConverterHelper.java` | Modify | Add JAVA routing via `JavaScriptUtils.executeJava()` |
| `server/modules-api-platform/api-platform/src/main/java/org/openwes/api/platform/controller/param/apiconfig/ApiConfigTestConverterParam.java` | Modify | Replace `jsScript` → `converterType` + `script` |
| `server/modules-api-platform/api-platform/src/main/java/org/openwes/api/platform/controller/ApiConfigManagementController.java` | Modify | Update `testConverter` to use unified param |
| `server/modules-api-platform/api-platform/src/test/java/org/openwes/api/platform/utils/ConverterHelperTest.java` | Create | Unit tests for converter routing |
| `docs/db-migration/2026-05-17-api-config-script-unification.sql` | Create | SQL to merge 4 columns → 2 |
| `client/src/pages/api_platform/api_management.tsx` | Modify | Add JAVA editor block, fix JS placeholder, unify field names |

---

## Task 1: Add JAVA to ConverterTypeEnum

**Files:**
- Modify: `server/modules-api-platform/api-platform-api/src/main/java/org/openwes/api/platform/api/constants/ConverterTypeEnum.java`

- [ ] **Step 1: Add the JAVA enum value**

  Open the file. Current content:
  ```java
  NONE("NONE", "NONE"),
  JS("JS", "javascript"),
  TEMPLATE("TEMPLATE", "template");
  ```

  Replace with:
  ```java
  NONE("NONE", "NONE"),
  JS("JS", "javascript"),
  JAVA("JAVA", "java"),
  TEMPLATE("TEMPLATE", "template");
  ```

- [ ] **Step 2: Verify the project compiles**

  ```bash
  cd server && ./gradlew :modules-api-platform:api-platform-api:compileJava
  ```
  Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

  ```bash
  git add server/modules-api-platform/api-platform-api/src/main/java/org/openwes/api/platform/api/constants/ConverterTypeEnum.java
  git commit -m "feat: add JAVA to ConverterTypeEnum"
  ```

---

## Task 2: Refactor script fields in data-layer classes

Replace four type-specific script fields (`jsParamConverter`, `jsResponseConverter`, `templateParamConverter`, `templateResponseConverter`) with two unified ones (`paramConverterScript`, `responseConverterScript`) across all four data classes.

`ApiConfigServiceImpl.updateConfig()` and `ApiConfigManagementController.getConfigByCode()` both use `BeanUtils.copyProperties` — they work by matching field names, so renaming consistently across source and target is all that is needed.

`ApiConfigTransfer` (MapStruct) uses `ReportingPolicy.IGNORE` and maps by name — no `@Mapping` annotations are needed, the new field names will match automatically.

**Files:**
- Modify: `server/modules-api-platform/api-platform/src/main/java/org/openwes/api/platform/domain/entity/ApiConfigPO.java`
- Modify: `server/modules-api-platform/api-platform-api/src/main/java/org/openwes/api/platform/api/dto/api/ApiConfigDTO.java`
- Modify: `server/modules-api-platform/api-platform/src/main/java/org/openwes/api/platform/controller/param/apiconfig/ApiConfigUpdateParam.java`
- Modify: `server/modules-api-platform/api-platform/src/main/java/org/openwes/api/platform/controller/param/apiconfig/ApiConfigVO.java`

- [ ] **Step 1: Update `ApiConfigPO.java`**

  Remove these four fields:
  ```java
  @Column(columnDefinition = "text comment '请求参数转换脚本'")
  private String jsParamConverter;
  @Column(columnDefinition = "text comment '响应参数转换脚本'")
  private String jsResponseConverter;

  @Column(columnDefinition = "text comment '请求参数转换模板'")
  private String templateParamConverter;
  @Column(columnDefinition = "text comment '响应参数转换模板'")
  private String templateResponseConverter;
  ```

  Add these two fields in their place:
  ```java
  @Column(columnDefinition = "text")
  @Comment("参数转换脚本")
  private String paramConverterScript;

  @Column(columnDefinition = "text")
  @Comment("响应转换脚本")
  private String responseConverterScript;
  ```

- [ ] **Step 2: Update `ApiConfigDTO.java`**

  Remove:
  ```java
  private String jsParamConverter;
  private String jsResponseConverter;

  private String templateParamConverter;
  private String templateResponseConverter;
  ```

  Add:
  ```java
  private String paramConverterScript;
  private String responseConverterScript;
  ```

- [ ] **Step 3: Update `ApiConfigUpdateParam.java`**

  Remove:
  ```java
  @Schema(title = "JS 类型的请求请求转换脚本")
  private String jsParamConverter;

  @Schema(title = "FreeMarker 类型的请求转换脚本")
  private String templateParamConverter;

  @Schema(title = "JS 类型的请求响应转换脚本")
  private String jsResponseConverter;

  @Schema(title = "FreeMarker 类型的响应转换脚本")
  private String templateResponseConverter;
  ```

  Add:
  ```java
  @Schema(title = "参数转换脚本")
  private String paramConverterScript;

  @Schema(title = "响应转换脚本")
  private String responseConverterScript;
  ```

- [ ] **Step 4: Update `ApiConfigVO.java`**

  Remove:
  ```java
  @Schema(title = "JS 类型的请求请求转换脚本")
  private String jsParamConverter;

  @Schema(title = "FreeMarker 类型的请求转换脚本")
  private String templateParamConverter;

  @Schema(title = "JS 类型的请求响应转换脚本")
  private String jsResponseConverter;

  @Schema(title = "FreeMarker 类型的响应转换脚本")
  private String templateResponseConverter;
  ```

  Add:
  ```java
  @Schema(title = "参数转换脚本")
  private String paramConverterScript;

  @Schema(title = "响应转换脚本")
  private String responseConverterScript;
  ```

- [ ] **Step 5: Verify compilation**

  ```bash
  cd server && ./gradlew :modules-api-platform:api-platform:compileJava :modules-api-platform:api-platform-api:compileJava
  ```
  Expected: `BUILD SUCCESSFUL`

- [ ] **Step 6: Commit**

  ```bash
  git add server/modules-api-platform/api-platform/src/main/java/org/openwes/api/platform/domain/entity/ApiConfigPO.java \
          server/modules-api-platform/api-platform-api/src/main/java/org/openwes/api/platform/api/dto/api/ApiConfigDTO.java \
          server/modules-api-platform/api-platform/src/main/java/org/openwes/api/platform/controller/param/apiconfig/ApiConfigUpdateParam.java \
          server/modules-api-platform/api-platform/src/main/java/org/openwes/api/platform/controller/param/apiconfig/ApiConfigVO.java
  git commit -m "refactor: unify converter script fields in ApiConfig data classes"
  ```

---

## Task 3: Update ConverterHelper to route JAVA type (TDD)

**Files:**
- Modify: `server/modules-api-platform/api-platform/src/main/java/org/openwes/api/platform/utils/ConverterHelper.java`
- Create: `server/modules-api-platform/api-platform/src/test/java/org/openwes/api/platform/utils/ConverterHelperTest.java`

- [ ] **Step 1: Create the test directory structure**

  ```bash
  mkdir -p "server/modules-api-platform/api-platform/src/test/java/org/openwes/api/platform/utils"
  ```

- [ ] **Step 2: Write the failing tests**

  Create `server/modules-api-platform/api-platform/src/test/java/org/openwes/api/platform/utils/ConverterHelperTest.java`:

  ```java
  package org.openwes.api.platform.utils;

  import org.junit.jupiter.api.Test;
  import org.openwes.api.platform.api.constants.ConverterTypeEnum;
  import org.openwes.api.platform.domain.entity.ApiConfigPO;

  import java.util.HashMap;
  import java.util.Map;

  import static org.assertj.core.api.Assertions.assertThat;

  class ConverterHelperTest {

      @Test
      void convertParam_withNullConfig_returnsInputUnchanged() {
          Map<String, Object> input = Map.of("key", "value");
          Object result = ConverterHelper.convertParam(null, input);
          assertThat(result).isEqualTo(input);
      }

      @Test
      void convertParam_withNoneType_returnsInputUnchanged() {
          Map<String, Object> input = Map.of("key", "value");
          ApiConfigPO config = new ApiConfigPO();
          config.setParamConverterType(ConverterTypeEnum.NONE);
          Object result = ConverterHelper.convertParam(config, input);
          assertThat(result).isEqualTo(input);
      }

      @Test
      void convertParam_withJavaType_executesGroovyScript() {
          Map<String, Object> input = new HashMap<>();
          input.put("name", "World");

          ApiConfigPO config = new ApiConfigPO();
          config.setParamConverterType(ConverterTypeEnum.JAVA);
          config.setParamConverterScript("""
                  //java:convert
                  public class TestConverter {
                      public Object convert(Object param) {
                          Map<String, Object> map = (Map<String, Object>) param;
                          return "Hello, " + map.get("name");
                      }
                  }
                  """);

          Object result = ConverterHelper.convertParam(config, input);
          assertThat(result).isEqualTo("Hello, World");
      }

      @Test
      void convertResponse_withJavaType_executesGroovyScript() {
          Map<String, Object> input = new HashMap<>();
          input.put("code", "200");

          ApiConfigPO config = new ApiConfigPO();
          config.setResponseConverterType(ConverterTypeEnum.JAVA);
          config.setResponseConverterScript("""
                  //java:convert
                  public class ResponseConverter {
                      public Object convert(Object param) {
                          Map<String, Object> map = (Map<String, Object>) param;
                          return map.get("code");
                      }
                  }
                  """);

          Object result = ConverterHelper.convertResponse(config, input);
          assertThat(result).isEqualTo("200");
      }

      @Test
      void convertParam_withNullConverterType_returnsInputUnchanged() {
          Map<String, Object> input = Map.of("key", "value");
          ApiConfigPO config = new ApiConfigPO();
          // paramConverterType is null by default
          Object result = ConverterHelper.convertParam(config, input);
          assertThat(result).isEqualTo(input);
      }
  }
  ```

- [ ] **Step 3: Run the tests to verify they fail for the right reason**

  ```bash
  cd server && ./gradlew :modules-api-platform:api-platform:test --tests "org.openwes.api.platform.utils.ConverterHelperTest" 2>&1 | tail -30
  ```
  Expected: compilation error on `setParamConverterScript` / `setResponseConverterScript` — these fields don't exist yet on `ApiConfigPO` if Task 2 isn't done, or the JAVA routing is missing in ConverterHelper. After Task 2, expect test failures on `convertParam_withJavaType_executesGroovyScript` because `ConverterHelper` doesn't handle JAVA yet.

- [ ] **Step 4: Update `ConverterHelper.java` to add JAVA routing**

  Replace the entire contents of `ConverterHelper.java` with:

  ```java
  package org.openwes.api.platform.utils;

  import lombok.extern.slf4j.Slf4j;
  import org.graalvm.polyglot.Context;
  import org.openwes.api.platform.api.constants.ConverterTypeEnum;
  import org.openwes.api.platform.domain.entity.ApiConfigPO;
  import org.openwes.common.utils.utils.JsonUtils;
  import org.springframework.context.annotation.Lazy;
  import org.springframework.stereotype.Component;

  import java.nio.charset.StandardCharsets;
  import java.util.Map;

  @Slf4j
  @Lazy
  @Component
  public class ConverterHelper {

      public static Object convertParam(ApiConfigPO apiConfigPO, Object dataObj) {
          if (apiConfigPO == null) {
              return dataObj;
          }
          ConverterTypeEnum type = apiConfigPO.getParamConverterType();
          if (type == null || type == ConverterTypeEnum.NONE) {
              return dataObj;
          }
          return convert(type, apiConfigPO.getParamConverterScript(), dataObj);
      }

      public static Object convertResponse(ApiConfigPO apiConfigPO, Object dataObj) {
          if (apiConfigPO == null) {
              return dataObj;
          }
          ConverterTypeEnum type = apiConfigPO.getResponseConverterType();
          if (type == null || type == ConverterTypeEnum.NONE) {
              return dataObj;
          }
          return convert(type, apiConfigPO.getResponseConverterScript(), dataObj);
      }

      private static Object convert(ConverterTypeEnum type, String script, Object dataObj) {
          return switch (type) {
              case JS -> convertWithJs(script, dataObj);
              case JAVA -> JavaScriptUtils.executeJava(script, dataObj);
              case TEMPLATE -> convertWithTemplate(script, dataObj);
              default -> dataObj;
          };
      }

      private static String convertWithJs(String script, Object obj) {
          try (Context context = Context.create()) {
              Object result = JavaScriptUtils.executeJs(context, script, obj);
              return JsonUtils.obj2String(result);
          }
      }

      private static Object convertWithTemplate(String script, Object dataObj) {
          return FreeMarkerHelper.convertByTemplate(
                  script.getBytes(StandardCharsets.UTF_8), dataObj, (Map<String, Object>) null);
      }

      public static boolean isAsyncApi(String apiType, Integer count) {
          return false;
      }
  }
  ```

- [ ] **Step 5: Run the tests to verify they pass**

  ```bash
  cd server && ./gradlew :modules-api-platform:api-platform:test --tests "org.openwes.api.platform.utils.ConverterHelperTest"
  ```
  Expected: `BUILD SUCCESSFUL` — all 5 tests pass

- [ ] **Step 6: Commit**

  ```bash
  git add server/modules-api-platform/api-platform/src/main/java/org/openwes/api/platform/utils/ConverterHelper.java \
          server/modules-api-platform/api-platform/src/test/java/org/openwes/api/platform/utils/ConverterHelperTest.java
  git commit -m "feat: add JAVA converter type routing in ConverterHelper"
  ```

---

## Task 4: Update test endpoint to support all script types

**Files:**
- Modify: `server/modules-api-platform/api-platform/src/main/java/org/openwes/api/platform/controller/param/apiconfig/ApiConfigTestConverterParam.java`
- Modify: `server/modules-api-platform/api-platform/src/main/java/org/openwes/api/platform/controller/ApiConfigManagementController.java`

- [ ] **Step 1: Replace `ApiConfigTestConverterParam.java`**

  Full file content:

  ```java
  package org.openwes.api.platform.controller.param.apiconfig;

  import io.swagger.v3.oas.annotations.media.Schema;
  import jakarta.validation.constraints.NotEmpty;
  import jakarta.validation.constraints.NotNull;
  import lombok.Data;
  import org.openwes.api.platform.api.constants.ConverterTypeEnum;

  @Data
  @Schema(description = "测试接口参数转换脚本")
  public class ApiConfigTestConverterParam {

      @NotNull(message = "转换类型不能为空")
      @Schema(title = "转换类型（仅支持 JS / JAVA）", requiredMode = Schema.RequiredMode.REQUIRED)
      private ConverterTypeEnum converterType;

      @NotEmpty(message = "转换脚本不能为空")
      @Schema(title = "转换脚本", requiredMode = Schema.RequiredMode.REQUIRED)
      private String script;

      @NotEmpty(message = "输入 JSON 不能为空")
      @Schema(title = "输入 JSON 字符串", requiredMode = Schema.RequiredMode.REQUIRED)
      private String inputJson;
  }
  ```

- [ ] **Step 2: Update `testConverter` in `ApiConfigManagementController.java`**

  Replace only the `testConverter` method (lines 48–61). New method:

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

  Also remove the now-unused import `org.openwes.api.platform.api.constants.ConverterTypeEnum` from the controller (it was only used to hardcode `ConverterTypeEnum.JS`).

- [ ] **Step 3: Verify compilation**

  ```bash
  cd server && ./gradlew :modules-api-platform:api-platform:compileJava
  ```
  Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

  ```bash
  git add server/modules-api-platform/api-platform/src/main/java/org/openwes/api/platform/controller/param/apiconfig/ApiConfigTestConverterParam.java \
          server/modules-api-platform/api-platform/src/main/java/org/openwes/api/platform/controller/ApiConfigManagementController.java
  git commit -m "feat: generalise test-converter endpoint to support JS and JAVA types"
  ```

---

## Task 5: DB Migration SQL

**Files:**
- Create: `docs/db-migration/2026-05-17-api-config-script-unification.sql`

- [ ] **Step 1: Create the migration file**

  ```sql
  -- Migration: unify api_config converter script columns
  -- Merges js_param_converter + template_param_converter → param_converter_script
  -- Merges js_response_converter + template_response_converter → response_converter_script
  -- Run against the wes database before deploying the new server version.

  -- Step 1: add unified columns
  ALTER TABLE a_api_config
    ADD COLUMN param_converter_script    TEXT COMMENT '参数转换脚本',
    ADD COLUMN response_converter_script TEXT COMMENT '响应转换脚本';

  -- Step 2: migrate existing data (JS takes priority; falls back to TEMPLATE)
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

- [ ] **Step 2: Commit**

  ```bash
  git add docs/db-migration/2026-05-17-api-config-script-unification.sql
  git commit -m "chore: add DB migration SQL for api_config script field unification"
  ```

---

## Task 6: Frontend — add JAVA editor block and fix JS placeholder

**Files:**
- Modify: `client/src/pages/api_platform/api_management.tsx`

This task modifies `configForm` in three areas for both tabs (request + response are mirrors of each other).

### Changes per tab

1. **Unified field names:** `jsParamConverter` → `paramConverterScript`, `templateParamConverter` → `paramConverterScript`, `jsResponseConverter` → `responseConverterScript`, `templateResponseConverter` → `responseConverterScript`
2. **JS editor:** fix `language` from `"java"` to `"javascript"`, update placeholder to JS example
3. **New JAVA block:** same grid structure as JS, `language: "java"`, Java/Groovy placeholder, test panel sends `converterType: "JAVA"`
4. **Test button payload:** add `converterType` field, rename `jsScript` → `script`
5. **TEMPLATE textarea:** rename field to unified name (already covered by point 1)

- [ ] **Step 1: Replace `configForm` in `api_management.tsx`**

  Replace the entire `const configForm = [...]` block (lines 137–423) with:

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
  ```

- [ ] **Step 2: Verify TypeScript compiles without errors**

  ```bash
  cd client && npx tsc --noEmit 2>&1 | head -30
  ```
  Expected: no errors

- [ ] **Step 3: Commit**

  ```bash
  git add client/src/pages/api_platform/api_management.tsx
  git commit -m "feat: add JAVA converter editor block and fix JS placeholder in config dialog"
  ```

---

## Self-Review Notes

- **Spec coverage:** All 10 spec sections have a corresponding task. ✓
- **Type consistency:** `paramConverterScript` / `responseConverterScript` used consistently across Tasks 2, 3, 4, 6. ✓
- **`ApiConfigTransfer`:** MapStruct with `IGNORE` unmapped policy — field renames auto-propagate, no manual mapping needed. ✓
- **`ApiConfigServiceImpl`:** Uses `BeanUtils.copyProperties` — works by field name match, no change needed. ✓
- **TEMPLATE type in test endpoint:** Not supported by `testConverter` (uses FreeMarker file templates, tested separately via `TemplateController`). The param class documents `JS / JAVA` only. ✓
- **`isAsyncApi` method:** Preserved unchanged in `ConverterHelper`. ✓
