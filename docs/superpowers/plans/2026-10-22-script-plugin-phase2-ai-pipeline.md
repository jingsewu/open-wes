# Script Plugin Phase 2 — AI Draft Pipeline Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add the `server/modules-ai/ai-plugin` module: requirement → structured Groovy plugin draft → static + materialization validation → retry (≤3) → AI revision saved with provenance, streamed to the workbench over SSE, with a per-user daily quota and a generation audit.

**Architecture:** `ai-plugin` is hosted in `wes-server` (it owns the database and the runtime). It reads the frozen Phase 1 contract from `PluginContractCatalog`, assembles a temperature-0 ZhipuAI prompt, consumes `ChatModel.stream(Prompt)` on a bounded-elastic worker, parses the model output into a draft document, validates it with a new reusable `ScriptPluginStaticValidator` plus the existing wes `ScriptPluginMaterializationValidator`, and saves a normal `RevisionOriginEnum.AI` revision through `ScriptPluginService`. It never enables a plugin. SSE events (`chunk`, `stage`, `result`, `error`) use a unicast Reactor sink; quota is an atomic Redis daily counter; business limits live in a new singleton DB table, not Nacos.

**Tech Stack:** Java 17, Spring Boot 3.2.2, Spring MVC 6.1 (`Flux<ServerSentEvent<T>>`), Spring AI 1.0.0-M6 / ZhipuAI starter (`GLM-4-air`), Reactor 3.6, Jackson, JPA/Liquibase, Redisson via `RedisUtils`, JUnit 5 + Mockito.

**Spec:** `docs/superpowers/specs/2026-10-08-script-plugin-ai-workbench-design.md`

## Global Constraints

- **Java 17 and Gradle from `server/`.** On this machine run every Gradle command as:
  ```bash
  cd D:/open-wes/server
  export JAVA_HOME="${JAVA_HOME:-C:/Users/niekang/.jdks/ms-17.0.20.1}"
  ./gradlew <task>
  ```
  Docker is **not installed** here. Phase 1b's manual E2E remains unrun; do not claim it passed. The Phase 2 manual E2E in Task 10 is documented but cannot be executed on this host.
- **No new Nacos/business configuration.** Runtime limits live in `p_script_plugin_ai_setting`; optional behavior switches (`ai-plugin.*`) have safe defaults.
- **AI output is untrusted.** Parse it into a typed draft document and validate it before saving. The only code execution during generation is the existing wes materialization dry-run (child context + injection + `@PostConstruct`), which the spec explicitly includes; declared script methods are not invoked.
- **AI never enables anything.** The pipeline may call `createDraft`-created identities and `saveRevision`; it must never call `enable`, `rollback`, `disable` or `archive`.
- **Only `DRAFT` or `DISABLED` plugins accept AI drafts.** This matches Phase 1's `saveRevision` guard; an `ENABLED` plugin must be disabled first.
- **Authorization is Phase 3.** Phase 2 adds no new role model: the AI endpoints sit behind the gateway JWT like the rest of `/plugin-platform/**`. Do not invent an admin-role check here; the workbench (Phase 3) adds roles and the audit UI.
- **Temperature is always `0.0`.** The model id is `ai-plugin.model` (default `GLM-4-air`) and is stored on every AI revision together with `PluginPromptTemplates.PROMPT_VERSION`.
- **Retries are capped at 3** by `AiSettings` (DB values above 3 clamp to 3). Each retry feeds the previous parse/validation error back into the prompt. A retry only starts when `elapsed + request-timeout-ms <= stream-budget-ms` (defaults 120 s / 170 s), so generation always ends before the 180 s MVC async timeout.
- **Per-call output cap** is `min(p_script_plugin_ai_setting.max_tokens_per_call, ai-plugin.max-tokens-ceiling)`.
- **Prompts carry structure, not secrets.** Never log the requirement, prompt, model output or generated source at info level. Scripts must read credentials via `ctx.configString` / `ctx.config`; the prompt states this.
- **English only** in logs, errors, prompts and code comments.
- **Liquibase:** new changelog only; `onFail="MARK_RAN"` preconditions; never edit an already-applied SQL file or existing changeset.
- **DDD/repository/test conventions:** only `find*`/`save*` repository methods; tests live in the module that owns the code; `test { ignoreFailures = false }` is already global.
- Commit after every task with a conventional message and end the message with `Co-Authored-By: Claude Code <noreply@anthropic.com>`.

## Spec normalization

The design spec predates the Phase 1 DTOs. Where the two disagree, **this plan follows the platform DTOs**; the spec's JSON example is illustrative. Do not "fix" the plan back to the spec text.

| Spec text | This plan / platform reality |
|---|---|
| Example declaration combines `"script": "WaveScript.wave"` (method inside the script name) | `ExtensionDeclarationDTO` has separate `script` (a key of `scripts`) and `method` (the Groovy method). |
| Example HTTP declaration uses `"method": "POST"` | HTTP endpoints carry `httpMethod`; `method` is always the script method. |
| Example path `"/ext/packing/calc"` | Stored paths are relative to `/plugin-platform/ext/{pluginKey}/` and start with `/` (e.g. `/status`); `ExtHttpDispatcher` owns the prefix. |
| Pipeline step 1 produces a "structured brief" with target app | The target app already exists on the plugin row (`apply_system`); the request carries only `pluginKey` + `requirement`. Entities, behavior and config needs are decided inside the model call (system-prompt rule 14) rather than persisted as a separate artifact. |
| Config field type `NUMBER` | The plan and prompt use `STRING` / `INT` / `BOOLEAN`, which `ctx.configInt`/`configBool` can read; `ConfigFieldDTO.type` is a free-form string. |
| SSE `stage` values `compiling` / `wiring` / `retry#2` | Same vocabulary; the validator additionally emits `declarations` before `compiling` when a declaration check fails, so the client should treat `stage` as an open label, not a closed enum. |
| `ctx.log` / `ctx.pluginKey` shown without parentheses | Both are methods: `ctx.log()` and `ctx.pluginKey()`. |

## File Map

**New module — `server/modules-ai/ai-plugin/`**

| File | Responsibility |
|---|---|
| `build.gradle` | Dependencies: script-plugin-api/core, script-engine, common-utils, Spring AI, JPA, web/validation, test-only runtime |
| `src/main/java/org/openwes/ai/plugin/config/AiPluginProperties.java` | `ai-plugin.*` switches with safe defaults |
| `src/main/java/org/openwes/ai/plugin/domain/model/AiSettings.java` | Clamped per-call policy record |
| `src/main/java/org/openwes/ai/plugin/domain/entity/ScriptPluginAiSetting.java` | Singleton settings row |
| `src/main/java/org/openwes/ai/plugin/domain/repository/ScriptPluginAiSettingRepository.java` | `findBySingletonKey` |
| `src/main/java/org/openwes/ai/plugin/domain/service/AiSettingsService.java` | Reads settings, falls back to defaults |
| `src/main/java/org/openwes/ai/plugin/domain/enums/AiGenerationStatusEnum.java` | `RUNNING/SUCCEEDED/FAILED/CANCELLED` |
| `src/main/java/org/openwes/ai/plugin/domain/entity/ScriptPluginAiGeneration.java` | Audit row with lifecycle methods |
| `src/main/java/org/openwes/ai/plugin/domain/repository/ScriptPluginAiGenerationRepository.java` | Recent audit lookup |
| `src/main/java/org/openwes/ai/plugin/domain/service/AiGenerationAuditService.java` | Start/finish audit rows, map to DTOs |
| `src/main/java/org/openwes/ai/plugin/domain/service/AiQuotaService.java` | Atomic Redis daily counter |
| `src/main/java/org/openwes/ai/plugin/dto/AiGenerationDTO.java` | Audit view |
| `src/main/java/org/openwes/ai/plugin/exception/AiPluginErrorDescEnum.java` | Stable error codes/messages |
| `src/main/java/org/openwes/ai/plugin/exception/AiDraftParseException.java` | Model-output parse failure |
| `src/main/java/org/openwes/ai/plugin/domain/model/AiDraftDocument.java` | Structured draft (`meta`, `extensions`, `configSchema`, `scripts`) |
| `src/main/java/org/openwes/ai/plugin/domain/service/AiDraftDocumentParser.java` | Fence/prose stripping + strict semantic checks |
| `src/main/java/org/openwes/ai/plugin/prompt/PluginPromptTemplates.java` | System rules, output format, few-shot examples, prompt version |
| `src/main/java/org/openwes/ai/plugin/prompt/AiPromptAssembler.java` | Builds a temperature-0 `Prompt` from plugin + contract + base revision + errors + grounding |
| `src/main/java/org/openwes/ai/plugin/validation/AiDraftValidator.java` | App-type checks + static validation + wes materialization, with stage callbacks |
| `src/main/java/org/openwes/ai/plugin/grounding/PluginGroundingService.java` | Optional Pinecone retrieval/indexing, graceful degradation |
| `src/main/java/org/openwes/ai/plugin/dto/AiDraftRequest.java` | Validated request body |
| `src/main/java/org/openwes/ai/plugin/dto/AiDraftResult.java` | Final result payload |
| `src/main/java/org/openwes/ai/plugin/dto/AiDraftStreamEvent.java` | SSE event payload + factories |
| `src/main/java/org/openwes/ai/plugin/domain/service/AiDraftGenerationService.java` | Orchestration: quota, prompt, streaming, retry, validation, save, audit |
| `src/main/java/org/openwes/ai/plugin/controller/AiDraftController.java` | SSE endpoint + audit/corpus endpoints |

**Modified files**

| File | Change |
|---|---|
| `server/settings.gradle` | `include 'modules-ai:ai-plugin'` |
| `server/server/wes-server/build.gradle` | Depend on `:modules-ai:ai-plugin` |
| `server/server/wes-server/src/main/resources/bootstrap.yml` | `spring.mvc.async.request-timeout: 180000` |
| `server/server/wes-server/src/main/resources/db/changelog/db.changelog-20261022.xml` | New AI tables + seed |
| `server/server/wes-server/src/main/resources/db/changelog/db.changelog-master.xml` | Include the new changelog |
| `server/modules-plugin/script-plugin-api/build.gradle` | `spring-context` for `CronExpression` |
| `server/modules-plugin/script-plugin-api/.../contract/PluginContractCatalog.java` | `typesForApp`, lifecycle entity names, host signatures |
| `server/modules-plugin/script-plugin-api/.../validation/ScriptPluginStaticValidator.java` | **New** reusable stages ①–② |
| `server/modules-plugin/script-plugin-runtime/.../validation/ScriptPluginMaterializationValidatorImpl.java` | Delegate stages ①–② to the static validator |
| `server/modules-plugin/script-plugin-api/src/test/.../PluginContractCatalogTest.java` | Cover app subsets and signatures |
| `docs/standards/backend.md` | Add rule 11: AI draft pipeline |
| `CLAUDE.md` | Add `ai-plugin` to structure and key locations |

---

### Task 1: Extract the reusable static validator

**Files:**
- Create: `server/modules-plugin/script-plugin-api/src/main/java/org/openwes/script/plugin/api/validation/ScriptPluginStaticValidator.java`
- Create: `server/modules-plugin/script-plugin-api/src/test/java/org/openwes/script/plugin/api/validation/ScriptPluginStaticValidatorTest.java`
- Modify: `server/modules-plugin/script-plugin-api/build.gradle`
- Modify: `server/modules-plugin/script-plugin-runtime/src/main/java/org/openwes/script/plugin/runtime/validation/ScriptPluginMaterializationValidatorImpl.java`

**Interfaces:**
- Consumes: `ScriptPluginBundleDTO`, `ExtensionDeclarationDTO`, `ValidationReportDTO.ValidationFailureDTO`, `PluginContractCatalog`, `GroovyScriptEngine`.
- Produces:
  - `new ScriptPluginStaticValidator(GroovyScriptEngine)`
  - `List<ValidationFailureDTO> validate(ScriptPluginBundleDTO bundle)`
  - `List<ValidationFailureDTO> validate(ScriptPluginBundleDTO bundle, Consumer<String> onStage)` — `onStage` receives `"declarations"` then `"compile"`.
  - `ScriptPluginMaterializationValidatorImpl.validate(bundle)` keeps its existing contract but only implements stages ③–⑤.

- [ ] **Step 1: Add the `spring-context` dependency**

In `server/modules-plugin/script-plugin-api/build.gradle`, add under the existing `implementation` lines:

```gradle
    implementation 'org.springframework:spring-context'
```

`CronExpression` comes from `org.springframework.scheduling.support`; the version is managed by the Spring Boot BOM.

- [ ] **Step 2: Write the failing static-validator test**

Create `server/modules-plugin/script-plugin-api/src/test/java/org/openwes/script/plugin/api/validation/ScriptPluginStaticValidatorTest.java`:

```java
package org.openwes.script.plugin.api.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openwes.script.engine.GroovyScriptEngine;
import org.openwes.script.plugin.api.ScriptPluginApiConstants;
import org.openwes.script.plugin.api.dto.ExtensionDeclarationDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginBundleDTO;
import org.openwes.script.plugin.api.dto.ValidationReportDTO;
import org.openwes.script.plugin.api.enums.ExtensionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScriptPluginStaticValidatorTest {

    private ScriptPluginStaticValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ScriptPluginStaticValidator(new GroovyScriptEngine());
    }

    private ExtensionDeclarationDTO waveDeclaration() {
        ExtensionDeclarationDTO declaration = new ExtensionDeclarationDTO();
        declaration.setType(ExtensionType.OUTBOUND_WAVE_WAVE_ACTION);
        declaration.setScript("Wave.groovy");
        declaration.setMethod("wave");
        return declaration;
    }

    private ScriptPluginBundleDTO bundle(Map<String, String> scripts, ExtensionDeclarationDTO declaration) {
        ScriptPluginBundleDTO bundle = new ScriptPluginBundleDTO();
        bundle.setPluginKey("packing-wave");
        bundle.setApplySystem("wes");
        bundle.setSdkVersion(ScriptPluginApiConstants.CONTRACT_VERSION);
        bundle.setRevisionId(11L);
        bundle.setScripts(scripts);
        bundle.setDeclarations(List.of(declaration));
        bundle.setConfig(Map.of());
        bundle.setConfigSchema(List.of());
        return bundle;
    }

    private boolean failedOn(List<ValidationReportDTO.ValidationFailureDTO> failures, String stage) {
        return failures.stream().anyMatch(failure -> failure.getStage().equals(stage));
    }

    @Test
    void validBundle_returnsNoFailures() {
        String source = "class Wave { Object wave(Object orders, Object ctx) { return [] } }";

        List<ValidationReportDTO.ValidationFailureDTO> failures =
                validator.validate(bundle(Map.of("Wave.groovy", source), waveDeclaration()));

        assertTrue(failures.isEmpty(), failures.toString());
    }

    @Test
    void wrongSdkVersion_reportsSdkVersionStage() {
        ScriptPluginBundleDTO bundle = bundle(
                Map.of("Wave.groovy", "class Wave { Object wave(Object o, Object ctx) { return [] } }"),
                waveDeclaration());
        bundle.setSdkVersion("0.9");

        assertTrue(failedOn(validator.validate(bundle), "sdkVersion"));
    }

    @Test
    void declarationWithWrongMethod_reportsDeclarationsStage() {
        ExtensionDeclarationDTO declaration = waveDeclaration();
        declaration.setMethod("split");

        List<ValidationReportDTO.ValidationFailureDTO> failures = validator.validate(bundle(
                Map.of("Wave.groovy", "class Wave { Object split(Object o, Object ctx) { return [] } }"),
                declaration));

        assertTrue(failedOn(failures, "declarations"));
        assertTrue(failures.stream().anyMatch(f -> f.getMessage().contains("requires script method 'wave'")));
    }

    @Test
    void declarationReferencingUnknownScript_reportsDeclarationsStage() {
        List<ValidationReportDTO.ValidationFailureDTO> failures = validator.validate(bundle(
                Map.of("Other.groovy", "class Other { Object wave(Object o, Object ctx) { return [] } }"),
                waveDeclaration()));

        assertTrue(failedOn(failures, "declarations"));
        assertTrue(failures.stream().anyMatch(f -> f.getMessage().contains("unknown script 'Wave.groovy'")));
    }

    @Test
    void duplicateHttpEndpoints_reportsDeclarationsStage() {
        ExtensionDeclarationDTO wildcard = httpEndpoint(null);
        ExtensionDeclarationDTO get = httpEndpoint("GET");
        ScriptPluginBundleDTO bundle = bundle(
                Map.of("Http.groovy", "class Http { Object handle(Object body, Object ctx) { return 'ok' } }"),
                wildcard);
        bundle.setDeclarations(List.of(wildcard, get));

        assertTrue(failedOn(validator.validate(bundle), "declarations"));
    }

    private ExtensionDeclarationDTO httpEndpoint(String httpMethod) {
        ExtensionDeclarationDTO declaration = new ExtensionDeclarationDTO();
        declaration.setType(ExtensionType.HTTP_ENDPOINT);
        declaration.setScript("Http.groovy");
        declaration.setMethod("handle");
        declaration.setHttpMethod(httpMethod);
        declaration.setPath("/x");
        return declaration;
    }

    @Test
    void invalidCron_reportsDeclarationsStage() {
        ExtensionDeclarationDTO declaration = new ExtensionDeclarationDTO();
        declaration.setType(ExtensionType.CRON);
        declaration.setScript("Job.groovy");
        declaration.setMethod("run");
        declaration.setCron("not a cron");

        List<ValidationReportDTO.ValidationFailureDTO> failures = validator.validate(bundle(
                Map.of("Job.groovy", "class Job { Object run(Object ctx) { return 1 } }"), declaration));

        assertTrue(failedOn(failures, "declarations"));
    }

    @Test
    void compileError_reportsCompileStageWithTheScriptName() {
        List<ValidationReportDTO.ValidationFailureDTO> failures = validator.validate(bundle(
                Map.of("Wave.groovy", "class Wave {"), waveDeclaration()));

        assertTrue(failedOn(failures, "compile"));
        assertTrue(failures.stream().anyMatch(f -> f.getMessage().startsWith("Wave.groovy:")));
    }

    @Test
    void compileReportsEveryBrokenScript() {
        ScriptPluginBundleDTO bundle = bundle(Map.of(
                "Wave.groovy", "class Wave {",
                "Helper.groovy", "class Helper {"), waveDeclaration());
        bundle.setDeclarations(List.of());

        List<ValidationReportDTO.ValidationFailureDTO> failures = validator.validate(bundle);

        assertEquals(2, failures.stream().filter(f -> f.getStage().equals("compile")).count());
    }

    @Test
    void stageCallback_reportsDeclarationsThenCompile() {
        List<String> stages = new ArrayList<>();

        validator.validate(bundle(
                Map.of("Wave.groovy", "class Wave { Object wave(Object o, Object ctx) { return [] } }"),
                waveDeclaration()), stages::add);

        assertEquals(List.of("declarations", "compile"), stages);
    }

    @Test
    void missingScripts_reportsDeclarationsWithoutThrowing() {
        ScriptPluginBundleDTO bundle = bundle(Map.of(), waveDeclaration());

        List<ValidationReportDTO.ValidationFailureDTO> failures = validator.validate(bundle);

        assertFalse(failures.isEmpty());
        assertTrue(failedOn(failures, "declarations"));
        assertTrue(failures.stream().anyMatch(f -> f.getMessage().contains("at least one script")));
    }
}
```

- [ ] **Step 3: Run the test to verify it fails to compile**

```bash
cd D:/open-wes/server
export JAVA_HOME="${JAVA_HOME:-C:/Users/niekang/.jdks/ms-17.0.20.1}"
./gradlew :modules-plugin:script-plugin-api:test --tests "org.openwes.script.plugin.api.validation.ScriptPluginStaticValidatorTest"
```

Expected: FAIL — `ScriptPluginStaticValidator` does not exist (compile error).

- [ ] **Step 4: Implement `ScriptPluginStaticValidator`**

Create the file:

```java
package org.openwes.script.plugin.api.validation;

import org.apache.commons.lang3.StringUtils;
import org.openwes.script.engine.GroovyScriptEngine;
import org.openwes.script.engine.ScriptCompileException;
import org.openwes.script.plugin.api.ScriptPluginApiConstants;
import org.openwes.script.plugin.api.contract.PluginContractCatalog;
import org.openwes.script.plugin.api.dto.ExtensionDeclarationDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginBundleDTO;
import org.openwes.script.plugin.api.dto.ValidationReportDTO;
import org.springframework.scheduling.support.CronExpression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;

/**
 * The host-independent half of the enable-time validation: contract version, declaration legality
 * and Groovy compilation. Used by the runtime's materialization validator and by the AI draft
 * pipeline before a revision exists. Stages are reported as "declarations" and "compile".
 */
public class ScriptPluginStaticValidator {

    private static final Consumer<String> NO_STAGES = stage -> { };

    private final GroovyScriptEngine scriptEngine;

    public ScriptPluginStaticValidator(GroovyScriptEngine scriptEngine) {
        this.scriptEngine = scriptEngine;
    }

    public List<ValidationReportDTO.ValidationFailureDTO> validate(ScriptPluginBundleDTO bundle) {
        return validate(bundle, NO_STAGES);
    }

    public List<ValidationReportDTO.ValidationFailureDTO> validate(ScriptPluginBundleDTO bundle,
                                                                   Consumer<String> onStage) {
        List<ValidationReportDTO.ValidationFailureDTO> failures = new ArrayList<>();

        if (!ScriptPluginApiConstants.CONTRACT_VERSION.equals(bundle.getSdkVersion())) {
            failures.add(failure("sdkVersion", "plugin sdk_version " + bundle.getSdkVersion()
                    + " does not equal platform contract " + ScriptPluginApiConstants.CONTRACT_VERSION));
        }

        onStage.accept("declarations");
        Map<String, String> scripts = bundle.getScripts() == null ? Map.of() : bundle.getScripts();
        List<ExtensionDeclarationDTO> declarations = bundle.getDeclarations() == null
                ? List.of() : bundle.getDeclarations();
        if (scripts.isEmpty()) {
            failures.add(failure("declarations", "a revision needs at least one script"));
        }
        validateDeclarations(declarations, scripts, failures);

        if (failures.isEmpty()) {
            onStage.accept("compile");
            compile(scripts, failures);
        }
        return failures;
    }

    private void validateDeclarations(List<ExtensionDeclarationDTO> declarations, Map<String, String> scripts,
                                      List<ValidationReportDTO.ValidationFailureDTO> failures) {
        Map<String, Set<String>> httpEndpointsByPath = new HashMap<>();
        for (ExtensionDeclarationDTO declaration : declarations) {
            if (declaration.getType() == null
                    || PluginContractCatalog.archetype(declaration.getType()).isEmpty()) {
                failures.add(failure("declarations", "unknown extension type " + declaration.getType()));
                continue;
            }
            if (StringUtils.isBlank(declaration.getScript()) || StringUtils.isBlank(declaration.getMethod())) {
                failures.add(failure("declarations", declaration.getType() + " needs both script and method"));
                continue;
            }
            if (!scripts.containsKey(declaration.getScript())) {
                failures.add(failure("declarations", declaration.getType() + " references unknown script '"
                        + declaration.getScript() + "'"));
                continue;
            }
            String requiredMethod = PluginContractCatalog.archetype(declaration.getType())
                    .map(PluginContractCatalog.ArchetypeDescriptor::scriptMethod).orElse(null);
            if (requiredMethod != null && !requiredMethod.equals(declaration.getMethod())) {
                failures.add(failure("declarations", declaration.getType() + " requires script method '"
                        + requiredMethod + "' but declaration uses '" + declaration.getMethod() + "'"));
            }
            switch (declaration.getType()) {
                case REQUEST_VALIDATE, REQUEST_SUPPLY, REQUEST_AFTER_INVOKE, CUSTOM_REQUEST_INVOKE -> {
                    if (StringUtils.isBlank(declaration.getApiType())) {
                        failures.add(failure("declarations", declaration.getType() + " needs apiType"));
                    }
                }
                case LIFECYCLE_LISTENER -> {
                    if (StringUtils.isBlank(declaration.getEntityName())) {
                        failures.add(failure("declarations", "LIFECYCLE_LISTENER needs entityName"));
                    }
                }
                case CUSTOM_API -> {
                    if (StringUtils.isBlank(declaration.getCustomApiCode())) {
                        failures.add(failure("declarations", "CUSTOM_API needs customApiCode"));
                    }
                }
                case HTTP_ENDPOINT -> validateHttpEndpoint(declaration, httpEndpointsByPath, failures);
                case CRON -> {
                    if (StringUtils.isBlank(declaration.getCron())) {
                        failures.add(failure("declarations", "CRON needs a cron expression"));
                    } else {
                        try {
                            CronExpression.parse(declaration.getCron());
                        } catch (IllegalArgumentException e) {
                            failures.add(failure("declarations", "invalid cron '" + declaration.getCron()
                                    + "': " + e.getMessage()));
                        }
                    }
                }
                default -> {
                }
            }
        }
    }

    private void validateHttpEndpoint(ExtensionDeclarationDTO declaration,
                                      Map<String, Set<String>> endpointsByPath,
                                      List<ValidationReportDTO.ValidationFailureDTO> failures) {
        if (StringUtils.isBlank(declaration.getPath()) || !declaration.getPath().startsWith("/")
                || declaration.getPath().endsWith("/")) {
            failures.add(failure("declarations", "HTTP_ENDPOINT path must start with '/' and not end with '/'"));
        } else if (isDuplicateHttpEndpoint(endpointsByPath, declaration)) {
            failures.add(failure("declarations", "duplicate HTTP endpoint "
                    + normalizeHttpMethod(declaration.getHttpMethod()) + " " + declaration.getPath()));
        }
    }

    /**
     * Mirrors {@code ScriptPluginRegistry#matchesHttpMethod}: a blank method is a wildcard and
     * method comparison is case-insensitive, so "*" overlaps every method declared on the path.
     */
    private boolean isDuplicateHttpEndpoint(Map<String, Set<String>> endpointsByPath,
                                            ExtensionDeclarationDTO declaration) {
        String method = normalizeHttpMethod(declaration.getHttpMethod());
        Set<String> methods = endpointsByPath.computeIfAbsent(declaration.getPath(), path -> new HashSet<>());
        boolean duplicate = methods.contains("*") || (method.equals("*") && !methods.isEmpty())
                || methods.contains(method);
        methods.add(method);
        return duplicate;
    }

    private String normalizeHttpMethod(String httpMethod) {
        return StringUtils.defaultIfBlank(httpMethod, "*").toUpperCase(Locale.ROOT);
    }

    private void compile(Map<String, String> scripts,
                         List<ValidationReportDTO.ValidationFailureDTO> failures) {
        // Sorted for deterministic failure order across JVM runs.
        for (Map.Entry<String, String> script : new TreeMap<>(scripts).entrySet()) {
            try {
                scriptEngine.compile(script.getValue());
            } catch (ScriptCompileException e) {
                failures.add(failure("compile", script.getKey() + ": " + e.getMessage()));
            }
        }
    }

    private ValidationReportDTO.ValidationFailureDTO failure(String stage, String message) {
        return new ValidationReportDTO.ValidationFailureDTO(stage, message);
    }
}
```

- [ ] **Step 5: Run the new test**

```bash
cd D:/open-wes/server
export JAVA_HOME="${JAVA_HOME:-C:/Users/niekang/.jdks/ms-17.0.20.1}"
./gradlew :modules-plugin:script-plugin-api:test --tests "org.openwes.script.plugin.api.validation.ScriptPluginStaticValidatorTest"
```

Expected: PASS (10 tests).

- [ ] **Step 6: Refactor the runtime validator to delegate stages ①–②**

In `ScriptPluginMaterializationValidatorImpl`:

1. Add imports:
   ```java
   import org.openwes.script.plugin.api.validation.ScriptPluginStaticValidator;
   ```
2. Add the field and initialize it in the constructor:
   ```java
   private final ScriptPluginStaticValidator staticValidator;
   ```
   ```java
   this.staticValidator = new ScriptPluginStaticValidator(scriptEngine);
   ```
3. Replace the body of `validate` with:
   ```java
   @Override
   public ValidationReportDTO validate(ScriptPluginBundleDTO bundle) {
       List<ValidationReportDTO.ValidationFailureDTO> failures = staticValidator.validate(bundle);
       if (failures.isEmpty()) {
           dryRun(bundle, failures);
       }
       return new ValidationReportDTO(bundle.getPluginKey(), bundle.getRevisionId(), targetApp,
               failures.isEmpty(), failures, System.currentTimeMillis());
   }
   ```
4. Delete the now-unused private methods `validateDeclarations`, `isDuplicateHttpEndpoint` and `normalizeHttpMethod`, plus imports that become unused (`StringUtils`, `PluginContractCatalog`, `CronExpression`, `Locale`, `HashMap`, `HashSet`, `Map`, `Set`). Keep `ArrayList`, `List`, and `StringUtils` only if still referenced; the final `dryRun` uses `List` and `ArrayList`.
5. Replace `dryRun` with the null-safe version (the loop body is unchanged):
   ```java
   private void dryRun(ScriptPluginBundleDTO bundle, List<ValidationReportDTO.ValidationFailureDTO> failures) {
       List<ExtensionDeclarationDTO> declarations = bundle.getDeclarations() == null
               ? List.of() : bundle.getDeclarations();
       try (ScriptRevisionContextManager.ValidationScope scope = revisionContextManager
               .openValidationScope(bundle.getPluginKey(), bundle.getScripts())) {
           for (ExtensionDeclarationDTO declaration : declarations) {
               Object instance = scope.instances().get(declaration.getScript());
               if (instance == null || !scriptEngine.respondsTo(instance, declaration.getMethod())) {
                   failures.add(failure("smoke", declaration.getScript() + " does not declare method '"
                           + declaration.getMethod() + "'"));
               }
           }
       } catch (ScriptCompileException e) {
           failures.add(failure("compile", e.getMessage()));
       } catch (Exception e) {
           String message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
           failures.add(failure("context", message));
       }
   }
   ```

- [ ] **Step 7: Run the runtime validator tests to prove the refactor is behavior-preserving**

```bash
cd D:/open-wes/server
export JAVA_HOME="${JAVA_HOME:-C:/Users/niekang/.jdks/ms-17.0.20.1}"
./gradlew :modules-plugin:script-plugin-runtime:test --tests "org.openwes.script.plugin.runtime.validation.ScriptPluginMaterializationValidatorImplTest"
```

Expected: PASS (all existing tests, including compile/context/smoke, duplicate-HTTP and cron cases).

- [ ] **Step 8: Commit**

```bash
git add server/modules-plugin/script-plugin-api server/modules-plugin/script-plugin-runtime
git commit -m "refactor(script-plugin-api): extract static revision validation

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 2: Create the `ai-plugin` module, AI tables and settings service

**Files:**
- Create: `server/modules-ai/ai-plugin/build.gradle`
- Create: `server/modules-ai/ai-plugin/src/main/java/org/openwes/ai/plugin/config/AiPluginProperties.java`
- Create: `server/modules-ai/ai-plugin/src/main/java/org/openwes/ai/plugin/domain/model/AiSettings.java`
- Create: `server/modules-ai/ai-plugin/src/main/java/org/openwes/ai/plugin/domain/entity/ScriptPluginAiSetting.java`
- Create: `server/modules-ai/ai-plugin/src/main/java/org/openwes/ai/plugin/domain/repository/ScriptPluginAiSettingRepository.java`
- Create: `server/modules-ai/ai-plugin/src/main/java/org/openwes/ai/plugin/domain/service/AiSettingsService.java`
- Create: `server/modules-ai/ai-plugin/src/test/java/org/openwes/ai/plugin/domain/service/AiSettingsServiceTest.java`
- Create: `server/server/wes-server/src/main/resources/db/changelog/db.changelog-20261022.xml`
- Modify: `server/settings.gradle`
- Modify: `server/server/wes-server/src/main/resources/db/changelog/db.changelog-master.xml`

**Interfaces:**
- Consumes: `ScriptPluginService` later, `RedisUtils`, `UserContext`; here only JPA and Spring config.
- Produces:
  - `AiPluginProperties` with `getModel()`, `getMaxTokensCeiling()`, `getRequestTimeoutMs()`, `isGroundingEnabled()`, `getGroundingTopK()`.
  - `AiSettings(int dailyGenerationLimit, int maxTokensPerCall, int maxAttempts)` and `AiSettings.defaults()`.
  - `AiSettingsService.current()`.
  - Tables `p_script_plugin_ai_setting` (seeded singleton) and `p_script_plugin_ai_generation`.

- [ ] **Step 1: Register the module and write its build file**

In `server/settings.gradle`, after `include 'modules-ai:ai-api'`, add:

```gradle
include 'modules-ai:ai-plugin'
```

Create `server/modules-ai/ai-plugin/build.gradle`:

```gradle
dependencies {
    implementation project(path: ":modules-plugin:script-plugin-api", configuration: 'default')
    implementation project(path: ":modules-plugin:script-plugin-core", configuration: 'default')
    implementation project(path: ":modules-utils:script-engine", configuration: 'default')
    implementation project(path: ":modules-utils:common-utils", configuration: 'default')

    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'

    implementation 'org.springframework.ai:spring-ai-core'
    implementation 'org.springframework.ai:spring-ai-zhipuai'
    implementation 'org.springframework.ai:spring-ai-zhipuai-spring-boot-starter'

    implementation 'com.fasterxml.jackson.core:jackson-databind'
    implementation 'org.apache.commons:commons-lang3'
    implementation 'io.swagger.core.v3:swagger-annotations'

    // Test-only: the in-process wes materialization validator for the end-to-end test.
    testImplementation project(path: ":modules-plugin:script-plugin-runtime", configuration: 'default')
    testImplementation 'io.projectreactor:reactor-test'
}
```

Why no `spring-ai-pinecone-store-spring-boot-starter`: `ai-core` already puts the Pinecone `VectorStore` on `wes-server`'s classpath; `ai-plugin` compiles against Spring AI core only and injects the store lazily through `ObjectProvider`.

- [ ] **Step 2: Write the failing settings test**

Create `ScriptPluginAiSettingRepository` first (the test needs the type), then the test.

`server/modules-ai/ai-plugin/src/main/java/org/openwes/ai/plugin/domain/repository/ScriptPluginAiSettingRepository.java`:

```java
package org.openwes.ai.plugin.domain.repository;

import org.openwes.ai.plugin.domain.entity.ScriptPluginAiSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScriptPluginAiSettingRepository extends JpaRepository<ScriptPluginAiSetting, Long> {

    Optional<ScriptPluginAiSetting> findBySingletonKey(String singletonKey);
}
```

Create `server/modules-ai/ai-plugin/src/test/java/org/openwes/ai/plugin/domain/service/AiSettingsServiceTest.java`:

```java
package org.openwes.ai.plugin.domain.service;

import org.junit.jupiter.api.Test;
import org.openwes.ai.plugin.domain.entity.ScriptPluginAiSetting;
import org.openwes.ai.plugin.domain.model.AiSettings;
import org.openwes.ai.plugin.domain.repository.ScriptPluginAiSettingRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AiSettingsServiceTest {

    private final ScriptPluginAiSettingRepository repository = mock(ScriptPluginAiSettingRepository.class);
    private final AiSettingsService service = new AiSettingsService(repository);

    @Test
    void missingRow_returnsSafeDefaults() {
        when(repository.findBySingletonKey(ScriptPluginAiSetting.DEFAULT_KEY)).thenReturn(Optional.empty());

        assertEquals(AiSettings.defaults(), service.current());
    }

    @Test
    void storedValues_areClampedToThePolicy() {
        ScriptPluginAiSetting row = new ScriptPluginAiSetting();
        row.setDailyGenerationLimit(-3);
        row.setMaxTokensPerCall(0);
        row.setMaxAttempts(99);
        when(repository.findBySingletonKey(ScriptPluginAiSetting.DEFAULT_KEY)).thenReturn(Optional.of(row));

        assertEquals(new AiSettings(0, 4096, 3), service.current());
    }

    @Test
    void storedValues_withinPolicy_areReturnedVerbatim() {
        ScriptPluginAiSetting row = new ScriptPluginAiSetting();
        row.setDailyGenerationLimit(7);
        row.setMaxTokensPerCall(2048);
        row.setMaxAttempts(2);
        when(repository.findBySingletonKey(ScriptPluginAiSetting.DEFAULT_KEY)).thenReturn(Optional.of(row));

        assertEquals(new AiSettings(7, 2048, 2), service.current());
    }
}
```

- [ ] **Step 3: Run the test to verify it fails to compile**

```bash
cd D:/open-wes/server
export JAVA_HOME="${JAVA_HOME:-C:/Users/niekang/.jdks/ms-17.0.20.1}"
./gradlew :modules-ai:ai-plugin:test --tests "org.openwes.ai.plugin.domain.service.AiSettingsServiceTest"
```

Expected: FAIL — `AiSettings`, `ScriptPluginAiSetting`, `AiSettingsService` do not exist.

- [ ] **Step 4: Implement the properties, settings model, entity and service**

`AiPluginProperties.java`:

```java
package org.openwes.ai.plugin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai-plugin")
public class AiPluginProperties {

    /** Model id recorded on every AI revision; also sent as the per-call model option. */
    private String model = "GLM-4-air";

    /** Hard ceiling applied on top of the DB max_tokens_per_call value. */
    private int maxTokensCeiling = 8192;

    /** Wall-clock limit for one model call, including all streamed chunks. */
    private long requestTimeoutMs = 120_000;

    /**
     * Overall budget for one generation. Keep it below spring.mvc.async.request-timeout so a retry
     * never starts that cannot finish before MVC closes the SSE stream.
     */
    private long streamBudgetMs = 170_000;

    /** Opt-in: requires a configured Pinecone VectorStore. */
    private boolean groundingEnabled = false;

    private int groundingTopK = 3;
}
```

`AiSettings.java`:

```java
package org.openwes.ai.plugin.domain.model;

/**
 * Effective generation policy for one request. Values from the DB are clamped here so no caller
 * can exceed the product guardrails: at most 3 attempts and a positive token cap.
 */
public record AiSettings(int dailyGenerationLimit, int maxTokensPerCall, int maxAttempts) {

    public static final int DEFAULT_MAX_TOKENS = 4096;
    public static final int MAX_ATTEMPTS_CAP = 3;

    public AiSettings {
        dailyGenerationLimit = Math.max(0, dailyGenerationLimit);
        maxAttempts = Math.min(MAX_ATTEMPTS_CAP, Math.max(1, maxAttempts));
        maxTokensPerCall = maxTokensPerCall > 0 ? maxTokensPerCall : DEFAULT_MAX_TOKENS;
    }

    public static AiSettings defaults() {
        return new AiSettings(20, DEFAULT_MAX_TOKENS, MAX_ATTEMPTS_CAP);
    }
}
```

`ScriptPluginAiSetting.java`:

```java
package org.openwes.ai.plugin.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.common.utils.id.IdGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "p_script_plugin_ai_setting", indexes = {
        @Index(unique = true, name = "uk_ai_setting_singleton", columnList = "singletonKey")})
@DynamicUpdate
public class ScriptPluginAiSetting extends UpdateUserPO {

    public static final String DEFAULT_KEY = "DEFAULT";

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", type = IdGenerator.class)
    private Long id;

    @Column(nullable = false, length = 32, unique = true)
    @Comment("Singleton key; always DEFAULT")
    private String singletonKey = DEFAULT_KEY;

    @Column(nullable = false)
    @Comment("Per-user generations per day; 0 disables the limit")
    private Integer dailyGenerationLimit = 20;

    @Column(nullable = false)
    @Comment("Output token cap per model call")
    private Integer maxTokensPerCall = 4096;

    @Column(nullable = false)
    @Comment("Retry cap; values above 3 are clamped to 3")
    private Integer maxAttempts = 3;

    @Version
    private Long version;
}
```

`AiSettingsService.java`:

```java
package org.openwes.ai.plugin.domain.service;

import lombok.RequiredArgsConstructor;
import org.openwes.ai.plugin.domain.entity.ScriptPluginAiSetting;
import org.openwes.ai.plugin.domain.model.AiSettings;
import org.openwes.ai.plugin.domain.repository.ScriptPluginAiSettingRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiSettingsService {

    private final ScriptPluginAiSettingRepository repository;

    /**
     * The Liquibase seed guarantees the row exists. If an operator deleted it, the safe product
     * defaults apply; the service deliberately does not recreate rows on the request path.
     */
    public AiSettings current() {
        return repository.findBySingletonKey(ScriptPluginAiSetting.DEFAULT_KEY)
                .map(row -> new AiSettings(row.getDailyGenerationLimit(), row.getMaxTokensPerCall(),
                        row.getMaxAttempts()))
                .orElseGet(AiSettings::defaults);
    }
}
```

- [ ] **Step 5: Add the Liquibase changelog and register it**

Create `server/server/wes-server/src/main/resources/db/changelog/db.changelog-20261022.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.20.xsd">

    <changeSet id="create_p_script_plugin_ai_setting" author="dev">
        <preConditions onFail="MARK_RAN">
            <not>
                <tableExists tableName="p_script_plugin_ai_setting"/>
            </not>
        </preConditions>
        <sql>
            CREATE TABLE p_script_plugin_ai_setting (
                id                     BIGINT      NOT NULL,
                singleton_key          VARCHAR(32) NOT NULL COMMENT 'Singleton key; always DEFAULT',
                daily_generation_limit INT         NOT NULL DEFAULT 20 COMMENT 'Per-user generations per day; 0 disables the limit',
                max_tokens_per_call    INT         NOT NULL DEFAULT 4096 COMMENT 'Output token cap per model call',
                max_attempts           INT         NOT NULL DEFAULT 3 COMMENT 'Retry cap; values above 3 are clamped to 3',
                version                BIGINT      NULL,
                create_time            BIGINT      NOT NULL DEFAULT 0,
                create_user            VARCHAR(60) NOT NULL DEFAULT '',
                update_time            BIGINT      NOT NULL DEFAULT 0,
                update_user            VARCHAR(60) NOT NULL DEFAULT '',
                PRIMARY KEY (id),
                UNIQUE KEY uk_ai_setting_singleton (singleton_key)
            ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT 'Script plugin AI settings (singleton)';
        </sql>
        <rollback>
            <sql>DROP TABLE p_script_plugin_ai_setting;</sql>
        </rollback>
    </changeSet>

    <changeSet id="create_p_script_plugin_ai_generation" author="dev">
        <preConditions onFail="MARK_RAN">
            <not>
                <tableExists tableName="p_script_plugin_ai_generation"/>
            </not>
        </preConditions>
        <sql>
            CREATE TABLE p_script_plugin_ai_generation (
                id                BIGINT        NOT NULL,
                plugin_key        VARCHAR(64)   NOT NULL,
                user_name         VARCHAR(64)   NOT NULL,
                model             VARCHAR(64)   NULL,
                prompt_version    VARCHAR(32)   NULL,
                status            VARCHAR(20)   NOT NULL COMMENT 'RUNNING/SUCCEEDED/FAILED/CANCELLED',
                attempts          INT           NOT NULL DEFAULT 0,
                prompt_tokens     INT           NULL,
                completion_tokens INT           NULL,
                duration_ms       BIGINT        NULL,
                error_message     VARCHAR(1024) NULL,
                created_at        BIGINT        NOT NULL DEFAULT 0,
                finished_at       BIGINT        NULL,
                PRIMARY KEY (id),
                KEY idx_ai_generation_plugin (plugin_key, id),
                KEY idx_ai_generation_user (user_name, created_at)
            ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT 'Script plugin AI generation audit';
        </sql>
        <rollback>
            <sql>DROP TABLE p_script_plugin_ai_generation;</sql>
        </rollback>
    </changeSet>

    <changeSet id="seed_p_script_plugin_ai_setting" author="dev">
        <preConditions onFail="MARK_RAN">
            <tableExists tableName="p_script_plugin_ai_setting"/>
            <sqlCheck expectedResult="0">SELECT COUNT(*) FROM p_script_plugin_ai_setting WHERE singleton_key = 'DEFAULT'</sqlCheck>
        </preConditions>
        <sql>
            INSERT INTO p_script_plugin_ai_setting
                (id, singleton_key, daily_generation_limit, max_tokens_per_call, max_attempts, version,
                 create_time, create_user, update_time, update_user)
            VALUES (1, 'DEFAULT', 20, 4096, 3, 0,
                    UNIX_TIMESTAMP() * 1000, 'system', UNIX_TIMESTAMP() * 1000, 'system');
        </sql>
        <rollback>
            <sql>DELETE FROM p_script_plugin_ai_setting WHERE singleton_key = 'DEFAULT';</sql>
        </rollback>
    </changeSet>

</databaseChangeLog>
```

In `db.changelog-master.xml`, add after the `20261008` include:

```xml
    <include file="/db/changelog/db.changelog-20261022.xml"/>
```

- [ ] **Step 6: Validate the XML is well-formed without Docker**

```bash
mkdir -p /tmp/wes-xml-check
cat > /tmp/wes-xml-check/ValidateXml.java <<'EOF'
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class ValidateXml {
    public static void main(String[] args) throws Exception {
        for (String path : args) {
            DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(path));
            System.out.println("OK " + path);
        }
    }
}
EOF
export JAVA_HOME="${JAVA_HOME:-C:/Users/niekang/.jdks/ms-17.0.20.1}"
"$JAVA_HOME/bin/java" /tmp/wes-xml-check/ValidateXml.java \
  server/server/wes-server/src/main/resources/db/changelog/db.changelog-20261022.xml \
  server/server/wes-server/src/main/resources/db/changelog/db.changelog-master.xml
```

Run from `D:/open-wes`. Expected: two `OK` lines.

- [ ] **Step 7: Run the settings test**

```bash
cd D:/open-wes/server
export JAVA_HOME="${JAVA_HOME:-C:/Users/niekang/.jdks/ms-17.0.20.1}"
./gradlew :modules-ai:ai-plugin:test --tests "org.openwes.ai.plugin.domain.service.AiSettingsServiceTest"
```

Expected: PASS (3 tests).

- [ ] **Step 8: Commit**

```bash
git add server/settings.gradle server/modules-ai/ai-plugin \
  server/server/wes-server/src/main/resources/db/changelog
git commit -m "feat(ai-plugin): add the AI module, tables and generation settings

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 3: Generation audit, Redis quota and error vocabulary

**Files:**
- Create: `server/modules-ai/ai-plugin/src/main/java/org/openwes/ai/plugin/domain/enums/AiGenerationStatusEnum.java`
- Create: `server/modules-ai/ai-plugin/src/main/java/org/openwes/ai/plugin/domain/entity/ScriptPluginAiGeneration.java`
- Create: `server/modules-ai/ai-plugin/src/main/java/org/openwes/ai/plugin/domain/repository/ScriptPluginAiGenerationRepository.java`
- Create: `server/modules-ai/ai-plugin/src/main/java/org/openwes/ai/plugin/domain/service/AiGenerationAuditService.java`
- Create: `server/modules-ai/ai-plugin/src/main/java/org/openwes/ai/plugin/domain/service/AiQuotaService.java`
- Create: `server/modules-ai/ai-plugin/src/main/java/org/openwes/ai/plugin/dto/AiGenerationDTO.java`
- Create: `server/modules-ai/ai-plugin/src/main/java/org/openwes/ai/plugin/exception/AiPluginErrorDescEnum.java`
- Create: `server/modules-ai/ai-plugin/src/test/java/org/openwes/ai/plugin/domain/service/AiQuotaServiceTest.java`
- Create: `server/modules-ai/ai-plugin/src/test/java/org/openwes/ai/plugin/domain/service/AiGenerationAuditServiceTest.java`

**Interfaces:**
- Consumes: `RedisUtils.getAndIncrement(String, int)`, `UserContext.getCurrentUser()` (used later), `AiGenerationDTO`.
- Produces:
  - `AiPluginErrorDescEnum` values: `AI_QUOTA_EXCEEDED("PL001001")`, `AI_PLUGIN_NOT_GENERATABLE("PL001002")`, `AI_BASE_REVISION_NOT_FOUND("PL001003")`, `AI_UNSUPPORTED_EXTENSION_TYPE("PL001004")`, `AI_CORPUS_UNAVAILABLE("PL001005")`, `AI_GENERATION_FAILED("PL001006")`, `AI_INTERNAL_ERROR("PL001999")`.
  - `AiQuotaService.tryAcquire(String user, int dailyLimit)`.
  - `AiGenerationAuditService.start(pluginKey, user, model, promptVersion)`, `.recordAttempt(id, attempts)`, `.succeed(id, attempts, promptTokens, completionTokens, durationMs)`, `.fail(id, attempts, error, durationMs)`, `.cancel(id, attempts, durationMs)`, `.recent(pluginKey)`.

- [ ] **Step 1: Write the failing quota test**

Create `AiQuotaServiceTest.java`:

```java
package org.openwes.ai.plugin.domain.service;

import org.junit.jupiter.api.Test;
import org.openwes.common.utils.utils.RedisUtils;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiQuotaServiceTest {

    private final RedisUtils redisUtils = mock(RedisUtils.class);
    private final AiQuotaService quotaService = new AiQuotaService(redisUtils);

    @Test
    void zeroLimit_disablesTheQuotaWithoutTouchingRedis() {
        assertTrue(quotaService.tryAcquire("alice", 0));

        verify(redisUtils, never()).getAndIncrement(anyString(), eq(1));
    }

    @Test
    void allowsCallsBelowTheDailyLimit_andDeniesTheNextOne() {
        when(redisUtils.getAndIncrement(anyString(), eq(1))).thenReturn(0L, 1L, 2L);

        assertTrue(quotaService.tryAcquire("alice", 2));
        assertTrue(quotaService.tryAcquire("alice", 2));
        assertFalse(quotaService.tryAcquire("alice", 2));
    }

    @Test
    void key_isScopedToUserAndCalendarDay() {
        when(redisUtils.getAndIncrement(anyString(), eq(1))).thenReturn(0L);

        quotaService.tryAcquire("alice", 5);

        verify(redisUtils).getAndIncrement(
                org.mockito.ArgumentMatchers.argThat(key ->
                        key.contains("alice") && key.contains(LocalDate.now().toString())), eq(1));
    }
}
```

- [ ] **Step 2: Write the failing audit test**

Create `AiGenerationAuditServiceTest.java`:

```java
package org.openwes.ai.plugin.domain.service;

import org.junit.jupiter.api.Test;
import org.openwes.ai.plugin.domain.entity.ScriptPluginAiGeneration;
import org.openwes.ai.plugin.domain.enums.AiGenerationStatusEnum;
import org.openwes.ai.plugin.domain.repository.ScriptPluginAiGenerationRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiGenerationAuditServiceTest {

    private final ScriptPluginAiGenerationRepository repository =
            mock(ScriptPluginAiGenerationRepository.class);
    private final AiGenerationAuditService service = new AiGenerationAuditService(repository);

    @Test
    void start_createsARunningRow() {
        when(repository.save(any(ScriptPluginAiGeneration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ScriptPluginAiGeneration row = service.start("packing-wave", "alice", "GLM-4-air", "v1");

        assertEquals(AiGenerationStatusEnum.RUNNING, row.getStatus());
        assertEquals("packing-wave", row.getPluginKey());
        assertEquals("alice", row.getUserName());
        assertEquals(0, row.getAttempts());
        assertTrue(row.getCreatedAt() > 0);
    }

    @Test
    void succeed_completesTheRowWithUsage() {
        ScriptPluginAiGeneration row = ScriptPluginAiGeneration.start(
                "packing-wave", "alice", "GLM-4-air", "v1");
        row.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(row));
        when(repository.save(row)).thenReturn(row);

        service.succeed(1L, 2, 120, 80, 1500L);

        assertEquals(AiGenerationStatusEnum.SUCCEEDED, row.getStatus());
        assertEquals(2, row.getAttempts());
        assertEquals(120, row.getPromptTokens());
        assertEquals(80, row.getCompletionTokens());
        assertEquals(1500L, row.getDurationMs());
        assertTrue(row.getFinishedAt() > 0);
        verify(repository).save(row);
    }

    @Test
    void fail_truncatesTheErrorMessage() {
        ScriptPluginAiGeneration row = ScriptPluginAiGeneration.start(
                "packing-wave", "alice", "GLM-4-air", "v1");
        row.setId(2L);
        when(repository.findById(2L)).thenReturn(Optional.of(row));
        when(repository.save(row)).thenReturn(row);

        service.fail(2L, 3, "x".repeat(2000), 900L);

        assertEquals(AiGenerationStatusEnum.FAILED, row.getStatus());
        assertEquals(1024, row.getErrorMessage().length());
    }

    @Test
    void recent_mapsRowsInRepositoryOrder() {
        ScriptPluginAiGeneration row = ScriptPluginAiGeneration.start(
                "packing-wave", "alice", "GLM-4-air", "v1");
        row.setId(3L);
        when(repository.findTop50ByPluginKeyOrderByIdDesc("packing-wave")).thenReturn(List.of(row));

        var result = service.recent("packing-wave");

        assertEquals(1, result.size());
        assertEquals(3L, result.get(0).getId());
        assertEquals("RUNNING", result.get(0).getStatus());
    }
}
```

- [ ] **Step 3: Run both tests to verify they fail to compile**

```bash
cd D:/open-wes/server
export JAVA_HOME="${JAVA_HOME:-C:/Users/niekang/.jdks/ms-17.0.20.1}"
./gradlew :modules-ai:ai-plugin:test \
  --tests "org.openwes.ai.plugin.domain.service.AiQuotaServiceTest" \
  --tests "org.openwes.ai.plugin.domain.service.AiGenerationAuditServiceTest"
```

Expected: FAIL — the service/entity/enum types do not exist.

- [ ] **Step 4: Implement the status enum and audit entity**

`AiGenerationStatusEnum.java`:

```java
package org.openwes.ai.plugin.domain.enums;

public enum AiGenerationStatusEnum {
    RUNNING,
    SUCCEEDED,
    FAILED,
    CANCELLED
}
```

`ScriptPluginAiGeneration.java`:

```java
package org.openwes.ai.plugin.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.GenericGenerator;
import org.openwes.ai.plugin.domain.enums.AiGenerationStatusEnum;
import org.openwes.common.utils.id.IdGenerator;

@Data
@Entity
@Table(name = "p_script_plugin_ai_generation", indexes = {
        @Index(name = "idx_ai_generation_plugin", columnList = "pluginKey,id"),
        @Index(name = "idx_ai_generation_user", columnList = "userName,createdAt")})
public class ScriptPluginAiGeneration {

    private static final int MAX_ERROR_LENGTH = 1024;

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", type = IdGenerator.class)
    private Long id;

    @Column(nullable = false, length = 64)
    private String pluginKey;

    @Column(nullable = false, length = 64)
    private String userName;

    @Column(length = 64)
    private String model;

    @Column(length = 32)
    private String promptVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Comment("RUNNING/SUCCEEDED/FAILED/CANCELLED")
    private AiGenerationStatusEnum status;

    @Column(nullable = false)
    private int attempts;

    private Integer promptTokens;

    private Integer completionTokens;

    private Long durationMs;

    @Column(length = MAX_ERROR_LENGTH)
    private String errorMessage;

    @Column(nullable = false)
    private Long createdAt;

    private Long finishedAt;

    public static ScriptPluginAiGeneration start(String pluginKey, String userName,
                                                 String model, String promptVersion) {
        ScriptPluginAiGeneration row = new ScriptPluginAiGeneration();
        row.pluginKey = pluginKey;
        row.userName = userName;
        row.model = model;
        row.promptVersion = promptVersion;
        row.status = AiGenerationStatusEnum.RUNNING;
        row.attempts = 0;
        row.createdAt = System.currentTimeMillis();
        return row;
    }

    public void recordAttempt(int attempts) {
        if (status != AiGenerationStatusEnum.RUNNING) {
            throw new IllegalStateException("generation is already finished");
        }
        this.attempts = attempts;
    }

    public void succeed(int attempts, Integer promptTokens, Integer completionTokens, long durationMs) {
        finish(AiGenerationStatusEnum.SUCCEEDED, attempts, null, durationMs);
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
    }

    public void fail(int attempts, String errorMessage, long durationMs) {
        finish(AiGenerationStatusEnum.FAILED, attempts, errorMessage, durationMs);
    }

    public void cancel(int attempts, long durationMs) {
        finish(AiGenerationStatusEnum.CANCELLED, attempts, null, durationMs);
    }

    private void finish(AiGenerationStatusEnum finalStatus, int attempts, String error, long durationMs) {
        if (status != AiGenerationStatusEnum.RUNNING) {
            throw new IllegalStateException("generation is already finished");
        }
        this.status = finalStatus;
        this.attempts = attempts;
        this.durationMs = durationMs;
        this.errorMessage = truncate(error);
        this.finishedAt = System.currentTimeMillis();
    }

    private String truncate(String error) {
        if (error == null) {
            return null;
        }
        return error.length() <= MAX_ERROR_LENGTH ? error : error.substring(0, MAX_ERROR_LENGTH);
    }
}
```

`ScriptPluginAiGenerationRepository.java`:

```java
package org.openwes.ai.plugin.domain.repository;

import org.openwes.ai.plugin.domain.entity.ScriptPluginAiGeneration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScriptPluginAiGenerationRepository extends JpaRepository<ScriptPluginAiGeneration, Long> {

    List<ScriptPluginAiGeneration> findTop50ByPluginKeyOrderByIdDesc(String pluginKey);
}
```

- [ ] **Step 5: Implement the audit DTO and service**

`AiGenerationDTO.java`:

```java
package org.openwes.ai.plugin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiGenerationDTO {
    private Long id;
    private String pluginKey;
    private String userName;
    private String model;
    private String promptVersion;
    private String status;
    private int attempts;
    private Integer promptTokens;
    private Integer completionTokens;
    private Long durationMs;
    private String errorMessage;
    private Long createdAt;
    private Long finishedAt;
}
```

`AiGenerationAuditService.java`:

```java
package org.openwes.ai.plugin.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.ai.plugin.domain.entity.ScriptPluginAiGeneration;
import org.openwes.ai.plugin.domain.repository.ScriptPluginAiGenerationRepository;
import org.openwes.ai.plugin.dto.AiGenerationDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiGenerationAuditService {

    private final ScriptPluginAiGenerationRepository repository;

    public ScriptPluginAiGeneration start(String pluginKey, String userName,
                                          String model, String promptVersion) {
        return repository.save(ScriptPluginAiGeneration.start(pluginKey, userName, model, promptVersion));
    }

    public void recordAttempt(Long id, int attempts) {
        update(id, row -> row.recordAttempt(attempts));
    }

    public void succeed(Long id, int attempts, Integer promptTokens, Integer completionTokens, long durationMs) {
        update(id, row -> row.succeed(attempts, promptTokens, completionTokens, durationMs));
    }

    public void fail(Long id, int attempts, String errorMessage, long durationMs) {
        update(id, row -> row.fail(attempts, errorMessage, durationMs));
    }

    public void cancel(Long id, int attempts, long durationMs) {
        update(id, row -> row.cancel(attempts, durationMs));
    }

    public List<AiGenerationDTO> recent(String pluginKey) {
        return repository.findTop50ByPluginKeyOrderByIdDesc(pluginKey).stream()
                .map(this::toDto).toList();
    }

    /**
     * Audit updates are best-effort: a generation that succeeded must still return its revision if
     * the audit table is temporarily unavailable. Failures are logged, never swallowed silently.
     */
    private void update(Long id, java.util.function.Consumer<ScriptPluginAiGeneration> mutation) {
        if (id == null) {
            return;
        }
        try {
            repository.findById(id).ifPresent(row -> {
                mutation.accept(row);
                repository.save(row);
            });
        } catch (RuntimeException e) {
            log.error("failed to update AI generation audit row {}", id, e);
        }
    }

    private AiGenerationDTO toDto(ScriptPluginAiGeneration row) {
        return AiGenerationDTO.builder()
                .id(row.getId())
                .pluginKey(row.getPluginKey())
                .userName(row.getUserName())
                .model(row.getModel())
                .promptVersion(row.getPromptVersion())
                .status(row.getStatus().name())
                .attempts(row.getAttempts())
                .promptTokens(row.getPromptTokens())
                .completionTokens(row.getCompletionTokens())
                .durationMs(row.getDurationMs())
                .errorMessage(row.getErrorMessage())
                .createdAt(row.getCreatedAt())
                .finishedAt(row.getFinishedAt())
                .build();
    }
}
```

- [ ] **Step 6: Implement the quota service**

`AiQuotaService.java`:

```java
package org.openwes.ai.plugin.domain.service;

import lombok.RequiredArgsConstructor;
import org.openwes.common.utils.utils.RedisUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AiQuotaService {

    static final String KEY_PREFIX = "ai:plugin:draft:quota:";

    private final RedisUtils redisUtils;

    /**
     * Atomic per-user daily counter. {@code getAndIncrement} returns the previous value, so the
     * Nth accepted call sees N-1. A non-positive limit means "unlimited" and is the operator's
     * explicit escape hatch.
     */
    public boolean tryAcquire(String user, int dailyLimit) {
        if (dailyLimit <= 0) {
            return true;
        }
        String key = KEY_PREFIX + user + ":" + LocalDate.now();
        long previous = redisUtils.getAndIncrement(key, 1);
        return previous < dailyLimit;
    }
}
```

- [ ] **Step 7: Implement the error enum**

`AiPluginErrorDescEnum.java`:

```java
package org.openwes.ai.plugin.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openwes.common.utils.constants.AppCodeEnum;
import org.openwes.common.utils.exception.code_enum.IBaseError;

@Getter
@AllArgsConstructor
public enum AiPluginErrorDescEnum implements IBaseError {

    AI_QUOTA_EXCEEDED("PL001001", "daily AI draft limit of {0} reached for user {1}",
            AppCodeEnum.PLUGIN_PLATFORM.name()),
    AI_PLUGIN_NOT_GENERATABLE("PL001002",
            "plugin {0} is {1}; only DRAFT or DISABLED plugins can receive an AI draft",
            AppCodeEnum.PLUGIN_PLATFORM.name()),
    AI_BASE_REVISION_NOT_FOUND("PL001003", "base revision {0} does not belong to plugin {1}",
            AppCodeEnum.PLUGIN_PLATFORM.name()),
    AI_UNSUPPORTED_EXTENSION_TYPE("PL001004", "extension type {0} is not supported for app {1}",
            AppCodeEnum.PLUGIN_PLATFORM.name()),
    AI_CORPUS_UNAVAILABLE("PL001005",
            "the plugin contract corpus needs a VectorStore; configure Pinecone or leave grounding disabled",
            AppCodeEnum.PLUGIN_PLATFORM.name()),
    AI_GENERATION_FAILED("PL001006", "AI draft generation failed after {0} attempts: {1}",
            AppCodeEnum.PLUGIN_PLATFORM.name()),
    AI_INTERNAL_ERROR("PL001999", "AI draft generation failed: {0}",
            AppCodeEnum.PLUGIN_PLATFORM.name());

    private final String code;
    private final String desc;
    private final String appCode;
}
```

- [ ] **Step 8: Run both tests**

```bash
cd D:/open-wes/server
export JAVA_HOME="${JAVA_HOME:-C:/Users/niekang/.jdks/ms-17.0.20.1}"
./gradlew :modules-ai:ai-plugin:test \
  --tests "org.openwes.ai.plugin.domain.service.AiQuotaServiceTest" \
  --tests "org.openwes.ai.plugin.domain.service.AiGenerationAuditServiceTest"
```

Expected: PASS (7 tests total).

- [ ] **Step 9: Commit**

```bash
git add server/modules-ai/ai-plugin
git commit -m "feat(ai-plugin): add generation audit, quota and error vocabulary

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---
### Task 4: Extend the contract catalog and build the prompt assembler

**Files:**
- Modify: `server/modules-plugin/script-plugin-api/src/main/java/org/openwes/script/plugin/api/contract/PluginContractCatalog.java`
- Modify: `server/modules-plugin/script-plugin-api/src/test/java/org/openwes/script/plugin/api/contract/PluginContractCatalogTest.java`
- Create: `server/modules-ai/ai-plugin/src/main/java/org/openwes/ai/plugin/prompt/PluginPromptTemplates.java`
- Create: `server/modules-ai/ai-plugin/src/main/java/org/openwes/ai/plugin/prompt/AiPromptAssembler.java`
- Create: `server/modules-ai/ai-plugin/src/test/java/org/openwes/ai/plugin/prompt/PluginPromptTemplatesTest.java`
- Create: `server/modules-ai/ai-plugin/src/test/java/org/openwes/ai/plugin/prompt/AiPromptAssemblerTest.java`

**Interfaces:**
- Consumes: `AiPluginProperties` (Task 2).
- Produces:
  - `PluginContractCatalog.typesForApp(String app)` → `List<ExtensionType>` (`wes` = every type except `CUSTOM_API`; `station` = `CUSTOM_API`, `HTTP_ENDPOINT`, `CRON`; anything else = empty).
  - `PluginContractCatalog.LIFECYCLE_ENTITY_NAMES` → `OutboundPlanOrder`, `OutboundWave`, `ContainerTask`.
  - `ArchetypeDescriptor(type, extensionPointClassName, scriptMethod, hostSignature, blocking)`, where `hostSignature` is the exact script method including the trailing `Object ctx` parameter.
  - `PluginPromptTemplates.PROMPT_VERSION = "plugin-draft-v1"`, `.systemPrompt(app)`, `.outputExample()`.
  - `AiPromptAssembler.assemble(String requirement, ScriptPluginDTO plugin, ScriptPluginRevisionDTO baseRevision, List<String> groundingSnippets, List<String> previousErrors, int maxTokens)` → `Prompt` with `ZhiPuAiChatOptions` (temperature `0.0`).

- [ ] **Step 1: Extend the catalog test**

Add to `PluginContractCatalogTest`:

```java
    @Test
    void typesForApp_excludesStationOnlyArchetypesFromWes() {
        assertEquals(ExtensionType.values().length - 1,
                PluginContractCatalog.typesForApp("wes").size());
        assertFalse(PluginContractCatalog.typesForApp("wes").contains(ExtensionType.CUSTOM_API));

        assertEquals(java.util.List.of(ExtensionType.CUSTOM_API, ExtensionType.HTTP_ENDPOINT, ExtensionType.CRON),
                PluginContractCatalog.typesForApp("station"));
        assertTrue(PluginContractCatalog.typesForApp("unknown").isEmpty());
    }

    @Test
    void everyArchetype_hasAHostSignatureEndingWithCtx() {
        for (PluginContractCatalog.ArchetypeDescriptor archetype : PluginContractCatalog.archetypes()) {
            assertTrue(archetype.hostSignature().contains("Object ctx"),
                    archetype.type() + " host signature must end with Object ctx");
            assertTrue(archetype.hostSignature().contains(archetype.scriptMethod() + "("),
                    archetype.type() + " host signature must contain its script method");
        }
    }

    @Test
    void lifecycleEntityNames_matchTheImplementations() {
        assertEquals(java.util.List.of("OutboundPlanOrder", "OutboundWave", "ContainerTask"),
                PluginContractCatalog.LIFECYCLE_ENTITY_NAMES);
    }

    @Test
    void promptFragment_includesTheSignatureAndLifecycleNames() {
        String fragment = PluginContractCatalog.toPromptFragment("wes",
                java.util.List.of(ExtensionType.LIFECYCLE_LISTENER));

        assertTrue(fragment.contains("afterStatusChange(Long entityId, String entityOrderNo, String newStatus, Object ctx)"));
        assertTrue(fragment.contains("OutboundPlanOrder"));
        assertTrue(fragment.contains("OutboundWave"));
        assertTrue(fragment.contains("ContainerTask"));
    }
```

- [ ] **Step 2: Run the catalog test to verify it fails to compile**

```bash
cd D:/open-wes/server
export JAVA_HOME="${JAVA_HOME:-C:/Users/niekang/.jdks/ms-17.0.20.1}"
./gradlew :modules-plugin:script-plugin-api:test --tests "org.openwes.script.plugin.api.contract.PluginContractCatalogTest"
```

Expected: FAIL — `typesForApp`, `LIFECYCLE_ENTITY_NAMES` and `hostSignature` do not exist.

- [ ] **Step 3: Update `PluginContractCatalog`**

Replace the `ArchetypeDescriptor` record and `ARCHETYPES` list with:

```java
    /**
     * @param type                   the archetype
     * @param extensionPointClassName host interface the proxy implements, or null for dispatcher-backed archetypes
     * @param scriptMethod           the method name a script must declare
     * @param hostSignature          the exact script method signature; the runtime appends ctx as the last argument
     * @param blocking               whether an exception blocks the host operation
     */
    public record ArchetypeDescriptor(ExtensionType type, String extensionPointClassName, String scriptMethod,
                                      String hostSignature, boolean blocking) {
    }

    public static final List<ArchetypeDescriptor> ARCHETYPES = List.of(
            new ArchetypeDescriptor(ExtensionType.OUTBOUND_WAVE_WAVE_ACTION,
                    "org.openwes.plugin.extension.business.wes.outbound.action.IOutboundWaveWaveAction",
                    "wave", "List<List<OutboundPlanOrderDTO>> wave(List<OutboundPlanOrderDTO> orders, Object ctx)",
                    true),
            new ArchetypeDescriptor(ExtensionType.OUTBOUND_WAVE_SPLIT_ACTION,
                    "org.openwes.plugin.extension.business.wes.outbound.action.IOutboundWaveSplitAction",
                    "split", "List<PickingOrderDTO> split(OutboundWaveDTO wave, Object ctx)", true),
            new ArchetypeDescriptor(ExtensionType.REQUEST_VALIDATE,
                    "org.openwes.plugin.extension.business.api.platform.request.IRequestPlugin",
                    "validate", "void validate(RequestHandleContext context, Object ctx)", true),
            new ArchetypeDescriptor(ExtensionType.REQUEST_SUPPLY,
                    "org.openwes.plugin.extension.business.api.platform.request.IRequestPlugin",
                    "supply", "void supply(RequestHandleContext context, Object ctx)", false),
            new ArchetypeDescriptor(ExtensionType.REQUEST_AFTER_INVOKE,
                    "org.openwes.plugin.extension.business.api.platform.request.IRequestPlugin",
                    "afterInvoke", "void afterInvoke(RequestHandleContext context, Object ctx)", false),
            new ArchetypeDescriptor(ExtensionType.CUSTOM_REQUEST_INVOKE,
                    "org.openwes.plugin.extension.business.api.platform.request.ICustomRequestPlugin",
                    "invoke", "void invoke(RequestHandleContext context, Object ctx)", true),
            new ArchetypeDescriptor(ExtensionType.CUSTOM_API,
                    "org.openwes.plugin.extension.business.station.ICustomApiPlugin",
                    "execute", "void execute(CustomApiParameter parameter, Object ctx)", true),
            new ArchetypeDescriptor(ExtensionType.LIFECYCLE_LISTENER,
                    "org.openwes.plugin.extension.business.IEntityLifecycleListener",
                    "afterStatusChange",
                    "void afterStatusChange(Long entityId, String entityOrderNo, String newStatus, Object ctx)", false),
            new ArchetypeDescriptor(ExtensionType.HTTP_ENDPOINT, null,
                    "handle", "Object handle(Object body, Object ctx)", true),
            new ArchetypeDescriptor(ExtensionType.CRON, null,
                    "run", "Object run(Object ctx)", false));

    /** Entity names that actually receive lifecycle events; declared by the host extension interfaces. */
    public static final List<String> LIFECYCLE_ENTITY_NAMES =
            List.of("OutboundPlanOrder", "OutboundWave", "ContainerTask");
```

Add after `beanCatalog()`:

```java
    /**
     * Archetypes a draft may declare for an app. CUSTOM_API is station-only; wes hosts everything
     * else. Returns empty for an unknown app so callers fail closed.
     */
    public static List<ExtensionType> typesForApp(String app) {
        if (ScriptPluginApiConstants.APP_WES.equals(app)) {
            return List.of(ExtensionType.values()).stream()
                    .filter(type -> type != ExtensionType.CUSTOM_API).toList();
        }
        if (ScriptPluginApiConstants.APP_STATION.equals(app)) {
            return List.of(ExtensionType.CUSTOM_API, ExtensionType.HTTP_ENDPOINT, ExtensionType.CRON);
        }
        return List.of();
    }
```

In `toPromptFragment`, replace the archetype line with:

```java
            fragment.append("- ").append(archetype.type())
                    .append(": script method ").append(archetype.hostSignature())
                    .append(archetype.blocking() ? ", blocking (exception blocks the host operation)"
                            : ", non-blocking (exception is logged and execution continues)")
                    .append('\n');
```

After the archetype loop, add:

```java
        if (types.contains(ExtensionType.LIFECYCLE_LISTENER)) {
            fragment.append("Lifecycle entityName values: ")
                    .append(String.join(", ", LIFECYCLE_ENTITY_NAMES)).append('\n');
        }
```

- [ ] **Step 4: Run the catalog test**

```bash
cd D:/open-wes/server
export JAVA_HOME="${JAVA_HOME:-C:/Users/niekang/.jdks/ms-17.0.20.1}"
./gradlew :modules-plugin:script-plugin-api:test --tests "org.openwes.script.plugin.api.contract.PluginContractCatalogTest"
```

Expected: PASS (8 tests). The existing `archetypeExtensionPointClasses_resolveAndDeclareTheScriptMethod` test still passes because it only inspects `scriptMethod`.

- [ ] **Step 5: Write the failing prompt-template test**

Create `server/modules-ai/ai-plugin/src/test/java/org/openwes/ai/plugin/prompt/PluginPromptTemplatesTest.java`:

```java
package org.openwes.ai.plugin.prompt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PluginPromptTemplatesTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void outputExample_isValidJsonInThePlatformDtoShape() throws Exception {
        JsonNode example = objectMapper.readTree(PluginPromptTemplates.outputExample());

        assertTrue(example.hasNonNull("meta"));
        assertTrue(example.get("extensions").isArray());
        assertEquals("HTTP_ENDPOINT", example.get("extensions").get(0).get("type").asText());
        assertEquals("handle", example.get("extensions").get(0).get("method").asText());
        assertEquals("GET", example.get("extensions").get(0).get("httpMethod").asText());
        assertTrue(example.get("scripts").has("Status.groovy"));
        assertTrue(example.get("scripts").get("Status.groovy").asText().contains("Object handle(Object body, Object ctx)"));
    }

    @Test
    void systemPrompt_carriesTheVersionRulesAndOutputExample() {
        String prompt = PluginPromptTemplates.systemPrompt("wes");

        assertTrue(prompt.contains("plugin-draft-v1"));
        assertTrue(prompt.contains("Return exactly one JSON object"));
        assertTrue(prompt.contains(PluginPromptTemplates.outputExample()));
        assertTrue(prompt.contains("ctx.configString"));
        assertTrue(prompt.contains("never contains credentials"));
        assertTrue(prompt.contains("CUSTOM_API is available on station only"));
        assertTrue(prompt.contains("decide internally"));
        assertTrue(prompt.contains("hard execution timeout"));
        assertTrue(prompt.contains("let the exception propagate"));
    }

    @Test
    void wesExample_showsAWaveScript() {
        assertTrue(PluginPromptTemplates.examples("wes").contains("OUTBOUND_WAVE_WAVE_ACTION"));
        assertFalse(PluginPromptTemplates.examples("station").contains("OUTBOUND_WAVE_WAVE_ACTION"));
        assertTrue(PluginPromptTemplates.examples("station").contains("ctx.bean('ptl')"));
        assertFalse(PluginPromptTemplates.examples("station").contains("ctx.ptl()"));
    }
}
```

- [ ] **Step 6: Implement `PluginPromptTemplates`**

Create the file:

```java
package org.openwes.ai.plugin.prompt;

import org.openwes.script.plugin.api.ScriptPluginApiConstants;

/**
 * Prompt text for AI plugin drafts. Versioned as a whole: changing any rule or example in this
 * class requires bumping PROMPT_VERSION, because every AI revision records the version it used.
 */
public final class PluginPromptTemplates {

    public static final String PROMPT_VERSION = "plugin-draft-v1";

    private PluginPromptTemplates() {
    }

    public static String systemPrompt(String app) {
        return """
                You are a senior warehouse-automation engineer writing Groovy script plugins for Open WES.
                Produce one revision of the plugin described by the user's requirement.

                Hard rules:
                1. Return exactly one JSON object. No markdown fences, no prose before or after it.
                2. The JSON shape is:
                   {"meta":{"name":string,"changeSummary":string},
                    "extensions":[ExtensionDeclaration],
                    "configSchema":[ConfigField],
                    "scripts":{fileName:groovySource}}
                   ExtensionDeclaration fields: type, priority, script, method, apiType, entityName,
                   customApiCode, httpMethod, path, cron. Only use the fields your type needs.
                   ConfigField fields: field, type (STRING/INT/BOOLEAN), required (boolean), defaultValue (string).
                3. Use only the archetypes listed in the host contract below. The target app is %s.
                4. Each extension's script must be a key of scripts, and its method must match the
                   exact host signature. Every script method receives Object ctx as its last argument.
                5. Each scripts value is a complete Groovy source file with no package declaration.
                   Imports are allowed. Define one class per file; the class name is free.
                6. Read configuration through ctx.configString/configInt/configBool/configObject/config.
                   The prompt never contains credentials; never hardcode passwords, tokens or API keys.
                7. Do not hardcode the plugin key; use ctx.pluginKey(). Log through ctx.log().
                8. HTTP_ENDPOINT paths are relative to /plugin-platform/ext/{pluginKey}/, start with '/',
                   and must set httpMethod explicitly (GET, POST, PUT, DELETE, PATCH, HEAD or OPTIONS).
                9. REQUEST_* and CUSTOM_REQUEST_INVOKE declarations must set apiType to an ApiTypeEnum value.
                10. LIFECYCLE_LISTENER entityName must be one of the entity names listed below.
                11. CUSTOM_API is available on station only and requires customApiCode.
                12. You only produce a draft revision. Never claim to enable, disable, roll back or archive a plugin.
                13. When a base revision is supplied, preserve its structure where the requirement does not
                    require a change, and describe the delta in meta.changeSummary.
                14. Before writing the output, decide internally: the entities involved, the expected behavior,
                    and the configuration needs. Encode configuration needs as configSchema fields and read
                    them through ctx.config* instead of hardcoding literals.
                15. Scripts run inline on a host thread under a hard execution timeout. Return promptly, never
                    start background threads, and never block indefinitely. In optional hooks (supply/afterInvoke,
                    wave/split, CRON, HTTP_ENDPOINT) catch recoverable failures, log through ctx.log() and return
                    the documented fallback (null or an empty list). In rejecting hooks (REQUEST_VALIDATE,
                    CUSTOM_REQUEST_INVOKE, CUSTOM_API) let the exception propagate so the platform surfaces it.
                16. All generated code and text must be in English.

                %s

                Output JSON example (valid and complete):
                %s
                """.formatted(app, examples(app), outputExample());
    }

    /** App-specific few-shot guidance; the generic output example above is valid for both apps. */
    public static String examples(String app) {
        if (!ScriptPluginApiConstants.APP_WES.equals(app)) {
            return "Station example: use CUSTOM_API with customApiCode and execute(CustomApiParameter, ctx), "
                    + "or HTTP_ENDPOINT / CRON. Station scripts reach station beans through ctx.station() "
                    + "or ctx.bean('ptl').";
        }
        return """
                WES example, wave grouping:
                extension: {"type":"OUTBOUND_WAVE_WAVE_ACTION","priority":100,"script":"Wave.groovy","method":"wave"}
                Wave.groovy:
                class Wave {
                    Object wave(List orders, Object ctx) {
                        int maxPerWave = ctx.configInt('maxPerWave') ?: 10
                        ctx.log().info('grouping {} outbound orders', orders.size())
                        return orders.collate(maxPerWave)
                    }
                }
                configSchema: [{"field":"maxPerWave","type":"INT","required":false,"defaultValue":"10"}]
                """;
    }

    /** A complete JSON draft; tests parse it and the validator accepts it. */
    public static String outputExample() {
        return """
                {
                  "meta": {
                    "name": "Packing status endpoint",
                    "changeSummary": "Expose a status endpoint for the packing cell"
                  },
                  "extensions": [
                    {
                      "type": "HTTP_ENDPOINT",
                      "priority": 100,
                      "script": "Status.groovy",
                      "method": "handle",
                      "httpMethod": "GET",
                      "path": "/status"
                    }
                  ],
                  "configSchema": [],
                  "scripts": {
                    "Status.groovy": "class Status {\\n    Object handle(Object body, Object ctx) {\\n        ctx.log().info('status checked')\\n        return [status: 'ok', plugin: ctx.pluginKey()]\\n    }\\n}\\n"
                  }
                }
                """;
    }
}
```

Note: `%s` inside rule 8 is `{pluginKey}` — in `String.formatted`, only `%` is special, so braces are safe. There are exactly three `%s` placeholders (rule 3 app, examples, example). Verify the text has no other `%`.

- [ ] **Step 7: Write the failing assembler test**

Create `AiPromptAssemblerTest.java`:

```java
package org.openwes.ai.plugin.prompt;

import org.junit.jupiter.api.Test;
import org.openwes.ai.plugin.config.AiPluginProperties;
import org.openwes.script.plugin.api.dto.ConfigFieldDTO;
import org.openwes.script.plugin.api.dto.ExtensionDeclarationDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginRevisionDTO;
import org.openwes.script.plugin.api.enums.ExtensionType;
import org.openwes.script.plugin.api.enums.ScriptPluginStatusEnum;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.zhipuai.ZhiPuAiChatOptions;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiPromptAssemblerTest {

    private final AiPluginProperties properties = new AiPluginProperties();
    private final AiPromptAssembler assembler = new AiPromptAssembler(properties);

    private ScriptPluginDTO plugin() {
        ScriptPluginDTO plugin = new ScriptPluginDTO();
        plugin.setPluginKey("packing-wave");
        plugin.setName("Packing wave");
        plugin.setDescription("Groups packing orders into waves");
        plugin.setApplySystem("wes");
        plugin.setStatus(ScriptPluginStatusEnum.DISABLED);
        return plugin;
    }

    private ScriptPluginRevisionDTO baseRevision() {
        ExtensionDeclarationDTO declaration = new ExtensionDeclarationDTO();
        declaration.setType(ExtensionType.OUTBOUND_WAVE_WAVE_ACTION);
        declaration.setScript("Wave.groovy");
        declaration.setMethod("wave");
        ScriptPluginRevisionDTO revision = new ScriptPluginRevisionDTO();
        revision.setRevisionNo(3);
        revision.setChangeSummary("initial");
        revision.setScripts(Map.of("Wave.groovy", "class Wave { Object wave(Object o, Object ctx) { return o } }"));
        revision.setExtensionPoints(List.of(declaration));
        revision.setConfigSchema(List.of(new ConfigFieldDTO()));
        return revision;
    }

    @Test
    void assemble_usesTemperatureZeroAndTheConfiguredModel() {
        properties.setModel("GLM-4-air");

        Prompt prompt = assembler.assemble("group orders", plugin(), null, List.of(), List.of(), 2048);

        ZhiPuAiChatOptions options = assertInstanceOf(ZhiPuAiChatOptions.class, prompt.getOptions());
        assertEquals("GLM-4-air", options.getModel());
        assertEquals(0.0, options.getTemperature());
        assertEquals(2048, options.getMaxTokens());
    }

    @Test
    void assemble_includesContractRequirementBaseRevisionGroundingAndErrors() {
        Prompt prompt = assembler.assemble("group orders into waves of ten", plugin(), baseRevision(),
                List.of("reference snippet"), List.of("declarations: method mismatch"), 4096);

        List<Message> messages = prompt.getInstructions();
        assertEquals(2, messages.size());
        assertInstanceOf(SystemMessage.class, messages.get(0));
        String user = messages.get(1).getText();

        assertTrue(user.contains("group orders into waves of ten"));
        assertTrue(user.contains("packing-wave"));
        assertTrue(user.contains("Contract version: 1.0"));
        assertTrue(user.contains("OUTBOUND_WAVE_WAVE_ACTION"));
        assertTrue(user.contains("\"revisionNo\" : 3"));
        assertTrue(user.contains("reference snippet"));
        assertTrue(user.contains("declarations: method mismatch"));
    }

    @Test
    void assemble_omitsEmptySectionsCleanly() {
        Prompt prompt = assembler.assemble("status endpoint", plugin(), null, List.of(), List.of(), 1024);

        String user = prompt.getInstructions().get(1).getText();
        assertTrue(user.contains("(none)"));
        assertTrue(user.contains("No base revision"));
    }
}
```

- [ ] **Step 8: Implement `AiPromptAssembler`**

Create the file:

```java
package org.openwes.ai.plugin.prompt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.openwes.ai.plugin.config.AiPluginProperties;
import org.openwes.script.plugin.api.contract.PluginContractCatalog;
import org.openwes.script.plugin.api.dto.ScriptPluginDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginRevisionDTO;
import org.openwes.script.plugin.api.enums.ExtensionType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.zhipuai.ZhiPuAiChatOptions;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AiPromptAssembler {

    private final AiPluginProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Prompt assemble(String requirement, ScriptPluginDTO plugin, ScriptPluginRevisionDTO baseRevision,
                           List<String> groundingSnippets, List<String> previousErrors, int maxTokens) {
        String app = plugin.getApplySystem();
        StringBuilder user = new StringBuilder();

        user.append("## Requirement\n").append(requirement).append("\n\n");
        user.append("## Plugin\n")
                .append("pluginKey: ").append(plugin.getPluginKey()).append('\n')
                .append("name: ").append(plugin.getName()).append('\n')
                .append("description: ").append(plugin.getDescription() == null ? "" : plugin.getDescription())
                .append('\n')
                .append("status: ").append(plugin.getStatus()).append('\n')
                .append("applyModule: ").append(plugin.getApplyModule() == null ? "" : plugin.getApplyModule())
                .append("\n\n");

        List<ExtensionType> allowedTypes = PluginContractCatalog.typesForApp(app);
        user.append("## Host contract (authoritative)\n")
                .append(PluginContractCatalog.toPromptFragment(app, allowedTypes)).append('\n');

        user.append("## Base revision\n")
                .append(baseRevision == null ? "No base revision; create the plugin from scratch."
                        : renderBaseRevision(baseRevision)).append("\n\n");

        user.append("## Retrieved reference snippets\n")
                .append(groundingSnippets.isEmpty() ? "(none)" : String.join("\n---\n", groundingSnippets))
                .append("\n\n");

        user.append("## Errors from previous attempts (fix every one)\n")
                .append(previousErrors.isEmpty() ? "(none)" : String.join("\n", previousErrors))
                .append('\n');

        ZhiPuAiChatOptions options = ZhiPuAiChatOptions.builder()
                .model(properties.getModel())
                .temperature(0.0)
                .maxTokens(maxTokens)
                .build();

        return new Prompt(List.of(
                new SystemMessage(PluginPromptTemplates.systemPrompt(app)),
                new UserMessage(user.toString())), options);
    }

    private String renderBaseRevision(ScriptPluginRevisionDTO revision) {
        Map<String, Object> base = new LinkedHashMap<>();
        base.put("revisionNo", revision.getRevisionNo());
        base.put("changeSummary", revision.getChangeSummary());
        base.put("scripts", revision.getScripts());
        base.put("extensions", revision.getExtensionPoints());
        base.put("configSchema", revision.getConfigSchema());
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(base);
        } catch (JsonProcessingException e) {
            // The base revision was already validated as JSON by ScriptPluginService; this cannot
            // happen for a revision loaded from the database.
            throw new IllegalStateException("cannot render base revision", e);
        }
    }
}
```

Unused import `ScriptPluginApiConstants` — remove it from the implementation (the plan's code should not import it). Keep `ExtensionType` imported normally instead of the fully-qualified generic; simplify:

```java
import org.openwes.script.plugin.api.enums.ExtensionType;
...
List<ExtensionType> allowedTypes = PluginContractCatalog.typesForApp(app);
```

- [ ] **Step 9: Run the prompt tests**

```bash
cd D:/open-wes/server
export JAVA_HOME="${JAVA_HOME:-C:/Users/niekang/.jdks/ms-17.0.20.1}"
./gradlew :modules-ai:ai-plugin:test \
  --tests "org.openwes.ai.plugin.prompt.PluginPromptTemplatesTest" \
  --tests "org.openwes.ai.plugin.prompt.AiPromptAssemblerTest"
```

Expected: PASS (6 tests).

- [ ] **Step 10: Commit**

```bash
git add server/modules-plugin/script-plugin-api server/modules-ai/ai-plugin
git commit -m "feat(ai-plugin): add host signatures and prompt assembly

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 5: Parse the model output into a draft document

**Files:**
- Create: `server/modules-ai/ai-plugin/src/main/java/org/openwes/ai/plugin/domain/model/AiDraftDocument.java`
- Create: `server/modules-ai/ai-plugin/src/main/java/org/openwes/ai/plugin/exception/AiDraftParseException.java`
- Create: `server/modules-ai/ai-plugin/src/main/java/org/openwes/ai/plugin/domain/service/AiDraftDocumentParser.java`
- Create: `server/modules-ai/ai-plugin/src/test/java/org/openwes/ai/plugin/domain/service/AiDraftDocumentParserTest.java`

**Interfaces:**
- Consumes: `PluginContractCatalog.typesForApp` and `LIFECYCLE_ENTITY_NAMES` (Task 4), `PluginPromptTemplates.outputExample()` (Task 4).
- Produces: `AiDraftDocumentParser.parse(String raw, String expectedApp)` → `AiDraftDocument` or throws `AiDraftParseException` with a human-readable message.
  - `AiDraftDocument.getScripts()` → `Map<String, String>`
  - `.getExtensions()` → `List<ExtensionDeclarationDTO>`
  - `.getConfigSchema()` → `List<ConfigFieldDTO>`
  - `.getMeta().getChangeSummary()` → `String`

- [ ] **Step 1: Write the failing parser test**

Create `server/modules-ai/ai-plugin/src/test/java/org/openwes/ai/plugin/domain/service/AiDraftDocumentParserTest.java`:

```java
package org.openwes.ai.plugin.domain.service;

import org.junit.jupiter.api.Test;
import org.openwes.ai.plugin.domain.model.AiDraftDocument;
import org.openwes.ai.plugin.exception.AiDraftParseException;
import org.openwes.ai.plugin.prompt.PluginPromptTemplates;
import org.openwes.script.plugin.api.enums.ExtensionType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiDraftDocumentParserTest {

    private final AiDraftDocumentParser parser = new AiDraftDocumentParser();

    @Test
    void parsesThePromptOutputExample() {
        AiDraftDocument document = parser.parse(PluginPromptTemplates.outputExample(), "wes");

        assertEquals("Packing status endpoint", document.getMeta().getName());
        assertEquals(1, document.getExtensions().size());
        assertEquals(ExtensionType.HTTP_ENDPOINT, document.getExtensions().get(0).getType());
        assertEquals("GET", document.getExtensions().get(0).getHttpMethod());
        assertTrue(document.getScripts().get("Status.groovy").contains("Object handle(Object body, Object ctx)"));
    }

    @Test
    void stripsMarkdownFencesAndSurroundingProse() {
        String raw = "Here is the draft:\n```json\n" + PluginPromptTemplates.outputExample() + "```\nDone.";

        AiDraftDocument document = parser.parse(raw, "wes");

        assertEquals("Status.groovy", document.getScripts().keySet().iterator().next());
    }

    @Test
    void stripsGroovyFencesBeforeTheJsonBlock() {
        String raw = "```groovy\nclass Scratch {}\n```\n```json\n" + PluginPromptTemplates.outputExample() + "```";

        AiDraftDocument document = parser.parse(raw, "station");

        assertEquals(1, document.getExtensions().size());
    }

    @Test
    void rejectsTextWithoutAJsonObject() {
        AiDraftParseException e = assertThrows(AiDraftParseException.class,
                () -> parser.parse("I cannot help with that.", "wes"));
        assertTrue(e.getMessage().contains("no JSON object"));
    }

    @Test
    void rejectsMissingScripts() {
        String raw = """
                {"meta":{"name":"x","changeSummary":"y"},"extensions":[],"configSchema":[],"scripts":{}}
                """;
        AiDraftParseException e = assertThrows(AiDraftParseException.class, () -> parser.parse(raw, "wes"));
        assertTrue(e.getMessage().contains("at least one script"));
    }

    @Test
    void rejectsMissingExtensions() {
        String raw = """
                {"meta":{"name":"x","changeSummary":"y"},"extensions":[],"configSchema":[],
                 "scripts":{"A.groovy":"class A { Object handle(Object b, Object ctx) { return 1 } }"}}
                """;
        AiDraftParseException e = assertThrows(AiDraftParseException.class, () -> parser.parse(raw, "wes"));
        assertTrue(e.getMessage().contains("at least one extension"));
    }

    @Test
    void rejectsUnknownScriptReferences() {
        String raw = """
                {"meta":{"name":"x","changeSummary":"y"},
                 "extensions":[{"type":"HTTP_ENDPOINT","script":"Missing.groovy","method":"handle","httpMethod":"GET","path":"/x"}],
                 "configSchema":[],"scripts":{"A.groovy":"class A { Object handle(Object b, Object ctx) { return 1 } }"}}
                """;
        AiDraftParseException e = assertThrows(AiDraftParseException.class, () -> parser.parse(raw, "wes"));
        assertTrue(e.getMessage().contains("unknown script 'Missing.groovy'"));
    }

    @Test
    void rejectsHttpEndpointWithoutMethod() {
        String raw = """
                {"meta":{"name":"x","changeSummary":"y"},
                 "extensions":[{"type":"HTTP_ENDPOINT","script":"A.groovy","method":"handle","path":"/x"}],
                 "configSchema":[],"scripts":{"A.groovy":"class A { Object handle(Object b, Object ctx) { return 1 } }"}}
                """;
        AiDraftParseException e = assertThrows(AiDraftParseException.class, () -> parser.parse(raw, "wes"));
        assertTrue(e.getMessage().contains("httpMethod"));
    }

    @Test
    void rejectsCustomApiOnWes() {
        String raw = """
                {"meta":{"name":"x","changeSummary":"y"},
                 "extensions":[{"type":"CUSTOM_API","script":"A.groovy","method":"execute","customApiCode":"X"}],
                 "configSchema":[],"scripts":{"A.groovy":"class A { Object execute(Object p, Object ctx) { return 1 } }"}}
                """;
        AiDraftParseException e = assertThrows(AiDraftParseException.class, () -> parser.parse(raw, "wes"));
        assertTrue(e.getMessage().contains("not supported for app wes"));
    }

    @Test
    void rejectsUnknownLifecycleEntity() {
        String raw = """
                {"meta":{"name":"x","changeSummary":"y"},
                 "extensions":[{"type":"LIFECYCLE_LISTENER","script":"A.groovy","method":"afterStatusChange","entityName":"Nope"}],
                 "configSchema":[],"scripts":{"A.groovy":"class A { Object afterStatusChange(Object a, Object b, Object c, Object ctx) { return 1 } }"}}
                """;
        AiDraftParseException e = assertThrows(AiDraftParseException.class, () -> parser.parse(raw, "wes"));
        assertTrue(e.getMessage().contains("entityName"));
        assertTrue(e.getMessage().contains("OutboundPlanOrder"));
    }

    @Test
    void rejectsNonJsonOutputWithTheParserError() {
        AiDraftParseException e = assertThrows(AiDraftParseException.class,
                () -> parser.parse("{ not json }", "wes"));
        assertTrue(e.getMessage().contains("not a valid draft JSON"));
    }
}
```

- [ ] **Step 2: Run the test to verify it fails to compile**

```bash
cd D:/open-wes/server
export JAVA_HOME="${JAVA_HOME:-C:/Users/niekang/.jdks/ms-17.0.20.1}"
./gradlew :modules-ai:ai-plugin:test --tests "org.openwes.ai.plugin.domain.service.AiDraftDocumentParserTest"
```

Expected: FAIL — the document, exception and parser types do not exist.

- [ ] **Step 3: Implement the document and exception**

`AiDraftDocument.java`:

```java
package org.openwes.ai.plugin.domain.model;

import lombok.Data;
import org.openwes.script.plugin.api.dto.ConfigFieldDTO;
import org.openwes.script.plugin.api.dto.ExtensionDeclarationDTO;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** The model's structured output; field names mirror the platform DTOs exactly. */
@Data
public class AiDraftDocument {

    private Meta meta = new Meta();
    private List<ExtensionDeclarationDTO> extensions = new ArrayList<>();
    private List<ConfigFieldDTO> configSchema = new ArrayList<>();
    private Map<String, String> scripts = new LinkedHashMap<>();

    @Data
    public static class Meta {
        private String name;
        private String changeSummary;
    }
}
```

`AiDraftParseException.java`:

```java
package org.openwes.ai.plugin.exception;

public class AiDraftParseException extends RuntimeException {

    public AiDraftParseException(String message) {
        super(message);
    }
}
```

- [ ] **Step 4: Implement the parser**

`AiDraftDocumentParser.java`:

```java
package org.openwes.ai.plugin.domain.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.openwes.ai.plugin.domain.model.AiDraftDocument;
import org.openwes.ai.plugin.exception.AiDraftParseException;
import org.openwes.script.plugin.api.contract.PluginContractCatalog;
import org.openwes.script.plugin.api.dto.ExtensionDeclarationDTO;
import org.openwes.script.plugin.api.enums.ExtensionType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Turns raw model text into a draft document. Model output is untrusted: tolerates fences and
 * prose, rejects everything the static validator or the target app would reject, and reports one
 * actionable message per problem so the retry prompt can fix it.
 */
@Component
public class AiDraftDocumentParser {

    private static final Pattern FENCED_BLOCK =
            Pattern.compile("```[A-Za-z0-9_-]*\\s*\\n?(.*?)```", Pattern.DOTALL);
    private static final Set<String> HTTP_METHODS =
            Set.of("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS");

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public AiDraftDocument parse(String raw, String expectedApp) {
        AiDraftDocument document = readDocument(raw);
        validate(document, expectedApp);
        return document;
    }

    private AiDraftDocument readDocument(String raw) {
        if (StringUtils.isBlank(raw)) {
            throw new AiDraftParseException("model returned an empty response");
        }
        AiDraftDocument direct = tryRead(raw.trim());
        if (direct != null) {
            return direct;
        }

        Matcher matcher = FENCED_BLOCK.matcher(raw);
        while (matcher.find()) {
            AiDraftDocument fenced = tryRead(matcher.group(1).trim());
            if (fenced != null) {
                return fenced;
            }
        }

        int first = raw.indexOf('{');
        int last = raw.lastIndexOf('}');
        if (first >= 0 && last > first) {
            AiDraftDocument embedded = tryRead(raw.substring(first, last + 1));
            if (embedded != null) {
                return embedded;
            }
        }
        throw new AiDraftParseException("model output contains no JSON object that matches the draft shape");
    }

    private AiDraftDocument tryRead(String candidate) {
        try {
            return objectMapper.readValue(candidate, AiDraftDocument.class);
        } catch (Exception e) {
            return null;
        }
    }

    private void validate(AiDraftDocument document, String expectedApp) {
        // Nulls can only come from an explicit JSON null; normalize so the checks below are total.
        if (document.getMeta() == null) {
            document.setMeta(new AiDraftDocument.Meta());
        }
        if (document.getScripts() == null) {
            document.setScripts(new java.util.LinkedHashMap<>());
        }
        if (document.getExtensions() == null) {
            document.setExtensions(new ArrayList<>());
        }
        if (document.getConfigSchema() == null) {
            document.setConfigSchema(new ArrayList<>());
        }

        List<String> errors = new ArrayList<>();
        if (document.getScripts().isEmpty()) {
            errors.add("scripts must contain at least one script");
        } else {
            document.getScripts().forEach((name, source) -> {
                if (StringUtils.isBlank(name)) {
                    errors.add("script file names must not be blank");
                }
                if (StringUtils.isBlank(source)) {
                    errors.add("script '" + name + "' has no source");
                }
            });
        }
        if (document.getExtensions() == null || document.getExtensions().isEmpty()) {
            errors.add("extensions must contain at least one extension");
        }

        List<ExtensionType> allowed = PluginContractCatalog.typesForApp(expectedApp);
        if (allowed.isEmpty()) {
            errors.add("app '" + expectedApp + "' does not host script plugins");
        }

        if (document.getExtensions() != null) {
            for (ExtensionDeclarationDTO extension : document.getExtensions()) {
                validateExtension(extension, document, allowed, expectedApp, errors);
            }
        }
        if (!errors.isEmpty()) {
            throw new AiDraftParseException(String.join("; ", errors));
        }

        if (StringUtils.isBlank(document.getMeta().getName())) {
            document.getMeta().setName("AI draft");
        }
        if (StringUtils.isBlank(document.getMeta().getChangeSummary())) {
            document.getMeta().setChangeSummary("AI generated draft");
        }
    }

    private void validateExtension(ExtensionDeclarationDTO extension, AiDraftDocument document,
                                   List<ExtensionType> allowed, String expectedApp, List<String> errors) {
        if (extension.getType() == null) {
            errors.add("every extension needs a type");
            return;
        }
        if (!allowed.contains(extension.getType())) {
            errors.add("extension type " + extension.getType() + " is not supported for app " + expectedApp);
            return;
        }
        if (StringUtils.isBlank(extension.getScript()) || StringUtils.isBlank(extension.getMethod())) {
            errors.add(extension.getType() + " needs both script and method");
            return;
        }
        if (!document.getScripts().containsKey(extension.getScript())) {
            errors.add(extension.getType() + " references unknown script '" + extension.getScript() + "'");
            return;
        }
        switch (extension.getType()) {
            case REQUEST_VALIDATE, REQUEST_SUPPLY, REQUEST_AFTER_INVOKE, CUSTOM_REQUEST_INVOKE -> {
                if (StringUtils.isBlank(extension.getApiType())) {
                    errors.add(extension.getType() + " needs apiType");
                }
            }
            case CUSTOM_API -> {
                if (StringUtils.isBlank(extension.getCustomApiCode())) {
                    errors.add("CUSTOM_API needs customApiCode");
                }
            }
            case LIFECYCLE_LISTENER -> {
                if (StringUtils.isBlank(extension.getEntityName())
                        || !PluginContractCatalog.LIFECYCLE_ENTITY_NAMES.contains(extension.getEntityName())) {
                    errors.add("LIFECYCLE_LISTENER entityName must be one of "
                            + PluginContractCatalog.LIFECYCLE_ENTITY_NAMES + " but was '"
                            + extension.getEntityName() + "'");
                }
            }
            case HTTP_ENDPOINT -> {
                if (StringUtils.isBlank(extension.getHttpMethod())
                        || !HTTP_METHODS.contains(extension.getHttpMethod().toUpperCase(Locale.ROOT))) {
                    errors.add("HTTP_ENDPOINT needs httpMethod in " + HTTP_METHODS);
                }
                if (StringUtils.isBlank(extension.getPath()) || !extension.getPath().startsWith("/")) {
                    errors.add("HTTP_ENDPOINT path must start with '/'");
                }
            }
            case CRON -> {
                if (StringUtils.isBlank(extension.getCron())) {
                    errors.add("CRON needs a cron expression");
                }
            }
            default -> {
            }
        }
    }
}
```

- [ ] **Step 5: Run the parser test**

```bash
cd D:/open-wes/server
export JAVA_HOME="${JAVA_HOME:-C:/Users/niekang/.jdks/ms-17.0.20.1}"
./gradlew :modules-ai:ai-plugin:test --tests "org.openwes.ai.plugin.domain.service.AiDraftDocumentParserTest"
```

Expected: PASS (11 tests).

- [ ] **Step 6: Commit**

```bash
git add server/modules-ai/ai-plugin
git commit -m "feat(ai-plugin): parse and sanity-check model draft output

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 6: Validate drafts without saving them

**Files:**
- Create: `server/modules-ai/ai-plugin/src/main/java/org/openwes/ai/plugin/validation/AiDraftValidator.java`
- Create: `server/modules-ai/ai-plugin/src/test/java/org/openwes/ai/plugin/validation/AiDraftValidatorTest.java`

**Interfaces:**
- Consumes:
  - `ScriptPluginStaticValidator.validate(ScriptPluginBundleDTO, Consumer<String>)` (Task 1).
  - `PluginContractCatalog.typesForApp` (Task 4).
  - `ScriptPluginMaterializationValidator.validate(ScriptPluginBundleDTO)` (Phase 1b).
- Produces:
  - `AiDraftValidator.validate(ScriptPluginDTO plugin, AiDraftDocument document, Consumer<String> onStage)` → `AiDraftValidator.Result`.
  - `Result.passed()`, `Result.materializationVerified()`, `Result.failures()`.

- [ ] **Step 1: Write the failing validator test**

Create `AiDraftValidatorTest.java`:

```java
package org.openwes.ai.plugin.validation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openwes.ai.plugin.domain.model.AiDraftDocument;
import org.openwes.ai.plugin.domain.service.AiDraftDocumentParser;
import org.openwes.ai.plugin.prompt.PluginPromptTemplates;
import org.openwes.script.engine.GroovyScriptEngine;
import org.openwes.script.plugin.api.ScriptPluginMaterializationValidator;
import org.openwes.script.plugin.api.dto.ScriptPluginBundleDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginDTO;
import org.openwes.script.plugin.api.dto.ValidationReportDTO;
import org.openwes.script.plugin.api.enums.ScriptPluginStatusEnum;
import org.openwes.script.plugin.runtime.materialize.ScriptRevisionContextManager;
import org.openwes.script.plugin.runtime.validation.ScriptPluginMaterializationValidatorImpl;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiDraftValidatorTest {

    private final AiDraftDocumentParser parser = new AiDraftDocumentParser();
    private final GroovyScriptEngine engine = new GroovyScriptEngine();
    private AnnotationConfigApplicationContext parent;
    private ScriptPluginMaterializationValidator wesMaterializer;
    private AiDraftValidator validator;

    @BeforeEach
    void setUp() {
        parent = new AnnotationConfigApplicationContext();
        parent.refresh();
        wesMaterializer = new ScriptPluginMaterializationValidatorImpl(engine,
                new ScriptRevisionContextManager(parent, engine), "wes");
        validator = new AiDraftValidator(engine, List.of(wesMaterializer));
    }

    @AfterEach
    void tearDown() {
        parent.close();
    }

    private ScriptPluginDTO plugin(String app) {
        ScriptPluginDTO plugin = new ScriptPluginDTO();
        plugin.setPluginKey("packing-wave");
        plugin.setName("Packing wave");
        plugin.setApplySystem(app);
        plugin.setStatus(ScriptPluginStatusEnum.DRAFT);
        return plugin;
    }

    @Test
    void wesDraft_passesStaticValidationAndInProcessMaterialization() {
        AiDraftDocument document = parser.parse(PluginPromptTemplates.outputExample(), "wes");
        List<String> stages = new ArrayList<>();

        AiDraftValidator.Result result = validator.validate(plugin("wes"), document, stages::add);

        assertTrue(result.passed(), result.failures().toString());
        assertTrue(result.materializationVerified());
        assertEquals(List.of("declarations", "compiling", "wiring"), stages);
    }

    @Test
    void stationDraft_passesStaticValidationWithoutMaterialization() {
        AiDraftDocument document = parser.parse(PluginPromptTemplates.outputExample(), "station");
        List<String> stages = new ArrayList<>();

        AiDraftValidator.Result result = validator.validate(plugin("station"), document, stages::add);

        assertTrue(result.passed(), result.failures().toString());
        assertFalse(result.materializationVerified());
        assertEquals(List.of("declarations", "compiling"), stages);
    }

    @Test
    void missingScriptMethod_failsAtTheWiringStage() {
        AiDraftDocument document = parser.parse("""
                {"meta":{"name":"x","changeSummary":"y"},
                 "extensions":[{"type":"HTTP_ENDPOINT","script":"A.groovy","method":"handle","httpMethod":"GET","path":"/x"}],
                 "configSchema":[],"scripts":{"A.groovy":"class A { Object other(Object b, Object ctx) { return 1 } }"}}
                """, "wes");
        List<String> stages = new ArrayList<>();

        AiDraftValidator.Result result = validator.validate(plugin("wes"), document, stages::add);

        assertFalse(result.passed());
        assertTrue(result.materializationVerified());
        assertTrue(stages.contains("wiring"));
        assertTrue(result.failures().stream().anyMatch(f -> f.getStage().equals("wiring")
                && f.getMessage().contains("does not declare method 'handle'")));
    }

    @Test
    void compileError_failsAtTheCompilingStage() {
        AiDraftDocument document = parser.parse("""
                {"meta":{"name":"x","changeSummary":"y"},
                 "extensions":[{"type":"HTTP_ENDPOINT","script":"A.groovy","method":"handle","httpMethod":"GET","path":"/x"}],
                 "configSchema":[],"scripts":{"A.groovy":"class A {"}}
                """, "wes");
        List<String> stages = new ArrayList<>();

        AiDraftValidator.Result result = validator.validate(plugin("wes"), document, stages::add);

        assertFalse(result.passed());
        assertTrue(stages.contains("compiling"));
        assertTrue(result.failures().stream().anyMatch(f -> f.getStage().equals("compiling")));
    }

    @Test
    void wesWithoutAMaterializer_failsClosed() {
        AiDraftValidator noMaterializer = new AiDraftValidator(engine, List.of());

        AiDraftValidator.Result result = noMaterializer.validate(plugin("wes"),
                parser.parse(PluginPromptTemplates.outputExample(), "wes"), stage -> { });

        assertFalse(result.passed());
        assertFalse(result.materializationVerified());
        assertTrue(result.failures().stream().anyMatch(f -> f.getStage().equals("wiring")));
    }

    @Test
    void materializerFailureStages_areMappedToWiring() {
        ScriptPluginMaterializationValidator alwaysContextFailure = new ScriptPluginMaterializationValidator() {
            @Override
            public String targetApp() {
                return "wes";
            }

            @Override
            public ValidationReportDTO validate(ScriptPluginBundleDTO bundle) {
                return ValidationReportDTO.failed("packing-wave", null, "wes", "context", "@PostConstruct failed");
            }
        };
        AiDraftValidator mapped = new AiDraftValidator(engine, List.of(alwaysContextFailure));

        AiDraftValidator.Result result = mapped.validate(plugin("wes"),
                parser.parse(PluginPromptTemplates.outputExample(), "wes"), stage -> { });

        assertFalse(result.passed());
        assertTrue(result.failures().stream().anyMatch(f -> f.getStage().equals("wiring")
                && f.getMessage().contains("@PostConstruct")));
    }
}
```

`ScriptPluginMaterializationValidator` is an interface with `targetApp()` and `validate`. Anonymous class works.

- [ ] **Step 2: Run the test to verify it fails to compile**

```bash
cd D:/open-wes/server
export JAVA_HOME="${JAVA_HOME:-C:/Users/niekang/.jdks/ms-17.0.20.1}"
./gradlew :modules-ai:ai-plugin:test --tests "org.openwes.ai.plugin.validation.AiDraftValidatorTest"
```

Expected: FAIL — `AiDraftValidator` does not exist.

- [ ] **Step 3: Implement `AiDraftValidator`**

Create the file:

```java
package org.openwes.ai.plugin.validation;

import org.openwes.ai.plugin.domain.model.AiDraftDocument;
import org.openwes.script.engine.GroovyScriptEngine;
import org.openwes.script.plugin.api.ScriptPluginApiConstants;
import org.openwes.script.plugin.api.ScriptPluginMaterializationValidator;
import org.openwes.script.plugin.api.contract.PluginContractCatalog;
import org.openwes.script.plugin.api.dto.ExtensionDeclarationDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginBundleDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginDTO;
import org.openwes.script.plugin.api.dto.ValidationReportDTO;
import org.openwes.script.plugin.api.enums.ExtensionType;
import org.openwes.script.plugin.api.validation.ScriptPluginStaticValidator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Validates an unsaved draft with the same rules as enable-time validation. Static stages run for
 * both apps. On wes, the in-process materializer additionally proves context creation, injection
 * and declared methods; station drafts are static-only until Phase 3 adds a preview handshake.
 */
@Component
public class AiDraftValidator {

    public record Result(boolean passed, boolean materializationVerified,
                         List<ValidationReportDTO.ValidationFailureDTO> failures) {
    }

    private final ScriptPluginStaticValidator staticValidator;
    private final List<ScriptPluginMaterializationValidator> materializationValidators;

    public AiDraftValidator(GroovyScriptEngine scriptEngine,
                            List<ScriptPluginMaterializationValidator> materializationValidators) {
        this.staticValidator = new ScriptPluginStaticValidator(scriptEngine);
        this.materializationValidators = materializationValidators;
    }

    public Result validate(ScriptPluginDTO plugin, AiDraftDocument document, Consumer<String> onStage) {
        List<ValidationReportDTO.ValidationFailureDTO> failures = new ArrayList<>();
        List<ExtensionType> allowed = PluginContractCatalog.typesForApp(plugin.getApplySystem());
        for (ExtensionDeclarationDTO extension : document.getExtensions()) {
            if (!allowed.contains(extension.getType())) {
                failures.add(failure("declarations", extension.getType()
                        + " is not supported for app " + plugin.getApplySystem()));
            }
        }
        if (!failures.isEmpty()) {
            return new Result(false, false, failures);
        }

        ScriptPluginBundleDTO bundle = toBundle(plugin, document);
        failures.addAll(staticValidator.validate(bundle, stage -> onStage.accept(mapStage(stage))));
        if (!failures.isEmpty()) {
            return new Result(false, false, failures);
        }

        if (!ScriptPluginApiConstants.APP_WES.equals(plugin.getApplySystem())) {
            return new Result(true, false, failures);
        }

        Optional<ScriptPluginMaterializationValidator> local = materializationValidators.stream()
                .filter(validator -> ScriptPluginApiConstants.APP_WES.equals(validator.targetApp()))
                .findFirst();
        if (local.isEmpty()) {
            failures.add(failure("wiring",
                    "wes materialization validator is unavailable; cannot verify the draft"));
            return new Result(false, false, failures);
        }

        onStage.accept("wiring");
        ValidationReportDTO report = local.get().validate(bundle);
        report.getFailures().forEach(failure ->
                failures.add(failure(mapStage(failure.getStage()), failure.getMessage())));
        return new Result(failures.isEmpty(), true, failures);
    }

    private ScriptPluginBundleDTO toBundle(ScriptPluginDTO plugin, AiDraftDocument document) {
        ScriptPluginBundleDTO bundle = new ScriptPluginBundleDTO();
        bundle.setPluginKey(plugin.getPluginKey());
        bundle.setName(plugin.getName());
        bundle.setApplySystem(plugin.getApplySystem());
        bundle.setApplyModule(plugin.getApplyModule());
        bundle.setSdkVersion(ScriptPluginApiConstants.CONTRACT_VERSION);
        bundle.setDeclarations(document.getExtensions());
        bundle.setConfigSchema(document.getConfigSchema());
        bundle.setScripts(document.getScripts());
        bundle.setConfig(java.util.Map.of());
        return bundle;
    }

    /** UI/SSE stage vocabulary; every failure maps onto declarations, compiling or wiring. */
    private String mapStage(String stage) {
        return switch (stage) {
            case "sdkVersion", "declarations" -> "declarations";
            case "compile" -> "compiling";
            case "context", "smoke" -> "wiring";
            default -> stage;
        };
    }

    private ValidationReportDTO.ValidationFailureDTO failure(String stage, String message) {
        return new ValidationReportDTO.ValidationFailureDTO(stage, message);
    }
}
```

- [ ] **Step 4: Run the validator test**

```bash
cd D:/open-wes/server
export JAVA_HOME="${JAVA_HOME:-C:/Users/niekang/.jdks/ms-17.0.20.1}"
./gradlew :modules-ai:ai-plugin:test --tests "org.openwes.ai.plugin.validation.AiDraftValidatorTest"
```

Expected: PASS (6 tests). The real materializer test proves the prompt's output example is actually valid.

- [ ] **Step 5: Commit**

```bash
git add server/modules-ai/ai-plugin
git commit -m "feat(ai-plugin): validate drafts before they are saved

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 7: Optional Pinecone grounding

**Files:**
- Create: `server/modules-ai/ai-plugin/src/main/java/org/openwes/ai/plugin/grounding/PluginGroundingService.java`
- Create: `server/modules-ai/ai-plugin/src/test/java/org/openwes/ai/plugin/grounding/PluginGroundingServiceTest.java`

**Interfaces:**
- Consumes: `AiPluginProperties` (Task 2), `AiPluginErrorDescEnum` (Task 3), `PluginContractCatalog` (Task 4).
- Produces:
  - `List<String> retrieve(String query)` — never throws; empty when disabled or unavailable.
  - `int refreshCorpus()` — deletes and re-adds the contract corpus; throws `WmsException(AI_CORPUS_UNAVAILABLE)` when no store is available.
  - `void deleteCorpus()`.

- [ ] **Step 1: Write the failing grounding test**

Create `PluginGroundingServiceTest.java`:

```java
package org.openwes.ai.plugin.grounding;

import org.junit.jupiter.api.Test;
import org.openwes.ai.plugin.config.AiPluginProperties;
import org.openwes.common.utils.exception.WmsException;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PluginGroundingServiceTest {

    private final ObjectProvider<VectorStore> provider = mock(ObjectProvider.class);
    private final VectorStore vectorStore = mock(VectorStore.class);
    private final AiPluginProperties properties = new AiPluginProperties();
    private final PluginGroundingService service = new PluginGroundingService(provider, properties);

    @Test
    void disabled_returnsEmptyWithoutTouchingTheStore() {
        properties.setGroundingEnabled(false);

        assertTrue(service.retrieve("waves").isEmpty());
        verify(provider, never()).getIfAvailable();
    }

    @Test
    void enabledWithoutAStore_returnsEmpty() {
        properties.setGroundingEnabled(true);
        when(provider.getIfAvailable()).thenReturn(null);

        assertTrue(service.retrieve("waves").isEmpty());
    }

    @Test
    void retrievalFailure_degradesToEmpty() {
        properties.setGroundingEnabled(true);
        when(provider.getIfAvailable()).thenReturn(vectorStore);
        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenThrow(new IllegalStateException("pinecone down"));

        assertTrue(service.retrieve("waves").isEmpty());
    }

    @Test
    void enabled_returnsTheTopKSnippets() {
        properties.setGroundingEnabled(true);
        properties.setGroundingTopK(3);
        when(provider.getIfAvailable()).thenReturn(vectorStore);
        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(new Document("first"), new Document("second")));

        assertEquals(List.of("first", "second"), service.retrieve("waves"));
    }

    @Test
    void refreshCorpus_replacesEveryContractDocument() {
        when(provider.getIfAvailable()).thenReturn(vectorStore);

        int count = service.refreshCorpus();

        assertEquals(12, count); // wes 9 archetypes + station 3 archetypes
        verify(vectorStore).delete(any(Filter.Expression.class));
        verify(vectorStore).add(anyList());
    }

    @Test
    void refreshCorpus_withoutAStore_failsLoudly() {
        when(provider.getIfAvailable()).thenReturn(null);

        assertThrows(WmsException.class, service::refreshCorpus);
    }

    @Test
    void deleteCorpus_withoutAStore_failsLoudly() {
        when(provider.getIfAvailable()).thenReturn(null);

        assertThrows(WmsException.class, service::deleteCorpus);
    }
}
```

- [ ] **Step 2: Run the test to verify it fails to compile**

```bash
cd D:/open-wes/server
export JAVA_HOME="${JAVA_HOME:-C:/Users/niekang/.jdks/ms-17.0.20.1}"
./gradlew :modules-ai:ai-plugin:test --tests "org.openwes.ai.plugin.grounding.PluginGroundingServiceTest"
```

Expected: FAIL — `PluginGroundingService` does not exist.

- [ ] **Step 3: Implement the grounding service**

```java
package org.openwes.ai.plugin.grounding;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.ai.plugin.config.AiPluginProperties;
import org.openwes.ai.plugin.exception.AiPluginErrorDescEnum;
import org.openwes.common.utils.exception.WmsException;
import org.openwes.script.plugin.api.ScriptPluginApiConstants;
import org.openwes.script.plugin.api.contract.PluginContractCatalog;
import org.openwes.script.plugin.api.enums.ExtensionType;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Optional retrieval grounding over the frozen plugin contract. A missing or broken VectorStore
 * must never block drafting; only an explicit corpus refresh fails, because that is an operator
 * action that cannot silently do nothing.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PluginGroundingService {

    static final String CORPUS_SOURCE = "script-plugin-contract";

    private final ObjectProvider<VectorStore> vectorStoreProvider;
    private final AiPluginProperties properties;

    public List<String> retrieve(String query) {
        if (!properties.isGroundingEnabled()) {
            return List.of();
        }
        VectorStore store = vectorStoreProvider.getIfAvailable();
        if (store == null) {
            log.warn("AI grounding is enabled but no VectorStore is configured; continuing without it");
            return List.of();
        }
        try {
            SearchRequest request = SearchRequest.builder()
                    .query(query)
                    .topK(properties.getGroundingTopK())
                    .similarityThresholdAll()
                    .build();
            return store.similaritySearch(request).stream().map(Document::getText).toList();
        } catch (RuntimeException e) {
            log.warn("AI grounding retrieval failed; continuing without it: {}", e.getMessage());
            return List.of();
        }
    }

    public int refreshCorpus() {
        VectorStore store = requireStore();
        store.delete(corpusFilter());
        List<Document> documents = corpusDocuments();
        store.add(documents);
        log.info("AI plugin contract corpus refreshed with {} documents", documents.size());
        return documents.size();
    }

    public void deleteCorpus() {
        requireStore().delete(corpusFilter());
    }

    List<Document> corpusDocuments() {
        List<Document> documents = new ArrayList<>();
        for (String app : ScriptPluginApiConstants.SUPPORTED_APPS) {
            for (ExtensionType type : PluginContractCatalog.typesForApp(app)) {
                String id = CORPUS_SOURCE + ":" + app + ":" + type.name();
                documents.add(new Document(id,
                        PluginContractCatalog.toPromptFragment(app, List.of(type)),
                        Map.of("source", CORPUS_SOURCE, "app", app, "type", type.name())));
            }
        }
        return documents;
    }

    private Filter.Expression corpusFilter() {
        return new FilterExpressionBuilder().eq("source", CORPUS_SOURCE).build();
    }

    private VectorStore requireStore() {
        VectorStore store = vectorStoreProvider.getIfAvailable();
        if (store == null) {
            throw new WmsException(AiPluginErrorDescEnum.AI_CORPUS_UNAVAILABLE);
        }
        return store;
    }
}
```

- [ ] **Step 4: Run the grounding test**

```bash
cd D:/open-wes/server
export JAVA_HOME="${JAVA_HOME:-C:/Users/niekang/.jdks/ms-17.0.20.1}"
./gradlew :modules-ai:ai-plugin:test --tests "org.openwes.ai.plugin.grounding.PluginGroundingServiceTest"
```

Expected: PASS (7 tests).

- [ ] **Step 5: Commit**

```bash
git add server/modules-ai/ai-plugin
git commit -m "feat(ai-plugin): add optional Pinecone contract grounding

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---
### Task 8: Orchestrate generation, streaming, retries and the AI revision

**Files:**
- Create: `server/modules-ai/ai-plugin/src/main/java/org/openwes/ai/plugin/dto/AiDraftRequest.java`
- Create: `server/modules-ai/ai-plugin/src/main/java/org/openwes/ai/plugin/dto/AiDraftResult.java`
- Create: `server/modules-ai/ai-plugin/src/main/java/org/openwes/ai/plugin/dto/AiDraftStreamEvent.java`
- Create: `server/modules-ai/ai-plugin/src/main/java/org/openwes/ai/plugin/domain/service/AiDraftGenerationService.java`
- Create: `server/modules-ai/ai-plugin/src/test/java/org/openwes/ai/plugin/domain/service/AiDraftGenerationServiceTest.java`

**Interfaces:**
- Consumes (all from Tasks 1–7):
  - `ScriptPluginService.detail`, `.revisions`, `.saveRevision(pluginKey, dto, AI, model, promptVersion)`
  - `AiSettingsService.current()`, `AiQuotaService.tryAcquire`, `AiGenerationAuditService.*`
  - `AiPromptAssembler.assemble`, `AiDraftDocumentParser.parse`, `AiDraftValidator.validate`
  - `PluginGroundingService.retrieve`, `ChatModel.stream(Prompt)`
- Produces:
  - `Flux<AiDraftStreamEvent> AiDraftGenerationService.stream(AiDraftRequest)` — throws `WmsException` synchronously for pre-flight failures.
  - `List<AiGenerationDTO> generations(String pluginKey)`.
  - `int refreshCorpus()`.
  - Event types: `chunk` (text), `stage` (`declarations` / `compiling` / `wiring` / `retry#N`), `result` (`AiDraftResult`), `error` (`code`, `text`).

- [ ] **Step 1: Write the failing orchestration test**

Create `AiDraftGenerationServiceTest.java`:

```java
package org.openwes.ai.plugin.domain.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openwes.ai.plugin.config.AiPluginProperties;
import org.openwes.ai.plugin.domain.entity.ScriptPluginAiGeneration;
import org.openwes.ai.plugin.domain.model.AiSettings;
import org.openwes.ai.plugin.dto.AiDraftRequest;
import org.openwes.ai.plugin.dto.AiDraftStreamEvent;
import org.openwes.ai.plugin.grounding.PluginGroundingService;
import org.openwes.ai.plugin.prompt.AiPromptAssembler;
import org.openwes.ai.plugin.prompt.PluginPromptTemplates;
import org.openwes.ai.plugin.validation.AiDraftValidator;
import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.user.UserContext;
import org.openwes.script.engine.GroovyScriptEngine;
import org.openwes.script.plugin.api.dto.ScriptPluginDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginRevisionDTO;
import org.openwes.script.plugin.api.enums.RevisionOriginEnum;
import org.openwes.script.plugin.api.enums.ScriptPluginStatusEnum;
import org.openwes.script.plugin.core.domain.service.ScriptPluginService;
import org.openwes.script.plugin.runtime.materialize.ScriptRevisionContextManager;
import org.openwes.script.plugin.runtime.validation.ScriptPluginMaterializationValidatorImpl;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiDraftGenerationServiceTest {

    private final ScriptPluginService scriptPluginService = mock(ScriptPluginService.class);
    private final AiSettingsService settingsService = mock(AiSettingsService.class);
    private final AiQuotaService quotaService = mock(AiQuotaService.class);
    private final AiGenerationAuditService auditService = mock(AiGenerationAuditService.class);
    private final PluginGroundingService groundingService = mock(PluginGroundingService.class);
    private final GroovyScriptEngine engine = new GroovyScriptEngine();
    private final AiPluginProperties properties = new AiPluginProperties();

    private AnnotationConfigApplicationContext parent;
    private AiDraftValidator validator;
    private AiDraftGenerationService service;

    @BeforeEach
    void setUp() {
        parent = new AnnotationConfigApplicationContext();
        parent.refresh();
        validator = new AiDraftValidator(engine, List.of(new ScriptPluginMaterializationValidatorImpl(
                engine, new ScriptRevisionContextManager(parent, engine), "wes")));
        properties.setModel("fake-model");

        when(settingsService.current()).thenReturn(new AiSettings(20, 4096, 3));
        when(quotaService.tryAcquire(anyString(), anyInt())).thenReturn(true);
        when(scriptPluginService.detail("packing-wave")).thenReturn(plugin());
        when(scriptPluginService.revisions("packing-wave")).thenReturn(List.of());
        when(groundingService.retrieve(anyString())).thenReturn(List.of());

        ScriptPluginAiGeneration audit = ScriptPluginAiGeneration.start(
                "packing-wave", "alice", "fake-model", PluginPromptTemplates.PROMPT_VERSION);
        audit.setId(1L);
        when(auditService.start(anyString(), anyString(), anyString(), anyString())).thenReturn(audit);

        when(scriptPluginService.saveRevision(anyString(), any(), eq(RevisionOriginEnum.AI),
                anyString(), anyString())).thenAnswer(invocation -> {
            ScriptPluginRevisionDTO dto = invocation.getArgument(1);
            dto.setId(55L);
            dto.setRevisionNo(4);
            return dto;
        });

        UserContext.setAccount("alice");
    }

    @AfterEach
    void tearDown() {
        UserContext.removeUser();
        parent.close();
    }

    private ScriptPluginDTO plugin() {
        ScriptPluginDTO plugin = new ScriptPluginDTO();
        plugin.setPluginKey("packing-wave");
        plugin.setName("Packing wave");
        plugin.setApplySystem("wes");
        plugin.setStatus(ScriptPluginStatusEnum.DRAFT);
        return plugin;
    }

    private AiDraftRequest request() {
        AiDraftRequest request = new AiDraftRequest();
        request.setPluginKey("packing-wave");
        request.setRequirement("group outbound orders into waves");
        return request;
    }

    private ChatModel modelReturning(String... outputs) {
        AtomicInteger index = new AtomicInteger();
        return new ChatModel() {
            @Override
            public ChatResponse call(Prompt prompt) {
                throw new UnsupportedOperationException("stream only");
            }

            @Override
            public Flux<ChatResponse> stream(Prompt prompt) {
                String output = outputs[Math.min(index.getAndIncrement(), outputs.length - 1)];
                return Flux.just(new ChatResponse(List.of(new Generation(new AssistantMessage(output)))));
            }
        };
    }

    private AiDraftGenerationService serviceWith(ChatModel chatModel) {
        return new AiDraftGenerationService(scriptPluginService, settingsService, quotaService,
                auditService, new AiPromptAssembler(properties), new AiDraftDocumentParser(),
                validator, groundingService, chatModel, properties);
    }

    private String invalidWaveDraft() {
        return """
                {"meta":{"name":"x","changeSummary":"wrong method"},
                 "extensions":[{"type":"OUTBOUND_WAVE_WAVE_ACTION","script":"Wave.groovy","method":"split"}],
                 "configSchema":[],"scripts":{"Wave.groovy":"class Wave { Object split(Object o, Object ctx) { return o } }"}}
                """;
    }

    private String validWaveDraft() {
        return """
                {"meta":{"name":"x","changeSummary":"waves of ten"},
                 "extensions":[{"type":"OUTBOUND_WAVE_WAVE_ACTION","script":"Wave.groovy","method":"wave"}],
                 "configSchema":[{"field":"maxPerWave","type":"INT","required":false,"defaultValue":"10"}],
                 "scripts":{"Wave.groovy":"class Wave { Object wave(Object orders, Object ctx) { return orders } }"}}
                """;
    }

    @Test
    void succeedsOnTheSecondAttemptAndSavesAnAiRevision() {
        List<AiDraftStreamEvent> events = serviceWith(modelReturning(invalidWaveDraft(), validWaveDraft()))
                .stream(request()).collectList().block(Duration.ofSeconds(10));

        assertNotNull(events);
        assertTrue(events.stream().anyMatch(e -> e.getType().equals("stage") && e.getText().equals("retry#2")));
        assertTrue(events.stream().anyMatch(e -> e.getType().equals("stage") && e.getText().equals("compiling")));
        assertTrue(events.stream().anyMatch(e -> e.getType().equals("stage") && e.getText().equals("wiring")));

        AiDraftStreamEvent last = events.get(events.size() - 1);
        assertEquals("result", last.getType());
        assertEquals(55L, last.getResult().getRevisionId());
        assertEquals(4, last.getResult().getRevisionNo());
        assertEquals(2, last.getResult().getAttempts());
        assertTrue(last.getResult().isMaterializationVerified());
        assertEquals("fake-model", last.getResult().getModel());
        assertEquals(PluginPromptTemplates.PROMPT_VERSION, last.getResult().getPromptVersion());

        verify(scriptPluginService).saveRevision(eq("packing-wave"), any(), eq(RevisionOriginEnum.AI),
                eq("fake-model"), eq(PluginPromptTemplates.PROMPT_VERSION));
        verify(scriptPluginService, never()).enable(anyString());
        verify(auditService).recordAttempt(1L, 1);
        verify(auditService).recordAttempt(1L, 2);
        verify(auditService).succeed(eq(1L), eq(2), isNull(), isNull(), anyLong());
    }

    @Test
    void retriesAtMostThreeTimesThenEmitsAnErrorEvent() {
        List<AiDraftStreamEvent> events = serviceWith(modelReturning("not a draft"))
                .stream(request()).collectList().block(Duration.ofSeconds(10));

        assertNotNull(events);
        AiDraftStreamEvent last = events.get(events.size() - 1);
        assertEquals("error", last.getType());
        assertEquals("PL001006", last.getCode());
        assertTrue(last.getText().contains("after 3 attempts"));
        verify(auditService).fail(eq(1L), eq(3), anyString(), anyLong());
        verify(scriptPluginService, never()).saveRevision(anyString(), any(), any(), anyString(), anyString());
    }

    @Test
    void quotaExhaustion_isRejectedBeforeTheStreamStarts() {
        when(quotaService.tryAcquire("alice", 20)).thenReturn(false);

        assertThrows(WmsException.class, () -> serviceWith(modelReturning(validWaveDraft())).stream(request()));
        verify(auditService, never()).start(anyString(), anyString(), anyString(), anyString());
        verify(scriptPluginService, never()).saveRevision(anyString(), any(), any(), anyString(), anyString());
    }

    @Test
    void enabledPlugins_areRejectedBeforeTheStreamStarts() {
        ScriptPluginDTO enabled = plugin();
        enabled.setStatus(ScriptPluginStatusEnum.ENABLED);
        when(scriptPluginService.detail("packing-wave")).thenReturn(enabled);

        assertThrows(WmsException.class, () -> serviceWith(modelReturning(validWaveDraft())).stream(request()));
        verify(auditService, never()).start(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void unknownBaseRevision_isRejectedBeforeTheStreamStarts() {
        AiDraftRequest request = request();
        request.setBaseRevisionId(999L);

        assertThrows(WmsException.class, () -> serviceWith(modelReturning(validWaveDraft())).stream(request()));
    }

    @Test
    void stopsRetryingWhenTheStreamBudgetIsExhausted() {
        properties.setStreamBudgetMs(0);

        List<AiDraftStreamEvent> events = serviceWith(modelReturning("not a draft"))
                .stream(request()).collectList().block(Duration.ofSeconds(10));

        assertNotNull(events);
        AiDraftStreamEvent last = events.get(events.size() - 1);
        assertEquals("error", last.getType());
        assertTrue(last.getText().contains("after 1 attempts"));
        verify(auditService).fail(eq(1L), eq(1), anyString(), anyLong());
    }

    @Test
    void modelCallFailure_isFedBackIntoTheRetryPrompt() {
        AtomicInteger calls = new AtomicInteger();
        ChatModel flaky = new ChatModel() {
            @Override
            public ChatResponse call(Prompt prompt) {
                throw new UnsupportedOperationException("stream only");
            }

            @Override
            public Flux<ChatResponse> stream(Prompt prompt) {
                if (calls.getAndIncrement() == 0) {
                    return Flux.error(new IllegalStateException("model unavailable"));
                }
                return Flux.just(new ChatResponse(List.of(new Generation(new AssistantMessage(validWaveDraft())))));
            }
        };

        List<AiDraftStreamEvent> events = serviceWith(flaky).stream(request())
                .collectList().block(Duration.ofSeconds(10));

        assertNotNull(events);
        assertEquals("result", events.get(events.size() - 1).getType());
        verify(auditService).succeed(eq(1L), eq(2), isNull(), isNull(), anyLong());
    }
}
```

- [ ] **Step 2: Run the test to verify it fails to compile**

```bash
cd D:/open-wes/server
export JAVA_HOME="${JAVA_HOME:-C:/Users/niekang/.jdks/ms-17.0.20.1}"
./gradlew :modules-ai:ai-plugin:test --tests "org.openwes.ai.plugin.domain.service.AiDraftGenerationServiceTest"
```

Expected: FAIL — the DTOs and `AiDraftGenerationService` do not exist.

- [ ] **Step 3: Implement the request, result and event DTOs**

`AiDraftRequest.java`:

```java
package org.openwes.ai.plugin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AiDraftRequest {

    @NotBlank
    @Size(max = 64)
    private String pluginKey;

    @NotBlank
    @Size(max = 4000)
    private String requirement;

    /** Optional revision whose scripts and declarations seed the prompt. */
    private Long baseRevisionId;
}
```

`AiDraftResult.java`:

```java
package org.openwes.ai.plugin.dto;

import lombok.Builder;
import lombok.Data;
import org.openwes.script.plugin.api.dto.ConfigFieldDTO;
import org.openwes.script.plugin.api.dto.ExtensionDeclarationDTO;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class AiDraftResult {
    private String pluginKey;
    private Long revisionId;
    private Integer revisionNo;
    private String changeSummary;
    private List<ExtensionDeclarationDTO> extensions;
    private List<ConfigFieldDTO> configSchema;
    private Map<String, String> scripts;
    private boolean materializationVerified;
    private int attempts;
    private String model;
    private String promptVersion;
}
```

`AiDraftStreamEvent.java`:

```java
package org.openwes.ai.plugin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Payload of one SSE event. {@code type} is mirrored in the SSE event name so clients can either
 * listen to named events or read the payload in a single onmessage handler.
 */
@Data
@AllArgsConstructor
public class AiDraftStreamEvent {

    private String type;
    private String text;
    private AiDraftResult result;
    private String code;

    public static AiDraftStreamEvent chunk(String text) {
        return new AiDraftStreamEvent("chunk", text, null, null);
    }

    public static AiDraftStreamEvent stage(String stage) {
        return new AiDraftStreamEvent("stage", stage, null, null);
    }

    public static AiDraftStreamEvent result(AiDraftResult result) {
        return new AiDraftStreamEvent("result", null, result, null);
    }

    public static AiDraftStreamEvent error(String code, String message) {
        return new AiDraftStreamEvent("error", message, null, code);
    }
}
```

- [ ] **Step 4: Implement `AiDraftGenerationService`**

Create the file with the dependencies in exactly this field order (the test uses the generated constructor):

```java
package org.openwes.ai.plugin.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openwes.ai.plugin.config.AiPluginProperties;
import org.openwes.ai.plugin.domain.entity.ScriptPluginAiGeneration;
import org.openwes.ai.plugin.domain.model.AiDraftDocument;
import org.openwes.ai.plugin.domain.model.AiSettings;
import org.openwes.ai.plugin.dto.AiDraftRequest;
import org.openwes.ai.plugin.dto.AiDraftResult;
import org.openwes.ai.plugin.dto.AiDraftStreamEvent;
import org.openwes.ai.plugin.dto.AiGenerationDTO;
import org.openwes.ai.plugin.exception.AiDraftParseException;
import org.openwes.ai.plugin.exception.AiPluginErrorDescEnum;
import org.openwes.ai.plugin.grounding.PluginGroundingService;
import org.openwes.ai.plugin.prompt.AiPromptAssembler;
import org.openwes.ai.plugin.prompt.PluginPromptTemplates;
import org.openwes.ai.plugin.validation.AiDraftValidator;
import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.user.UserContext;
import org.openwes.script.plugin.api.dto.ScriptPluginDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginRevisionDTO;
import org.openwes.script.plugin.api.enums.RevisionOriginEnum;
import org.openwes.script.plugin.api.enums.ScriptPluginStatusEnum;
import org.openwes.script.plugin.core.domain.service.ScriptPluginService;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiDraftGenerationService {

    private final ScriptPluginService scriptPluginService;
    private final AiSettingsService settingsService;
    private final AiQuotaService quotaService;
    private final AiGenerationAuditService auditService;
    private final AiPromptAssembler promptAssembler;
    private final AiDraftDocumentParser parser;
    private final AiDraftValidator validator;
    private final PluginGroundingService groundingService;
    private final ChatModel chatModel;
    private final AiPluginProperties properties;

    public Flux<AiDraftStreamEvent> stream(AiDraftRequest request) {
        String user = StringUtils.defaultIfBlank(UserContext.getCurrentUser(), "anonymous");
        ScriptPluginDTO plugin = scriptPluginService.detail(request.getPluginKey());
        if (plugin.getStatus() != ScriptPluginStatusEnum.DRAFT
                && plugin.getStatus() != ScriptPluginStatusEnum.DISABLED) {
            throw new WmsException(AiPluginErrorDescEnum.AI_PLUGIN_NOT_GENERATABLE,
                    new Object[]{plugin.getPluginKey(), plugin.getStatus()});
        }
        ScriptPluginRevisionDTO baseRevision = resolveBaseRevision(plugin.getPluginKey(),
                request.getBaseRevisionId());

        AiSettings settings = settingsService.current();
        if (!quotaService.tryAcquire(user, settings.dailyGenerationLimit())) {
            throw new WmsException(AiPluginErrorDescEnum.AI_QUOTA_EXCEEDED,
                    new Object[]{settings.dailyGenerationLimit(), user});
        }
        int maxTokens = Math.min(settings.maxTokensPerCall(), properties.getMaxTokensCeiling());

        ScriptPluginAiGeneration audit = auditService.start(plugin.getPluginKey(), user,
                properties.getModel(), PluginPromptTemplates.PROMPT_VERSION);
        GenerationContext context = new GenerationContext(request.getRequirement(), plugin, baseRevision,
                settings, maxTokens, audit.getId());

        Sinks.Many<AiDraftStreamEvent> sink = Sinks.many().unicast().onBackpressureBuffer();
        AtomicBoolean terminal = new AtomicBoolean(false);
        Disposable worker = Schedulers.boundedElastic().schedule(() -> {
            // The worker runs on a pooled thread. Rebind the acting user so JPA auditing
            // (AuditorAwareImpl reads UserContext) records the same user on the AI revision.
            UserContext.setAccount(user);
            try {
                run(context, sink, terminal);
            } finally {
                UserContext.removeUser();
            }
        });

        return sink.asFlux().doOnCancel(() -> {
            if (terminal.compareAndSet(false, true)) {
                auditService.cancel(context.auditId, context.attempts.get(), context.elapsed());
            }
            worker.dispose();
        });
    }

    public List<AiGenerationDTO> generations(String pluginKey) {
        return auditService.recent(pluginKey);
    }

    public int refreshCorpus() {
        return groundingService.refreshCorpus();
    }

    private ScriptPluginRevisionDTO resolveBaseRevision(String pluginKey, Long revisionId) {
        if (revisionId == null) {
            return null;
        }
        return scriptPluginService.revisions(pluginKey).stream()
                .filter(revision -> revisionId.equals(revision.getId()))
                .findFirst()
                .orElseThrow(() -> new WmsException(AiPluginErrorDescEnum.AI_BASE_REVISION_NOT_FOUND,
                        new Object[]{revisionId, pluginKey}));
    }

    private void run(GenerationContext context, Sinks.Many<AiDraftStreamEvent> sink, AtomicBoolean terminal) {
        List<String> previousErrors = new ArrayList<>();
        try {
            List<String> grounding = groundingService.retrieve(context.requirement);
            for (int attempt = 1; attempt <= context.settings.maxAttempts(); attempt++) {
                if (terminal.get()) {
                    return;
                }
                if (attempt > 1 && context.elapsed() + properties.getRequestTimeoutMs()
                        > properties.getStreamBudgetMs()) {
                    previousErrors.add("stream budget exhausted before attempt " + attempt);
                    break;
                }
                context.attempts.set(attempt);
                auditService.recordAttempt(context.auditId, attempt);
                if (attempt > 1 && !emit(sink, AiDraftStreamEvent.stage("retry#" + attempt), terminal)) {
                    return;
                }

                String content;
                try {
                    content = callModel(context, grounding, previousErrors, sink, terminal);
                } catch (RuntimeException e) {
                    if (terminal.get()) {
                        log.debug("AI draft stream for {} cancelled", context.plugin.getPluginKey());
                        return;
                    }
                    previousErrors.add("model call: " + message(e));
                    continue;
                }
                if (terminal.get()) {
                    return;
                }

                AiDraftDocument document;
                try {
                    document = parser.parse(content, context.plugin.getApplySystem());
                } catch (AiDraftParseException e) {
                    previousErrors.add(e.getMessage());
                    continue;
                }

                AiDraftValidator.Result validation = validator.validate(context.plugin, document,
                        stage -> emit(sink, AiDraftStreamEvent.stage(stage), terminal));
                if (terminal.get()) {
                    return;
                }
                if (!validation.passed()) {
                    previousErrors.addAll(validation.failures().stream()
                            .map(failure -> failure.getStage() + ": " + failure.getMessage()).toList());
                    continue;
                }

                ScriptPluginRevisionDTO saved = save(context, document);
                if (terminal.get()) {
                    return;
                }
                terminal.set(true);
                auditService.succeed(context.auditId, attempt, context.promptTokens,
                        context.completionTokens, context.elapsed());
                emit(sink, AiDraftStreamEvent.result(toResult(saved, context,
                        validation.materializationVerified(), attempt)), terminal);
                sink.tryEmitComplete();
                return;
            }

            String lastError = previousErrors.isEmpty()
                    ? "model produced no usable draft" : previousErrors.get(previousErrors.size() - 1);
            terminal.set(true);
            auditService.fail(context.auditId, context.attempts.get(), String.join("\n", previousErrors),
                    context.elapsed());
            emit(sink, AiDraftStreamEvent.error(AiPluginErrorDescEnum.AI_GENERATION_FAILED.getCode(),
                    "AI draft generation failed after " + context.attempts.get() + " attempts: " + lastError),
                    terminal);
            sink.tryEmitComplete();
        } catch (RuntimeException e) {
            if (terminal.get()) {
                log.debug("AI draft stream for {} cancelled", context.plugin.getPluginKey());
                return;
            }
            log.error("AI draft generation for plugin {} failed unexpectedly",
                    context.plugin.getPluginKey(), e);
            terminal.set(true);
            auditService.fail(context.auditId, context.attempts.get(), message(e), context.elapsed());
            emit(sink, AiDraftStreamEvent.error(AiPluginErrorDescEnum.AI_INTERNAL_ERROR.getCode(),
                    "AI draft generation failed: " + message(e)), terminal);
            sink.tryEmitComplete();
        }
    }

    private String callModel(GenerationContext context, List<String> grounding, List<String> previousErrors,
                             Sinks.Many<AiDraftStreamEvent> sink, AtomicBoolean terminal) {
        Prompt prompt = promptAssembler.assemble(context.requirement, context.plugin, context.baseRevision,
                grounding, previousErrors, context.maxTokens);
        StringBuilder content = new StringBuilder();
        Flux<ChatResponse> responses = chatModel.stream(prompt)
                .timeout(Duration.ofMillis(properties.getRequestTimeoutMs()));

        for (ChatResponse response : responses.toIterable()) {
            if (terminal.get()) {
                return content.toString();
            }
            String text = extractText(response);
            if (!text.isEmpty()) {
                content.append(text);
                if (!emit(sink, AiDraftStreamEvent.chunk(text), terminal)) {
                    return content.toString();
                }
            }
            recordUsage(context, response);
        }
        return content.toString();
    }

    private String extractText(ChatResponse response) {
        if (response == null || response.getResult() == null || response.getResult().getOutput() == null) {
            return "";
        }
        String text = response.getResult().getOutput().getText();
        return text == null ? "" : text;
    }

    private void recordUsage(GenerationContext context, ChatResponse response) {
        if (response.getMetadata() == null || response.getMetadata().getUsage() == null) {
            return;
        }
        Usage usage = response.getMetadata().getUsage();
        if (usage.getPromptTokens() != null) {
            context.promptTokens = usage.getPromptTokens();
        }
        if (usage.getCompletionTokens() != null) {
            context.completionTokens = usage.getCompletionTokens();
        }
    }

    private ScriptPluginRevisionDTO save(GenerationContext context, AiDraftDocument document) {
        ScriptPluginRevisionDTO revision = new ScriptPluginRevisionDTO();
        revision.setScripts(document.getScripts());
        revision.setExtensionPoints(document.getExtensions());
        revision.setConfigSchema(document.getConfigSchema());
        revision.setChangeSummary(document.getMeta().getChangeSummary());
        return scriptPluginService.saveRevision(context.plugin.getPluginKey(), revision,
                RevisionOriginEnum.AI, properties.getModel(), PluginPromptTemplates.PROMPT_VERSION);
    }

    private AiDraftResult toResult(ScriptPluginRevisionDTO revision, GenerationContext context,
                                   boolean materializationVerified, int attempts) {
        return AiDraftResult.builder()
                .pluginKey(context.plugin.getPluginKey())
                .revisionId(revision.getId())
                .revisionNo(revision.getRevisionNo())
                .changeSummary(revision.getChangeSummary())
                .extensions(revision.getExtensionPoints())
                .configSchema(revision.getConfigSchema())
                .scripts(revision.getScripts())
                .materializationVerified(materializationVerified)
                .attempts(attempts)
                .model(properties.getModel())
                .promptVersion(PluginPromptTemplates.PROMPT_VERSION)
                .build();
    }

    private boolean emit(Sinks.Many<AiDraftStreamEvent> sink, AiDraftStreamEvent event,
                         AtomicBoolean terminal) {
        Sinks.EmitResult result = sink.tryEmitNext(event);
        if (!result.isSuccess()) {
            terminal.set(true);
            log.debug("stopped emitting AI draft events: {}", result);
            return false;
        }
        return true;
    }

    private String message(Throwable error) {
        return error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
    }

    private static final class GenerationContext {
        private final String requirement;
        private final ScriptPluginDTO plugin;
        private final ScriptPluginRevisionDTO baseRevision;
        private final AiSettings settings;
        private final int maxTokens;
        private final Long auditId;
        private final AtomicInteger attempts = new AtomicInteger();
        private final long startedAt = System.currentTimeMillis();
        private volatile Integer promptTokens;
        private volatile Integer completionTokens;

        private GenerationContext(String requirement, ScriptPluginDTO plugin,
                                  ScriptPluginRevisionDTO baseRevision, AiSettings settings,
                                  int maxTokens, Long auditId) {
            this.requirement = requirement;
            this.plugin = plugin;
            this.baseRevision = baseRevision;
            this.settings = settings;
            this.maxTokens = maxTokens;
            this.auditId = auditId;
        }

        private long elapsed() {
            return System.currentTimeMillis() - startedAt;
        }
    }
}
```

- [ ] **Step 5: Run the orchestration test**

```bash
cd D:/open-wes/server
export JAVA_HOME="${JAVA_HOME:-C:/Users/niekang/.jdks/ms-17.0.20.1}"
./gradlew :modules-ai:ai-plugin:test --tests "org.openwes.ai.plugin.domain.service.AiDraftGenerationServiceTest"
```

Expected: PASS (7 tests). If `modelCallFailure_isFedBackIntoTheRetryPrompt` is flaky, it is a real bug in retry accounting — do not loosen the assertion.

- [ ] **Step 6: Commit**

```bash
git add server/modules-ai/ai-plugin
git commit -m "feat(ai-plugin): stream drafts with validation retries and audit

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 9: Expose the SSE endpoint, audit history and corpus refresh

**Files:**
- Create: `server/modules-ai/ai-plugin/src/main/java/org/openwes/ai/plugin/controller/AiDraftController.java`
- Create: `server/modules-ai/ai-plugin/src/test/java/org/openwes/ai/plugin/controller/AiDraftControllerTest.java`

**Interfaces:**
- Consumes: `AiDraftGenerationService` (Task 8), `AiDraftRequest`, `AiDraftStreamEvent`, `AiDraftResult`, `AiGenerationDTO`.
- Produces:
  - `POST plugin-platform/ai/draft/stream` → `text/event-stream`; SSE event names `chunk`, `stage`, `result`, `error`.
  - `GET plugin-platform/ai/generations/{pluginKey}` → `Response<List<AiGenerationDTO>>`.
  - `POST plugin-platform/ai/corpus/refresh` → `Response<Integer>`.

- [ ] **Step 1: Write the failing controller test**

Create `AiDraftControllerTest.java`:

```java
package org.openwes.ai.plugin.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openwes.ai.plugin.domain.service.AiDraftGenerationService;
import org.openwes.ai.plugin.dto.AiDraftResult;
import org.openwes.ai.plugin.dto.AiDraftStreamEvent;
import org.openwes.ai.plugin.dto.AiGenerationDTO;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AiDraftControllerTest {

    private AiDraftGenerationService generationService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        generationService = mock(AiDraftGenerationService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new AiDraftController(generationService)).build();
    }

    @Test
    void stream_writesNamedSseEventsWithJsonPayloads() throws Exception {
        AiDraftResult result = AiDraftResult.builder().pluginKey("packing-wave").revisionId(55L).build();
        when(generationService.stream(any())).thenReturn(Flux.just(
                AiDraftStreamEvent.chunk("hello"),
                AiDraftStreamEvent.stage("declarations"),
                AiDraftStreamEvent.result(result)));

        MvcResult started = mockMvc.perform(post("/plugin-platform/ai/draft/stream")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pluginKey\":\"packing-wave\",\"requirement\":\"group orders\"}"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(started))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andExpect(content().string(containsString("event:chunk")))
                .andExpect(content().string(containsString("event:stage")))
                .andExpect(content().string(containsString("event:result")))
                .andExpect(content().string(containsString("\"text\":\"hello\"")))
                .andExpect(content().string(containsString("\"revisionId\":55")));
    }

    @Test
    void stream_rejectsABlankRequirement() throws Exception {
        mockMvc.perform(post("/plugin-platform/ai/draft/stream")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pluginKey\":\"packing-wave\",\"requirement\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generations_returnsTheAuditView() throws Exception {
        when(generationService.generations("packing-wave")).thenReturn(List.of(
                AiGenerationDTO.builder().id(9L).pluginKey("packing-wave").status("SUCCEEDED").attempts(2).build()));

        mockMvc.perform(get("/plugin-platform/ai/generations/packing-wave"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0"))
                .andExpect(jsonPath("$.data[0].id").value(9))
                .andExpect(jsonPath("$.data[0].status").value("SUCCEEDED"))
                .andExpect(jsonPath("$.data[0].attempts").value(2));
    }

    @Test
    void refreshCorpus_returnsTheDocumentCount() throws Exception {
        when(generationService.refreshCorpus()).thenReturn(12);

        mockMvc.perform(post("/plugin-platform/ai/corpus/refresh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(12));
    }
}
```

- [ ] **Step 2: Run the test to verify it fails to compile**

```bash
cd D:/open-wes/server
export JAVA_HOME="${JAVA_HOME:-C:/Users/niekang/.jdks/ms-17.0.20.1}"
./gradlew :modules-ai:ai-plugin:test --tests "org.openwes.ai.plugin.controller.AiDraftControllerTest"
```

Expected: FAIL — `AiDraftController` does not exist.

- [ ] **Step 3: Implement the controller**

```java
package org.openwes.ai.plugin.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.openwes.ai.plugin.domain.service.AiDraftGenerationService;
import org.openwes.ai.plugin.dto.AiDraftRequest;
import org.openwes.ai.plugin.dto.AiDraftStreamEvent;
import org.openwes.ai.plugin.dto.AiGenerationDTO;
import org.openwes.common.utils.http.Response;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * AI draft surface. The SSE endpoint is intentionally not wrapped in {@code Response}: the stream
 * carries its own result/error events. Authentication is the gateway JWT today; role checks land
 * with the workbench in Phase 3.
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("plugin-platform/ai")
@Tag(name = "Script Plugin AI Api")
public class AiDraftController {

    private final AiDraftGenerationService generationService;

    @PostMapping(value = "/draft/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<AiDraftStreamEvent>> stream(@Valid @RequestBody AiDraftRequest request) {
        return generationService.stream(request)
                .map(event -> ServerSentEvent.<AiDraftStreamEvent>builder(event)
                        .event(event.getType())
                        .build());
    }

    @GetMapping("/generations/{pluginKey}")
    public Response<List<AiGenerationDTO>> generations(@PathVariable("pluginKey") String pluginKey) {
        return Response.success(generationService.generations(pluginKey));
    }

    @PostMapping("/corpus/refresh")
    public Response<Integer> refreshCorpus() {
        return Response.success(generationService.refreshCorpus());
    }
}
```

- [ ] **Step 4: Run the controller test**

```bash
cd D:/open-wes/server
export JAVA_HOME="${JAVA_HOME:-C:/Users/niekang/.jdks/ms-17.0.20.1}"
./gradlew :modules-ai:ai-plugin:test --tests "org.openwes.ai.plugin.controller.AiDraftControllerTest"
```

Expected: PASS (4 tests). If `asyncDispatch` returns an empty body, confirm the `produces` attribute is present on `stream`; without it Spring MVC may pick a non-streaming converter.

Client note: callers should send `Accept: text/event-stream, application/json`. Pre-flight failures (`WmsException` for quota, status, or a foreign base revision) are thrown before async dispatch and resolved by the global exception handler into the project's JSON error body with the `PLxxxxxx` code in `status`; a bare `text/event-stream` Accept header would make that error response fail content negotiation with 406.

- [ ] **Step 5: Commit**

```bash
git add server/modules-ai/ai-plugin
git commit -m "feat(ai-plugin): expose draft streaming and generation history

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 10: Wire into wes-server, document, and run the full verification

**Files:**
- Modify: `server/server/wes-server/build.gradle`
- Modify: `server/server/wes-server/src/main/resources/bootstrap.yml`
- Modify: `docs/standards/backend.md`
- Modify: `CLAUDE.md`
- Create: `server/modules-ai/ai-plugin/src/test/java/org/openwes/ai/plugin/AiDraftPipelineEndToEndTest.java`

**Interfaces:**
- Consumes: every prior task.
- Produces: the assembled `wes-server` application and the documented operating contract.

- [ ] **Step 1: Add the module to `wes-server`**

In `server/server/wes-server/build.gradle`, next to the existing `:modules-ai:ai-core` line, add:

```gradle
    implementation project(path: ":modules-ai:ai-plugin", configuration: 'default')
```

- [ ] **Step 2: Raise the MVC async timeout for SSE**

In `server/server/wes-server/src/main/resources/bootstrap.yml`, under `spring:` (same indentation as `application:`), add:

```yaml
  mvc:
    async:
      request-timeout: 180000
```

This is longer than `ai-plugin.request-timeout-ms` (120 s) and `ai-plugin.stream-budget-ms` (170 s), so the model timeout and the retry budget both win and a clean `error` event reaches the client. If an operator raises either AI value, raise this timeout above `stream-budget-ms` as well.

- [ ] **Step 3: Write the end-to-end pipeline test with a fake model**

Create `AiDraftPipelineEndToEndTest.java`. This wires the real parser, validator, materializer, prompt assembler and generation service, with a scripted `ChatModel`:

```java
package org.openwes.ai.plugin;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openwes.ai.plugin.config.AiPluginProperties;
import org.openwes.ai.plugin.domain.entity.ScriptPluginAiGeneration;
import org.openwes.ai.plugin.domain.model.AiSettings;
import org.openwes.ai.plugin.domain.service.AiDraftDocumentParser;
import org.openwes.ai.plugin.domain.service.AiDraftGenerationService;
import org.openwes.ai.plugin.domain.service.AiGenerationAuditService;
import org.openwes.ai.plugin.domain.service.AiQuotaService;
import org.openwes.ai.plugin.domain.service.AiSettingsService;
import org.openwes.ai.plugin.dto.AiDraftRequest;
import org.openwes.ai.plugin.dto.AiDraftStreamEvent;
import org.openwes.ai.plugin.grounding.PluginGroundingService;
import org.openwes.ai.plugin.prompt.AiPromptAssembler;
import org.openwes.ai.plugin.prompt.PluginPromptTemplates;
import org.openwes.ai.plugin.validation.AiDraftValidator;
import org.openwes.common.utils.user.UserContext;
import org.openwes.script.engine.GroovyScriptEngine;
import org.openwes.script.plugin.api.dto.ScriptPluginDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginRevisionDTO;
import org.openwes.script.plugin.api.enums.RevisionOriginEnum;
import org.openwes.script.plugin.api.enums.ScriptPluginStatusEnum;
import org.openwes.script.plugin.core.domain.service.ScriptPluginService;
import org.openwes.script.plugin.runtime.materialize.ScriptRevisionContextManager;
import org.openwes.script.plugin.runtime.validation.ScriptPluginMaterializationValidatorImpl;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiDraftPipelineEndToEndTest {

    private final ScriptPluginService scriptPluginService = mock(ScriptPluginService.class);
    private final AiSettingsService settingsService = mock(AiSettingsService.class);
    private final AiQuotaService quotaService = mock(AiQuotaService.class);
    private final AiGenerationAuditService auditService = mock(AiGenerationAuditService.class);
    private final PluginGroundingService groundingService = mock(PluginGroundingService.class);
    private final GroovyScriptEngine engine = new GroovyScriptEngine();
    private final AiPluginProperties properties = new AiPluginProperties();
    private AnnotationConfigApplicationContext parent;

    @BeforeEach
    void setUp() {
        parent = new AnnotationConfigApplicationContext();
        parent.refresh();
        properties.setModel("fake-model");
        UserContext.setAccount("alice");

        when(settingsService.current()).thenReturn(new AiSettings(20, 4096, 3));
        when(quotaService.tryAcquire(anyString(), anyInt())).thenReturn(true);
        when(groundingService.retrieve(anyString())).thenReturn(List.of());

        ScriptPluginDTO plugin = new ScriptPluginDTO();
        plugin.setPluginKey("packing-wave");
        plugin.setName("Packing wave");
        plugin.setApplySystem("wes");
        plugin.setStatus(ScriptPluginStatusEnum.DRAFT);
        when(scriptPluginService.detail("packing-wave")).thenReturn(plugin);
        when(scriptPluginService.revisions("packing-wave")).thenReturn(List.of());

        ScriptPluginAiGeneration audit = ScriptPluginAiGeneration.start(
                "packing-wave", "alice", "fake-model", PluginPromptTemplates.PROMPT_VERSION);
        audit.setId(1L);
        when(auditService.start(anyString(), anyString(), anyString(), anyString())).thenReturn(audit);
        when(scriptPluginService.saveRevision(anyString(), any(), eq(RevisionOriginEnum.AI),
                anyString(), anyString())).thenAnswer(invocation -> {
            ScriptPluginRevisionDTO dto = invocation.getArgument(1);
            dto.setId(55L);
            dto.setRevisionNo(4);
            return dto;
        });
    }

    @AfterEach
    void tearDown() {
        UserContext.removeUser();
        parent.close();
    }

    @Test
    void invalidThenValidModelOutput_savesOneAiRevisionAndNeverEnablesIt() {
        String invalid = """
                {"meta":{"name":"x","changeSummary":"wrong"},
                 "extensions":[{"type":"OUTBOUND_WAVE_WAVE_ACTION","script":"Wave.groovy","method":"split"}],
                 "configSchema":[],"scripts":{"Wave.groovy":"class Wave { Object split(Object o, Object ctx) { return o } }"}}
                """;
        String valid = """
                {"meta":{"name":"x","changeSummary":"waves"},
                 "extensions":[{"type":"OUTBOUND_WAVE_WAVE_ACTION","script":"Wave.groovy","method":"wave"}],
                 "configSchema":[],"scripts":{"Wave.groovy":"class Wave { Object wave(Object orders, Object ctx) { return orders } }"}}
                """;
        AtomicInteger index = new AtomicInteger();
        ChatModel model = new ChatModel() {
            @Override
            public ChatResponse call(Prompt prompt) {
                throw new UnsupportedOperationException("stream only");
            }

            @Override
            public Flux<ChatResponse> stream(Prompt prompt) {
                String output = index.getAndIncrement() == 0 ? invalid : valid;
                return Flux.just(new ChatResponse(List.of(new Generation(new AssistantMessage(output)))));
            }
        };

        AiDraftValidator validator = new AiDraftValidator(engine, List.of(
                new ScriptPluginMaterializationValidatorImpl(engine,
                        new ScriptRevisionContextManager(parent, engine), "wes")));
        AiDraftGenerationService service = new AiDraftGenerationService(scriptPluginService, settingsService,
                quotaService, auditService, new AiPromptAssembler(properties), new AiDraftDocumentParser(),
                validator, groundingService, model, properties);

        AiDraftRequest request = new AiDraftRequest();
        request.setPluginKey("packing-wave");
        request.setRequirement("group outbound orders into waves");

        List<AiDraftStreamEvent> events = service.stream(request).collectList().block(Duration.ofSeconds(10));

        assertTrue(events != null && events.stream()
                .anyMatch(event -> "stage".equals(event.getType()) && "retry#2".equals(event.getText())));
        AiDraftStreamEvent result = events.get(events.size() - 1);
        assertEquals("result", result.getType());
        assertEquals(55L, result.getResult().getRevisionId());
        assertTrue(result.getResult().isMaterializationVerified());
        assertEquals(Map.of("Wave.groovy",
                        "class Wave { Object wave(Object orders, Object ctx) { return orders } }"),
                result.getResult().getScripts());
        verify(scriptPluginService).saveRevision(eq("packing-wave"), any(), eq(RevisionOriginEnum.AI),
                eq("fake-model"), eq(PluginPromptTemplates.PROMPT_VERSION));
        verify(scriptPluginService, never()).enable(anyString());
    }
}
```

- [ ] **Step 4: Document the pipeline in `docs/standards/backend.md`**

Find the "Script Plugin Rules" list and append:

```markdown
11. **AI draft pipeline (`ai-plugin`)** — AI may only save revisions, never enable, disable, roll back or archive. Model output is untrusted: it is parsed into `AiDraftDocument`, checked against `PluginContractCatalog.typesForApp`, then run through the same static validation as enable-time; on wes it also gets the in-process materialization dry-run, while station drafts are static-only in v2 (preview handshake lands with the workbench). Every AI revision stores `RevisionOriginEnum.AI`, the model id and `PluginPromptTemplates.PROMPT_VERSION`. Never log requirements, prompts, model output or generated sources at info level. Generated scripts must read secrets through `ctx.config`, never hardcode them; prompts state the same rule. The SSE endpoint is `/plugin-platform/ai/draft/stream`; the gateway and any reverse proxy must disable response buffering for it.
```

- [ ] **Step 5: Update `CLAUDE.md`**

1. In the module list, change:
   ```
   modules-ai/           # AI integration (ai-core, ai-api)
   ```
   to:
   ```
   modules-ai/           # AI integration (ai-core, ai-api, ai-plugin — script plugin drafts)
   ```
2. In the Key File Locations table, add:
   ```markdown
   | Script plugin AI pipeline | `server/modules-ai/ai-plugin/` |
   ```

- [ ] **Step 6: Run every affected test suite**

```bash
cd D:/open-wes/server
export JAVA_HOME="${JAVA_HOME:-C:/Users/niekang/.jdks/ms-17.0.20.1}"
./gradlew :modules-plugin:script-plugin-api:test \
  :modules-plugin:script-plugin-runtime:test \
  :modules-ai:ai-plugin:test
```

Expected: PASS. The totals in `ai-plugin` are 3 + 7 + 6 + 11 + 6 + 7 + 7 + 4 + 1 = 52 tests; the two script-plugin modules run their existing suites plus the 10 new static-validator tests.

- [ ] **Step 7: Compile the assembled server**

```bash
cd D:/open-wes/server
export JAVA_HOME="${JAVA_HOME:-C:/Users/niekang/.jdks/ms-17.0.20.1}"
./gradlew :server:wes-server:compileJava
```

Expected: BUILD SUCCESSFUL. This proves component scan, JPA repositories and the new module's transitive dependencies resolve on the real server classpath.

- [ ] **Step 8: Run the full backend build**

```bash
cd D:/open-wes/server
export JAVA_HOME="${JAVA_HOME:-C:/Users/niekang/.jdks/ms-17.0.20.1}"
./gradlew build
```

Expected: BUILD SUCCESSFUL. If an unrelated module fails, capture the failing module/test and report it rather than silently excluding it.

- [ ] **Step 9: Record the manual acceptance steps (cannot run on this host)**

Write the following into the commit message body or the plan ledger; do **not** claim it passed:

```text
Manual acceptance for Phase 2 (requires MySQL+Redis, a real ZhipuAI API key and a DRAFT plugin):
1. Start wes-server (spring.ai.zhipuai.api-key set, script-plugin.registry.mode=local).
2. curl -N -X POST http://localhost:9010/plugin-platform/ai/draft/stream \
     -H 'Content-Type: application/json' \
     -d '{"pluginKey":"packing-wave","requirement":"Group outbound orders into waves of at most 12, grouping by carrier"}'
   Expect SSE: chunk*, stage declarations, stage compiling, stage wiring, result with revisionId.
3. GET /plugin-platform/scriptPlugin/packing-wave/revisions
   Expect the new revision with origin=AI, aiModel=GLM-4-air, promptVersion=plugin-draft-v1.
4. POST /plugin-platform/scriptPlugin/packing-wave/enable must still succeed only after a manual
   validate; the AI call itself must never change the plugin status.
5. GET /plugin-platform/ai/generations/packing-wave shows the audit row with status SUCCEEDED and usage.
6. Repeat until the daily limit returns the quota error; POST /plugin-platform/ai/corpus/refresh
   returns 12 when Pinecone is configured.
```

- [ ] **Step 10: Commit**

```bash
git add server/server/wes-server server/modules-ai/ai-plugin docs/standards/backend.md CLAUDE.md
git commit -m "feat(ai-plugin): wire the AI draft pipeline into wes-server

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

## Release gates carried from Phase 1b

These are **not** Phase 2 work, but they block merging/releasing the branch:

- [ ] **Manual Phase 1b E2E was never executed** — Docker is not installed on this host. Run the Phase 1b plan's E2E (MySQL + Redis + wes-server; create/list/revision/validate/enable) before merge.
- [ ] **Rotate the leaked Pinecone key** — it was externalized to `${PINECONE_API_KEY:}` in `55a8412` but remains in git history. Rotation is still pending; see `C:\Users\niekang\.claude\projects\D--open-wes\memory\leaked-credentials-rotation-pending.md`.
- [ ] **Phase 2 manual acceptance** — Step 9 above requires a real model key and infrastructure; it cannot be run in this environment.

## Phase 2 acceptance mapping

| Spec requirement | Implemented by |
|---|---|
| Prompt assembly from the frozen contract | Task 4 (`PluginPromptTemplates`, `AiPromptAssembler`) |
| Structured draft output | Task 5 (`AiDraftDocument`, parser) |
| Static + materialization validation before save | Tasks 1, 6 (`ScriptPluginStaticValidator`, `AiDraftValidator`) |
| Retry ≤ 3 with error feedback | Task 8 (`AiSettings` cap, retry loop) |
| SSE streaming + stage events | Tasks 8, 9 (`AiDraftGenerationService`, `AiDraftController`) |
| Per-user daily quota | Tasks 2, 3 (`p_script_plugin_ai_setting`, `AiQuotaService`) |
| Generation audit | Task 3 (`p_script_plugin_ai_generation`, `AiGenerationAuditService`) |
| Pinecone grounding (optional) | Task 7 (`PluginGroundingService`) |
| AI revision provenance, never enable | Task 8 (`RevisionOriginEnum.AI`, model, prompt version; no enable call) |
| Station static-only validation | Task 6 (`materializationVerified=false`) |
| Operator docs | Task 10 (`backend.md`, `CLAUDE.md`) |
