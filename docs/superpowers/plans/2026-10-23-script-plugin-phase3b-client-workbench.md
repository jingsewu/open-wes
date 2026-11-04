# Script Plugin Phase 3B — Client Workbench Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the script-plugin workbench client: an AMIS plugin list, a custom React plugin detail workspace (scripts, revisions, config, test-run, executions, AI history, audit, chat), and a new-plugin wizard backed by the Phase 3A APIs.

**Architecture:** One exact workbench route (`/plugin-platform/plugin-workbench`) carries `?mode=new` or `?pluginKey=...`; the component reacts to query-only navigation because `RouterGuard`/`KeepAlive` key cached pages by pathname only. AMIS renders the two read-only, menu-backed list pages; custom React + antd renders the wizard, detail tabs and AI chat drawer. A small typed API layer unwraps the `Response<T>` envelope and tolerates the HTTP-200 business-error envelope.

**Tech Stack:** React 17.0.2, TypeScript 5.9, antd 4.24, AMIS 6.x (`editor`, `diff-editor`, `schema2component`), MobX stores (untouched), dayjs, axe-free fetch/`ReadableStream` for SSE, i18next.

**Spec:** `docs/superpowers/specs/2026-10-22-script-plugin-phase3-workbench-design.md`

## Global Constraints

- Work only under `client/`. Phase 3A backend code is complete; do not modify `server/` or `monitoring/`.
- Do not run `npm install`. The installed `node_modules` is already pinned (webpack 5.101.3); a fresh install re-resolves it and breaks the build.
- Typecheck with `cd client && npx tsc --noEmit --skipLibCheck`. The baseline has exactly four pre-existing errors: `src/pages/wms/station/index.tsx(152,46)`, `(152,50)`, `(152,68)` and `src/utils/polyfill.tsx(13,27)`. A task is clean when those four are the only errors and none mention `plugin_platform`.
- Build with `cd client && npm run build`; the baseline exits 0. First build takes minutes.
- No new npm dependencies. Use React 17, antd 4.24, AMIS, axios, dayjs only.
- API success envelope is `{code:"0", msg:"success", data:T}`. Business errors arrive as HTTP 200 `{status, msg:"Business Error", description}`. `registerFilter`-independent API wrappers must handle both.
- **Jackson serializes Java `Long`/`long`/`BigInteger` as JSON strings.** Every id, timestamp, duration, total and `lockVersion` is a TypeScript `string`. Never `Number()` an id. Java `int`/`Integer` fields stay numbers: `page`, `pageSize`, `revisionNo`, `priority`, `attempts`, `promptTokens`, `completionTokens`.
- HTTP calls use `request` from `@/utils/requestInterceptor`, exactly as the rest of the client does. Raw `fetch` is allowed only in `sse.ts`, because `EventSource` cannot POST or set headers.
- The interceptor already toasts business errors and still resolves. API wrappers throw `ApiError`; components show the message inline and must not toast the same error again.
- SSE base URL is `/gw/plugin-platform/ai/draft/stream`; the interceptor prefix `/gw` is not applied to raw `fetch`, so `sse.ts` adds it.
- All user-facing strings via `pluginPlatform.*` keys in `client/src/locales/en-us.json` and `client/src/locales/zh-cn.json`; ja/ko fall back through the existing mechanism. No Chinese literals in components.
- All logs, toasts and error fallbacks are English. No debug `console.log`; never suppress `console.error`.
- No `any` types. Use `Record<string, unknown>` for truly unknown JSON. AMIS schema literals keep their inferred object type.
- React keys are backend `id`s or generated `rowKey`s, never array indexes.
- Props are read-only. AMIS `renderSchema()` results are ReactElements and are never `destroy()`ed.
- Do not touch or commit unrelated working-tree changes: `client/src/pages/wms/dashboard/index.tsx`, `client/src/scss/dashboard.scss`, `client/src/locales/en-us.json` / `zh-cn.json` dashboard edits, `server/modules-search/search-api/src/main/java/com/openwes/search/api/vo/dashboard/OperatorRanking.java`, `server/modules-search/search-api/src/main/java/com/openwes/search/api/vo/dashboard/WorkstationTask.java`, and the untracked dashboard plan files.
- **Pre-flight (human decision required):** the dashboard WIP modifies the same two locale files this plan edits. Before Task 1, ask the human to either commit the dashboard WIP on its own commit or stash it. Never mix it into a 3B commit.
- Do not claim browser E2E or manual acceptance. Verification is `tsc`, `npm run build` and static review.
- End every commit message with `Co-Authored-By: Claude Code <noreply@anthropic.com>`.

## File Map

All paths are relative to `client/src/pages/plugin_platform/` unless noted.

| Task | Create | Modify |
|---|---|---|
| 1 | `types.ts`, `api.ts`, `sse.ts`, `labels.ts`, `format.ts`, `usePluginCapabilities.ts`, `PluginStatusTag.tsx` | `client/src/react-i18next-config.ts`, `client/src/locales/en-us.json`, `client/src/locales/zh-cn.json` |
| 2 | `formModels.ts`, `ScriptEditor.tsx`, `DiffViewer.tsx`, `ScriptFileEditor.tsx`, `DeclarationTable.tsx`, `ConfigSchemaTable.tsx` | — |
| 3 | `list/index.tsx`, `workbench/index.tsx` (placeholder) | `client/src/routes/path2Compoment.tsx`, both locale files (delete `pluginSystem.*`) |
| 4 | `ValidationReportView.tsx`, `workbench/context.tsx`, `workbench/WorkbenchShell.tsx`, `workbench/tabs/OverviewTab.tsx` | `workbench/index.tsx` |
| 5 | `workbench/tabs/ScriptsTab.tsx` | `workbench/WorkbenchShell.tsx` (tab entry) |
| 6 | `workbench/RollbackModal.tsx`, `workbench/tabs/RevisionsTab.tsx` | `workbench/WorkbenchShell.tsx` |
| 7 | `workbench/tabs/ConfigTab.tsx` | `workbench/WorkbenchShell.tsx` |
| 8 | `workbench/tabs/TestRunTab.tsx` | `workbench/WorkbenchShell.tsx` |
| 9 | `workbench/tabs/ExecutionsTab.tsx` | `workbench/WorkbenchShell.tsx` |
| 10 | `workbench/tabs/AiHistoryTab.tsx`, `workbench/tabs/AuditTab.tsx` | `workbench/WorkbenchShell.tsx` |
| 11 | `workbench/AiDraftPanel.tsx`, `workbench/ChatDrawer.tsx` | `workbench/WorkbenchShell.tsx` |
| 12 | `workbench/wizard/NewPluginWizard.tsx`, `workbench/wizard/BasicsStep.tsx`, `workbench/wizard/RequirementStep.tsx`, `workbench/wizard/ReviewStep.tsx`, `workbench/wizard/FinishStep.tsx` | `workbench/index.tsx` |
| 13 | `audit/index.tsx` | `client/src/routes/path2Compoment.tsx`; final verification |

Shared module responsibilities (spec §5.2): `types.ts` mirrors every DTO; `api.ts` wraps every endpoint; `sse.ts` owns the streaming POST; `usePluginCapabilities.ts` gates UI only (the server enforces); `ScriptEditor.tsx` uses AMIS's bundled Monaco; `DiffViewer.tsx` uses AMIS `diff-editor`; `PluginStatusTag.tsx` localizes status.

---

### Task 1: Typed API Foundation, Capabilities and Locale Block

**Files:**
- Create: `client/src/pages/plugin_platform/types.ts`
- Create: `client/src/pages/plugin_platform/api.ts`
- Create: `client/src/pages/plugin_platform/sse.ts`
- Create: `client/src/pages/plugin_platform/labels.ts`
- Create: `client/src/pages/plugin_platform/format.ts`
- Create: `client/src/pages/plugin_platform/usePluginCapabilities.ts`
- Create: `client/src/pages/plugin_platform/PluginStatusTag.tsx`
- Modify: `client/src/react-i18next-config.ts`
- Modify: `client/src/locales/en-us.json`
- Modify: `client/src/locales/zh-cn.json`

**Interfaces:**
- Consumes: `request` from `@/utils/requestInterceptor`; the Phase 3A REST surface at `/plugin-platform/scriptPlugin/**` and `/plugin-platform/ai/**`.
- Produces: every exported type and function listed below. All later tasks import these names; do not rename them.

- [ ] **Step 1: Create `types.ts`**

```ts
// Client types mirroring the script-plugin-platform REST DTOs.
// Jackson serializes Java Long/long/BigInteger as JSON strings, so every id,
// timestamp, duration, total and lockVersion is a string. int/Integer fields
// (page, pageSize, revisionNo, priority, attempts, tokens) stay numbers.

export type ExtensionType =
    | "OUTBOUND_WAVE_WAVE_ACTION"
    | "OUTBOUND_WAVE_SPLIT_ACTION"
    | "REQUEST_VALIDATE"
    | "REQUEST_SUPPLY"
    | "REQUEST_AFTER_INVOKE"
    | "CUSTOM_REQUEST_INVOKE"
    | "CUSTOM_API"
    | "LIFECYCLE_LISTENER"
    | "HTTP_ENDPOINT"
    | "CRON"

export type PluginStatus = "DRAFT" | "ENABLED" | "DISABLED" | "ARCHIVED"

export type RevisionOrigin = "AI" | "MANUAL"

export type ValidationStatus = "PASSED" | "FAILED"

export type AiGenerationStatus = "RUNNING" | "SUCCEEDED" | "FAILED" | "CANCELLED"

export type AuditAction =
    | "CREATE_DRAFT"
    | "SAVE_REVISION"
    | "VALIDATE"
    | "ENABLE"
    | "DISABLE"
    | "ARCHIVE"
    | "ROLLBACK"
    | "CONFIG_SAVE"
    | "METADATA_UPDATE"
    | "TEST_RUN"

export type Scripts = Record<string, string>

export interface ExtensionDeclaration {
    type: ExtensionType
    priority: number
    script: string
    method: string
    apiType: string | null
    entityName: string | null
    customApiCode: string | null
    httpMethod: string | null
    path: string | null
    cron: string | null
}

export interface ConfigField {
    field: string
    type: string
    required: boolean
    defaultValue: string | null
}

export interface ScriptPlugin {
    id: string
    pluginKey: string
    name: string
    description: string | null
    applySystem: string
    applyModule: string | null
    status: PluginStatus
    extensionPoints: ExtensionDeclaration[]
    configSchema: ConfigField[]
    sdkVersion: string | null
    currentRevisionId: string | null
    lastValidationStatus: string | null
    lastValidationError: string | null
    lastValidationAt: string | null
    updateTime: string | null
}

export interface ScriptPluginRevision {
    id: string
    pluginId: string
    revisionNo: number
    scripts: Scripts
    extensionPoints: ExtensionDeclaration[]
    configSchema: ConfigField[]
    contentHash: string
    changeSummary: string
    origin: RevisionOrigin
    aiModel: string | null
    promptVersion: string | null
    createdBy: string
    createdAt: string
}

export interface ValidationFailure {
    stage: string
    message: string
}

export interface ValidationReport {
    pluginKey: string
    revisionId: string
    targetApp: string
    passed: boolean
    failures: ValidationFailure[]
    finishedAt: string
}

export interface ExecutionLog {
    id: string
    pluginKey: string
    revisionId: string | null
    extensionType: ExtensionType
    success: boolean
    durationMs: string
    errorMessage: string | null
    createdAt: string
}

export interface ExecutionTypeStats {
    extensionType: ExtensionType
    total: string
    failed: string
    avgDurationMs: string
    maxDurationMs: string
}

export interface ExecutionStats {
    total: string
    failed: string
    failureRate: number
    avgDurationMs: string
    maxDurationMs: string
    byType: ExecutionTypeStats[]
}

export interface PluginAudit {
    id: string
    pluginKey: string
    action: AuditAction
    revisionId: string | null
    operator: string
    success: boolean
    detail: string | null
    createdAt: string
}

export interface AiGeneration {
    id: string
    pluginKey: string
    userName: string
    model: string
    promptVersion: string
    status: AiGenerationStatus
    attempts: number
    promptTokens: number | null
    completionTokens: number | null
    durationMs: string | null
    errorMessage: string | null
    createdAt: string
    finishedAt: string | null
}

export interface AiDraftRequest {
    pluginKey: string
    requirement: string
    baseRevisionId: string | null
}

export interface AiDraftResult {
    pluginKey: string
    revisionId: string
    revisionNo: number
    changeSummary: string | null
    extensions: ExtensionDeclaration[]
    configSchema: ConfigField[]
    scripts: Scripts
    materializationVerified: boolean
    attempts: number
    model: string
    promptVersion: string
}

export interface AiDraftStreamEvent {
    type: string
    text: string | null
    result: AiDraftResult | null
    code: string | null
}

export interface PluginCapabilities {
    canDevelop: boolean
    canAdminister: boolean
    roles: string[]
}

export interface PluginArchetype {
    type: ExtensionType
    scriptMethod: string
    hostSignature: string
    blocking: boolean
}

export interface PluginContract {
    contractVersion: string
    applySystem: string
    archetypes: PluginArchetype[]
    lifecycleEntityNames: string[]
    promptFragment: string
}

export interface TestRunRequest {
    extensionType: ExtensionType
    script: string
    method: string
    arguments: Record<string, unknown>
}

export interface TestRunResult {
    success: boolean
    result: string | null
    resultTruncated: boolean
    error: string | null
    durationMs: string
}

export interface ScriptPluginConfig {
    values: Record<string, unknown>
    schema: ConfigField[]
    lockVersion: string | null
}

export interface PageResult<T> {
    items: T[]
    total: string
    page: number
    pageSize: number
}

export interface CreateDraftRequest {
    pluginKey: string
    name: string
    description: string
    applySystem: string
    applyModule: string
}

export interface SaveRevisionRequest {
    scripts: Scripts
    extensionPoints: ExtensionDeclaration[]
    configSchema: ConfigField[]
    changeSummary: string
}

export interface UpdateMetadataRequest {
    name: string
    description: string
    applyModule: string
}
```

- [ ] **Step 2: Create `api.ts`**

```ts
import { AxiosRequestConfig } from "axios"
import request from "@/utils/requestInterceptor"
import {
    AiGeneration,
    CreateDraftRequest,
    ExecutionLog,
    ExecutionStats,
    ExtensionType,
    PageResult,
    PluginAudit,
    PluginCapabilities,
    PluginContract,
    SaveRevisionRequest,
    ScriptPlugin,
    ScriptPluginConfig,
    ScriptPluginRevision,
    TestRunRequest,
    TestRunResult,
    UpdateMetadataRequest,
    ValidationReport
} from "./types"

const BASE = "/plugin-platform/scriptPlugin"
const AI_BASE = "/plugin-platform/ai"

interface RawEnvelope {
    code?: string
    msg?: string
    data?: unknown
    status?: string
    description?: string
}

interface RawResponse {
    data?: RawEnvelope | null
}

export class ApiError extends Error {
    readonly code: string

    constructor(code: string, message: string) {
        super(message)
        this.name = "ApiError"
        this.code = code
        Object.setPrototypeOf(this, ApiError.prototype)
    }
}

export function errorMessage(error: unknown, fallback: string): string {
    if (error instanceof ApiError && error.message) {
        return error.message
    }
    if (error instanceof Error && error.message) {
        return error.message
    }
    return fallback
}

async function call<T>(config: AxiosRequestConfig): Promise<T> {
    const response = (await request(config)) as RawResponse
    const body = response && response.data
    if (!body) {
        throw new ApiError("EMPTY_RESPONSE", "The server returned an empty response.")
    }
    if (body.status) {
        // The interceptor already showed this business error as a toast.
        throw new ApiError(body.status, body.description || body.msg || "Request failed.")
    }
    if (body.code !== "0") {
        throw new ApiError(body.code || "UNKNOWN", body.msg || "Request failed.")
    }
    return body.data as T
}

function pluginPath(pluginKey: string, suffix: string): string {
    return BASE + "/" + encodeURIComponent(pluginKey) + suffix
}

export function getCapabilities(): Promise<PluginCapabilities> {
    return call<PluginCapabilities>({ url: BASE + "/capabilities", method: "get" })
}

export function getContract(applySystem: string): Promise<PluginContract> {
    return call<PluginContract>({ url: BASE + "/contract", method: "get", params: { applySystem } })
}

export function listPlugins(): Promise<ScriptPlugin[]> {
    return call<ScriptPlugin[]>({ url: BASE + "/list", method: "get" })
}

export function getPlugin(pluginKey: string): Promise<ScriptPlugin> {
    return call<ScriptPlugin>({ url: BASE + "/detail/" + encodeURIComponent(pluginKey), method: "get" })
}

export function createDraft(payload: CreateDraftRequest): Promise<ScriptPlugin> {
    return call<ScriptPlugin>({ url: BASE + "/createDraft", method: "post", data: payload })
}

export function saveRevision(pluginKey: string, payload: SaveRevisionRequest): Promise<ScriptPluginRevision> {
    return call<ScriptPluginRevision>({
        url: pluginPath(pluginKey, "/revision"),
        method: "post",
        data: payload
    })
}

export function listRevisions(pluginKey: string): Promise<ScriptPluginRevision[]> {
    return call<ScriptPluginRevision[]>({ url: pluginPath(pluginKey, "/revisions"), method: "get" })
}

export function validatePlugin(pluginKey: string): Promise<ValidationReport> {
    return call<ValidationReport>({ url: pluginPath(pluginKey, "/validate"), method: "post" })
}

export function enablePlugin(pluginKey: string): Promise<ScriptPlugin> {
    return call<ScriptPlugin>({ url: pluginPath(pluginKey, "/enable"), method: "post" })
}

export function disablePlugin(pluginKey: string, reason: string): Promise<ScriptPlugin> {
    return call<ScriptPlugin>({
        url: pluginPath(pluginKey, "/disable"),
        method: "post",
        data: { reason }
    })
}

export function archivePlugin(pluginKey: string): Promise<ScriptPlugin> {
    return call<ScriptPlugin>({ url: pluginPath(pluginKey, "/archive"), method: "post" })
}

export function rollbackPlugin(pluginKey: string, revisionId: string): Promise<ScriptPlugin> {
    return call<ScriptPlugin>({
        url: pluginPath(pluginKey, "/rollback/" + encodeURIComponent(revisionId)),
        method: "post"
    })
}

export function updatePluginMetadata(
    pluginKey: string,
    payload: UpdateMetadataRequest
): Promise<ScriptPlugin> {
    return call<ScriptPlugin>({ url: pluginPath(pluginKey, "/metadata"), method: "post", data: payload })
}

export function listPluginAudit(
    pluginKey: string,
    page: number,
    pageSize: number
): Promise<PageResult<PluginAudit>> {
    return call<PageResult<PluginAudit>>({
        url: pluginPath(pluginKey, "/audit"),
        method: "get",
        params: { page, pageSize }
    })
}

export function listAllAudit(page: number, pageSize: number): Promise<PageResult<PluginAudit>> {
    return call<PageResult<PluginAudit>>({
        url: BASE + "/audit",
        method: "get",
        params: { page, pageSize }
    })
}

export function getPluginConfig(pluginKey: string): Promise<ScriptPluginConfig> {
    return call<ScriptPluginConfig>({ url: pluginPath(pluginKey, "/config"), method: "get" })
}

export function savePluginConfig(
    pluginKey: string,
    values: Record<string, unknown>,
    lockVersion: string | null
): Promise<ScriptPluginConfig> {
    return call<ScriptPluginConfig>({
        url: pluginPath(pluginKey, "/config"),
        method: "post",
        data: { values, lockVersion }
    })
}

export interface ExecutionQuery {
    page: number
    pageSize: number
    success?: boolean
    extensionType?: ExtensionType
}

export function listExecutions(
    pluginKey: string,
    query: ExecutionQuery
): Promise<PageResult<ExecutionLog>> {
    const params: Record<string, string | number | boolean> = {
        page: query.page,
        pageSize: query.pageSize
    }
    if (query.success !== undefined) {
        params.success = query.success
    }
    if (query.extensionType !== undefined) {
        params.extensionType = query.extensionType
    }
    return call<PageResult<ExecutionLog>>({
        url: pluginPath(pluginKey, "/executions"),
        method: "get",
        params
    })
}

export function getExecutionStats(pluginKey: string, hours: number): Promise<ExecutionStats> {
    return call<ExecutionStats>({
        url: pluginPath(pluginKey, "/executions/stats"),
        method: "get",
        params: { hours }
    })
}

export function runTest(pluginKey: string, payload: TestRunRequest): Promise<TestRunResult> {
    return call<TestRunResult>({ url: pluginPath(pluginKey, "/test-run"), method: "post", data: payload })
}

export function listAiGenerations(pluginKey: string): Promise<AiGeneration[]> {
    return call<AiGeneration[]>({
        url: AI_BASE + "/generations/" + encodeURIComponent(pluginKey),
        method: "get"
    })
}
```

- [ ] **Step 3: Create `sse.ts`**

```ts
import store from "@/stores"
import { AiDraftRequest, AiDraftResult, AiDraftStreamEvent } from "./types"
import { ApiError } from "./api"

const STREAM_URL = "/gw/plugin-platform/ai/draft/stream"

export interface AiDraftHandlers {
    onChunk: (text: string) => void
    onStage: (stage: string) => void
    onResult: (result: AiDraftResult) => void
    onError: (code: string, message: string) => void
}

export interface AiDraftStreamOptions extends AiDraftHandlers {
    signal?: AbortSignal
}

export function isAbortError(error: unknown): boolean {
    return !!error && typeof error === "object" && (error as { name?: string }).name === "AbortError"
}

function buildHeaders(): Record<string, string> {
    const headers: Record<string, string> = {
        "Content-Type": "application/json",
        Accept: "text/event-stream",
        Authorization: localStorage.getItem("ws_token") || "",
        "X-TenantID": window.location.hostname.split(".")[0],
        "X-WarehouseID": localStorage.getItem("warehouseCode") || "",
        locale: store.locale
    }
    const stationId = localStorage.getItem("stationId")
    if (stationId) {
        headers.StationCode = stationId
    }
    return headers
}

function handleFrameText(frame: string, handle: (eventName: string, data: string) => void): void {
    let eventName = ""
    const dataLines: string[] = []
    frame.split("\n").forEach((line) => {
        if (!line || line.indexOf(":") === 0) {
            return
        }
        const colon = line.indexOf(":")
        const field = colon >= 0 ? line.substring(0, colon) : line
        let value = colon >= 0 ? line.substring(colon + 1) : ""
        if (value.indexOf(" ") === 0) {
            value = value.substring(1)
        }
        if (field === "event") {
            eventName = value
        } else if (field === "data") {
            dataLines.push(value)
        }
    })
    if (dataLines.length > 0) {
        handle(eventName, dataLines.join("\n"))
    }
}

/** Returns the unconsumed remainder of the buffer. */
function consumeFrames(buffer: string, handle: (eventName: string, data: string) => void): string {
    let normalized = buffer.replace(/\r\n/g, "\n")
    let boundary = normalized.indexOf("\n\n")
    while (boundary >= 0) {
        handleFrameText(normalized.substring(0, boundary), handle)
        normalized = normalized.substring(boundary + 2)
        boundary = normalized.indexOf("\n\n")
    }
    return normalized
}

async function readErrorEnvelope(response: Response): Promise<ApiError> {
    const text = await response.text()
    try {
        const body = JSON.parse(text) as { status?: string; description?: string; msg?: string }
        return new ApiError(body.status || "AI_STREAM_ERROR", body.description || body.msg || "The AI request failed.")
    } catch (ignored) {
        return new ApiError("AI_STREAM_ERROR", "The AI request failed.")
    }
}

export async function streamAiDraft(
    requestBody: AiDraftRequest,
    options: AiDraftStreamOptions
): Promise<void> {
    const response = await fetch(STREAM_URL, {
        method: "POST",
        headers: buildHeaders(),
        body: JSON.stringify(requestBody),
        // Keep parity with the axios interceptor's withCredentials = true.
        credentials: "include",
        signal: options.signal
    })

    if (response.status === 401) {
        localStorage.removeItem("ws_token")
        window.location.href = "/login"
        throw new ApiError("UNAUTHORIZED", "Your session has expired. Sign in again.")
    }

    const contentType = response.headers.get("content-type") || ""
    if (response.status >= 400 || contentType.indexOf("text/event-stream") < 0) {
        throw await readErrorEnvelope(response)
    }
    if (!response.body) {
        throw new ApiError("AI_STREAM_ERROR", "This browser does not support streaming responses.")
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder("utf-8")
    let buffer = ""
    let sawResult = false
    let sawError = false

    const onFrame = (eventName: string, data: string): void => {
        if (!data) {
            return
        }
        let payload: AiDraftStreamEvent
        try {
            payload = JSON.parse(data) as AiDraftStreamEvent
        } catch (ignored) {
            return
        }
        const type = payload.type || eventName
        if (type === "chunk") {
            options.onChunk(payload.text || "")
        } else if (type === "stage") {
            options.onStage(payload.text || "")
        } else if (type === "result") {
            if (payload.result) {
                sawResult = true
                options.onResult(payload.result)
            }
        } else if (type === "error") {
            sawError = true
            options.onError(payload.code || "AI_ERROR", payload.text || "The AI generation failed.")
        }
    }

    try {
        for (;;) {
            const chunk = await reader.read()
            if (chunk.done) {
                break
            }
            buffer += decoder.decode(chunk.value, { stream: true })
            buffer = consumeFrames(buffer, onFrame)
        }
        buffer += decoder.decode()
        buffer = consumeFrames(buffer, onFrame)
        if (buffer.trim().length > 0) {
            handleFrameText(buffer, onFrame)
        }
    } finally {
        reader.releaseLock()
    }

    if (!sawResult && !sawError) {
        throw new ApiError("AI_STREAM_INCOMPLETE", "The AI stream ended without a result.")
    }
}
```

- [ ] **Step 4: Create `labels.ts`**

```ts
import { AuditAction, ExtensionType, PluginStatus, RevisionOrigin } from "./types"

export const STATUS_LABEL_KEYS: Record<PluginStatus, string> = {
    DRAFT: "pluginPlatform.status.draft",
    ENABLED: "pluginPlatform.status.enabled",
    DISABLED: "pluginPlatform.status.disabled",
    ARCHIVED: "pluginPlatform.status.archived"
}

export const STATUS_COLORS: Record<PluginStatus, string> = {
    DRAFT: "default",
    ENABLED: "green",
    DISABLED: "orange",
    ARCHIVED: "red"
}

export const VALIDATION_LABEL_KEYS: Record<string, string> = {
    PASSED: "pluginPlatform.validation.passed",
    FAILED: "pluginPlatform.validation.failed"
}

export const REVISION_ORIGIN_LABEL_KEYS: Record<RevisionOrigin, string> = {
    AI: "pluginPlatform.revisionOrigin.ai",
    MANUAL: "pluginPlatform.revisionOrigin.manual"
}

export const AI_STATUS_LABEL_KEYS: Record<string, string> = {
    RUNNING: "pluginPlatform.aiStatus.running",
    SUCCEEDED: "pluginPlatform.aiStatus.succeeded",
    FAILED: "pluginPlatform.aiStatus.failed",
    CANCELLED: "pluginPlatform.aiStatus.cancelled"
}

export const AI_STATUS_COLORS: Record<string, string> = {
    RUNNING: "blue",
    SUCCEEDED: "green",
    FAILED: "red",
    CANCELLED: "default"
}

export const EXTENSION_TYPE_LABEL_KEYS: Record<ExtensionType, string> = {
    OUTBOUND_WAVE_WAVE_ACTION: "pluginPlatform.extensionType.outboundWaveWaveAction",
    OUTBOUND_WAVE_SPLIT_ACTION: "pluginPlatform.extensionType.outboundWaveSplitAction",
    REQUEST_VALIDATE: "pluginPlatform.extensionType.requestValidate",
    REQUEST_SUPPLY: "pluginPlatform.extensionType.requestSupply",
    REQUEST_AFTER_INVOKE: "pluginPlatform.extensionType.requestAfterInvoke",
    CUSTOM_REQUEST_INVOKE: "pluginPlatform.extensionType.customRequestInvoke",
    CUSTOM_API: "pluginPlatform.extensionType.customApi",
    LIFECYCLE_LISTENER: "pluginPlatform.extensionType.lifecycleListener",
    HTTP_ENDPOINT: "pluginPlatform.extensionType.httpEndpoint",
    CRON: "pluginPlatform.extensionType.cron"
}

export const AUDIT_ACTION_LABEL_KEYS: Record<AuditAction, string> = {
    CREATE_DRAFT: "pluginPlatform.auditAction.createDraft",
    SAVE_REVISION: "pluginPlatform.auditAction.saveRevision",
    VALIDATE: "pluginPlatform.auditAction.validate",
    ENABLE: "pluginPlatform.auditAction.enable",
    DISABLE: "pluginPlatform.auditAction.disable",
    ARCHIVE: "pluginPlatform.auditAction.archive",
    ROLLBACK: "pluginPlatform.auditAction.rollback",
    CONFIG_SAVE: "pluginPlatform.auditAction.configSave",
    METADATA_UPDATE: "pluginPlatform.auditAction.metadataUpdate",
    TEST_RUN: "pluginPlatform.auditAction.testRun"
}
```

- [ ] **Step 5: Create `format.ts`**

```ts
import dayjs from "dayjs"

export function formatMillis(value: string | null | undefined): string {
    if (value === null || value === undefined || value === "") {
        return "-"
    }
    const millis = Number(value)
    if (!isFinite(millis)) {
        return "-"
    }
    return dayjs(millis).format("YYYY-MM-DD HH:mm:ss")
}

export function formatDuration(value: string | number | null | undefined): string {
    if (value === null || value === undefined || value === "") {
        return "-"
    }
    const millis = Number(value)
    if (!isFinite(millis)) {
        return "-"
    }
    if (millis < 1000) {
        return millis + " ms"
    }
    return (millis / 1000).toFixed(2) + " s"
}

export function formatPercent(value: number | null | undefined): string {
    if (value === null || value === undefined || !isFinite(value)) {
        return "-"
    }
    return value.toFixed(2) + "%"
}

export function formatNumber(value: string | number | null | undefined): string {
    if (value === null || value === undefined || value === "") {
        return "-"
    }
    const numeric = Number(value)
    return isFinite(numeric) ? String(numeric) : "-"
}
```

- [ ] **Step 6: Create `usePluginCapabilities.ts`**

```ts
import { useEffect, useState } from "react"
import { getCapabilities } from "./api"

export interface PluginCapabilitiesState {
    canDevelop: boolean
    canAdminister: boolean
    roles: string[]
    loading: boolean
}

const INITIAL: PluginCapabilitiesState = {
    canDevelop: false,
    canAdminister: false,
    roles: [],
    loading: true
}

/**
 * UI gating only. Every mutation is authorized again by the server, which audits denials.
 */
export default function usePluginCapabilities(): PluginCapabilitiesState {
    const [state, setState] = useState<PluginCapabilitiesState>(INITIAL)

    useEffect(() => {
        let active = true
        getCapabilities()
            .then((capabilities) => {
                if (!active) {
                    return
                }
                setState({
                    canDevelop: capabilities.canDevelop,
                    canAdminister: capabilities.canAdminister,
                    roles: capabilities.roles || [],
                    loading: false
                })
            })
            .catch(() => {
                if (active) {
                    setState({ canDevelop: false, canAdminister: false, roles: [], loading: false })
                }
            })
        return () => {
            active = false
        }
    }, [])

    return state
}
```

- [ ] **Step 7: Create `PluginStatusTag.tsx`**

```tsx
import React from "react"
import { Tag } from "antd"
import { useTranslation } from "react-i18next"
import { PluginStatus } from "./types"
import { STATUS_COLORS, STATUS_LABEL_KEYS } from "./labels"

interface PluginStatusTagProps {
    status: PluginStatus
}

const PluginStatusTag: React.FC<PluginStatusTagProps> = ({ status }) => {
    const { t } = useTranslation()
    return <Tag color={STATUS_COLORS[status]}>{t(STATUS_LABEL_KEYS[status])}</Tag>
}

export default PluginStatusTag
```

- [ ] **Step 8: Add the `millis` AMIS filter**

In `client/src/react-i18next-config.ts`, add the import and filter after the existing `t` filter:

```ts
import dayjs from "dayjs"

registerFilter("millis", (input: string) => {
    if (input === null || input === undefined || input === "") {
        return "-"
    }
    const millis = Number(input)
    return isFinite(millis) ? dayjs(millis).format("YYYY-MM-DD HH:mm:ss") : "-"
})
```

- [ ] **Step 9: Add the `pluginPlatform.*` locale block to `en-us.json`**

Insert this block immediately before `"pluginSystem.pluginManagement.title"`. Keep the existing blank-line style of the file.

```json
    "pluginPlatform.list.title": "Plugins",
    "pluginPlatform.list.newPlugin": "New plugin",
    "pluginPlatform.list.open": "Open",
    "pluginPlatform.list.filterTitle": "Filters",
    "pluginPlatform.list.search": "Search",
    "pluginPlatform.list.empty": "No plugins yet. Use \"New plugin\" to create one.",
    "pluginPlatform.list.column.pluginKey": "Plugin key",
    "pluginPlatform.list.column.name": "Name",
    "pluginPlatform.list.column.applySystem": "Apply system",
    "pluginPlatform.list.column.applyModule": "Apply module",
    "pluginPlatform.list.column.status": "Status",
    "pluginPlatform.list.column.lastValidation": "Last validation",
    "pluginPlatform.list.column.currentRevision": "Current revision",
    "pluginPlatform.list.column.updateTime": "Updated at",
    "pluginPlatform.list.column.actions": "Actions",

    "pluginPlatform.status.draft": "Draft",
    "pluginPlatform.status.enabled": "Enabled",
    "pluginPlatform.status.disabled": "Disabled",
    "pluginPlatform.status.archived": "Archived",

    "pluginPlatform.validation.passed": "Passed",
    "pluginPlatform.validation.failed": "Failed",
    "pluginPlatform.validation.notValidated": "Not validated",
    "pluginPlatform.validation.reportTitle": "Validation report",
    "pluginPlatform.validation.stage": "Stage",
    "pluginPlatform.validation.message": "Message",
    "pluginPlatform.validation.lastValidation": "Last validation",
    "pluginPlatform.validation.lastError": "Last validation error",
    "pluginPlatform.validation.lastAt": "Validated at",

    "pluginPlatform.extensionType.outboundWaveWaveAction": "Outbound wave action",
    "pluginPlatform.extensionType.outboundWaveSplitAction": "Outbound wave split",
    "pluginPlatform.extensionType.requestValidate": "Request validation",
    "pluginPlatform.extensionType.requestSupply": "Request supply",
    "pluginPlatform.extensionType.requestAfterInvoke": "Request after-invoke",
    "pluginPlatform.extensionType.customRequestInvoke": "Custom request invoke",
    "pluginPlatform.extensionType.customApi": "Custom API",
    "pluginPlatform.extensionType.lifecycleListener": "Lifecycle listener",
    "pluginPlatform.extensionType.httpEndpoint": "HTTP endpoint",
    "pluginPlatform.extensionType.cron": "Cron",

    "pluginPlatform.revisionOrigin.ai": "AI",
    "pluginPlatform.revisionOrigin.manual": "Manual",

    "pluginPlatform.auditAction.createDraft": "Create draft",
    "pluginPlatform.auditAction.saveRevision": "Save revision",
    "pluginPlatform.auditAction.validate": "Validate",
    "pluginPlatform.auditAction.enable": "Enable",
    "pluginPlatform.auditAction.disable": "Disable",
    "pluginPlatform.auditAction.archive": "Archive",
    "pluginPlatform.auditAction.rollback": "Rollback",
    "pluginPlatform.auditAction.configSave": "Save config",
    "pluginPlatform.auditAction.metadataUpdate": "Update metadata",
    "pluginPlatform.auditAction.testRun": "Test run",

    "pluginPlatform.aiStatus.running": "Running",
    "pluginPlatform.aiStatus.succeeded": "Succeeded",
    "pluginPlatform.aiStatus.failed": "Failed",
    "pluginPlatform.aiStatus.cancelled": "Cancelled",

    "pluginPlatform.common.cancel": "Cancel",
    "pluginPlatform.common.next": "Next",
    "pluginPlatform.common.refresh": "Refresh",
    "pluginPlatform.common.remove": "Remove",
    "pluginPlatform.common.confirm": "Confirm",
    "pluginPlatform.common.all": "All",
    "pluginPlatform.common.readOnly": "Read only",

    "pluginPlatform.applySystem.wes": "WES",
    "pluginPlatform.applySystem.station": "Station",

    "pluginPlatform.workbench.title": "Plugin workbench",
    "pluginPlatform.workbench.noPlugin": "No plugin selected",
    "pluginPlatform.workbench.noPluginHint": "Open a plugin from the plugin list or create a new one.",
    "pluginPlatform.workbench.newPlugin": "New plugin",
    "pluginPlatform.workbench.backToList": "Plugin list",
    "pluginPlatform.workbench.aiAssistant": "AI assistant",
    "pluginPlatform.workbench.refresh": "Refresh",
    "pluginPlatform.workbench.sdkVersion": "SDK version",
    "pluginPlatform.workbench.pluginKey": "Plugin key",
    "pluginPlatform.workbench.applySystem": "Apply system",
    "pluginPlatform.workbench.applyModule": "Apply module",
    "pluginPlatform.workbench.currentRevision": "Current revision",
    "pluginPlatform.workbench.updatedAt": "Updated at",
    "pluginPlatform.workbench.actions": "Actions",
    "pluginPlatform.workbench.tab.overview": "Overview",
    "pluginPlatform.workbench.tab.scripts": "Scripts",
    "pluginPlatform.workbench.tab.revisions": "Revisions",
    "pluginPlatform.workbench.tab.config": "Config",
    "pluginPlatform.workbench.tab.testRun": "Test run",
    "pluginPlatform.workbench.tab.executions": "Executions",
    "pluginPlatform.workbench.tab.aiHistory": "AI history",
    "pluginPlatform.workbench.tab.audit": "Audit",

    "pluginPlatform.overview.title": "Plugin information",
    "pluginPlatform.overview.metadata": "Edit metadata",
    "pluginPlatform.overview.name": "Name",
    "pluginPlatform.overview.description": "Description",
    "pluginPlatform.overview.saveMetadata": "Save metadata",
    "pluginPlatform.overview.metadataSaved": "Plugin metadata saved.",
    "pluginPlatform.overview.validate": "Validate",
    "pluginPlatform.overview.enable": "Enable",
    "pluginPlatform.overview.disable": "Disable",
    "pluginPlatform.overview.archive": "Archive",
    "pluginPlatform.overview.rollback": "Rollback",
    "pluginPlatform.overview.disableTitle": "Disable plugin",
    "pluginPlatform.overview.disableReason": "Reason (optional)",
    "pluginPlatform.overview.enableConfirm": "Validate and enable this plugin?",
    "pluginPlatform.overview.archiveConfirm": "Archive this plugin? Archived plugins are read-only.",
    "pluginPlatform.overview.rollbackTitle": "Roll back to a revision",
    "pluginPlatform.overview.rollbackRevision": "Revision",
    "pluginPlatform.overview.enabled": "Plugin enabled.",
    "pluginPlatform.overview.disabled": "Plugin disabled.",
    "pluginPlatform.overview.archived": "Plugin archived.",
    "pluginPlatform.overview.rolledBack": "Plugin rolled back.",
    "pluginPlatform.overview.validationFinished": "Validation finished.",
    "pluginPlatform.overview.noRevisions": "This plugin has no revisions yet.",
    "pluginPlatform.overview.statusActions": "Status actions",
    "pluginPlatform.overview.noPermission": "You need the plugin administrator role for this action.",

    "pluginPlatform.scripts.files": "Script files",
    "pluginPlatform.scripts.newFile": "New file name",
    "pluginPlatform.scripts.addFile": "Add file",
    "pluginPlatform.scripts.deleteFile": "Delete file",
    "pluginPlatform.scripts.deleteFileConfirm": "Delete script file \"{{file}}\"?",
    "pluginPlatform.scripts.empty": "No script files. Add a file to start.",
    "pluginPlatform.scripts.declarations": "Extension declarations",
    "pluginPlatform.scripts.addDeclaration": "Add declaration",
    "pluginPlatform.scripts.configSchema": "Configuration schema",
    "pluginPlatform.scripts.addConfigField": "Add config field",
    "pluginPlatform.scripts.changeSummary": "Change summary",
    "pluginPlatform.scripts.saveRevision": "Save revision",
    "pluginPlatform.scripts.revisionSaved": "Revision saved.",
    "pluginPlatform.scripts.readOnlyEnabled": "Disable the plugin before saving a new revision.",
    "pluginPlatform.scripts.readOnlyArchived": "Archived plugins are read-only.",
    "pluginPlatform.scripts.noPermission": "You need the plugin developer role to edit scripts.",
    "pluginPlatform.scripts.missingScriptFile": "Declaration \"{{declaration}}\" references a missing script file.",
    "pluginPlatform.scripts.noScripts": "A revision needs at least one script file.",
    "pluginPlatform.scripts.defaultFile": "script.groovy",
    "pluginPlatform.scripts.fileExists": "A script file with this name already exists.",

    "pluginPlatform.declaration.type": "Type",
    "pluginPlatform.declaration.script": "Script file",
    "pluginPlatform.declaration.method": "Method",
    "pluginPlatform.declaration.priority": "Priority",
    "pluginPlatform.declaration.details": "Details",
    "pluginPlatform.declaration.apiType": "API type",
    "pluginPlatform.declaration.entityName": "Entity",
    "pluginPlatform.declaration.customApiCode": "Custom API code",
    "pluginPlatform.declaration.path": "Path",
    "pluginPlatform.declaration.cron": "Cron expression",
    "pluginPlatform.declaration.empty": "No declarations.",
    "pluginPlatform.declaration.scriptRequired": "A declaration needs a script file.",
    "pluginPlatform.declaration.methodRequired": "A declaration needs a method.",
    "pluginPlatform.declaration.apiTypeRequired": "Request declarations need an API type.",
    "pluginPlatform.declaration.entityRequired": "Lifecycle listeners need an entity.",
    "pluginPlatform.declaration.customApiRequired": "Custom API declarations need a custom API code.",
    "pluginPlatform.declaration.pathRequired": "HTTP endpoints need a path starting with '/'.",
    "pluginPlatform.declaration.cronRequired": "Cron declarations need an expression.",

    "pluginPlatform.configSchema.field": "Field",
    "pluginPlatform.configSchema.type": "Type",
    "pluginPlatform.configSchema.required": "Required",
    "pluginPlatform.configSchema.defaultValue": "Default value",
    "pluginPlatform.configSchema.empty": "No configuration fields.",
    "pluginPlatform.configSchema.fieldRequired": "A config field needs a name.",
    "pluginPlatform.configSchema.typeRequired": "Config field \"{{field}}\" needs a type.",
    "pluginPlatform.configSchema.duplicate": "Duplicate config field \"{{field}}\".",

    "pluginPlatform.revisions.revisionNo": "Revision",
    "pluginPlatform.revisions.origin": "Origin",
    "pluginPlatform.revisions.createdBy": "Created by",
    "pluginPlatform.revisions.createdAt": "Created at",
    "pluginPlatform.revisions.hash": "Content hash",
    "pluginPlatform.revisions.changeSummary": "Change summary",
    "pluginPlatform.revisions.selectTwo": "Select two revisions to compare.",
    "pluginPlatform.revisions.base": "Base",
    "pluginPlatform.revisions.target": "Target",
    "pluginPlatform.revisions.rollback": "Rollback",
    "pluginPlatform.revisions.empty": "No revisions yet.",
    "pluginPlatform.revisions.file": "File",
    "pluginPlatform.revisions.compareTitle": "Compare revisions",
    "pluginPlatform.revisions.rollbackTitle": "Roll back to a revision",
    "pluginPlatform.revisions.missingFile": "This file does not exist in one of the selected revisions; the missing side is empty.",

    "pluginPlatform.config.title": "Config",
    "pluginPlatform.config.empty": "This plugin declares no configuration fields.",
    "pluginPlatform.config.save": "Save config",
    "pluginPlatform.config.saved": "Configuration saved.",
    "pluginPlatform.config.adminOnly": "Only plugin administrators can view configuration.",
    "pluginPlatform.config.unsupportedType": "Unsupported config field type \"{{type}}\".",

    "pluginPlatform.testRun.title": "Test run",
    "pluginPlatform.testRun.warning": "Test runs invoke real host services with the arguments below. Do not use production data.",
    "pluginPlatform.testRun.wesOnly": "Test run is available only for WES plugins.",
    "pluginPlatform.testRun.noRevision": "This plugin has no revision to test.",
    "pluginPlatform.testRun.declaration": "Extension",
    "pluginPlatform.testRun.arguments": "Arguments (JSON)",
    "pluginPlatform.testRun.invalidArguments": "Arguments must be a JSON object.",
    "pluginPlatform.testRun.run": "Run",
    "pluginPlatform.testRun.running": "Running...",
    "pluginPlatform.testRun.success": "Success",
    "pluginPlatform.testRun.failure": "Failure",
    "pluginPlatform.testRun.duration": "Duration",
    "pluginPlatform.testRun.truncated": "The result was truncated to 8 KB.",
    "pluginPlatform.testRun.noDeclarations": "The current revision declares no extensions.",

    "pluginPlatform.executions.title": "Executions",
    "pluginPlatform.executions.total": "Total executions",
    "pluginPlatform.executions.failed": "Failed",
    "pluginPlatform.executions.failureRate": "Failure rate",
    "pluginPlatform.executions.avgDuration": "Average duration",
    "pluginPlatform.executions.maxDuration": "Maximum duration",
    "pluginPlatform.executions.byType": "By extension type",
    "pluginPlatform.executions.time": "Time",
    "pluginPlatform.executions.type": "Type",
    "pluginPlatform.executions.result": "Result",
    "pluginPlatform.executions.duration": "Duration",
    "pluginPlatform.executions.revision": "Revision",
    "pluginPlatform.executions.error": "Error",
    "pluginPlatform.executions.success": "Success",
    "pluginPlatform.executions.failure": "Failure",
    "pluginPlatform.executions.hours": "Window",
    "pluginPlatform.executions.lastHour": "Last hour",
    "pluginPlatform.executions.last24Hours": "Last 24 hours",
    "pluginPlatform.executions.last7Days": "Last 7 days",
    "pluginPlatform.executions.empty": "No executions recorded.",

    "pluginPlatform.aiHistory.title": "AI history",
    "pluginPlatform.aiHistory.user": "User",
    "pluginPlatform.aiHistory.model": "Model",
    "pluginPlatform.aiHistory.promptVersion": "Prompt version",
    "pluginPlatform.aiHistory.status": "Status",
    "pluginPlatform.aiHistory.attempts": "Attempts",
    "pluginPlatform.aiHistory.tokens": "Tokens",
    "pluginPlatform.aiHistory.duration": "Duration",
    "pluginPlatform.aiHistory.createdAt": "Created at",
    "pluginPlatform.aiHistory.finishedAt": "Finished at",
    "pluginPlatform.aiHistory.error": "Error",
    "pluginPlatform.aiHistory.empty": "No AI generations yet.",

    "pluginPlatform.audit.title": "Plugin audit",
    "pluginPlatform.audit.plugin": "Plugin",
    "pluginPlatform.audit.action": "Action",
    "pluginPlatform.audit.operator": "Operator",
    "pluginPlatform.audit.success": "Result",
    "pluginPlatform.audit.detail": "Detail",
    "pluginPlatform.audit.createdAt": "Time",
    "pluginPlatform.audit.revision": "Revision",
    "pluginPlatform.audit.empty": "No audit records.",
    "pluginPlatform.audit.successValue": "Success",
    "pluginPlatform.audit.failureValue": "Failure",

    "pluginPlatform.chat.title": "AI assistant",
    "pluginPlatform.chat.baseRevision": "Base revision",
    "pluginPlatform.chat.baseRevisionNone": "No base revision",
    "pluginPlatform.chat.requirementPlaceholder": "Describe what the plugin should do...",
    "pluginPlatform.chat.generate": "Generate",
    "pluginPlatform.chat.cancel": "Cancel generation",
    "pluginPlatform.chat.streaming": "Generating...",
    "pluginPlatform.chat.completed": "Generation complete. Revision {{no}} saved.",
    "pluginPlatform.chat.cancelled": "Generation cancelled.",
    "pluginPlatform.chat.requirementRequired": "Enter a requirement.",
    "pluginPlatform.chat.requirementTooLong": "The requirement is too long for the AI endpoint.",
    "pluginPlatform.chat.noPermission": "You need the plugin developer role to use AI generation.",
    "pluginPlatform.chat.stage": "Stages",
    "pluginPlatform.chat.output": "Model output",

    "pluginPlatform.wizard.title": "New plugin",
    "pluginPlatform.wizard.step.basics": "Basics",
    "pluginPlatform.wizard.step.requirement": "Requirement and AI",
    "pluginPlatform.wizard.step.review": "Review",
    "pluginPlatform.wizard.step.finish": "Finish",
    "pluginPlatform.wizard.basics.pluginKey": "Plugin key",
    "pluginPlatform.wizard.basics.pluginKeyHint": "Letters, digits, '.', '_' and '-' only.",
    "pluginPlatform.wizard.basics.pluginKeyRequired": "Enter a plugin key.",
    "pluginPlatform.wizard.basics.pluginKeyInvalid": "Use only letters, digits, '.', '_' and '-'.",
    "pluginPlatform.wizard.basics.name": "Name",
    "pluginPlatform.wizard.basics.nameRequired": "Enter a name.",
    "pluginPlatform.wizard.basics.description": "Description",
    "pluginPlatform.wizard.basics.applySystem": "Apply system",
    "pluginPlatform.wizard.basics.applySystemRequired": "Select an apply system.",
    "pluginPlatform.wizard.basics.applyModule": "Apply module",
    "pluginPlatform.wizard.requirement.title": "Requirement and AI generation",
    "pluginPlatform.wizard.requirement.hints": "Archetype hints",
    "pluginPlatform.wizard.requirement.hintsHint": "Optional: the selected extension types are appended to the requirement.",
    "pluginPlatform.wizard.requirement.skipAi": "Continue without AI",
    "pluginPlatform.wizard.requirement.created": "Draft created.",
    "pluginPlatform.wizard.requirement.result": "AI result",
    "pluginPlatform.wizard.requirement.resultSummary": "Revision {{no}} created.",
    "pluginPlatform.wizard.requirement.stage.declarations": "Declarations",
    "pluginPlatform.wizard.requirement.stage.compiling": "Compiling",
    "pluginPlatform.wizard.requirement.stage.wiring": "Wiring",
    "pluginPlatform.wizard.requirement.stage.retry": "Retry {{n}}",
    "pluginPlatform.wizard.review.title": "Review",
    "pluginPlatform.wizard.review.materializationVerified": "Materialization verified by the backend dry-run.",
    "pluginPlatform.wizard.review.materializationUnverified": "Materialization was not verified. Run validation before enabling.",
    "pluginPlatform.wizard.review.saveRevision": "Save revision",
    "pluginPlatform.wizard.review.saved": "Revision saved.",
    "pluginPlatform.wizard.review.next": "Next",
    "pluginPlatform.wizard.finish.title": "Finish",
    "pluginPlatform.wizard.finish.runValidation": "Run validation",
    "pluginPlatform.wizard.finish.enableNow": "Enable now",
    "pluginPlatform.wizard.finish.developerHint": "The draft is saved. An administrator must validate and enable it.",
    "pluginPlatform.wizard.finish.finish": "Close",
    "pluginPlatform.wizard.finish.validationRequired": "Run validation successfully before enabling.",
    "pluginPlatform.wizard.finish.enabled": "Plugin enabled.",
    "pluginPlatform.wizard.cancel": "Cancel",
    "pluginPlatform.wizard.cancelConfirm": "Close the wizard? The draft is kept.",
    "pluginPlatform.wizard.noPermission": "You need the plugin developer role to create plugins."
```

- [ ] **Step 10: Add the same keys to `zh-cn.json` with Chinese values**

Insert immediately before `"pluginSystem.pluginManagement.title"`:

```json
    "pluginPlatform.list.title": "插件列表",
    "pluginPlatform.list.newPlugin": "新建插件",
    "pluginPlatform.list.open": "打开",
    "pluginPlatform.list.filterTitle": "筛选",
    "pluginPlatform.list.search": "查询",
    "pluginPlatform.list.empty": "暂无插件，请点击“新建插件”创建。",
    "pluginPlatform.list.column.pluginKey": "插件标识",
    "pluginPlatform.list.column.name": "名称",
    "pluginPlatform.list.column.applySystem": "适用系统",
    "pluginPlatform.list.column.applyModule": "适用模块",
    "pluginPlatform.list.column.status": "状态",
    "pluginPlatform.list.column.lastValidation": "最近校验",
    "pluginPlatform.list.column.currentRevision": "当前版本",
    "pluginPlatform.list.column.updateTime": "更新时间",
    "pluginPlatform.list.column.actions": "操作",

    "pluginPlatform.status.draft": "草稿",
    "pluginPlatform.status.enabled": "已启用",
    "pluginPlatform.status.disabled": "已停用",
    "pluginPlatform.status.archived": "已归档",

    "pluginPlatform.validation.passed": "通过",
    "pluginPlatform.validation.failed": "失败",
    "pluginPlatform.validation.notValidated": "未校验",
    "pluginPlatform.validation.reportTitle": "校验报告",
    "pluginPlatform.validation.stage": "阶段",
    "pluginPlatform.validation.message": "信息",
    "pluginPlatform.validation.lastValidation": "最近校验",
    "pluginPlatform.validation.lastError": "最近校验错误",
    "pluginPlatform.validation.lastAt": "校验时间",

    "pluginPlatform.extensionType.outboundWaveWaveAction": "出库波次动作",
    "pluginPlatform.extensionType.outboundWaveSplitAction": "出库波次拆分",
    "pluginPlatform.extensionType.requestValidate": "请求校验",
    "pluginPlatform.extensionType.requestSupply": "请求补全",
    "pluginPlatform.extensionType.requestAfterInvoke": "请求调用后",
    "pluginPlatform.extensionType.customRequestInvoke": "自定义请求调用",
    "pluginPlatform.extensionType.customApi": "自定义 API",
    "pluginPlatform.extensionType.lifecycleListener": "生命周期监听",
    "pluginPlatform.extensionType.httpEndpoint": "HTTP 端点",
    "pluginPlatform.extensionType.cron": "定时任务",

    "pluginPlatform.revisionOrigin.ai": "AI",
    "pluginPlatform.revisionOrigin.manual": "手动",

    "pluginPlatform.auditAction.createDraft": "创建草稿",
    "pluginPlatform.auditAction.saveRevision": "保存版本",
    "pluginPlatform.auditAction.validate": "校验",
    "pluginPlatform.auditAction.enable": "启用",
    "pluginPlatform.auditAction.disable": "停用",
    "pluginPlatform.auditAction.archive": "归档",
    "pluginPlatform.auditAction.rollback": "回滚",
    "pluginPlatform.auditAction.configSave": "保存配置",
    "pluginPlatform.auditAction.metadataUpdate": "更新元数据",
    "pluginPlatform.auditAction.testRun": "试运行",

    "pluginPlatform.aiStatus.running": "进行中",
    "pluginPlatform.aiStatus.succeeded": "成功",
    "pluginPlatform.aiStatus.failed": "失败",
    "pluginPlatform.aiStatus.cancelled": "已取消",

    "pluginPlatform.common.cancel": "取消",
    "pluginPlatform.common.next": "下一步",
    "pluginPlatform.common.refresh": "刷新",
    "pluginPlatform.common.remove": "删除",
    "pluginPlatform.common.confirm": "确认",
    "pluginPlatform.common.all": "全部",
    "pluginPlatform.common.readOnly": "只读",

    "pluginPlatform.applySystem.wes": "WES",
    "pluginPlatform.applySystem.station": "工位",

    "pluginPlatform.workbench.title": "插件工作台",
    "pluginPlatform.workbench.noPlugin": "未选择插件",
    "pluginPlatform.workbench.noPluginHint": "请从插件列表打开插件，或新建插件。",
    "pluginPlatform.workbench.newPlugin": "新建插件",
    "pluginPlatform.workbench.backToList": "插件列表",
    "pluginPlatform.workbench.aiAssistant": "AI 助手",
    "pluginPlatform.workbench.refresh": "刷新",
    "pluginPlatform.workbench.sdkVersion": "SDK 版本",
    "pluginPlatform.workbench.pluginKey": "插件标识",
    "pluginPlatform.workbench.applySystem": "适用系统",
    "pluginPlatform.workbench.applyModule": "适用模块",
    "pluginPlatform.workbench.currentRevision": "当前版本",
    "pluginPlatform.workbench.updatedAt": "更新时间",
    "pluginPlatform.workbench.actions": "操作",
    "pluginPlatform.workbench.tab.overview": "概览",
    "pluginPlatform.workbench.tab.scripts": "脚本",
    "pluginPlatform.workbench.tab.revisions": "版本",
    "pluginPlatform.workbench.tab.config": "配置",
    "pluginPlatform.workbench.tab.testRun": "试运行",
    "pluginPlatform.workbench.tab.executions": "执行记录",
    "pluginPlatform.workbench.tab.aiHistory": "AI 记录",
    "pluginPlatform.workbench.tab.audit": "操作审计",

    "pluginPlatform.overview.title": "插件信息",
    "pluginPlatform.overview.metadata": "编辑元数据",
    "pluginPlatform.overview.name": "名称",
    "pluginPlatform.overview.description": "描述",
    "pluginPlatform.overview.saveMetadata": "保存元数据",
    "pluginPlatform.overview.metadataSaved": "插件元数据已保存。",
    "pluginPlatform.overview.validate": "校验",
    "pluginPlatform.overview.enable": "启用",
    "pluginPlatform.overview.disable": "停用",
    "pluginPlatform.overview.archive": "归档",
    "pluginPlatform.overview.rollback": "回滚",
    "pluginPlatform.overview.disableTitle": "停用插件",
    "pluginPlatform.overview.disableReason": "原因（可选）",
    "pluginPlatform.overview.enableConfirm": "校验并启用该插件？",
    "pluginPlatform.overview.archiveConfirm": "归档该插件？归档后只读。",
    "pluginPlatform.overview.rollbackTitle": "回滚到指定版本",
    "pluginPlatform.overview.rollbackRevision": "版本",
    "pluginPlatform.overview.enabled": "插件已启用。",
    "pluginPlatform.overview.disabled": "插件已停用。",
    "pluginPlatform.overview.archived": "插件已归档。",
    "pluginPlatform.overview.rolledBack": "插件已回滚。",
    "pluginPlatform.overview.validationFinished": "校验完成。",
    "pluginPlatform.overview.noRevisions": "该插件暂无版本。",
    "pluginPlatform.overview.statusActions": "状态操作",
    "pluginPlatform.overview.noPermission": "该操作需要插件管理员角色。",

    "pluginPlatform.scripts.files": "脚本文件",
    "pluginPlatform.scripts.newFile": "新文件名",
    "pluginPlatform.scripts.addFile": "添加文件",
    "pluginPlatform.scripts.deleteFile": "删除文件",
    "pluginPlatform.scripts.deleteFileConfirm": "删除脚本文件“{{file}}”？",
    "pluginPlatform.scripts.empty": "暂无脚本文件，请添加文件。",
    "pluginPlatform.scripts.declarations": "扩展声明",
    "pluginPlatform.scripts.addDeclaration": "添加声明",
    "pluginPlatform.scripts.configSchema": "配置定义",
    "pluginPlatform.scripts.addConfigField": "添加配置项",
    "pluginPlatform.scripts.changeSummary": "变更说明",
    "pluginPlatform.scripts.saveRevision": "保存版本",
    "pluginPlatform.scripts.revisionSaved": "版本已保存。",
    "pluginPlatform.scripts.readOnlyEnabled": "请先停用插件再保存新版本。",
    "pluginPlatform.scripts.readOnlyArchived": "已归档插件只读。",
    "pluginPlatform.scripts.noPermission": "编辑脚本需要插件开发者角色。",
    "pluginPlatform.scripts.missingScriptFile": "声明“{{declaration}}”引用了不存在的脚本文件。",
    "pluginPlatform.scripts.noScripts": "版本至少需要一个脚本文件。",
    "pluginPlatform.scripts.defaultFile": "script.groovy",
    "pluginPlatform.scripts.fileExists": "同名脚本文件已存在。",

    "pluginPlatform.declaration.type": "类型",
    "pluginPlatform.declaration.script": "脚本文件",
    "pluginPlatform.declaration.method": "方法",
    "pluginPlatform.declaration.priority": "优先级",
    "pluginPlatform.declaration.details": "详情",
    "pluginPlatform.declaration.apiType": "API 类型",
    "pluginPlatform.declaration.entityName": "实体",
    "pluginPlatform.declaration.customApiCode": "自定义 API 编码",
    "pluginPlatform.declaration.path": "路径",
    "pluginPlatform.declaration.cron": "Cron 表达式",
    "pluginPlatform.declaration.empty": "暂无声明。",
    "pluginPlatform.declaration.scriptRequired": "声明需要脚本文件。",
    "pluginPlatform.declaration.methodRequired": "声明需要方法名。",
    "pluginPlatform.declaration.apiTypeRequired": "请求类声明需要 API 类型。",
    "pluginPlatform.declaration.entityRequired": "生命周期监听需要实体。",
    "pluginPlatform.declaration.customApiRequired": "自定义 API 声明需要自定义 API 编码。",
    "pluginPlatform.declaration.pathRequired": "HTTP 端点路径需以 '/' 开头。",
    "pluginPlatform.declaration.cronRequired": "定时任务声明需要 Cron 表达式。",

    "pluginPlatform.configSchema.field": "字段",
    "pluginPlatform.configSchema.type": "类型",
    "pluginPlatform.configSchema.required": "必填",
    "pluginPlatform.configSchema.defaultValue": "默认值",
    "pluginPlatform.configSchema.empty": "暂无配置项。",
    "pluginPlatform.configSchema.fieldRequired": "配置项需要名称。",
    "pluginPlatform.configSchema.typeRequired": "配置项“{{field}}”需要类型。",
    "pluginPlatform.configSchema.duplicate": "配置项“{{field}}”重复。",

    "pluginPlatform.revisions.revisionNo": "版本号",
    "pluginPlatform.revisions.origin": "来源",
    "pluginPlatform.revisions.createdBy": "创建人",
    "pluginPlatform.revisions.createdAt": "创建时间",
    "pluginPlatform.revisions.hash": "内容哈希",
    "pluginPlatform.revisions.changeSummary": "变更说明",
    "pluginPlatform.revisions.selectTwo": "请选择两个版本进行对比。",
    "pluginPlatform.revisions.base": "基准",
    "pluginPlatform.revisions.target": "目标",
    "pluginPlatform.revisions.rollback": "回滚",
    "pluginPlatform.revisions.empty": "暂无版本。",
    "pluginPlatform.revisions.file": "文件",
    "pluginPlatform.revisions.compareTitle": "版本对比",
    "pluginPlatform.revisions.rollbackTitle": "回滚到指定版本",
    "pluginPlatform.revisions.missingFile": "所选版本中有一方不存在该文件，缺失的一侧为空。",

    "pluginPlatform.config.title": "配置",
    "pluginPlatform.config.empty": "该插件未声明配置项。",
    "pluginPlatform.config.save": "保存配置",
    "pluginPlatform.config.saved": "配置已保存。",
    "pluginPlatform.config.adminOnly": "仅插件管理员可查看配置。",
    "pluginPlatform.config.unsupportedType": "不支持的配置类型“{{type}}”。",

    "pluginPlatform.testRun.title": "试运行",
    "pluginPlatform.testRun.warning": "试运行会使用下方参数调用真实宿主服务，请勿使用生产数据。",
    "pluginPlatform.testRun.wesOnly": "仅 WES 插件支持试运行。",
    "pluginPlatform.testRun.noRevision": "该插件暂无可用版本。",
    "pluginPlatform.testRun.declaration": "扩展点",
    "pluginPlatform.testRun.arguments": "参数（JSON）",
    "pluginPlatform.testRun.invalidArguments": "参数必须是 JSON 对象。",
    "pluginPlatform.testRun.run": "运行",
    "pluginPlatform.testRun.running": "运行中...",
    "pluginPlatform.testRun.success": "成功",
    "pluginPlatform.testRun.failure": "失败",
    "pluginPlatform.testRun.duration": "耗时",
    "pluginPlatform.testRun.truncated": "结果已截断至 8 KB。",
    "pluginPlatform.testRun.noDeclarations": "当前版本未声明扩展点。",

    "pluginPlatform.executions.title": "执行记录",
    "pluginPlatform.executions.total": "执行总数",
    "pluginPlatform.executions.failed": "失败数",
    "pluginPlatform.executions.failureRate": "失败率",
    "pluginPlatform.executions.avgDuration": "平均耗时",
    "pluginPlatform.executions.maxDuration": "最大耗时",
    "pluginPlatform.executions.byType": "按扩展类型",
    "pluginPlatform.executions.time": "时间",
    "pluginPlatform.executions.type": "类型",
    "pluginPlatform.executions.result": "结果",
    "pluginPlatform.executions.duration": "耗时",
    "pluginPlatform.executions.revision": "版本",
    "pluginPlatform.executions.error": "错误",
    "pluginPlatform.executions.success": "成功",
    "pluginPlatform.executions.failure": "失败",
    "pluginPlatform.executions.hours": "时间范围",
    "pluginPlatform.executions.lastHour": "最近 1 小时",
    "pluginPlatform.executions.last24Hours": "最近 24 小时",
    "pluginPlatform.executions.last7Days": "最近 7 天",
    "pluginPlatform.executions.empty": "暂无执行记录。",

    "pluginPlatform.aiHistory.title": "AI 记录",
    "pluginPlatform.aiHistory.user": "用户",
    "pluginPlatform.aiHistory.model": "模型",
    "pluginPlatform.aiHistory.promptVersion": "提示词版本",
    "pluginPlatform.aiHistory.status": "状态",
    "pluginPlatform.aiHistory.attempts": "尝试次数",
    "pluginPlatform.aiHistory.tokens": "Token",
    "pluginPlatform.aiHistory.duration": "耗时",
    "pluginPlatform.aiHistory.createdAt": "创建时间",
    "pluginPlatform.aiHistory.finishedAt": "完成时间",
    "pluginPlatform.aiHistory.error": "错误",
    "pluginPlatform.aiHistory.empty": "暂无 AI 生成记录。",

    "pluginPlatform.audit.title": "插件审计",
    "pluginPlatform.audit.plugin": "插件",
    "pluginPlatform.audit.action": "操作",
    "pluginPlatform.audit.operator": "操作人",
    "pluginPlatform.audit.success": "结果",
    "pluginPlatform.audit.detail": "详情",
    "pluginPlatform.audit.createdAt": "时间",
    "pluginPlatform.audit.revision": "版本",
    "pluginPlatform.audit.empty": "暂无审计记录。",
    "pluginPlatform.audit.successValue": "成功",
    "pluginPlatform.audit.failureValue": "失败",

    "pluginPlatform.chat.title": "AI 助手",
    "pluginPlatform.chat.baseRevision": "基准版本",
    "pluginPlatform.chat.baseRevisionNone": "无基准版本",
    "pluginPlatform.chat.requirementPlaceholder": "描述插件需要完成的功能...",
    "pluginPlatform.chat.generate": "生成",
    "pluginPlatform.chat.cancel": "取消生成",
    "pluginPlatform.chat.streaming": "生成中...",
    "pluginPlatform.chat.completed": "生成完成，已保存版本 {{no}}。",
    "pluginPlatform.chat.cancelled": "生成已取消。",
    "pluginPlatform.chat.requirementRequired": "请输入需求描述。",
    "pluginPlatform.chat.requirementTooLong": "需求描述超出 AI 接口长度限制。",
    "pluginPlatform.chat.noPermission": "使用 AI 生成需要插件开发者角色。",
    "pluginPlatform.chat.stage": "阶段",
    "pluginPlatform.chat.output": "模型输出",

    "pluginPlatform.wizard.title": "新建插件",
    "pluginPlatform.wizard.step.basics": "基本信息",
    "pluginPlatform.wizard.step.requirement": "需求与 AI",
    "pluginPlatform.wizard.step.review": "检查",
    "pluginPlatform.wizard.step.finish": "完成",
    "pluginPlatform.wizard.basics.pluginKey": "插件标识",
    "pluginPlatform.wizard.basics.pluginKeyHint": "仅允许字母、数字、'.'、'_' 和 '-'。",
    "pluginPlatform.wizard.basics.pluginKeyRequired": "请输入插件标识。",
    "pluginPlatform.wizard.basics.pluginKeyInvalid": "仅允许字母、数字、'.'、'_' 和 '-'。",
    "pluginPlatform.wizard.basics.name": "名称",
    "pluginPlatform.wizard.basics.nameRequired": "请输入名称。",
    "pluginPlatform.wizard.basics.description": "描述",
    "pluginPlatform.wizard.basics.applySystem": "适用系统",
    "pluginPlatform.wizard.basics.applySystemRequired": "请选择适用系统。",
    "pluginPlatform.wizard.basics.applyModule": "适用模块",
    "pluginPlatform.wizard.requirement.title": "需求与 AI 生成",
    "pluginPlatform.wizard.requirement.hints": "扩展类型提示",
    "pluginPlatform.wizard.requirement.hintsHint": "可选：所选扩展类型会追加到需求描述中。",
    "pluginPlatform.wizard.requirement.skipAi": "跳过 AI 继续",
    "pluginPlatform.wizard.requirement.created": "草稿已创建。",
    "pluginPlatform.wizard.requirement.result": "AI 结果",
    "pluginPlatform.wizard.requirement.resultSummary": "已创建版本 {{no}}。",
    "pluginPlatform.wizard.requirement.stage.declarations": "声明",
    "pluginPlatform.wizard.requirement.stage.compiling": "编译",
    "pluginPlatform.wizard.requirement.stage.wiring": "装配",
    "pluginPlatform.wizard.requirement.stage.retry": "重试 {{n}}",
    "pluginPlatform.wizard.review.title": "检查",
    "pluginPlatform.wizard.review.materializationVerified": "后端试运行已验证可物化。",
    "pluginPlatform.wizard.review.materializationUnverified": "未验证物化，启用前请先运行校验。",
    "pluginPlatform.wizard.review.saveRevision": "保存版本",
    "pluginPlatform.wizard.review.saved": "版本已保存。",
    "pluginPlatform.wizard.review.next": "下一步",
    "pluginPlatform.wizard.finish.title": "完成",
    "pluginPlatform.wizard.finish.runValidation": "运行校验",
    "pluginPlatform.wizard.finish.enableNow": "立即启用",
    "pluginPlatform.wizard.finish.developerHint": "草稿已保存，需要管理员校验并启用。",
    "pluginPlatform.wizard.finish.finish": "关闭",
    "pluginPlatform.wizard.finish.validationRequired": "启用前请先通过校验。",
    "pluginPlatform.wizard.finish.enabled": "插件已启用。",
    "pluginPlatform.wizard.cancel": "取消",
    "pluginPlatform.wizard.cancelConfirm": "关闭向导？草稿会保留。",
    "pluginPlatform.wizard.noPermission": "创建插件需要插件开发者角色。"
```

- [ ] **Step 11: Verify the foundation**

Run: `cd client && npx tsc --noEmit --skipLibCheck`
Expected: exactly the four baseline errors listed in Global Constraints; no errors mentioning `plugin_platform` or `react-i18next-config`.

Run: `cd client && node -e "const en=require('./src/locales/en-us.json'); const zh=require('./src/locales/zh-cn.json'); const missing=Object.keys(en).filter(k=>k.startsWith('pluginPlatform.')&&!(k in zh)); console.log(missing.length);"`
Expected: `0`

- [ ] **Step 12: Commit**

```bash
git add client/src/pages/plugin_platform/types.ts \
  client/src/pages/plugin_platform/api.ts \
  client/src/pages/plugin_platform/sse.ts \
  client/src/pages/plugin_platform/labels.ts \
  client/src/pages/plugin_platform/format.ts \
  client/src/pages/plugin_platform/usePluginCapabilities.ts \
  client/src/pages/plugin_platform/PluginStatusTag.tsx \
  client/src/react-i18next-config.ts \
  client/src/locales/en-us.json client/src/locales/zh-cn.json
git commit -m "feat(script-plugin): add typed workbench API layer and locale block

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 2: Shared Editors, Row Models and Schema Tables

**Files:**
- Create: `client/src/pages/plugin_platform/formModels.ts`
- Create: `client/src/pages/plugin_platform/ScriptEditor.tsx`
- Create: `client/src/pages/plugin_platform/DiffViewer.tsx`
- Create: `client/src/pages/plugin_platform/ScriptFileEditor.tsx`
- Create: `client/src/pages/plugin_platform/DeclarationTable.tsx`
- Create: `client/src/pages/plugin_platform/ConfigSchemaTable.tsx`

**Interfaces:**
- Consumes: `ExtensionDeclaration`, `ConfigField`, `ExtensionType`, `PluginArchetype`, `Scripts` from `./types`; `EXTENSION_TYPE_LABEL_KEYS` from `./labels`; `schema2component` from `@/utils/schema2component`.
- Produces:
  - `DeclarationRow`, `ConfigRow`, `RowIssue`
  - `EXTENSION_TYPES`, `DEFAULT_METHOD_BY_TYPE`, `DEFAULT_LIFECYCLE_ENTITY_NAMES`, `CONFIG_FIELD_TYPES`, `isRequestType`, `newDeclarationRowKey`, `newConfigRowKey`
  - `toDeclarationRows(declarations)`, `fromDeclarationRows(rows)`, `validateDeclarationRows(rows, scriptNames)`
  - `toConfigRows(fields)`, `fromConfigRows(rows)`, `validateConfigRows(rows)`
  - `<ScriptEditor initialValue readOnly? onChange? />`
  - `<DiffViewer left right viewKey />`
  - `<ScriptFileEditor scripts onChange readOnly seedKey />`
  - `<DeclarationTable rows onChange readOnly allowedTypes lifecycleEntityNames />`
  - `<ConfigSchemaTable rows onChange readOnly />`

- [ ] **Step 1: Create `formModels.ts`**

```ts
import { ConfigField, ExtensionDeclaration, ExtensionType } from "./types"

export interface RowIssue {
    key: string
    values?: Record<string, string>
}

export interface DeclarationRow {
    rowKey: string
    type: ExtensionType
    priority: number
    script: string
    method: string
    apiType: string
    entityName: string
    customApiCode: string
    httpMethod: string
    path: string
    cron: string
}

export interface ConfigRow {
    rowKey: string
    field: string
    type: string
    required: boolean
    defaultValue: string
}

export const EXTENSION_TYPES: ExtensionType[] = [
    "OUTBOUND_WAVE_WAVE_ACTION",
    "OUTBOUND_WAVE_SPLIT_ACTION",
    "REQUEST_VALIDATE",
    "REQUEST_SUPPLY",
    "REQUEST_AFTER_INVOKE",
    "CUSTOM_REQUEST_INVOKE",
    "CUSTOM_API",
    "LIFECYCLE_LISTENER",
    "HTTP_ENDPOINT",
    "CRON"
]

// Mirrors PluginContractCatalog.ARCHETYPES.scriptsMethod on the server.
export const DEFAULT_METHOD_BY_TYPE: Record<ExtensionType, string> = {
    OUTBOUND_WAVE_WAVE_ACTION: "wave",
    OUTBOUND_WAVE_SPLIT_ACTION: "split",
    REQUEST_VALIDATE: "validate",
    REQUEST_SUPPLY: "supply",
    REQUEST_AFTER_INVOKE: "afterInvoke",
    CUSTOM_REQUEST_INVOKE: "invoke",
    CUSTOM_API: "execute",
    LIFECYCLE_LISTENER: "afterStatusChange",
    HTTP_ENDPOINT: "handle",
    CRON: "run"
}

export const DEFAULT_LIFECYCLE_ENTITY_NAMES: string[] = [
    "OutboundPlanOrder",
    "OutboundWave",
    "ContainerTask"
]

// Mirrors ScriptPluginConfigValidator: STRING, INT, INTEGER, NUMBER, BOOLEAN.
export const CONFIG_FIELD_TYPES: string[] = ["STRING", "INT", "INTEGER", "NUMBER", "BOOLEAN"]

let declarationSequence = 0
let configSequence = 0

export function newDeclarationRowKey(): string {
    declarationSequence += 1
    return "declaration-row-" + declarationSequence
}

export function newConfigRowKey(): string {
    configSequence += 1
    return "config-row-" + configSequence
}

export function isRequestType(type: ExtensionType): boolean {
    return type === "REQUEST_VALIDATE" || type === "REQUEST_SUPPLY" || type === "REQUEST_AFTER_INVOKE"
}

export function toDeclarationRows(declarations: ExtensionDeclaration[]): DeclarationRow[] {
    return declarations.map((declaration) => ({
        rowKey: newDeclarationRowKey(),
        type: declaration.type,
        priority: declaration.priority,
        script: declaration.script || "",
        method: declaration.method || "",
        apiType: declaration.apiType || "",
        entityName: declaration.entityName || "",
        customApiCode: declaration.customApiCode || "",
        httpMethod: declaration.httpMethod || "",
        path: declaration.path || "",
        cron: declaration.cron || ""
    }))
}

function nullable(value: string): string | null {
    const trimmed = value.trim()
    return trimmed === "" ? null : trimmed
}

export function fromDeclarationRows(rows: DeclarationRow[]): ExtensionDeclaration[] {
    return rows.map((row) => ({
        type: row.type,
        priority: row.priority,
        script: row.script.trim(),
        method: row.method.trim(),
        apiType: isRequestType(row.type) ? nullable(row.apiType) : null,
        entityName: row.type === "LIFECYCLE_LISTENER" ? nullable(row.entityName) : null,
        customApiCode: row.type === "CUSTOM_API" ? nullable(row.customApiCode) : null,
        httpMethod: row.type === "HTTP_ENDPOINT" ? nullable(row.httpMethod) : null,
        path: row.type === "HTTP_ENDPOINT" ? nullable(row.path) : null,
        cron: row.type === "CRON" ? nullable(row.cron) : null
    }))
}

export function validateDeclarationRows(rows: DeclarationRow[], scriptNames: string[]): RowIssue | null {
    for (let index = 0; index < rows.length; index++) {
        const row = rows[index]
        const script = row.script.trim()
        if (script === "") {
            return { key: "pluginPlatform.declaration.scriptRequired" }
        }
        if (scriptNames.indexOf(script) < 0) {
            return { key: "pluginPlatform.scripts.missingScriptFile", values: { declaration: script } }
        }
        if (row.method.trim() === "") {
            return { key: "pluginPlatform.declaration.methodRequired" }
        }
        if (isRequestType(row.type) && row.apiType.trim() === "") {
            return { key: "pluginPlatform.declaration.apiTypeRequired" }
        }
        if (row.type === "LIFECYCLE_LISTENER" && row.entityName.trim() === "") {
            return { key: "pluginPlatform.declaration.entityRequired" }
        }
        if (row.type === "CUSTOM_API" && row.customApiCode.trim() === "") {
            return { key: "pluginPlatform.declaration.customApiRequired" }
        }
        if (row.type === "HTTP_ENDPOINT") {
            const path = row.path.trim()
            if (!path || path.charAt(0) !== "/") {
                return { key: "pluginPlatform.declaration.pathRequired" }
            }
        }
        if (row.type === "CRON" && row.cron.trim() === "") {
            return { key: "pluginPlatform.declaration.cronRequired" }
        }
    }
    return null
}

export function toConfigRows(fields: ConfigField[]): ConfigRow[] {
    return fields.map((field) => ({
        rowKey: newConfigRowKey(),
        field: field.field || "",
        type: field.type || "STRING",
        required: field.required,
        defaultValue: field.defaultValue === null || field.defaultValue === undefined ? "" : field.defaultValue
    }))
}

export function fromConfigRows(rows: ConfigRow[]): ConfigField[] {
    return rows.map((row) => ({
        field: row.field.trim(),
        type: row.type,
        required: row.required,
        defaultValue: nullable(row.defaultValue)
    }))
}

export function validateConfigRows(rows: ConfigRow[]): RowIssue | null {
    const seen: string[] = []
    for (let index = 0; index < rows.length; index++) {
        const row = rows[index]
        const field = row.field.trim()
        if (field === "") {
            return { key: "pluginPlatform.configSchema.fieldRequired" }
        }
        if (seen.indexOf(field) >= 0) {
            return { key: "pluginPlatform.configSchema.duplicate", values: { field } }
        }
        seen.push(field)
        if (CONFIG_FIELD_TYPES.indexOf(row.type) < 0) {
            return { key: "pluginPlatform.configSchema.typeRequired", values: { field } }
        }
    }
    return null
}
```

- [ ] **Step 2: Create `ScriptEditor.tsx`**

```tsx
import React, { useCallback, useRef } from "react"
import { Schema } from "amis/lib/types"
import schema2component from "@/utils/schema2component"

interface AmisEditorFormProps {
    data: Record<string, unknown>
    onChange?: (data: Record<string, unknown>) => void
}

function buildSchema(disabled: boolean): Schema {
    return {
        type: "form",
        wrapWithPanel: false,
        submitOnChange: false,
        body: [
            {
                type: "editor",
                name: "source",
                // AMIS bundles no Groovy grammar; Java highlighting is the closest match.
                language: "java",
                size: "lg",
                disabled,
                allowFullscreen: false,
                options: {
                    automaticLayout: true,
                    minimap: { enabled: false },
                    scrollBeyondLastLine: false,
                    wordWrap: "on",
                    fontSize: 13,
                    tabSize: 4
                }
            }
        ]
    }
}

const EditableAmisEditor = schema2component(buildSchema(false)) as React.FC<AmisEditorFormProps>
const ReadOnlyAmisEditor = schema2component(buildSchema(true)) as React.FC<AmisEditorFormProps>

interface ScriptEditorProps {
    initialValue: string
    readOnly?: boolean
    onChange?: (value: string) => void
}

/**
 * AMIS editor renderer over the Monaco build AMIS already bundles. The seed value is captured once
 * per mount; the parent changes files or reloads by remounting this component with a new key.
 */
const ScriptEditor: React.FC<ScriptEditorProps> = ({ initialValue, readOnly, onChange }) => {
    const initialData = useRef<Record<string, unknown>>({ source: initialValue }).current
    const onChangeRef = useRef(onChange)
    onChangeRef.current = onChange

    const handleFormChange = useCallback((data: Record<string, unknown>) => {
        const next = data.source
        if (onChangeRef.current) {
            onChangeRef.current(typeof next === "string" ? next : "")
        }
    }, [])

    return (
        <div style={{ maxHeight: 520, overflow: "auto", border: "1px solid #d9d9d9", borderRadius: 2 }}>
            {readOnly ? (
                <ReadOnlyAmisEditor data={initialData} />
            ) : (
                <EditableAmisEditor data={initialData} onChange={handleFormChange} />
            )}
        </div>
    )
}

export default ScriptEditor
```

- [ ] **Step 3: Create `DiffViewer.tsx`**

```tsx
import React, { useMemo } from "react"
import { Schema } from "amis/lib/types"
import schema2component from "@/utils/schema2component"

const diffSchema: Schema = {
    type: "form",
    wrapWithPanel: false,
    submitOnChange: false,
    body: [
        {
            type: "diff-editor",
            name: "diff",
            language: "java",
            disabled: true,
            // AMIS renders diffValue as the left/original pane and value as the right/modified pane.
            value: "${right}",
            diffValue: "${left}",
            options: {
                automaticLayout: true,
                minimap: { enabled: false },
                scrollBeyondLastLine: false,
                readOnly: true
            }
        }
    ]
}

interface AmisDiffFormProps {
    data: Record<string, string>
}

const AmisDiffForm = schema2component(diffSchema) as React.FC<AmisDiffFormProps>

interface DiffViewerProps {
    left: string
    right: string
    /** Changes only when the compared revisions or file change, forcing a fresh render. */
    viewKey: string
}

const DiffViewer: React.FC<DiffViewerProps> = ({ left, right, viewKey }) => {
    const data = useMemo(() => ({ left, right }), [left, right])
    return (
        <div style={{ border: "1px solid #d9d9d9", borderRadius: 2, overflow: "hidden" }}>
            <AmisDiffForm key={viewKey} data={data} />
        </div>
    )
}

export default DiffViewer
```

- [ ] **Step 4: Create `ScriptFileEditor.tsx`**

```tsx
import React, { useEffect, useState } from "react"
import { Alert, Button, Input, Popconfirm, Select, Space, message } from "antd"
import { useTranslation } from "react-i18next"
import { Scripts } from "./types"
import ScriptEditor from "./ScriptEditor"

interface ScriptFileEditorProps {
    scripts: Scripts
    onChange: (next: Scripts) => void
    readOnly: boolean
    /** Changes when the underlying revision changes, forcing a reseed and editor remount. */
    seedKey: string
}

const ScriptFileEditor: React.FC<ScriptFileEditorProps> = ({ scripts, onChange, readOnly, seedKey }) => {
    const { t } = useTranslation()
    const [activeFile, setActiveFile] = useState<string | null>(null)
    const [newFileName, setNewFileName] = useState("")
    const [editorVersion, setEditorVersion] = useState(0)

    useEffect(() => {
        const names = Object.keys(scripts)
        setActiveFile((previous) => {
            if (previous !== null && names.indexOf(previous) >= 0) {
                return previous
            }
            return names.length > 0 ? names[0] : null
        })
        setEditorVersion((version) => version + 1)
        setNewFileName("")
    }, [seedKey])

    const fileNames = Object.keys(scripts)

    const selectFile = (name: string) => {
        setActiveFile(name)
        setEditorVersion((version) => version + 1)
    }

    const addFile = () => {
        const name = newFileName.trim()
        if (name === "") {
            return
        }
        if (Object.prototype.hasOwnProperty.call(scripts, name)) {
            message.error(t("pluginPlatform.scripts.fileExists"))
            return
        }
        onChange({ ...scripts, [name]: "" })
        setActiveFile(name)
        setNewFileName("")
        setEditorVersion((version) => version + 1)
    }

    const deleteFile = () => {
        if (activeFile === null) {
            return
        }
        const next: Scripts = { ...scripts }
        delete next[activeFile]
        onChange(next)
        const remaining = Object.keys(next)
        setActiveFile(remaining.length > 0 ? remaining[0] : null)
        setEditorVersion((version) => version + 1)
    }

    const handleEditorChange = (value: string) => {
        if (activeFile === null) {
            return
        }
        onChange({ ...scripts, [activeFile]: value })
    }

    return (
        <Space direction="vertical" style={{ width: "100%" }} size="middle">
            <Space wrap>
                <Select
                    style={{ minWidth: 240 }}
                    value={activeFile === null ? undefined : activeFile}
                    onChange={selectFile}
                    options={fileNames.map((name) => ({ label: name, value: name }))}
                    placeholder={t("pluginPlatform.scripts.empty")}
                />
                {!readOnly && (
                    <>
                        <Input
                            style={{ width: 200 }}
                            value={newFileName}
                            onChange={(event) => setNewFileName(event.target.value)}
                            placeholder={t("pluginPlatform.scripts.newFile")}
                            onPressEnter={addFile}
                        />
                        <Button onClick={addFile} disabled={newFileName.trim() === ""}>
                            {t("pluginPlatform.scripts.addFile")}
                        </Button>
                        <Popconfirm
                            title={t("pluginPlatform.scripts.deleteFileConfirm", { file: activeFile || "" })}
                            onConfirm={deleteFile}
                            disabled={activeFile === null}
                            okText={t("pluginPlatform.common.confirm")}
                            cancelText={t("pluginPlatform.common.cancel")}
                        >
                            <Button danger disabled={activeFile === null}>
                                {t("pluginPlatform.scripts.deleteFile")}
                            </Button>
                        </Popconfirm>
                    </>
                )}
            </Space>
            {activeFile === null ? (
                <Alert type="info" showIcon message={t("pluginPlatform.scripts.empty")} />
            ) : (
                <ScriptEditor
                    key={activeFile + ":" + editorVersion}
                    initialValue={scripts[activeFile] || ""}
                    readOnly={readOnly}
                    onChange={readOnly ? undefined : handleEditorChange}
                />
            )}
        </Space>
    )
}

export default ScriptFileEditor
```

- [ ] **Step 5: Create `DeclarationTable.tsx`**

```tsx
import React from "react"
import { Button, Input, InputNumber, Select, Space, Table, Tag } from "antd"
import { ColumnsType } from "antd/lib/table"
import { useTranslation } from "react-i18next"
import { ExtensionType } from "./types"
import { EXTENSION_TYPE_LABEL_KEYS } from "./labels"
import {
    DEFAULT_LIFECYCLE_ENTITY_NAMES,
    DEFAULT_METHOD_BY_TYPE,
    DeclarationRow,
    EXTENSION_TYPES,
    isRequestType,
    newDeclarationRowKey
} from "./formModels"

interface DeclarationTableProps {
    rows: DeclarationRow[]
    onChange: (rows: DeclarationRow[]) => void
    readOnly: boolean
    allowedTypes: ExtensionType[]
    lifecycleEntityNames: string[]
}

const HTTP_METHODS = ["GET", "POST", "PUT", "PATCH", "DELETE"]

const DeclarationTable: React.FC<DeclarationTableProps> = ({
    rows,
    onChange,
    readOnly,
    allowedTypes,
    lifecycleEntityNames
}) => {
    const { t } = useTranslation()
    const typeOptions = allowedTypes.length > 0 ? allowedTypes : EXTENSION_TYPES
    const entityOptions = lifecycleEntityNames.length > 0 ? lifecycleEntityNames : DEFAULT_LIFECYCLE_ENTITY_NAMES

    const update = (rowKey: string, patch: Partial<DeclarationRow>) => {
        onChange(rows.map((row) => (row.rowKey === rowKey ? { ...row, ...patch } : row)))
    }

    const changeType = (row: DeclarationRow, type: ExtensionType) => {
        const defaultMethod = DEFAULT_METHOD_BY_TYPE[type]
        const method = row.method.trim() === "" || row.method === DEFAULT_METHOD_BY_TYPE[row.type]
            ? defaultMethod
            : row.method
        update(row.rowKey, { type, method })
    }

    const addRow = () => {
        const type = typeOptions[0] || "HTTP_ENDPOINT"
        onChange([
            ...rows,
            {
                rowKey: newDeclarationRowKey(),
                type,
                priority: 100,
                script: "",
                method: DEFAULT_METHOD_BY_TYPE[type],
                apiType: "",
                entityName: type === "LIFECYCLE_LISTENER" ? entityOptions[0] || "" : "",
                customApiCode: "",
                httpMethod: type === "HTTP_ENDPOINT" ? "POST" : "",
                path: "",
                cron: ""
            }
        ])
    }

    const details = (row: DeclarationRow): React.ReactNode => {
        if (readOnly) {
            if (isRequestType(row.type)) {
                return row.apiType
            }
            if (row.type === "LIFECYCLE_LISTENER") {
                return row.entityName
            }
            if (row.type === "CUSTOM_API") {
                return row.customApiCode
            }
            if (row.type === "HTTP_ENDPOINT") {
                return (row.httpMethod || "") + " " + (row.path || "")
            }
            if (row.type === "CRON") {
                return row.cron
            }
            return "-"
        }
        if (isRequestType(row.type)) {
            return (
                <Input
                    value={row.apiType}
                    onChange={(event) => update(row.rowKey, { apiType: event.target.value })}
                    placeholder={t("pluginPlatform.declaration.apiType")}
                />
            )
        }
        if (row.type === "LIFECYCLE_LISTENER") {
            return (
                <Select
                    style={{ width: "100%" }}
                    value={row.entityName || undefined}
                    onChange={(value: string) => update(row.rowKey, { entityName: value })}
                    options={entityOptions.map((name) => ({ label: name, value: name }))}
                    placeholder={t("pluginPlatform.declaration.entityName")}
                />
            )
        }
        if (row.type === "CUSTOM_API") {
            return (
                <Input
                    value={row.customApiCode}
                    onChange={(event) => update(row.rowKey, { customApiCode: event.target.value })}
                    placeholder={t("pluginPlatform.declaration.customApiCode")}
                />
            )
        }
        if (row.type === "HTTP_ENDPOINT") {
            return (
                <Space.Compact style={{ width: "100%" }}>
                    <Select
                        style={{ width: 110 }}
                        value={row.httpMethod || "POST"}
                        onChange={(value: string) => update(row.rowKey, { httpMethod: value })}
                        options={HTTP_METHODS.map((method) => ({ label: method, value: method }))}
                    />
                    <Input
                        value={row.path}
                        onChange={(event) => update(row.rowKey, { path: event.target.value })}
                        placeholder={t("pluginPlatform.declaration.path")}
                    />
                </Space.Compact>
            )
        }
        if (row.type === "CRON") {
            return (
                <Input
                    value={row.cron}
                    onChange={(event) => update(row.rowKey, { cron: event.target.value })}
                    placeholder={t("pluginPlatform.declaration.cron")}
                />
            )
        }
        return "-"
    }

    const columns: ColumnsType<DeclarationRow> = [
        {
            title: t("pluginPlatform.declaration.type"),
            dataIndex: "type",
            width: 230,
            render: (type: ExtensionType, row) =>
                readOnly ? (
                    <Tag>{t(EXTENSION_TYPE_LABEL_KEYS[type])}</Tag>
                ) : (
                    <Select
                        style={{ width: "100%" }}
                        value={type}
                        onChange={(value: ExtensionType) => changeType(row, value)}
                        options={typeOptions.map((option) => ({
                            label: t(EXTENSION_TYPE_LABEL_KEYS[option]),
                            value: option
                        }))}
                    />
                )
        },
        {
            title: t("pluginPlatform.declaration.script"),
            dataIndex: "script",
            width: 170,
            render: (script: string, row) =>
                readOnly ? (
                    script
                ) : (
                    <Input
                        value={script}
                        onChange={(event) => update(row.rowKey, { script: event.target.value })}
                        placeholder={t("pluginPlatform.scripts.defaultFile")}
                    />
                )
        },
        {
            title: t("pluginPlatform.declaration.method"),
            dataIndex: "method",
            width: 150,
            render: (method: string, row) =>
                readOnly ? (
                    method
                ) : (
                    <Input
                        value={method}
                        onChange={(event) => update(row.rowKey, { method: event.target.value })}
                    />
                )
        },
        {
            title: t("pluginPlatform.declaration.priority"),
            dataIndex: "priority",
            width: 110,
            render: (priority: number, row) =>
                readOnly ? (
                    String(priority)
                ) : (
                    <InputNumber
                        min={0}
                        precision={0}
                        style={{ width: "100%" }}
                        value={priority}
                        onChange={(value) =>
                            update(row.rowKey, { priority: typeof value === "number" ? value : 0 })
                        }
                    />
                )
        },
        {
            title: t("pluginPlatform.declaration.details"),
            key: "details",
            render: (_value, row) => details(row)
        },
        {
            title: t("pluginPlatform.workbench.actions"),
            key: "actions",
            width: 90,
            render: (_value, row) =>
                readOnly ? null : (
                    <Button
                        type="link"
                        danger
                        onClick={() => onChange(rows.filter((candidate) => candidate.rowKey !== row.rowKey))}
                    >
                        {t("pluginPlatform.common.remove")}
                    </Button>
                )
        }
    ]

    return (
        <Space direction="vertical" style={{ width: "100%" }}>
            <Table<DeclarationRow>
                rowKey="rowKey"
                size="small"
                pagination={false}
                columns={readOnly ? columns.filter((column) => column.key !== "actions") : columns}
                dataSource={rows}
                scroll={{ x: 1100 }}
                locale={{ emptyText: t("pluginPlatform.declaration.empty") }}
            />
            {!readOnly && (
                <Button type="dashed" onClick={addRow}>
                    {t("pluginPlatform.scripts.addDeclaration")}
                </Button>
            )}
        </Space>
    )
}

export default DeclarationTable
```

- [ ] **Step 6: Create `ConfigSchemaTable.tsx`**

```tsx
import React from "react"
import { Button, Input, Select, Space, Switch, Table } from "antd"
import { ColumnsType } from "antd/lib/table"
import { useTranslation } from "react-i18next"
import { CONFIG_FIELD_TYPES, ConfigRow, newConfigRowKey } from "./formModels"

interface ConfigSchemaTableProps {
    rows: ConfigRow[]
    onChange: (rows: ConfigRow[]) => void
    readOnly: boolean
}

const ConfigSchemaTable: React.FC<ConfigSchemaTableProps> = ({ rows, onChange, readOnly }) => {
    const { t } = useTranslation()

    const update = (rowKey: string, patch: Partial<ConfigRow>) => {
        onChange(rows.map((row) => (row.rowKey === rowKey ? { ...row, ...patch } : row)))
    }

    const addRow = () => {
        onChange([
            ...rows,
            { rowKey: newConfigRowKey(), field: "", type: "STRING", required: false, defaultValue: "" }
        ])
    }

    const columns: ColumnsType<ConfigRow> = [
        {
            title: t("pluginPlatform.configSchema.field"),
            dataIndex: "field",
            width: 220,
            render: (field: string, row) =>
                readOnly ? (
                    field
                ) : (
                    <Input value={field} onChange={(event) => update(row.rowKey, { field: event.target.value })} />
                )
        },
        {
            title: t("pluginPlatform.configSchema.type"),
            dataIndex: "type",
            width: 160,
            render: (type: string, row) =>
                readOnly ? (
                    type
                ) : (
                    <Select
                        style={{ width: "100%" }}
                        value={type}
                        onChange={(value: string) => update(row.rowKey, { type: value })}
                        options={CONFIG_FIELD_TYPES.map((option) => ({ label: option, value: option }))}
                    />
                )
        },
        {
            title: t("pluginPlatform.configSchema.required"),
            dataIndex: "required",
            width: 110,
            render: (required: boolean, row) =>
                readOnly ? (
                    required ? t("pluginPlatform.common.confirm") : "-"
                ) : (
                    <Switch checked={required} onChange={(checked) => update(row.rowKey, { required: checked })} />
                )
        },
        {
            title: t("pluginPlatform.configSchema.defaultValue"),
            dataIndex: "defaultValue",
            render: (defaultValue: string, row) =>
                readOnly ? (
                    defaultValue
                ) : (
                    <Input
                        value={defaultValue}
                        onChange={(event) => update(row.rowKey, { defaultValue: event.target.value })}
                    />
                )
        },
        {
            title: t("pluginPlatform.workbench.actions"),
            key: "actions",
            width: 90,
            render: (_value, row) =>
                readOnly ? null : (
                    <Button
                        type="link"
                        danger
                        onClick={() => onChange(rows.filter((candidate) => candidate.rowKey !== row.rowKey))}
                    >
                        {t("pluginPlatform.common.remove")}
                    </Button>
                )
        }
    ]

    return (
        <Space direction="vertical" style={{ width: "100%" }}>
            <Table<ConfigRow>
                rowKey="rowKey"
                size="small"
                pagination={false}
                columns={readOnly ? columns.filter((column) => column.key !== "actions") : columns}
                dataSource={rows}
                locale={{ emptyText: t("pluginPlatform.configSchema.empty") }}
            />
            {!readOnly && (
                <Button type="dashed" onClick={addRow}>
                    {t("pluginPlatform.scripts.addConfigField")}
                </Button>
            )}
        </Space>
    )
}

export default ConfigSchemaTable
```

**Note:** the read-only "required" cell must not say "Confirm". Use a dedicated key. Add `"pluginPlatform.common.yes": "Yes"` / `"pluginPlatform.common.no": "No"` to both locale files in this task before verifying.

- [ ] **Step 7: Add the yes/no locale keys**

In `en-us.json`, after `"pluginPlatform.common.readOnly"`, add:

```json
    "pluginPlatform.common.yes": "Yes",
    "pluginPlatform.common.no": "No",
```

In `zh-cn.json`, after `"pluginPlatform.common.readOnly"`, add:

```json
    "pluginPlatform.common.yes": "是",
    "pluginPlatform.common.no": "否",
```

Then change the read-only required cell in `ConfigSchemaTable.tsx` to:

```tsx
required ? t("pluginPlatform.common.yes") : t("pluginPlatform.common.no")
```

- [ ] **Step 8: Verify typecheck**

Run: `cd client && npx tsc --noEmit --skipLibCheck`
Expected: only the four baseline errors.

- [ ] **Step 9: Commit**

```bash
git add client/src/pages/plugin_platform/formModels.ts \
  client/src/pages/plugin_platform/ScriptEditor.tsx \
  client/src/pages/plugin_platform/DiffViewer.tsx \
  client/src/pages/plugin_platform/ScriptFileEditor.tsx \
  client/src/pages/plugin_platform/DeclarationTable.tsx \
  client/src/pages/plugin_platform/ConfigSchemaTable.tsx \
  client/src/locales/en-us.json client/src/locales/zh-cn.json
git commit -m "feat(script-plugin): add workbench editors and schema tables

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 3: Plugin List, Routes and Legacy Cleanup

**Files:**
- Create: `client/src/pages/plugin_platform/list/index.tsx`
- Create: `client/src/pages/plugin_platform/workbench/index.tsx` (temporary placeholder; Task 4 replaces it)
- Modify: `client/src/routes/path2Compoment.tsx`
- Modify: `client/src/locales/en-us.json`
- Modify: `client/src/locales/zh-cn.json`
- Delete: `client/src/pages/plugin_platform/plugin_management.tsx`
- Delete: `client/src/pages/plugin_platform/plugin_store.tsx`
- Delete: `client/src/pages/plugin_platform/constants/api_constant.tsx`

**Interfaces:**
- Consumes: `listRevisions` from `../api`; `ScriptPlugin`, `ScriptPluginRevision` from `../types`.
- Produces: the three routes `/plugin-platform/plugins`, `/plugin-platform/plugin-workbench` and the AMIS list page. Task 4 replaces `workbench/index.tsx`; Task 13 adds `/plugin-platform/audit`.

- [ ] **Step 1: Create the AMIS plugin list at `list/index.tsx`**

The list endpoint returns a bare array (`Response<List<ScriptPluginDTO>>`), and `ScriptPluginDTO` carries only `currentRevisionId`, not a revision number. The adaptor resolves the number with one read-only `revisions` call per plugin that has a current revision. Listing requires the developer role, and so do the revisions calls, so the enrichment cannot fail authorization-wise (`PluginWorkbenchAccessService.isDeveloper` treats any admin as a developer, and menuless users never reach this page). It must never fail the list: a failed revisions call degrades that cell to `-`.

```tsx
import schema2component from "@/utils/schema2component"
import { listRevisions } from "../api"
import { ScriptPlugin, ScriptPluginRevision } from "../types"

interface PluginListRow extends ScriptPlugin {
    currentRevisionNo: number | null
}

/**
 * AMIS CRUD adaptor. The interceptor resolves business errors as HTTP 200 envelopes; returning an
 * empty page here keeps AMIS from rendering the envelope as a row and from toasting twice.
 */
async function adaptPluginList(payload: unknown): Promise<{ items: PluginListRow[]; total: number }> {
    const envelope = payload as { code?: string; status?: string; data?: ScriptPlugin[] } | null
    if (!envelope || envelope.status || envelope.code !== "0" || !Array.isArray(envelope.data)) {
        return { items: [], total: 0 }
    }
    const plugins = envelope.data
    const revisionNos: Record<string, number | null> = {}
    await Promise.all(
        plugins.map(async (plugin) => {
            if (!plugin.currentRevisionId) {
                revisionNos[plugin.pluginKey] = null
                return
            }
            const revisions = await listRevisions(plugin.pluginKey).catch(
                () => [] as ScriptPluginRevision[]
            )
            const current = revisions.filter((revision) => revision.id === plugin.currentRevisionId)[0]
            revisionNos[plugin.pluginKey] = current ? current.revisionNo : null
        })
    )
    const items: PluginListRow[] = plugins.map((plugin) => ({
        ...plugin,
        currentRevisionNo: revisionNos[plugin.pluginKey]
    }))
    return { items, total: items.length }
}

const newPluginButton = {
    type: "button",
    level: "primary",
    icon: "fa fa-plus",
    label: "pluginPlatform.list.newPlugin",
    // "link" navigates with history.push; this app's AMIS env opens "url" in a new window.
    actionType: "link",
    link: "/plugin-platform/plugin-workbench?mode=new"
}

const columns = [
    {
        name: "pluginKey",
        label: "pluginPlatform.list.column.pluginKey",
        width: 200
    },
    {
        name: "name",
        label: "pluginPlatform.list.column.name",
        width: 180
    },
    {
        name: "applySystem",
        label: "pluginPlatform.list.column.applySystem",
        type: "mapping",
        width: 120,
        map: {
            wes: "pluginPlatform.applySystem.wes",
            station: "pluginPlatform.applySystem.station"
        }
    },
    {
        name: "applyModule",
        label: "pluginPlatform.list.column.applyModule",
        width: 140,
        tpl: "${applyModule || '-'}"
    },
    {
        name: "status",
        label: "pluginPlatform.list.column.status",
        type: "mapping",
        width: 110,
        map: {
            DRAFT: "pluginPlatform.status.draft",
            ENABLED: "pluginPlatform.status.enabled",
            DISABLED: "pluginPlatform.status.disabled",
            ARCHIVED: "pluginPlatform.status.archived"
        }
    },
    {
        name: "lastValidationStatus",
        label: "pluginPlatform.list.column.lastValidation",
        type: "mapping",
        width: 130,
        map: {
            PASSED: "pluginPlatform.validation.passed",
            FAILED: "pluginPlatform.validation.failed",
            "*": "pluginPlatform.validation.notValidated"
        }
    },
    {
        name: "lastValidationAt",
        label: "pluginPlatform.validation.lastAt",
        width: 170,
        tpl: "${lastValidationAt|millis}"
    },
    {
        name: "currentRevisionNo",
        label: "pluginPlatform.list.column.currentRevision",
        width: 130,
        tpl: "${currentRevisionNo ? 'v' + currentRevisionNo : '-'}"
    },
    {
        name: "updateTime",
        label: "pluginPlatform.list.column.updateTime",
        width: 170,
        tpl: "${updateTime|millis}"
    },
    {
        type: "operation",
        label: "pluginPlatform.list.column.actions",
        width: 90,
        buttons: [
            {
                type: "button",
                level: "link",
                label: "pluginPlatform.list.open",
                actionType: "link",
                link: "/plugin-platform/plugin-workbench?pluginKey=${pluginKey}"
            }
        ],
        toggled: true
    }
]

const schema = {
    type: "page",
    title: "pluginPlatform.list.title",
    toolbar: [],
    body: [
        {
            type: "crud",
            name: "PluginTable",
            syncLocation: false,
            api: {
                method: "get",
                url: "/plugin-platform/scriptPlugin/list",
                adaptor: adaptPluginList
            },
            // One fetch; filter changes apply client-side because the endpoint is not paged.
            loadDataOnce: true,
            loadDataOnceFetchOnFilter: false,
            autoFillHeight: true,
            placeholder: "pluginPlatform.list.empty",
            filterTogglable: true,
            filterDefaultVisible: true,
            filter: {
                title: "pluginPlatform.list.filterTitle",
                mode: "inline",
                submitText: "pluginPlatform.list.search",
                body: [
                    {
                        type: "input-text",
                        name: "pluginKey",
                        label: "pluginPlatform.list.column.pluginKey",
                        clearable: true
                    },
                    {
                        type: "input-text",
                        name: "name",
                        label: "pluginPlatform.list.column.name",
                        clearable: true
                    },
                    {
                        type: "select",
                        name: "status",
                        label: "pluginPlatform.list.column.status",
                        clearable: true,
                        options: [
                            { label: "pluginPlatform.status.draft", value: "DRAFT" },
                            { label: "pluginPlatform.status.enabled", value: "ENABLED" },
                            { label: "pluginPlatform.status.disabled", value: "DISABLED" },
                            { label: "pluginPlatform.status.archived", value: "ARCHIVED" }
                        ]
                    },
                    {
                        type: "select",
                        name: "applySystem",
                        label: "pluginPlatform.list.column.applySystem",
                        clearable: true,
                        options: [
                            { label: "pluginPlatform.applySystem.wes", value: "wes" },
                            { label: "pluginPlatform.applySystem.station", value: "station" }
                        ]
                    }
                ]
            },
            headerToolbar: ["reload", newPluginButton],
            footerToolbar: ["switch-per-page", "statistics", "pagination"],
            columns: columns
        }
    ]
}

export default schema2component(schema)
```

- [ ] **Step 2: Create the temporary workbench placeholder at `workbench/index.tsx`**

This file exists only so the route resolves in this task; Task 4 replaces its contents entirely.

```tsx
import React from "react"
import { Card, Space, Typography } from "antd"
import { useTranslation } from "react-i18next"
import { useLocation } from "react-router"

const Workbench: React.FC = () => {
    const { t } = useTranslation()
    const location = useLocation()
    const params = new URLSearchParams(location.search)
    const pluginKey = params.get("pluginKey")
    const isNew = params.get("mode") === "new"
    return (
        <Card>
            <Space direction="vertical">
                <Typography.Title level={4}>{t("pluginPlatform.workbench.title")}</Typography.Title>
                <Typography.Text type="secondary">
                    {isNew
                        ? t("pluginPlatform.workbench.newPlugin")
                        : pluginKey || t("pluginPlatform.workbench.noPlugin")}
                </Typography.Text>
            </Space>
        </Card>
    )
}

export default Workbench
```

- [ ] **Step 3: Replace the legacy routes in `path2Compoment.tsx`**

Delete both existing "Plugin Platform" entries (`/plugin-platform/plugin-management` and `/plugin-platform/plugin-store`) and replace them with:

```tsx
    // Plugin Platform
    {
        path: "/plugin-platform/plugins",
        name: (
            <Translation>{(t) => t("pluginPlatform.list.title")}</Translation>
        ),
        component: lazy(() => import("@/pages/plugin_platform/list"))
    },
    // Not in the sidebar: opened from the list or a deep link with ?mode=new / ?pluginKey=...
    {
        path: "/plugin-platform/plugin-workbench",
        name: (
            <Translation>{(t) => t("pluginPlatform.workbench.title")}</Translation>
        ),
        component: lazy(() => import("@/pages/plugin_platform/workbench"))
    },
```

Both entries receive the default `meta` (`keepAlive`, `closable`, `cache`) from the `menuRouter.map` at the bottom of the file. The workbench therefore opens as a closable, cached tab. It must stay in `menuRouter`, not `baseRouter`, or `TabsLayout` would not register a tab for it.

- [ ] **Step 4: Delete the legacy plugin pages**

```bash
git rm client/src/pages/plugin_platform/plugin_management.tsx \
  client/src/pages/plugin_platform/plugin_store.tsx \
  client/src/pages/plugin_platform/constants/api_constant.tsx
```

The `constants/` directory is now empty and disappears with the file.

- [ ] **Step 5: Remove the dead `pluginSystem.*` keys and add the menu keys**

In both locale files, delete these ten keys (the whole `pluginSystem.*` block, including one of the two blank separators):

```
pluginSystem.pluginManagement.title
pluginSystem.pluginManagement.table.pluginCoding
pluginSystem.pluginManagement.table.pluginName
pluginSystem.pluginManagement.table.developers
pluginSystem.pluginManagement.table.pluginVersion
pluginSystem.pluginStore.title
pluginSystem.pluginStore.table.installation
pluginSystem.pluginStore.table.unload
pluginSystem.pluginStore.table.parameterSettings
pluginSystem.pluginStore.table.pluginConfiguration
```

Then insert these five keys where the block was, immediately before `"wms.menu.dashboard"`:

`en-us.json`:

```json
    "pluginPlatform.system.title": "Plugin platform",
    "pluginPlatform.menu.plugins": "Plugins",
    "pluginPlatform.menu.audit": "Plugin audit",
    "pluginPlatform.permission.get": "Plugin platform read access",
    "pluginPlatform.permission.post": "Plugin platform write access",
```

`zh-cn.json`:

```json
    "pluginPlatform.system.title": "插件平台",
    "pluginPlatform.menu.plugins": "插件",
    "pluginPlatform.menu.audit": "插件审计",
    "pluginPlatform.permission.get": "插件平台读取权限",
    "pluginPlatform.permission.post": "插件平台操作权限",
```

These keys are required by the Phase 3A Liquibase seed (`db.changelog-20261023.xml`): `pluginPlatform.system.title` is the application-switcher label, `pluginPlatform.menu.*` are the two sidebar entries, and `pluginPlatform.permission.*` are the gateway permission rows. `LayoutAside` and the app switcher translate `u_menu.title` through `t(...)`.

- [ ] **Step 6: Verify**

Run: `cd client && npx tsc --noEmit --skipLibCheck`
Expected: the four baseline errors only.

Run: `cd client && grep -rn "pluginSystem\\.\|plugin_management\|plugin_store\|plugin-platform/plugin-management\|plugin-platform/plugin-store" src || true`
Expected: no output.

Run: `cd client && node -e "JSON.parse(require('fs').readFileSync('src/locales/en-us.json','utf8')); JSON.parse(require('fs').readFileSync('src/locales/zh-cn.json','utf8')); const en=require('./src/locales/en-us.json'); const zh=require('./src/locales/zh-cn.json'); const missing=Object.keys(en).filter(k=>k.startsWith('pluginPlatform.')&&!(k in zh)); console.log(missing.length)"`
Expected: `0`

- [ ] **Step 7: Commit**

```bash
git add client/src/pages/plugin_platform/list/index.tsx \
  client/src/pages/plugin_platform/workbench/index.tsx \
  client/src/routes/path2Compoment.tsx \
  client/src/locales/en-us.json client/src/locales/zh-cn.json
git commit -m "feat(script-plugin): add the plugin list and workbench routes

Removes the legacy plugin management and plugin store pages, whose endpoints no longer exist.

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 4: Workbench Context, Shell, Overview Tab and Validation Report

**Files:**
- Create: `client/src/pages/plugin_platform/ValidationReportView.tsx`
- Create: `client/src/pages/plugin_platform/workbench/context.tsx`
- Create: `client/src/pages/plugin_platform/workbench/WorkbenchShell.tsx`
- Create: `client/src/pages/plugin_platform/workbench/tabs/OverviewTab.tsx`
- Modify: `client/src/pages/plugin_platform/workbench/index.tsx`

**Interfaces:**
- Consumes: `getPlugin`, `listRevisions`, `getContract`, `validatePlugin`, `enablePlugin`, `disablePlugin`, `archivePlugin`, `rollbackPlugin`, `updatePluginMetadata`, `errorMessage` from `../api`; `usePluginCapabilities` from `../usePluginCapabilities`; `formatMillis` from `../format`; `VALIDATION_LABEL_KEYS`, `REVISION_ORIGIN_LABEL_KEYS`, `STATUS_LABEL_KEYS` from `../labels`; `PluginStatusTag` from `../PluginStatusTag`.
- Produces:
  - `<ValidationReportView report />`
  - `WorkbenchProvider`, `useWorkbench()` → `{pluginKey, plugin, revisions, currentRevision, contract, capabilities, loading, error, reload}`
  - `<WorkbenchShell />` with the `overview` tab
  - the real `workbench/index.tsx` (the wizard stays a placeholder until Task 12)

- [ ] **Step 1: Create `ValidationReportView.tsx`**

```tsx
import React from "react"
import { Alert, Descriptions, Table } from "antd"
import { ColumnsType } from "antd/lib/table"
import { useTranslation } from "react-i18next"
import { ValidationFailure, ValidationReport } from "./types"
import { formatMillis } from "./format"

interface ValidationReportViewProps {
    report: ValidationReport
}

const ValidationReportView: React.FC<ValidationReportViewProps> = ({ report }) => {
    const { t } = useTranslation()
    const failures = report.failures || []
    const columns: ColumnsType<ValidationFailure> = [
        {
            title: t("pluginPlatform.validation.stage"),
            dataIndex: "stage",
            width: 180
        },
        {
            title: t("pluginPlatform.validation.message"),
            dataIndex: "message"
        }
    ]

    return (
        <div>
            <Alert
                type={report.passed ? "success" : "error"}
                showIcon
                message={
                    report.passed
                        ? t("pluginPlatform.validation.passed")
                        : t("pluginPlatform.validation.failed")
                }
                style={{ marginBottom: 12 }}
            />
            <Descriptions size="small" bordered column={2}>
                <Descriptions.Item label={t("pluginPlatform.workbench.pluginKey")}>
                    {report.pluginKey}
                </Descriptions.Item>
                <Descriptions.Item label={t("pluginPlatform.workbench.applySystem")}>
                    {report.targetApp}
                </Descriptions.Item>
                <Descriptions.Item label={t("pluginPlatform.revisions.revisionNo")}>
                    {report.revisionId}
                </Descriptions.Item>
                <Descriptions.Item label={t("pluginPlatform.validation.lastAt")}>
                    {formatMillis(report.finishedAt)}
                </Descriptions.Item>
            </Descriptions>
            {failures.length > 0 && (
                <Table<ValidationFailure>
                    rowKey={(failure) => failure.stage + ":" + failure.message}
                    size="small"
                    style={{ marginTop: 12 }}
                    pagination={false}
                    dataSource={failures}
                    columns={columns}
                />
            )}
        </div>
    )
}

export default ValidationReportView
```

- [ ] **Step 2: Create `workbench/context.tsx`**

```tsx
import React, { createContext, useCallback, useContext, useEffect, useState } from "react"
import {
    errorMessage,
    getContract,
    getPlugin,
    listRevisions
} from "../api"
import {
    PluginContract,
    ScriptPlugin,
    ScriptPluginRevision
} from "../types"
import usePluginCapabilities, { PluginCapabilitiesState } from "../usePluginCapabilities"

export interface WorkbenchContextValue {
    pluginKey: string
    plugin: ScriptPlugin | null
    revisions: ScriptPluginRevision[]
    currentRevision: ScriptPluginRevision | null
    contract: PluginContract | null
    capabilities: PluginCapabilitiesState
    loading: boolean
    error: string | null
    reload: () => Promise<void>
}

const WorkbenchContext = createContext<WorkbenchContextValue | null>(null)

interface WorkbenchProviderProps {
    pluginKey: string
    children: React.ReactNode
}

export const WorkbenchProvider: React.FC<WorkbenchProviderProps> = ({ pluginKey, children }) => {
    const [plugin, setPlugin] = useState<ScriptPlugin | null>(null)
    const [revisions, setRevisions] = useState<ScriptPluginRevision[]>([])
    const [contract, setContract] = useState<PluginContract | null>(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)
    const capabilities = usePluginCapabilities()

    useEffect(() => {
        let active = true
        setLoading(true)
        setError(null)
        getPlugin(pluginKey)
            .then((detail) => {
                if (!active) {
                    return
                }
                setPlugin(detail)
                listRevisions(pluginKey)
                    .then((list) => {
                        if (active) {
                            setRevisions(list)
                        }
                    })
                    .catch((revisionError) => {
                        if (active) {
                            setError(errorMessage(revisionError, "The plugin revisions could not be loaded."))
                        }
                    })
                    .then(() => {
                        if (active) {
                            setLoading(false)
                        }
                    })
                getContract(detail.applySystem)
                    .then((loaded) => {
                        if (active) {
                            setContract(loaded)
                        }
                    })
                    .catch(() => {
                        if (active) {
                            setContract(null)
                        }
                    })
            })
            .catch((detailError) => {
                if (active) {
                    setError(errorMessage(detailError, "The plugin could not be loaded."))
                    setLoading(false)
                }
            })
        return () => {
            active = false
        }
    }, [pluginKey])

    const reload = useCallback(() => {
        return Promise.all([getPlugin(pluginKey), listRevisions(pluginKey)])
            .then(([detail, list]) => {
                setPlugin(detail)
                setRevisions(list)
                setError(null)
            })
            .catch((reloadError) => {
                setError(errorMessage(reloadError, "The plugin could not be refreshed."))
            })
    }, [pluginKey])

    const currentRevision =
        revisions.filter((revision) => revision.id === (plugin && plugin.currentRevisionId))[0] || null

    const value: WorkbenchContextValue = {
        pluginKey,
        plugin,
        revisions,
        currentRevision,
        contract,
        capabilities,
        loading,
        error,
        reload
    }

    return <WorkbenchContext.Provider value={value}>{children}</WorkbenchContext.Provider>
}

export function useWorkbench(): WorkbenchContextValue {
    const value = useContext(WorkbenchContext)
    if (!value) {
        throw new Error("useWorkbench must be used inside a WorkbenchProvider")
    }
    return value
}
```

**Note:** the two loads run sequentially on purpose — the contract needs `applySystem` from the detail. If the detail succeeds but revisions fail, the page still renders the overview with an inline error. A failed contract load is silent: the declaration tables fall back to bundled defaults from `formModels.ts`.

- [ ] **Step 3: Create `workbench/WorkbenchShell.tsx`**

```tsx
import React, { useState } from "react"
import { Alert, Button, Card, Space, Spin, Tabs, Typography } from "antd"
import { ArrowLeftOutlined } from "@ant-design/icons"
import { useTranslation } from "react-i18next"
import { useHistory } from "react-router"
import PluginStatusTag from "../PluginStatusTag"
import { useWorkbench } from "./context"
import OverviewTab from "./tabs/OverviewTab"

const WorkbenchShell: React.FC = () => {
    const { t } = useTranslation()
    const history = useHistory()
    const { plugin, loading, error, reload } = useWorkbench()
    const [activeTab, setActiveTab] = useState("overview")

    const tabs = [
        {
            key: "overview",
            label: t("pluginPlatform.workbench.tab.overview"),
            children: <OverviewTab />
        }
    ]

    const refresh = () => {
        reload().then(() => undefined)
    }

    return (
        <Card>
            <Space direction="vertical" style={{ width: "100%" }} size="middle">
                <Space wrap style={{ width: "100%", justifyContent: "space-between" }}>
                    <Space wrap>
                        <Button
                            icon={<ArrowLeftOutlined />}
                            onClick={() => history.push("/plugin-platform/plugins")}
                        >
                            {t("pluginPlatform.workbench.backToList")}
                        </Button>
                        <Typography.Title level={4} style={{ margin: 0 }}>
                            {plugin ? plugin.name : t("pluginPlatform.workbench.title")}
                        </Typography.Title>
                        {plugin && <PluginStatusTag status={plugin.status} />}
                    </Space>
                    <Space wrap>
                        <Button onClick={refresh} loading={loading}>
                            {t("pluginPlatform.workbench.refresh")}
                        </Button>
                    </Space>
                </Space>
                {error && <Alert type="error" showIcon message={error} />}
                {plugin ? (
                    <Tabs activeKey={activeTab} onChange={setActiveTab} items={tabs} />
                ) : loading ? (
                    <div style={{ textAlign: "center", padding: 32 }}>
                        <Spin />
                    </div>
                ) : (
                    <div style={{ textAlign: "center", padding: 32 }}>
                        <Button onClick={refresh}>{t("pluginPlatform.workbench.refresh")}</Button>
                    </div>
                )}
            </Space>
        </Card>
    )
}

export default WorkbenchShell
```

Later tasks append tab entries to the `tabs` array. Keep the keys stable: `overview`, `scripts`, `revisions`, `config`, `testRun`, `executions`, `aiHistory`, `audit`.

- [ ] **Step 4: Create `workbench/tabs/OverviewTab.tsx`**

```tsx
import React, { useState } from "react"
import {
    Alert,
    Button,
    Card,
    Descriptions,
    Form,
    Input,
    Modal,
    Select,
    Space,
    Tag,
    Typography,
    message
} from "antd"
import { useTranslation } from "react-i18next"
import {
    archivePlugin,
    disablePlugin,
    enablePlugin,
    rollbackPlugin,
    updatePluginMetadata,
    validatePlugin
} from "../../api"
import { REVISION_ORIGIN_LABEL_KEYS, VALIDATION_LABEL_KEYS } from "../../labels"
import { formatMillis } from "../../format"
import { ValidationReport } from "../../types"
import PluginStatusTag from "../../PluginStatusTag"
import ValidationReportView from "../../ValidationReportView"
import { useWorkbench } from "../context"

const OverviewTab: React.FC = () => {
    const { t } = useTranslation()
    const { plugin, revisions, capabilities, reload } = useWorkbench()
    const [metadataForm] = Form.useForm()
    const [savingMetadata, setSavingMetadata] = useState(false)
    const [busyAction, setBusyAction] = useState<string | null>(null)
    const [report, setReport] = useState<ValidationReport | null>(null)
    const [disableOpen, setDisableOpen] = useState(false)
    const [disableReason, setDisableReason] = useState("")
    const [rollbackOpen, setRollbackOpen] = useState(false)
    const [rollbackRevisionId, setRollbackRevisionId] = useState<string | null>(null)

    if (!plugin) {
        return null
    }

    const archived = plugin.status === "ARCHIVED"
    const canEditMetadata = capabilities.canDevelop && !archived
    const canValidate = capabilities.canDevelop && !archived
    const canEnable = capabilities.canAdminister && !archived && plugin.status !== "ENABLED"
    const canDisable = capabilities.canAdminister && plugin.status === "ENABLED"
    const canArchive = capabilities.canAdminister && !archived
    const canRollback = capabilities.canAdminister && !archived && revisions.length > 0
    const currentRevision =
        revisions.filter((revision) => revision.id === plugin.currentRevisionId)[0] || null
    const applySystemLabel =
        plugin.applySystem === "wes"
            ? t("pluginPlatform.applySystem.wes")
            : plugin.applySystem === "station"
            ? t("pluginPlatform.applySystem.station")
            : plugin.applySystem
    const lastValidationLabel = plugin.lastValidationStatus
        ? t(VALIDATION_LABEL_KEYS[plugin.lastValidationStatus] || plugin.lastValidationStatus)
        : t("pluginPlatform.validation.notValidated")

    const saveMetadata = (values: { name: string; description?: string; applyModule?: string }) => {
        setSavingMetadata(true)
        updatePluginMetadata(plugin.pluginKey, {
            name: values.name,
            description: values.description || "",
            applyModule: values.applyModule || ""
        })
            .then(() => {
                message.success(t("pluginPlatform.overview.metadataSaved"))
                return reload()
            })
            .catch(() => undefined)
            .then(() => setSavingMetadata(false))
    }

    const handleValidate = () => {
        setBusyAction("validate")
        validatePlugin(plugin.pluginKey)
            .then((validationReport) => {
                setReport(validationReport)
                message.success(t("pluginPlatform.overview.validationFinished"))
                return reload()
            })
            .catch(() => undefined)
            .then(() => setBusyAction(null))
    }

    const handleEnable = () => {
        Modal.confirm({
            title: t("pluginPlatform.overview.enable"),
            content: t("pluginPlatform.overview.enableConfirm"),
            okText: t("pluginPlatform.common.confirm"),
            cancelText: t("pluginPlatform.common.cancel"),
            onOk: () => {
                setBusyAction("enable")
                return enablePlugin(plugin.pluginKey)
                    .then(() => {
                        message.success(t("pluginPlatform.overview.enabled"))
                        return reload()
                    })
                    .catch(() => undefined)
                    .then(() => setBusyAction(null))
            }
        })
    }

    const submitDisable = () => {
        setBusyAction("disable")
        disablePlugin(plugin.pluginKey, disableReason)
            .then(() => {
                setDisableOpen(false)
                setDisableReason("")
                message.success(t("pluginPlatform.overview.disabled"))
                return reload()
            })
            .catch(() => undefined)
            .then(() => setBusyAction(null))
    }

    const handleArchive = () => {
        Modal.confirm({
            title: t("pluginPlatform.overview.archive"),
            content: t("pluginPlatform.overview.archiveConfirm"),
            okText: t("pluginPlatform.common.confirm"),
            cancelText: t("pluginPlatform.common.cancel"),
            onOk: () => {
                setBusyAction("archive")
                return archivePlugin(plugin.pluginKey)
                    .then(() => {
                        message.success(t("pluginPlatform.overview.archived"))
                        return reload()
                    })
                    .catch(() => undefined)
                    .then(() => setBusyAction(null))
            }
        })
    }

    const openRollback = () => {
        setRollbackRevisionId(revisions.length > 0 ? revisions[0].id : null)
        setRollbackOpen(true)
    }

    const submitRollback = () => {
        if (!rollbackRevisionId) {
            return
        }
        setBusyAction("rollback")
        rollbackPlugin(plugin.pluginKey, rollbackRevisionId)
            .then(() => {
                setRollbackOpen(false)
                message.success(t("pluginPlatform.overview.rolledBack"))
                return reload()
            })
            .catch(() => undefined)
            .then(() => setBusyAction(null))
    }

    return (
        <Space direction="vertical" style={{ width: "100%" }} size="middle">
            <Card title={t("pluginPlatform.overview.title")} size="small">
                <Descriptions size="small" bordered column={2}>
                    <Descriptions.Item label={t("pluginPlatform.workbench.pluginKey")}>
                        {plugin.pluginKey}
                    </Descriptions.Item>
                    <Descriptions.Item label={t("pluginPlatform.list.column.status")}>
                        <PluginStatusTag status={plugin.status} />
                    </Descriptions.Item>
                    <Descriptions.Item label={t("pluginPlatform.workbench.applySystem")}>
                        {applySystemLabel}
                    </Descriptions.Item>
                    <Descriptions.Item label={t("pluginPlatform.workbench.applyModule")}>
                        {plugin.applyModule || "-"}
                    </Descriptions.Item>
                    <Descriptions.Item label={t("pluginPlatform.workbench.sdkVersion")}>
                        {plugin.sdkVersion || "-"}
                    </Descriptions.Item>
                    <Descriptions.Item label={t("pluginPlatform.workbench.currentRevision")}>
                        {currentRevision ? (
                            <Space size={4}>
                                <Tag color="blue">{"v" + currentRevision.revisionNo}</Tag>
                                <span>{t(REVISION_ORIGIN_LABEL_KEYS[currentRevision.origin])}</span>
                            </Space>
                        ) : (
                            t("pluginPlatform.overview.noRevisions")
                        )}
                    </Descriptions.Item>
                    <Descriptions.Item label={t("pluginPlatform.validation.lastValidation")}>
                        {lastValidationLabel}
                    </Descriptions.Item>
                    <Descriptions.Item label={t("pluginPlatform.validation.lastAt")}>
                        {formatMillis(plugin.lastValidationAt)}
                    </Descriptions.Item>
                    <Descriptions.Item label={t("pluginPlatform.workbench.updatedAt")}>
                        {formatMillis(plugin.updateTime)}
                    </Descriptions.Item>
                    <Descriptions.Item label={t("pluginPlatform.overview.description")}>
                        {plugin.description || "-"}
                    </Descriptions.Item>
                </Descriptions>
                {plugin.lastValidationStatus === "FAILED" && plugin.lastValidationError && (
                    <Alert
                        style={{ marginTop: 12 }}
                        type="error"
                        showIcon
                        message={t("pluginPlatform.validation.lastError")}
                        description={
                            <Typography.Paragraph style={{ whiteSpace: "pre-wrap", marginBottom: 0 }}>
                                {plugin.lastValidationError}
                            </Typography.Paragraph>
                        }
                    />
                )}
            </Card>

            <Card title={t("pluginPlatform.overview.statusActions")} size="small">
                <Space direction="vertical" style={{ width: "100%" }}>
                    <Space wrap>
                        {capabilities.canDevelop && (
                            <Button onClick={handleValidate} loading={busyAction === "validate"} disabled={!canValidate}>
                                {t("pluginPlatform.overview.validate")}
                            </Button>
                        )}
                        {capabilities.canAdminister && (
                            <Button
                                type="primary"
                                onClick={handleEnable}
                                loading={busyAction === "enable"}
                                disabled={!canEnable}
                            >
                                {t("pluginPlatform.overview.enable")}
                            </Button>
                        )}
                        {capabilities.canAdminister && (
                            <Button onClick={() => setDisableOpen(true)} disabled={!canDisable}>
                                {t("pluginPlatform.overview.disable")}
                            </Button>
                        )}
                        {capabilities.canAdminister && (
                            <Button danger onClick={handleArchive} loading={busyAction === "archive"} disabled={!canArchive}>
                                {t("pluginPlatform.overview.archive")}
                            </Button>
                        )}
                        {capabilities.canAdminister && (
                            <Button onClick={openRollback} disabled={!canRollback}>
                                {t("pluginPlatform.overview.rollback")}
                            </Button>
                        )}
                    </Space>
                    {!capabilities.canAdminister && (
                        <Typography.Text type="secondary">
                            {t("pluginPlatform.overview.noPermission")}
                        </Typography.Text>
                    )}
                    {archived && (
                        <Typography.Text type="secondary">
                            {t("pluginPlatform.scripts.readOnlyArchived")}
                        </Typography.Text>
                    )}
                </Space>
            </Card>

            {report && (
                <Card title={t("pluginPlatform.validation.reportTitle")} size="small">
                    <ValidationReportView report={report} />
                </Card>
            )}

            <Card title={t("pluginPlatform.overview.metadata")} size="small">
                <Form
                    form={metadataForm}
                    layout="vertical"
                    initialValues={{
                        name: plugin.name,
                        description: plugin.description || "",
                        applyModule: plugin.applyModule || ""
                    }}
                    onFinish={saveMetadata}
                >
                    <Form.Item
                        name="name"
                        label={t("pluginPlatform.overview.name")}
                        rules={[
                            {
                                required: true,
                                message: t("pluginPlatform.wizard.basics.nameRequired")
                            }
                        ]}
                    >
                        <Input maxLength={128} disabled={!canEditMetadata} />
                    </Form.Item>
                    <Form.Item name="description" label={t("pluginPlatform.overview.description")}>
                        <Input.TextArea rows={3} maxLength={512} disabled={!canEditMetadata} />
                    </Form.Item>
                    <Form.Item name="applyModule" label={t("pluginPlatform.workbench.applyModule")}>
                        <Input maxLength={64} disabled={!canEditMetadata} />
                    </Form.Item>
                    {canEditMetadata && (
                        <Form.Item style={{ marginBottom: 0 }}>
                            <Button type="primary" htmlType="submit" loading={savingMetadata}>
                                {t("pluginPlatform.overview.saveMetadata")}
                            </Button>
                        </Form.Item>
                    )}
                </Form>
            </Card>

            <Modal
                title={t("pluginPlatform.overview.disableTitle")}
                visible={disableOpen}
                onCancel={() => setDisableOpen(false)}
                onOk={submitDisable}
                confirmLoading={busyAction === "disable"}
                okText={t("pluginPlatform.common.confirm")}
                cancelText={t("pluginPlatform.common.cancel")}
            >
                <Input
                    value={disableReason}
                    onChange={(event) => setDisableReason(event.target.value)}
                    placeholder={t("pluginPlatform.overview.disableReason")}
                    maxLength={255}
                />
            </Modal>

            <Modal
                title={t("pluginPlatform.overview.rollbackTitle")}
                visible={rollbackOpen}
                onCancel={() => setRollbackOpen(false)}
                onOk={submitRollback}
                confirmLoading={busyAction === "rollback"}
                okText={t("pluginPlatform.common.confirm")}
                cancelText={t("pluginPlatform.common.cancel")}
                okButtonProps={{ disabled: !rollbackRevisionId }}
            >
                <Select
                    style={{ width: "100%" }}
                    value={rollbackRevisionId === null ? undefined : rollbackRevisionId}
                    onChange={(value: string) => setRollbackRevisionId(value)}
                    placeholder={t("pluginPlatform.overview.rollbackRevision")}
                    options={revisions.map((revision) => ({
                        label:
                            "v" +
                            revision.revisionNo +
                            " · " +
                            formatMillis(revision.createdAt) +
                            " · " +
                            t(REVISION_ORIGIN_LABEL_KEYS[revision.origin]),
                        value: revision.id
                    }))}
                />
            </Modal>
        </Space>
    )
}

export default OverviewTab
```

`errorMessage` is imported but not used in this file. Remove it from the import list. (I listed it above; fix in the plan.)

- [ ] **Step 5: Replace `workbench/index.tsx`**

```tsx
import React, { useRef } from "react"
import { Button, Card, Empty, Typography } from "antd"
import { useTranslation } from "react-i18next"
import { useHistory, useLocation } from "react-router"
import { WorkbenchProvider } from "./context"
import WorkbenchShell from "./WorkbenchShell"

const Workbench: React.FC = () => {
    const { t } = useTranslation()
    const history = useHistory()
    const location = useLocation()
    const params = new URLSearchParams(location.search)
    const mode = params.get("mode")
    const pluginKey = params.get("pluginKey")
    // Keeps the current plugin when TabsLayout restores this tab without its query string.
    const lastPluginKeyRef = useRef<string | null>(null)

    if (pluginKey) {
        lastPluginKeyRef.current = pluginKey
    }

    if (mode === "new") {
        // Replaced by the real wizard in Task 12.
        return (
            <Card>
                <Typography.Title level={4}>{t("pluginPlatform.wizard.title")}</Typography.Title>
            </Card>
        )
    }

    const activePluginKey = pluginKey || lastPluginKeyRef.current
    if (!activePluginKey) {
        return (
            <Card>
                <Empty description={t("pluginPlatform.workbench.noPlugin")}>
                    <Typography.Paragraph type="secondary">
                        {t("pluginPlatform.workbench.noPluginHint")}
                    </Typography.Paragraph>
                    <Button type="primary" onClick={() => history.push("/plugin-platform/plugins")}>
                        {t("pluginPlatform.workbench.backToList")}
                    </Button>
                </Empty>
            </Card>
        )
    }

    return (
        <WorkbenchProvider key={activePluginKey} pluginKey={activePluginKey}>
            <WorkbenchShell />
        </WorkbenchProvider>
    )
}

export default Workbench
```

The `key={activePluginKey}` resets all tab state when the user opens a different plugin from the list. Query-only navigation does not remount `RouterGuard`, so this key is what makes the workbench switch plugins.

- [ ] **Step 6: Verify**

Run: `cd client && npx tsc --noEmit --skipLibCheck`
Expected: the four baseline errors only.

Static checks before committing:
- `OverviewTab` imports `errorMessage` nowhere (it is not used).
- Every component that imports `useWorkbench` renders below `WorkbenchProvider`.
- Admin-only buttons are hidden for non-admins and the server remains the authority.

- [ ] **Step 7: Commit**

```bash
git add client/src/pages/plugin_platform/ValidationReportView.tsx \
  client/src/pages/plugin_platform/workbench/context.tsx \
  client/src/pages/plugin_platform/workbench/WorkbenchShell.tsx \
  client/src/pages/plugin_platform/workbench/tabs/OverviewTab.tsx \
  client/src/pages/plugin_platform/workbench/index.tsx
git commit -m "feat(script-plugin): add the workbench shell and overview tab

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 5: Scripts Tab

**Files:**
- Create: `client/src/pages/plugin_platform/workbench/tabs/ScriptsTab.tsx`
- Modify: `client/src/pages/plugin_platform/workbench/WorkbenchShell.tsx`

**Interfaces:**
- Consumes: `saveRevision` from `../../api`; `toDeclarationRows`, `fromDeclarationRows`, `validateDeclarationRows`, `toConfigRows`, `fromConfigRows`, `validateConfigRows`, `RowIssue`, `DeclarationRow`, `ConfigRow` from `../../formModels`; `<ScriptFileEditor scripts onChange readOnly seedKey />`, `<DeclarationTable rows onChange readOnly allowedTypes lifecycleEntityNames />`, `<ConfigSchemaTable rows onChange readOnly />`; `useWorkbench()`.
- Produces: `<ScriptsTab />` registered as the `scripts` tab.

- [ ] **Step 1: Create `workbench/tabs/ScriptsTab.tsx`**

```tsx
import React, { useEffect, useState } from "react"
import { Alert, Button, Card, Input, Space, Tag, message } from "antd"
import { useTranslation } from "react-i18next"
import { saveRevision } from "../../api"
import {
    ConfigRow,
    DeclarationRow,
    RowIssue,
    fromConfigRows,
    fromDeclarationRows,
    toConfigRows,
    toDeclarationRows,
    validateConfigRows,
    validateDeclarationRows
} from "../../formModels"
import { Scripts } from "../../types"
import ConfigSchemaTable from "../../ConfigSchemaTable"
import DeclarationTable from "../../DeclarationTable"
import ScriptFileEditor from "../../ScriptFileEditor"
import { useWorkbench } from "../context"

/**
 * Manual revision editor. The seed is the current revision; saving appends a new revision and
 * makes it current (server rule). Enabled plugins must be disabled first, and archived plugins are
 * read-only — both are server rules mirrored here by disabling the editors.
 */
const ScriptsTab: React.FC = () => {
    const { t } = useTranslation()
    const { plugin, currentRevision, contract, capabilities, reload } = useWorkbench()
    const [scripts, setScripts] = useState<Scripts>({})
    const [declarations, setDeclarations] = useState<DeclarationRow[]>([])
    const [configRows, setConfigRows] = useState<ConfigRow[]>([])
    const [changeSummary, setChangeSummary] = useState("")
    const [saving, setSaving] = useState(false)

    const revisionId = currentRevision ? currentRevision.id : null

    useEffect(() => {
        if (currentRevision) {
            setScripts(currentRevision.scripts || {})
            setDeclarations(toDeclarationRows(currentRevision.extensionPoints || []))
            setConfigRows(toConfigRows(currentRevision.configSchema || []))
        } else {
            setScripts({})
            setDeclarations([])
            setConfigRows([])
        }
        setChangeSummary("")
    }, [revisionId])

    if (!plugin) {
        return null
    }

    const archived = plugin.status === "ARCHIVED"
    const enabled = plugin.status === "ENABLED"
    const canEdit = capabilities.canDevelop && !archived && !enabled
    const readOnlyReason = archived
        ? t("pluginPlatform.scripts.readOnlyArchived")
        : enabled
        ? t("pluginPlatform.scripts.readOnlyEnabled")
        : null

    const issueText = (issue: RowIssue): string => t(issue.key, issue.values)

    const handleSave = () => {
        const fileNames = Object.keys(scripts)
        if (fileNames.length === 0) {
            message.error(t("pluginPlatform.scripts.noScripts"))
            return
        }
        const declarationIssue = validateDeclarationRows(declarations, fileNames)
        if (declarationIssue) {
            message.error(issueText(declarationIssue))
            return
        }
        const configIssue = validateConfigRows(configRows)
        if (configIssue) {
            message.error(issueText(configIssue))
            return
        }
        setSaving(true)
        saveRevision(plugin.pluginKey, {
            scripts,
            extensionPoints: fromDeclarationRows(declarations),
            configSchema: fromConfigRows(configRows),
            changeSummary: changeSummary.trim()
        })
            .then(() => {
                message.success(t("pluginPlatform.scripts.revisionSaved"))
                return reload()
            })
            .catch(() => undefined)
            .then(() => setSaving(false))
    }

    return (
        <Space direction="vertical" style={{ width: "100%" }} size="middle">
            {readOnlyReason && <Alert type="info" showIcon message={readOnlyReason} />}
            {!capabilities.canDevelop && (
                <Alert type="warning" showIcon message={t("pluginPlatform.scripts.noPermission")} />
            )}
            <Card
                title={t("pluginPlatform.scripts.files")}
                size="small"
                extra={
                    currentRevision ? (
                        <Tag color="blue">{"v" + currentRevision.revisionNo}</Tag>
                    ) : (
                        <Tag>{t("pluginPlatform.overview.noRevisions")}</Tag>
                    )
                }
            >
                <ScriptFileEditor
                    scripts={scripts}
                    onChange={setScripts}
                    readOnly={!canEdit}
                    seedKey={revisionId ? "rev:" + revisionId : "new"}
                />
            </Card>
            <Card title={t("pluginPlatform.scripts.declarations")} size="small">
                <DeclarationTable
                    rows={declarations}
                    onChange={setDeclarations}
                    readOnly={!canEdit}
                    allowedTypes={contract ? contract.archetypes.map((archetype) => archetype.type) : []}
                    lifecycleEntityNames={contract ? contract.lifecycleEntityNames : []}
                />
            </Card>
            <Card title={t("pluginPlatform.scripts.configSchema")} size="small">
                <ConfigSchemaTable rows={configRows} onChange={setConfigRows} readOnly={!canEdit} />
            </Card>
            <Card size="small">
                <Space direction="vertical" style={{ width: "100%" }}>
                    <Input.TextArea
                        rows={2}
                        maxLength={512}
                        value={changeSummary}
                        onChange={(event) => setChangeSummary(event.target.value)}
                        placeholder={t("pluginPlatform.scripts.changeSummary")}
                        disabled={!canEdit}
                    />
                    <Button type="primary" onClick={handleSave} loading={saving} disabled={!canEdit}>
                        {t("pluginPlatform.scripts.saveRevision")}
                    </Button>
                </Space>
            </Card>
        </Space>
    )
}

export default ScriptsTab
```

**Notes:**
- `seedKey` changes with the revision id, which makes `ScriptFileEditor` reseed and remount the Monaco editor after every save.
- Passing `setScripts`, `setDeclarations` and `setConfigRows` directly as `onChange` is safe: a React state setter accepts the plain array/object value.
- `canEdit` mirrors the server: saveRevision rejects `ARCHIVED` and `ENABLED` plugins and non-developers. The editors stay read-only; no request is attempted.

- [ ] **Step 2: Register the tab in `WorkbenchShell.tsx`**

Add the import:

```tsx
import ScriptsTab from "./tabs/ScriptsTab"
```

Append this entry to the `tabs` array, immediately after the `overview` entry:

```tsx
        {
            key: "scripts",
            label: t("pluginPlatform.workbench.tab.scripts"),
            children: <ScriptsTab />
        }
```

- [ ] **Step 3: Verify**

Run: `cd client && npx tsc --noEmit --skipLibCheck`
Expected: the four baseline errors only.

Static checks:
- The `scripts` tab key matches the spec (`overview`, `scripts`, `revisions`, `config`, `testRun`, `executions`, `aiHistory`, `audit`).
- No new locale keys are required for this task; every key used exists in the Task 1 block.

- [ ] **Step 4: Commit**

```bash
git add client/src/pages/plugin_platform/workbench/tabs/ScriptsTab.tsx \
  client/src/pages/plugin_platform/workbench/WorkbenchShell.tsx
git commit -m "feat(script-plugin): add the scripts tab with manual revision editing

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 6: Revisions Tab and Rollback Modal

**Files:**
- Create: `client/src/pages/plugin_platform/workbench/RollbackModal.tsx`
- Create: `client/src/pages/plugin_platform/workbench/tabs/RevisionsTab.tsx`
- Modify: `client/src/pages/plugin_platform/workbench/WorkbenchShell.tsx`

**Interfaces:**
- Consumes: `rollbackPlugin` from `../api`; `formatMillis` from `../format`; `REVISION_ORIGIN_LABEL_KEYS` from `../labels`; `<DiffViewer left right viewKey />`; `useWorkbench()`.
- Produces: `<RollbackModal open pluginKey revisions initialRevisionId onCancel onRolledBack />`; `<RevisionsTab />` registered as the `revisions` tab.

- [ ] **Step 1: Create `workbench/RollbackModal.tsx`**

```tsx
import React, { useEffect, useState } from "react"
import { Modal, Select, message } from "antd"
import { useTranslation } from "react-i18next"
import { rollbackPlugin } from "../api"
import { formatMillis } from "../format"
import { REVISION_ORIGIN_LABEL_KEYS } from "../labels"
import { ScriptPluginRevision } from "../types"

interface RollbackModalProps {
    open: boolean
    pluginKey: string
    revisions: ScriptPluginRevision[]
    initialRevisionId: string | null
    onCancel: () => void
    onRolledBack: () => void
}

const RollbackModal: React.FC<RollbackModalProps> = ({
    open,
    pluginKey,
    revisions,
    initialRevisionId,
    onCancel,
    onRolledBack
}) => {
    const { t } = useTranslation()
    const [selectedId, setSelectedId] = useState<string | null>(null)
    const [submitting, setSubmitting] = useState(false)

    useEffect(() => {
        if (open) {
            setSelectedId(initialRevisionId || (revisions.length > 0 ? revisions[0].id : null))
        }
    }, [open, initialRevisionId, revisions.length])

    const submit = () => {
        if (!selectedId) {
            return
        }
        setSubmitting(true)
        rollbackPlugin(pluginKey, selectedId)
            .then(() => {
                message.success(t("pluginPlatform.overview.rolledBack"))
                onRolledBack()
            })
            .catch(() => undefined)
            .then(() => setSubmitting(false))
    }

    return (
        <Modal
            title={t("pluginPlatform.revisions.rollbackTitle")}
            visible={open}
            onCancel={onCancel}
            onOk={submit}
            confirmLoading={submitting}
            okText={t("pluginPlatform.common.confirm")}
            cancelText={t("pluginPlatform.common.cancel")}
            okButtonProps={{ disabled: !selectedId }}
        >
            <Select
                style={{ width: "100%" }}
                value={selectedId === null ? undefined : selectedId}
                onChange={(value: string) => setSelectedId(value)}
                options={revisions.map((revision) => ({
                    label:
                        "v" +
                        revision.revisionNo +
                        " · " +
                        formatMillis(revision.createdAt) +
                        " · " +
                        t(REVISION_ORIGIN_LABEL_KEYS[revision.origin]),
                    value: revision.id
                }))}
            />
        </Modal>
    )
}

export default RollbackModal
```

The server rejects rollback for archived plugins, so callers only open this modal from the Revisions tab after checking `plugin.status !== "ARCHIVED"`. The server audits the rollback.

- [ ] **Step 2: Create `workbench/tabs/RevisionsTab.tsx`**

```tsx
import React, { useEffect, useState } from "react"
import { Alert, Button, Card, Descriptions, Select, Space, Table, Tag, Tooltip } from "antd"
import { ColumnsType } from "antd/lib/table"
import { useTranslation } from "react-i18next"
import { formatMillis } from "../../format"
import { REVISION_ORIGIN_LABEL_KEYS } from "../../labels"
import { RevisionOrigin, ScriptPluginRevision } from "../../types"
import DiffViewer from "../../DiffViewer"
import RollbackModal from "../RollbackModal"
import { useWorkbench } from "../context"

function scriptFileNames(left: ScriptPluginRevision, right: ScriptPluginRevision): string[] {
    const names: string[] = []
    Object.keys(left.scripts || {}).forEach((name) => {
        if (names.indexOf(name) < 0) {
            names.push(name)
        }
    })
    Object.keys(right.scripts || {}).forEach((name) => {
        if (names.indexOf(name) < 0) {
            names.push(name)
        }
    })
    return names
}

const RevisionsTab: React.FC = () => {
    const { t } = useTranslation()
    const { plugin, revisions, capabilities, reload } = useWorkbench()
    const [selectedIds, setSelectedIds] = useState<string[]>([])
    const [compareFile, setCompareFile] = useState<string | null>(null)
    const [rollbackTarget, setRollbackTarget] = useState<string | null>(null)

    const selected = revisions.filter((revision) => selectedIds.indexOf(revision.id) >= 0)
    const ordered = selected.slice().sort((left, right) => left.revisionNo - right.revisionNo)
    const base = ordered.length === 2 ? ordered[0] : null
    const target = ordered.length === 2 ? ordered[1] : null
    const baseId = base ? base.id : null
    const targetId = target ? target.id : null

    useEffect(() => {
        if (!base || !target) {
            setCompareFile(null)
            return
        }
        const names = scriptFileNames(base, target)
        setCompareFile((previous) =>
            previous && names.indexOf(previous) >= 0 ? previous : names.length > 0 ? names[0] : null
        )
    }, [baseId, targetId])

    if (!plugin) {
        return null
    }

    const archived = plugin.status === "ARCHIVED"
    const canRollback = capabilities.canAdminister && !archived
    const fileNames = base && target ? scriptFileNames(base, target) : []
    const leftText = base && compareFile ? base.scripts[compareFile] || "" : ""
    const rightText = target && compareFile ? target.scripts[compareFile] || "" : ""
    const missingSide =
        !!base &&
        !!target &&
        !!compareFile &&
        (!Object.prototype.hasOwnProperty.call(base.scripts || {}, compareFile) ||
            !Object.prototype.hasOwnProperty.call(target.scripts || {}, compareFile))

    const columns: ColumnsType<ScriptPluginRevision> = [
        {
            title: t("pluginPlatform.revisions.revisionNo"),
            dataIndex: "revisionNo",
            width: 100,
            render: (revisionNo: number) => "v" + revisionNo
        },
        {
            title: t("pluginPlatform.revisions.origin"),
            dataIndex: "origin",
            width: 110,
            render: (origin: RevisionOrigin) => <Tag>{t(REVISION_ORIGIN_LABEL_KEYS[origin])}</Tag>
        },
        {
            title: t("pluginPlatform.revisions.changeSummary"),
            dataIndex: "changeSummary",
            ellipsis: true
        },
        {
            title: t("pluginPlatform.revisions.createdBy"),
            dataIndex: "createdBy",
            width: 140
        },
        {
            title: t("pluginPlatform.revisions.createdAt"),
            dataIndex: "createdAt",
            width: 170,
            render: (createdAt: string) => formatMillis(createdAt)
        },
        {
            title: t("pluginPlatform.revisions.hash"),
            dataIndex: "contentHash",
            width: 140,
            render: (hash: string) => (
                <Tooltip title={hash}>
                    <span>{hash.substring(0, 10)}</span>
                </Tooltip>
            )
        },
        {
            title: t("pluginPlatform.workbench.actions"),
            width: 110,
            render: (revision: ScriptPluginRevision) => (
                <Button
                    size="small"
                    disabled={
                        !canRollback ||
                        revision.id === plugin.currentRevisionId
                    }
                    onClick={() => setRollbackTarget(revision.id)}
                >
                    {t("pluginPlatform.revisions.rollback")}
                </Button>
            )
        }
    ]

    return (
        <Space direction="vertical" style={{ width: "100%" }} size="middle">
            {archived && <Alert type="info" showIcon message={t("pluginPlatform.scripts.readOnlyArchived")} />}
            <Table<ScriptPluginRevision>
                rowKey="id"
                size="small"
                loading={false}
                dataSource={revisions}
                columns={columns}
                pagination={false}
                locale={{ emptyText: t("pluginPlatform.revisions.empty") }}
                rowSelection={{
                    selectedRowKeys: selectedIds,
                    onChange: (keys) => setSelectedIds(keys.map((key) => String(key))),
                    getCheckboxProps: (revision) => ({
                        disabled: selectedIds.length >= 2 && selectedIds.indexOf(revision.id) < 0
                    })
                }}
            />
            {!base || !target ? (
                <Alert type="info" showIcon message={t("pluginPlatform.revisions.selectTwo")} />
            ) : (
                <Card size="small" title={t("pluginPlatform.revisions.compareTitle")}>
                    <Space direction="vertical" style={{ width: "100%" }}>
                        <Descriptions size="small" column={2}>
                            <Descriptions.Item label={t("pluginPlatform.revisions.base")}>
                                {"v" + base.revisionNo + " · " + formatMillis(base.createdAt)}
                            </Descriptions.Item>
                            <Descriptions.Item label={t("pluginPlatform.revisions.target")}>
                                {"v" + target.revisionNo + " · " + formatMillis(target.createdAt)}
                            </Descriptions.Item>
                        </Descriptions>
                        <Select
                            style={{ width: 320 }}
                            value={compareFile === null ? undefined : compareFile}
                            onChange={(value: string) => setCompareFile(value)}
                            placeholder={t("pluginPlatform.revisions.file")}
                            options={fileNames.map((name) => ({ label: name, value: name }))}
                        />
                        {compareFile && missingSide && (
                            <Alert type="warning" showIcon message={t("pluginPlatform.revisions.missingFile")} />
                        )}
                        {compareFile && (
                            <DiffViewer
                                left={leftText}
                                right={rightText}
                                viewKey={base.id + ":" + target.id + ":" + compareFile}
                            />
                        )}
                    </Space>
                </Card>
            )}
            <RollbackModal
                open={rollbackTarget !== null}
                pluginKey={plugin.pluginKey}
                revisions={revisions}
                initialRevisionId={rollbackTarget}
                onCancel={() => setRollbackTarget(null)}
                onRolledBack={() => {
                    setRollbackTarget(null)
                    reload().then(() => undefined)
                }}
            />
        </Space>
    )
}

export default RevisionsTab
```

**Notes:**
- The list is ordered newest-first by the server; `ordered` sorts the two selected revisions by `revisionNo` so `base` is always the older one.
- Checkboxes disable once two revisions are selected; selecting a third requires deselecting one.
- The rollback button is disabled for the current revision (a no-op server-side that would still create an audit row) and for archived plugins or non-admins.
- `DiffViewer` remounts when `viewKey` changes, because the AMIS `diff-editor` reads `value`/`diffValue` only on mount.

- [ ] **Step 3: Register the tab in `WorkbenchShell.tsx`**

Add the import:

```tsx
import RevisionsTab from "./tabs/RevisionsTab"
```

Append this entry to the `tabs` array, immediately after the `scripts` entry:

```tsx
        {
            key: "revisions",
            label: t("pluginPlatform.workbench.tab.revisions"),
            children: <RevisionsTab />
        }
```

- [ ] **Step 4: Verify**

Run: `cd client && npx tsc --noEmit --skipLibCheck`
Expected: the four baseline errors only.

Static checks:
- Exactly two row checkboxes can be selected.
- Rollback opens with the clicked revision preselected.
- No new locale keys are required for this task.

- [ ] **Step 5: Commit**

```bash
git add client/src/pages/plugin_platform/workbench/RollbackModal.tsx \
  client/src/pages/plugin_platform/workbench/tabs/RevisionsTab.tsx \
  client/src/pages/plugin_platform/workbench/WorkbenchShell.tsx
git commit -m "feat(script-plugin): add the revisions tab with compare and rollback

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 7: Config Tab

**Files:**
- Create: `client/src/pages/plugin_platform/workbench/tabs/ConfigTab.tsx`
- Modify: `client/src/pages/plugin_platform/workbench/WorkbenchShell.tsx`

**Interfaces:**
- Consumes: `getPluginConfig`, `savePluginConfig` from `../../api`; `ConfigField`, `ScriptPluginConfig` from `../../types`; `useWorkbench()`.
- Produces: `<ConfigTab />` registered as the `config` tab.

- [ ] **Step 1: Create `workbench/tabs/ConfigTab.tsx`**

```tsx
import React, { useEffect, useState } from "react"
import { Alert, Button, Card, Input, InputNumber, Space, Spin, Switch, Typography, message } from "antd"
import { useTranslation } from "react-i18next"
import { getPluginConfig, savePluginConfig } from "../../api"
import { ConfigField, ScriptPluginConfig } from "../../types"
import { useWorkbench } from "../context"

const SUPPORTED_TYPES = ["STRING", "INT", "INTEGER", "NUMBER", "BOOLEAN"]

function normaliseValue(field: ConfigField, raw: unknown): unknown {
    if (field.type === "BOOLEAN") {
        return raw === true || raw === "true"
    }
    if (field.type === "INT" || field.type === "INTEGER" || field.type === "NUMBER") {
        if (typeof raw === "number") {
            return raw
        }
        if (typeof raw === "string" && raw.trim() !== "") {
            const numeric = Number(raw)
            return isFinite(numeric) ? numeric : undefined
        }
        return undefined
    }
    return raw === null || raw === undefined ? undefined : String(raw)
}

function defaultValue(field: ConfigField): unknown {
    if (field.type === "BOOLEAN") {
        return field.defaultValue === "true"
    }
    if (field.defaultValue === null || field.defaultValue === "") {
        return undefined
    }
    return normaliseValue(field, field.defaultValue)
}

/**
 * The server rejects unknown fields, so the form state and the save payload contain only the
 * fields declared by the current config schema. Booleans always have a value (a switch cannot be
 * "undefined"), which also makes required booleans saveable without touching them.
 */
function initialValues(config: ScriptPluginConfig): Record<string, unknown> {
    const source = config.values || {}
    const values: Record<string, unknown> = {}
    ;(config.schema || []).forEach((field) => {
        if (!field.field) {
            return
        }
        const present = Object.prototype.hasOwnProperty.call(source, field.field)
        const normalised = present ? normaliseValue(field, source[field.field]) : defaultValue(field)
        if (normalised !== undefined) {
            values[field.field] = normalised
        } else if (field.type === "BOOLEAN") {
            values[field.field] = false
        }
    })
    return values
}

const ConfigTab: React.FC = () => {
    const { t } = useTranslation()
    const { plugin, capabilities } = useWorkbench()
    const [config, setConfig] = useState<ScriptPluginConfig | null>(null)
    const [values, setValues] = useState<Record<string, unknown>>({})
    const [loading, setLoading] = useState(false)
    const [saving, setSaving] = useState(false)

    const pluginKey = plugin ? plugin.pluginKey : null

    const load = () => {
        if (!pluginKey || !capabilities.canAdminister) {
            return
        }
        setLoading(true)
        getPluginConfig(pluginKey)
            .then((loaded) => {
                setConfig(loaded)
                setValues(initialValues(loaded))
            })
            .catch(() => undefined)
            .then(() => setLoading(false))
    }

    useEffect(() => {
        load()
    }, [pluginKey, capabilities.canAdminister])

    if (!plugin) {
        return null
    }
    if (capabilities.loading) {
        return <Spin />
    }
    if (!capabilities.canAdminister) {
        return <Alert type="info" showIcon message={t("pluginPlatform.config.adminOnly")} />
    }

    const archived = plugin.status === "ARCHIVED"
    const fields = config ? config.schema || [] : []
    const supportedFields = fields.filter((field) => SUPPORTED_TYPES.indexOf(field.type) >= 0)

    const setField = (field: ConfigField, value: unknown) => {
        setValues({ ...values, [field.field]: value })
    }

    const save = () => {
        if (!config) {
            return
        }
        const payload: Record<string, unknown> = {}
        supportedFields.forEach((field) => {
            if (Object.prototype.hasOwnProperty.call(values, field.field)) {
                const value = values[field.field]
                if (value !== undefined) {
                    payload[field.field] = value
                }
            }
        })
        setSaving(true)
        savePluginConfig(plugin.pluginKey, payload, config.lockVersion)
            .then((saved) => {
                setConfig(saved)
                setValues(initialValues(saved))
                message.success(t("pluginPlatform.config.saved"))
            })
            .catch(() => {
                // A stale lockVersion means another user saved first. Refetch so the next save
                // carries the current lockVersion; the interceptor already toasted the reason.
                load()
            })
            .then(() => setSaving(false))
    }

    const renderInput = (field: ConfigField): React.ReactNode => {
        const value = values[field.field]
        if (field.type === "BOOLEAN") {
            return (
                <Switch
                    checked={value === true}
                    onChange={(checked) => setField(field, checked)}
                    disabled={archived}
                />
            )
        }
        if (field.type === "INT" || field.type === "INTEGER" || field.type === "NUMBER") {
            return (
                <InputNumber
                    style={{ width: 240 }}
                    value={typeof value === "number" ? value : undefined}
                    precision={field.type === "NUMBER" ? undefined : 0}
                    onChange={(next) => setField(field, normaliseValue(field, next))}
                    disabled={archived}
                />
            )
        }
        if (field.type === "STRING") {
            return (
                <Input
                    style={{ width: 360 }}
                    value={typeof value === "string" ? value : ""}
                    onChange={(event) => setField(field, event.target.value)}
                    disabled={archived}
                />
            )
        }
        return (
            <Alert
                type="warning"
                showIcon
                message={t("pluginPlatform.config.unsupportedType", { type: field.type })}
            />
        )
    }

    return (
        <Space direction="vertical" style={{ width: "100%" }} size="middle">
            {archived && <Alert type="info" showIcon message={t("pluginPlatform.scripts.readOnlyArchived")} />}
            {loading ? (
                <Spin />
            ) : fields.length === 0 ? (
                <Alert type="info" showIcon message={t("pluginPlatform.config.empty")} />
            ) : (
                <Card size="small" title={t("pluginPlatform.config.title")}>
                    <Space direction="vertical" style={{ width: "100%" }} size="middle">
                        {fields.map((field) => (
                            <div key={field.field}>
                                <Typography.Text>
                                    {field.field}
                                    {field.required ? " *" : ""}
                                </Typography.Text>
                                <div style={{ marginTop: 4 }}>{renderInput(field)}</div>
                            </div>
                        ))}
                        <Button type="primary" onClick={save} loading={saving} disabled={archived}>
                            {t("pluginPlatform.config.save")}
                        </Button>
                    </Space>
                </Card>
            )}
        </Space>
    )
}

export default ConfigTab
```

**Notes:**
- The server's `ScriptPluginConfigValidator` rejects unknown fields and wrong types; the form therefore sends only schema-declared values, normalised by type.
- `saveConfig` enforces optimistic locking via `lockVersion`. On any failure we refetch, which also covers a stale-lock conflict. The interceptor has already shown the server's message, so the catch adds no toast.
- Archived plugins cannot save config (server rule); inputs and the button are disabled.
- `getConfig` itself requires the administrator role, so non-admins get the informational alert and no request is made.

- [ ] **Step 2: Register the tab in `WorkbenchShell.tsx`**

Add the import:

```tsx
import ConfigTab from "./tabs/ConfigTab"
```

Append this entry to the `tabs` array, immediately after the `revisions` entry:

```tsx
        {
            key: "config",
            label: t("pluginPlatform.workbench.tab.config"),
            children: <ConfigTab />
        }
```

- [ ] **Step 3: Verify**

Run: `cd client && npx tsc --noEmit --skipLibCheck`
Expected: the four baseline errors only.

Static checks:
- Non-admins never call `getPluginConfig`.
- A save sends only schema-declared fields; booleans default to `false`.
- No new locale keys are required for this task.

- [ ] **Step 4: Commit**

```bash
git add client/src/pages/plugin_platform/workbench/tabs/ConfigTab.tsx \
  client/src/pages/plugin_platform/workbench/WorkbenchShell.tsx
git commit -m "feat(script-plugin): add the config tab with typed values and lock version

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 8: Test-Run Tab

**Files:**
- Create: `client/src/pages/plugin_platform/workbench/tabs/TestRunTab.tsx`
- Modify: `client/src/pages/plugin_platform/workbench/WorkbenchShell.tsx`
- Modify: `client/src/locales/en-us.json`, `client/src/locales/zh-cn.json`

**Interfaces:**
- Consumes: `runTest` from `../../api`; `EXTENSION_TYPE_LABEL_KEYS` from `../../labels`; `formatDuration` from `../../format`; `TestRunResult` from `../../types`; `useWorkbench()`.
- Produces: `<TestRunTab />` registered as the `testRun` tab; new locale key `pluginPlatform.testRun.noPermission`.

- [ ] **Step 1: Add the missing locale key**

`en-us.json`, after `"pluginPlatform.testRun.title": "Test run",`:

```json
    "pluginPlatform.testRun.noPermission": "You need the plugin developer role to run tests.",
```

`zh-cn.json`, after `"pluginPlatform.testRun.title": "试运行",`:

```json
    "pluginPlatform.testRun.noPermission": "需要插件开发者角色才能试运行。",
```

- [ ] **Step 2: Create `workbench/tabs/TestRunTab.tsx`**

```tsx
import React, { useEffect, useState } from "react"
import { Alert, Button, Card, Descriptions, Input, Select, Space, Typography } from "antd"
import { useTranslation } from "react-i18next"
import { runTest } from "../../api"
import { formatDuration } from "../../format"
import { EXTENSION_TYPE_LABEL_KEYS } from "../../labels"
import { ExtensionType, TestRunResult } from "../../types"
import { useWorkbench } from "../context"

/**
 * Starting points for the argument editor, matching TestRunArgumentBuilder on the server.
 * Values are placeholders: the user must replace them with data the host type accepts.
 */
const ARGUMENT_TEMPLATES: Record<ExtensionType, Record<string, unknown>> = {
    OUTBOUND_WAVE_WAVE_ACTION: { orders: [] },
    OUTBOUND_WAVE_SPLIT_ACTION: { wave: {} },
    REQUEST_VALIDATE: { context: {} },
    REQUEST_SUPPLY: { context: {} },
    REQUEST_AFTER_INVOKE: { context: {} },
    CUSTOM_REQUEST_INVOKE: { context: {} },
    CUSTOM_API: { parameter: { workStationId: 1, requestParameter: {} } },
    LIFECYCLE_LISTENER: { entityId: 1, entityOrderNo: "", newStatus: "" },
    HTTP_ENDPOINT: { body: {} },
    CRON: {}
}

/**
 * Test runs execute the declared script against real host services through the server's test
 * runner. The server re-checks the developer role, the applySystem and that the selected
 * extension is declared by the current revision; the UI mirrors those rules to avoid dead ends.
 */
const TestRunTab: React.FC = () => {
    const { t } = useTranslation()
    const { plugin, currentRevision, capabilities } = useWorkbench()
    const declarations = currentRevision ? currentRevision.extensionPoints || [] : []
    const [selectedIndex, setSelectedIndex] = useState(0)
    const [argumentsText, setArgumentsText] = useState("{}")
    const [argumentsError, setArgumentsError] = useState<string | null>(null)
    const [running, setRunning] = useState(false)
    const [result, setResult] = useState<TestRunResult | null>(null)

    const revisionId = currentRevision ? currentRevision.id : null
    const declaration =
        declarations.length === 0
            ? null
            : selectedIndex < declarations.length
            ? declarations[selectedIndex]
            : declarations[0]
    const declarationKey = declaration
        ? declaration.type + "|" + declaration.script + "|" + declaration.method
        : ""

    useEffect(() => {
        setSelectedIndex(0)
        setArgumentsError(null)
        setResult(null)
    }, [revisionId])

    // Seed the editor with the selected archetype's expected top-level keys.
    useEffect(() => {
        if (declaration) {
            setArgumentsText(JSON.stringify(ARGUMENT_TEMPLATES[declaration.type], null, 2))
        }
    }, [declarationKey])

    if (!plugin) {
        return null
    }
    if (!capabilities.canDevelop) {
        return <Alert type="warning" showIcon message={t("pluginPlatform.testRun.noPermission")} />
    }
    if (plugin.applySystem !== "wes") {
        return <Alert type="info" showIcon message={t("pluginPlatform.testRun.wesOnly")} />
    }
    if (plugin.status === "ARCHIVED") {
        return <Alert type="info" showIcon message={t("pluginPlatform.scripts.readOnlyArchived")} />
    }
    if (!currentRevision) {
        return <Alert type="info" showIcon message={t("pluginPlatform.testRun.noRevision")} />
    }
    if (declarations.length === 0) {
        return <Alert type="info" showIcon message={t("pluginPlatform.testRun.noDeclarations")} />
    }

    const activeDeclaration = declaration || declarations[0]

    const run = () => {
        let parsed: unknown
        try {
            parsed = JSON.parse(argumentsText || "{}")
        } catch (ignored) {
            setArgumentsError(t("pluginPlatform.testRun.invalidArguments"))
            return
        }
        if (parsed === null || typeof parsed !== "object" || Array.isArray(parsed)) {
            setArgumentsError(t("pluginPlatform.testRun.invalidArguments"))
            return
        }
        setArgumentsError(null)
        setRunning(true)
        setResult(null)
        runTest(plugin.pluginKey, {
            extensionType: activeDeclaration.type,
            script: activeDeclaration.script,
            method: activeDeclaration.method,
            arguments: parsed as Record<string, unknown>
        })
            .then((testResult) => setResult(testResult))
            .catch(() => undefined)
            .then(() => setRunning(false))
    }

    return (
        <Space direction="vertical" style={{ width: "100%" }} size="middle">
            <Alert type="warning" showIcon message={t("pluginPlatform.testRun.warning")} />
            <Card size="small" title={t("pluginPlatform.testRun.title")}>
                <Space direction="vertical" style={{ width: "100%" }} size="middle">
                    <div>
                        <Typography.Text>{t("pluginPlatform.testRun.declaration")}</Typography.Text>
                        <Select
                            style={{ width: "100%", marginTop: 4 }}
                            value={selectedIndex}
                            onChange={(value: number) => setSelectedIndex(value)}
                            options={declarations.map((item, index) => ({
                                label:
                                    t(EXTENSION_TYPE_LABEL_KEYS[item.type]) +
                                    " · " +
                                    item.script +
                                    "#" +
                                    item.method,
                                value: index
                            }))}
                        />
                    </div>
                    <div>
                        <Typography.Text>{t("pluginPlatform.testRun.arguments")}</Typography.Text>
                        <Input.TextArea
                            style={{ marginTop: 4 }}
                            rows={6}
                            value={argumentsText}
                            onChange={(event) => setArgumentsText(event.target.value)}
                            disabled={running}
                        />
                    </div>
                    {argumentsError && <Alert type="error" showIcon message={argumentsError} />}
                    <Button type="primary" onClick={run} loading={running}>
                        {running ? t("pluginPlatform.testRun.running") : t("pluginPlatform.testRun.run")}
                    </Button>
                </Space>
            </Card>
            {result && (
                <Card size="small" title={result.success ? t("pluginPlatform.testRun.success") : t("pluginPlatform.testRun.failure")}>
                    <Space direction="vertical" style={{ width: "100%" }} size="middle">
                        <Alert
                            type={result.success ? "success" : "error"}
                            showIcon
                            message={
                                result.success ? t("pluginPlatform.testRun.success") : t("pluginPlatform.testRun.failure")
                            }
                            description={result.error || undefined}
                        />
                        <Descriptions size="small" column={2}>
                            <Descriptions.Item label={t("pluginPlatform.testRun.duration")}>
                                {formatDuration(result.durationMs)}
                            </Descriptions.Item>
                        </Descriptions>
                        {result.resultTruncated && (
                            <Alert type="info" showIcon message={t("pluginPlatform.testRun.truncated")} />
                        )}
                        {result.result && (
                            <Typography.Paragraph
                                style={{
                                    whiteSpace: "pre-wrap",
                                    fontFamily: "monospace",
                                    background: "#fafafa",
                                    padding: 8,
                                    marginBottom: 0,
                                    maxHeight: 320,
                                    overflow: "auto"
                                }}
                            >
                                {result.result}
                            </Typography.Paragraph>
                        )}
                    </Space>
                </Card>
            )}
        </Space>
    )
}

export default TestRunTab
```

**Notes:**
- The declaration `<Select>` uses the array index as its value. This is state, not a React key; the list is immutable for a given revision and is reset whenever the revision changes.
- The argument editor is seeded from `ARGUMENT_TEMPLATES` whenever the selected declaration changes. The templates mirror `TestRunArgumentBuilder`'s expected top-level keys; their values are placeholders, not valid host objects. The spec's "JSON argument template for its archetype" is this seeding.
- Argument parsing is client-side only to fail fast; the server parses and passes the map to the test runner.
- `runTest` failures (forbidden, wrong applySystem, undeclared extension) arrive as business errors and are toasted by the interceptor; the catch adds nothing.
- The result panel renders the JSON text the server returns, truncated to 8 KB by the server.

- [ ] **Step 3: Register the tab in `WorkbenchShell.tsx`**

Add the import:

```tsx
import TestRunTab from "./tabs/TestRunTab"
```

Append this entry to the `tabs` array, immediately after the `config` entry:

```tsx
        {
            key: "testRun",
            label: t("pluginPlatform.workbench.tab.testRun"),
            children: <TestRunTab />
        }
```

- [ ] **Step 4: Verify**

Run: `cd client && npx tsc --noEmit --skipLibCheck`
Expected: the four baseline errors only.

Run: `cd client && node -e "const en=require('./src/locales/en-us.json'); const zh=require('./src/locales/zh-cn.json'); const missing=Object.keys(en).filter(k=>k.startsWith('pluginPlatform.')&&!(k in zh)); console.log(missing.length)"`
Expected: `0`

- [ ] **Step 5: Commit**

```bash
git add client/src/pages/plugin_platform/workbench/tabs/TestRunTab.tsx \
  client/src/pages/plugin_platform/workbench/WorkbenchShell.tsx \
  client/src/locales/en-us.json client/src/locales/zh-cn.json
git commit -m "feat(script-plugin): add the test-run tab

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 9: Executions Tab

**Files:**
- Create: `client/src/pages/plugin_platform/workbench/tabs/ExecutionsTab.tsx`
- Modify: `client/src/pages/plugin_platform/workbench/WorkbenchShell.tsx`

**Interfaces:**
- Consumes: `listExecutions`, `getExecutionStats`, `ExecutionQuery` from `../../api`; `formatDuration`, `formatMillis`, `formatNumber`, `formatPercent` from `../../format`; `EXTENSION_TYPES` from `../../formModels`; `EXTENSION_TYPE_LABEL_KEYS` from `../../labels`; `useWorkbench()`.
- Produces: `<ExecutionsTab />` registered as the `executions` tab.

- [ ] **Step 1: Create `workbench/tabs/ExecutionsTab.tsx`**

```tsx
import React, { useEffect, useState } from "react"
import { Button, Card, Col, Row, Select, Space, Statistic, Table, Tag, Tooltip, Typography } from "antd"
import { ColumnsType } from "antd/lib/table"
import { useTranslation } from "react-i18next"
import { ExecutionQuery, getExecutionStats, listExecutions } from "../../api"
import { formatDuration, formatMillis, formatNumber, formatPercent } from "../../format"
import { EXTENSION_TYPES } from "../../formModels"
import { EXTENSION_TYPE_LABEL_KEYS } from "../../labels"
import {
    ExecutionLog,
    ExecutionStats,
    ExecutionTypeStats,
    ExtensionType,
    PageResult
} from "../../types"
import { useWorkbench } from "../context"

const PAGE_SIZE = 20

type ResultFilter = "all" | "success" | "failure"

/**
 * Execution history and aggregate stats for the current plugin. The list is server-paged and
 * server-filtered; the stats window is independent of the list filters, matching the API.
 */
const ExecutionsTab: React.FC = () => {
    const { t } = useTranslation()
    const { pluginKey } = useWorkbench()
    const [hours, setHours] = useState(24)
    const [stats, setStats] = useState<ExecutionStats | null>(null)
    const [page, setPage] = useState(1)
    const [pageSize, setPageSize] = useState(PAGE_SIZE)
    const [resultFilter, setResultFilter] = useState<ResultFilter>("all")
    const [typeFilter, setTypeFilter] = useState<ExtensionType | "all">("all")
    const [data, setData] = useState<PageResult<ExecutionLog> | null>(null)
    const [loading, setLoading] = useState(false)
    const [reloadKey, setReloadKey] = useState(0)

    useEffect(() => {
        let active = true
        getExecutionStats(pluginKey, hours)
            .then((loaded) => {
                if (active) {
                    setStats(loaded)
                }
            })
            .catch(() => {
                if (active) {
                    setStats(null)
                }
            })
        return () => {
            active = false
        }
    }, [pluginKey, hours, reloadKey])

    useEffect(() => {
        let active = true
        setLoading(true)
        const query: ExecutionQuery = { page, pageSize }
        if (resultFilter !== "all") {
            query.success = resultFilter === "success"
        }
        if (typeFilter !== "all") {
            query.extensionType = typeFilter
        }
        listExecutions(pluginKey, query)
            .then((loaded) => {
                if (active) {
                    setData(loaded)
                }
            })
            .catch(() => {
                if (active) {
                    setData({ items: [], total: "0", page, pageSize })
                }
            })
            .then(() => {
                if (active) {
                    setLoading(false)
                }
            })
        return () => {
            active = false
        }
    }, [pluginKey, page, pageSize, resultFilter, typeFilter, reloadKey])

    const statsColumns: ColumnsType<ExecutionTypeStats> = [
        {
            title: t("pluginPlatform.executions.type"),
            dataIndex: "extensionType",
            render: (type: ExtensionType) => t(EXTENSION_TYPE_LABEL_KEYS[type])
        },
        {
            title: t("pluginPlatform.executions.total"),
            dataIndex: "total",
            width: 120,
            render: (value: string) => formatNumber(value)
        },
        {
            title: t("pluginPlatform.executions.failed"),
            dataIndex: "failed",
            width: 120,
            render: (value: string) => formatNumber(value)
        },
        {
            title: t("pluginPlatform.executions.avgDuration"),
            dataIndex: "avgDurationMs",
            width: 140,
            render: (value: string) => formatDuration(value)
        },
        {
            title: t("pluginPlatform.executions.maxDuration"),
            dataIndex: "maxDurationMs",
            width: 140,
            render: (value: string) => formatDuration(value)
        }
    ]

    const columns: ColumnsType<ExecutionLog> = [
        {
            title: t("pluginPlatform.executions.time"),
            dataIndex: "createdAt",
            width: 170,
            render: (value: string) => formatMillis(value)
        },
        {
            title: t("pluginPlatform.executions.type"),
            dataIndex: "extensionType",
            width: 180,
            render: (type: ExtensionType) => t(EXTENSION_TYPE_LABEL_KEYS[type])
        },
        {
            title: t("pluginPlatform.executions.result"),
            dataIndex: "success",
            width: 110,
            render: (success: boolean) => (
                <Tag color={success ? "success" : "error"}>
                    {t(
                        success
                            ? "pluginPlatform.executions.success"
                            : "pluginPlatform.executions.failure"
                    )}
                </Tag>
            )
        },
        {
            title: t("pluginPlatform.executions.duration"),
            dataIndex: "durationMs",
            width: 110,
            render: (value: string) => formatDuration(value)
        },
        {
            title: t("pluginPlatform.executions.revision"),
            dataIndex: "revisionId",
            width: 120,
            render: (value: string | null) => value || "-"
        },
        {
            title: t("pluginPlatform.executions.error"),
            dataIndex: "errorMessage",
            ellipsis: true,
            render: (value: string | null) =>
                value ? (
                    <Tooltip title={value}>
                        <span>{value}</span>
                    </Tooltip>
                ) : (
                    "-"
                )
        }
    ]

    const typeOptions: { label: string; value: string }[] = [
        { label: t("pluginPlatform.common.all"), value: "all" }
    ].concat(
        EXTENSION_TYPES.map((type) => ({ label: t(EXTENSION_TYPE_LABEL_KEYS[type]), value: type }))
    )
    const resultOptions = [
        { label: t("pluginPlatform.common.all"), value: "all" },
        { label: t("pluginPlatform.executions.success"), value: "success" },
        { label: t("pluginPlatform.executions.failure"), value: "failure" }
    ]
    const hourOptions = [
        { label: t("pluginPlatform.executions.lastHour"), value: 1 },
        { label: t("pluginPlatform.executions.last24Hours"), value: 24 },
        { label: t("pluginPlatform.executions.last7Days"), value: 168 }
    ]

    return (
        <Space direction="vertical" style={{ width: "100%" }} size="middle">
            <Space wrap>
                <Typography.Text type="secondary">{t("pluginPlatform.executions.hours")}</Typography.Text>
                <Select
                    style={{ width: 160 }}
                    value={hours}
                    onChange={(value: number) => setHours(value)}
                    options={hourOptions}
                />
                <Select
                    style={{ width: 180 }}
                    value={resultFilter}
                    onChange={(value: ResultFilter) => {
                        setResultFilter(value)
                        setPage(1)
                    }}
                    options={resultOptions}
                />
                <Select
                    style={{ width: 220 }}
                    value={typeFilter}
                    onChange={(value: ExtensionType | "all") => {
                        setTypeFilter(value)
                        setPage(1)
                    }}
                    options={typeOptions}
                />
                <Button onClick={() => setReloadKey(reloadKey + 1)} loading={loading}>
                    {t("pluginPlatform.common.refresh")}
                </Button>
            </Space>
            {stats && (
                <Row gutter={16}>
                    <Col span={5}>
                        <Card size="small">
                            <Statistic title={t("pluginPlatform.executions.total")} value={formatNumber(stats.total)} />
                        </Card>
                    </Col>
                    <Col span={5}>
                        <Card size="small">
                            <Statistic title={t("pluginPlatform.executions.failed")} value={formatNumber(stats.failed)} />
                        </Card>
                    </Col>
                    <Col span={5}>
                        <Card size="small">
                            <Statistic
                                title={t("pluginPlatform.executions.failureRate")}
                                value={formatPercent(stats.failureRate)}
                            />
                        </Card>
                    </Col>
                    <Col span={5}>
                        <Card size="small">
                            <Statistic
                                title={t("pluginPlatform.executions.avgDuration")}
                                value={formatDuration(stats.avgDurationMs)}
                            />
                        </Card>
                    </Col>
                    <Col span={4}>
                        <Card size="small">
                            <Statistic
                                title={t("pluginPlatform.executions.maxDuration")}
                                value={formatDuration(stats.maxDurationMs)}
                            />
                        </Card>
                    </Col>
                </Row>
            )}
            {stats && stats.byType && stats.byType.length > 0 && (
                <Card size="small" title={t("pluginPlatform.executions.byType")}>
                    <Table<ExecutionTypeStats>
                        rowKey="extensionType"
                        size="small"
                        pagination={false}
                        dataSource={stats.byType}
                        columns={statsColumns}
                    />
                </Card>
            )}
            <Card size="small" title={t("pluginPlatform.executions.title")}>
                <Table<ExecutionLog>
                    rowKey="id"
                    size="small"
                    loading={loading}
                    dataSource={data ? data.items : []}
                    columns={columns}
                    locale={{ emptyText: t("pluginPlatform.executions.empty") }}
                    pagination={{
                        current: page,
                        pageSize,
                        total: data ? Number(data.total) : 0,
                        showSizeChanger: true,
                        onChange: (nextPage, nextPageSize) => {
                            setPage(nextPage)
                            setPageSize(nextPageSize || PAGE_SIZE)
                        }
                    }}
                />
            </Card>
        </Space>
    )
}

export default ExecutionsTab
```

**Notes:**
- `total`, `durationMs` and the stats fields are JSON strings (Java `long`); `failureRate` is a number. `formatNumber`, `formatDuration` and `formatPercent` accept both.
- A failed list request shows an empty page rather than a stuck spinner; the interceptor already toasted the HTTP error.
- Changing a filter resets the page to 1.

- [ ] **Step 2: Register the tab in `WorkbenchShell.tsx`**

Add the import:

```tsx
import ExecutionsTab from "./tabs/ExecutionsTab"
```

Append this entry to the `tabs` array, immediately after the `testRun` entry:

```tsx
        {
            key: "executions",
            label: t("pluginPlatform.workbench.tab.executions"),
            children: <ExecutionsTab />
        }
```

- [ ] **Step 3: Verify**

Run: `cd client && npx tsc --noEmit --skipLibCheck`
Expected: the four baseline errors only.

- [ ] **Step 4: Commit**

```bash
git add client/src/pages/plugin_platform/workbench/tabs/ExecutionsTab.tsx \
  client/src/pages/plugin_platform/workbench/WorkbenchShell.tsx
git commit -m "feat(script-plugin): add the executions tab with stats and server-side paging

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 10: AI History and Per-Plugin Audit Tabs

**Files:**
- Create: `client/src/pages/plugin_platform/workbench/tabs/AiHistoryTab.tsx`
- Create: `client/src/pages/plugin_platform/workbench/tabs/AuditTab.tsx`
- Modify: `client/src/pages/plugin_platform/workbench/WorkbenchShell.tsx`

**Interfaces:**
- Consumes: `listAiGenerations`, `listPluginAudit` from `../../api`; `AI_STATUS_COLORS`, `AI_STATUS_LABEL_KEYS`, `AUDIT_ACTION_LABEL_KEYS` from `../../labels`; `formatDuration`, `formatMillis`, `formatNumber` from `../../format`; `useWorkbench()`.
- Produces: `<AiHistoryTab />` as the `aiHistory` tab and `<AuditTab />` as the `audit` tab.

- [ ] **Step 1: Create `workbench/tabs/AiHistoryTab.tsx`**

```tsx
import React, { useCallback, useEffect, useState } from "react"
import { Button, Card, Table, Tag, Tooltip } from "antd"
import { ColumnsType } from "antd/lib/table"
import { useTranslation } from "react-i18next"
import { listAiGenerations } from "../../api"
import { formatDuration, formatMillis, formatNumber } from "../../format"
import { AI_STATUS_COLORS, AI_STATUS_LABEL_KEYS } from "../../labels"
import { AiGeneration, AiGenerationStatus } from "../../types"
import { useWorkbench } from "../context"

const AiHistoryTab: React.FC = () => {
    const { t } = useTranslation()
    const { plugin } = useWorkbench()
    const [generations, setGenerations] = useState<AiGeneration[]>([])
    const [loading, setLoading] = useState(false)
    const pluginKey = plugin ? plugin.pluginKey : null

    const load = useCallback(() => {
        if (!pluginKey) {
            return
        }
        setLoading(true)
        listAiGenerations(pluginKey)
            .then((loaded) => setGenerations(loaded))
            .catch(() => undefined)
            .then(() => setLoading(false))
    }, [pluginKey])

    useEffect(() => {
        load()
    }, [load])

    if (!plugin) {
        return null
    }

    const columns: ColumnsType<AiGeneration> = [
        {
            title: t("pluginPlatform.aiHistory.createdAt"),
            dataIndex: "createdAt",
            width: 170,
            render: (value: string) => formatMillis(value)
        },
        {
            title: t("pluginPlatform.aiHistory.user"),
            dataIndex: "userName",
            width: 140
        },
        {
            title: t("pluginPlatform.aiHistory.model"),
            dataIndex: "model",
            width: 160
        },
        {
            title: t("pluginPlatform.aiHistory.promptVersion"),
            dataIndex: "promptVersion",
            width: 130
        },
        {
            title: t("pluginPlatform.aiHistory.status"),
            dataIndex: "status",
            width: 110,
            render: (status: AiGenerationStatus) => (
                <Tag color={AI_STATUS_COLORS[status]}>{t(AI_STATUS_LABEL_KEYS[status])}</Tag>
            )
        },
        {
            title: t("pluginPlatform.aiHistory.attempts"),
            dataIndex: "attempts",
            width: 90
        },
        {
            title: t("pluginPlatform.aiHistory.tokens"),
            width: 140,
            render: (generation: AiGeneration) =>
                formatNumber(generation.promptTokens) + " / " + formatNumber(generation.completionTokens)
        },
        {
            title: t("pluginPlatform.aiHistory.duration"),
            dataIndex: "durationMs",
            width: 110,
            render: (value: string | null) => formatDuration(value)
        },
        {
            title: t("pluginPlatform.aiHistory.finishedAt"),
            dataIndex: "finishedAt",
            width: 170,
            render: (value: string | null) => formatMillis(value)
        },
        {
            title: t("pluginPlatform.aiHistory.error"),
            dataIndex: "errorMessage",
            ellipsis: true,
            render: (value: string | null) =>
                value ? (
                    <Tooltip title={value}>
                        <span>{value}</span>
                    </Tooltip>
                ) : (
                    "-"
                )
        }
    ]

    return (
        <Card
            size="small"
            title={t("pluginPlatform.aiHistory.title")}
            extra={
                <Button onClick={load} loading={loading}>
                    {t("pluginPlatform.common.refresh")}
                </Button>
            }
        >
            <Table<AiGeneration>
                rowKey="id"
                size="small"
                loading={loading}
                dataSource={generations}
                columns={columns}
                pagination={false}
                locale={{ emptyText: t("pluginPlatform.aiHistory.empty") }}
            />
        </Card>
    )
}

export default AiHistoryTab
```

- [ ] **Step 2: Create `workbench/tabs/AuditTab.tsx`**

```tsx
import React, { useCallback, useEffect, useState } from "react"
import { Button, Card, Table, Tag, Tooltip } from "antd"
import { ColumnsType } from "antd/lib/table"
import { useTranslation } from "react-i18next"
import { listPluginAudit } from "../../api"
import { formatMillis } from "../../format"
import { AUDIT_ACTION_LABEL_KEYS } from "../../labels"
import { AuditAction, PageResult, PluginAudit } from "../../types"
import { useWorkbench } from "../context"

const PAGE_SIZE = 20

const AuditTab: React.FC = () => {
    const { t } = useTranslation()
    const { plugin } = useWorkbench()
    const [data, setData] = useState<PageResult<PluginAudit> | null>(null)
    const [page, setPage] = useState(1)
    const [pageSize, setPageSize] = useState(PAGE_SIZE)
    const [loading, setLoading] = useState(false)
    const pluginKey = plugin ? plugin.pluginKey : null

    const load = useCallback(() => {
        if (!pluginKey) {
            return
        }
        setLoading(true)
        listPluginAudit(pluginKey, page, pageSize)
            .then((loaded) => setData(loaded))
            .catch(() => undefined)
            .then(() => setLoading(false))
    }, [pluginKey, page, pageSize])

    useEffect(() => {
        load()
    }, [load])

    if (!plugin) {
        return null
    }

    const columns: ColumnsType<PluginAudit> = [
        {
            title: t("pluginPlatform.audit.createdAt"),
            dataIndex: "createdAt",
            width: 170,
            render: (value: string) => formatMillis(value)
        },
        {
            title: t("pluginPlatform.audit.action"),
            dataIndex: "action",
            width: 150,
            render: (action: AuditAction) => t(AUDIT_ACTION_LABEL_KEYS[action])
        },
        {
            title: t("pluginPlatform.audit.operator"),
            dataIndex: "operator",
            width: 140
        },
        {
            title: t("pluginPlatform.audit.success"),
            dataIndex: "success",
            width: 110,
            render: (success: boolean) => (
                <Tag color={success ? "success" : "error"}>
                    {t(
                        success
                            ? "pluginPlatform.audit.successValue"
                            : "pluginPlatform.audit.failureValue"
                    )}
                </Tag>
            )
        },
        {
            title: t("pluginPlatform.audit.revision"),
            dataIndex: "revisionId",
            width: 120,
            render: (value: string | null) => value || "-"
        },
        {
            title: t("pluginPlatform.audit.detail"),
            dataIndex: "detail",
            ellipsis: true,
            render: (value: string | null) =>
                value ? (
                    <Tooltip title={value}>
                        <span>{value}</span>
                    </Tooltip>
                ) : (
                    "-"
                )
        }
    ]

    return (
        <Card
            size="small"
            title={t("pluginPlatform.audit.title")}
            extra={
                <Button onClick={load} loading={loading}>
                    {t("pluginPlatform.common.refresh")}
                </Button>
            }
        >
            <Table<PluginAudit>
                rowKey="id"
                size="small"
                loading={loading}
                dataSource={data ? data.items : []}
                columns={columns}
                locale={{ emptyText: t("pluginPlatform.audit.empty") }}
                pagination={{
                    current: page,
                    pageSize,
                    total: data ? Number(data.total) : 0,
                    showSizeChanger: true,
                    onChange: (nextPage, nextPageSize) => {
                        setPage(nextPage)
                        setPageSize(nextPageSize || PAGE_SIZE)
                    }
                }}
            />
        </Card>
    )
}

export default AuditTab
```

**Notes:**
- The spec's "created revision" column is not shown: `AiGenerationDTO` exposes no `revisionId`/`revisionNo`, and Phase 3B must not change the server. The `SAVE_REVISION` row in the per-plugin audit tab is the available link from a generation to its saved revision.
- Both tabs send no role-specific headers: the service layer decides, and denials are audited server-side.
- The audit list is append-only and server-paged. Refreshing re-runs the current page.
- AI generation rows are read-only; the AI history is a record, not an action surface.

- [ ] **Step 3: Register both tabs in `WorkbenchShell.tsx`**

Add the imports:

```tsx
import AiHistoryTab from "./tabs/AiHistoryTab"
import AuditTab from "./tabs/AuditTab"
```

Append these entries to the `tabs` array, immediately after the `executions` entry:

```tsx
        {
            key: "aiHistory",
            label: t("pluginPlatform.workbench.tab.aiHistory"),
            children: <AiHistoryTab />
        },
        {
            key: "audit",
            label: t("pluginPlatform.workbench.tab.audit"),
            children: <AuditTab />
        }
```

- [ ] **Step 4: Verify**

Run: `cd client && npx tsc --noEmit --skipLibCheck`
Expected: the four baseline errors only.

Static checks:
- Tab order is `overview`, `scripts`, `revisions`, `config`, `testRun`, `executions`, `aiHistory`, `audit`.
- No new locale keys are required for this task.

- [ ] **Step 5: Commit**

```bash
git add client/src/pages/plugin_platform/workbench/tabs/AiHistoryTab.tsx \
  client/src/pages/plugin_platform/workbench/tabs/AuditTab.tsx \
  client/src/pages/plugin_platform/workbench/WorkbenchShell.tsx
git commit -m "feat(script-plugin): add AI history and per-plugin audit tabs

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 11: AI Assistant — Shared Draft Panel and Chat Drawer

**Files:**
- Create: `client/src/pages/plugin_platform/workbench/AiDraftPanel.tsx`
- Create: `client/src/pages/plugin_platform/workbench/ChatDrawer.tsx`
- Modify: `client/src/pages/plugin_platform/workbench/WorkbenchShell.tsx`

**Interfaces:**
- Consumes: `streamAiDraft`, `isAbortError` from `../sse`; `errorMessage` from `../api`; `formatMillis` from `../format`; `AiDraftResult` from `../types`; `useWorkbench()` (ChatDrawer only — AiDraftPanel takes everything through props so the wizard can use it outside the provider).
- Produces:
  - `<AiDraftPanel pluginKey baseRevisionId requirementSuffix? disabled? onCompleted />` — the shared requirement/streaming UI, reused by the wizard in Task 12.
  - `<ChatDrawer open onClose />`

- [ ] **Step 1: Create `workbench/AiDraftPanel.tsx`**

```tsx
import React, { useEffect, useRef, useState } from "react"
import { Alert, Button, Input, Space, Tag, Typography, message } from "antd"
import { useTranslation } from "react-i18next"
import { errorMessage } from "../api"
import { isAbortError, streamAiDraft } from "../sse"
import { AiDraftResult } from "../types"

interface StageEntry {
    id: number
    name: string
}

interface AiDraftPanelProps {
    pluginKey: string
    baseRevisionId: string | null
    /** Appended to the requirement, separated by a blank line (e.g. archetype hints). */
    requirementSuffix?: string
    disabled?: boolean
    onCompleted: (result: AiDraftResult) => void
}

const MAX_REQUIREMENT_LENGTH = 4000

/**
 * Requirement input plus SSE generation. The server owns quota, role checks, retries and revision
 * creation; this panel only renders stream progress and hands the result back to its parent.
 */
const AiDraftPanel: React.FC<AiDraftPanelProps> = ({
    pluginKey,
    baseRevisionId,
    requirementSuffix,
    disabled,
    onCompleted
}) => {
    const { t } = useTranslation()
    const [requirement, setRequirement] = useState("")
    const [streaming, setStreaming] = useState(false)
    const [output, setOutput] = useState("")
    const [stages, setStages] = useState<StageEntry[]>([])
    const [error, setError] = useState<string | null>(null)
    const controllerRef = useRef<AbortController | null>(null)
    const stageSeqRef = useRef(0)

    useEffect(() => {
        return () => {
            if (controllerRef.current) {
                controllerRef.current.abort()
            }
        }
    }, [])

    const formatStage = (stage: string): string => {
        if (stage.indexOf("retry#") === 0) {
            return t("pluginPlatform.wizard.requirement.stage.retry", { n: stage.substring(6) })
        }
        const key = "pluginPlatform.wizard.requirement.stage." + stage
        const translated = t(key)
        return translated === key ? stage : translated
    }

    const stop = () => {
        if (controllerRef.current) {
            controllerRef.current.abort()
            controllerRef.current = null
        }
        setStreaming(false)
    }

    const generate = () => {
        const trimmed = requirement.trim()
        if (trimmed === "") {
            message.error(t("pluginPlatform.chat.requirementRequired"))
            return
        }
        const fullRequirement = requirementSuffix ? trimmed + "\n\n" + requirementSuffix : trimmed
        if (fullRequirement.length > MAX_REQUIREMENT_LENGTH) {
            message.error(t("pluginPlatform.chat.requirementTooLong"))
            return
        }
        const controller = new AbortController()
        controllerRef.current = controller
        setStreaming(true)
        setOutput("")
        setStages([])
        setError(null)
        stageSeqRef.current = 0
        streamAiDraft(
            { pluginKey, requirement: fullRequirement, baseRevisionId },
            {
                signal: controller.signal,
                onChunk: (text) => setOutput((previous) => previous + text),
                onStage: (stage) => {
                    stageSeqRef.current += 1
                    const entry: StageEntry = { id: stageSeqRef.current, name: stage }
                    setStages((previous) => previous.concat(entry))
                },
                onResult: (result) => {
                    controllerRef.current = null
                    setStreaming(false)
                    message.success(t("pluginPlatform.chat.completed", { no: result.revisionNo }))
                    onCompleted(result)
                },
                onError: (code, description) => {
                    controllerRef.current = null
                    setStreaming(false)
                    setError(description)
                }
            }
        ).catch((streamError) => {
            controllerRef.current = null
            setStreaming(false)
            if (isAbortError(streamError)) {
                message.info(t("pluginPlatform.chat.cancelled"))
                return
            }
            setError(errorMessage(streamError, "The AI request failed."))
        })
    }

    return (
        <Space direction="vertical" style={{ width: "100%" }} size="middle">
            <Input.TextArea
                rows={6}
                maxLength={MAX_REQUIREMENT_LENGTH}
                showCount
                value={requirement}
                onChange={(event) => setRequirement(event.target.value)}
                placeholder={t("pluginPlatform.chat.requirementPlaceholder")}
                disabled={disabled || streaming}
            />
            <Space wrap>
                <Button
                    type="primary"
                    onClick={generate}
                    loading={streaming}
                    disabled={disabled || streaming}
                >
                    {streaming ? t("pluginPlatform.chat.streaming") : t("pluginPlatform.chat.generate")}
                </Button>
                {streaming && (
                    <Button danger onClick={stop}>
                        {t("pluginPlatform.chat.cancel")}
                    </Button>
                )}
            </Space>
            {stages.length > 0 && (
                <div>
                    <Typography.Text type="secondary">{t("pluginPlatform.chat.stage")}</Typography.Text>
                    <div style={{ marginTop: 4 }}>
                        <Space wrap>
                            {stages.map((stage) => (
                                <Tag key={stage.id}>{formatStage(stage.name)}</Tag>
                            ))}
                        </Space>
                    </div>
                </div>
            )}
            {output && (
                <div>
                    <Typography.Text type="secondary">{t("pluginPlatform.chat.output")}</Typography.Text>
                    <Typography.Paragraph
                        style={{
                            whiteSpace: "pre-wrap",
                            background: "#fafafa",
                            padding: 8,
                            maxHeight: 240,
                            overflow: "auto"
                        }}
                    >
                        {output}
                    </Typography.Paragraph>
                </div>
            )}
            {error && <Alert type="error" showIcon message={error} />}
        </Space>
    )
}

export default AiDraftPanel
```

**Notes:**
- `onError` from the SSE envelope is displayed inline. Unlike HTTP calls made through `request`, the streaming POST bypasses the interceptor, so nothing is toasted twice.
- Cancelling aborts the fetch; the server notices the closed stream and stops emitting. A generation already persisted stays in the AI history; reopening the drawer and refreshing shows it.
- Stage ids come from a monotonic ref, so the tag list has stable keys even when the same stage repeats (retries).

- [ ] **Step 2: Create `workbench/ChatDrawer.tsx`**

```tsx
import React, { useState } from "react"
import { Alert, Drawer, Select, Space, Typography } from "antd"
import { useTranslation } from "react-i18next"
import { formatMillis } from "../format"
import AiDraftPanel from "./AiDraftPanel"
import { useWorkbench } from "./context"

interface ChatDrawerProps {
    open: boolean
    onClose: () => void
}

const ChatDrawer: React.FC<ChatDrawerProps> = ({ open, onClose }) => {
    const { t } = useTranslation()
    const { plugin, revisions, capabilities, reload } = useWorkbench()
    const [baseRevisionId, setBaseRevisionId] = useState<string | null>(null)
    const [lastRevisionNo, setLastRevisionNo] = useState<number | null>(null)

    if (!plugin) {
        return null
    }

    const revisionOptions = [
        { label: t("pluginPlatform.chat.baseRevisionNone"), value: "" }
    ].concat(
        revisions.map((revision) => ({
            label: "v" + revision.revisionNo + " · " + formatMillis(revision.createdAt),
            value: revision.id
        }))
    )

    return (
        <Drawer
            title={t("pluginPlatform.chat.title")}
            placement="right"
            width={560}
            visible={open}
            onClose={onClose}
        >
            {!capabilities.canDevelop ? (
                <Alert type="warning" showIcon message={t("pluginPlatform.chat.noPermission")} />
            ) : (
                <Space direction="vertical" style={{ width: "100%" }} size="middle">
                    <div>
                        <Typography.Text type="secondary">
                            {t("pluginPlatform.chat.baseRevision")}
                        </Typography.Text>
                        <Select
                            style={{ width: "100%", marginTop: 4 }}
                            value={baseRevisionId === null ? "" : baseRevisionId}
                            onChange={(value: string) => setBaseRevisionId(value === "" ? null : value)}
                            options={revisionOptions}
                        />
                    </div>
                    {lastRevisionNo !== null && (
                        <Alert
                            type="success"
                            showIcon
                            message={t("pluginPlatform.chat.completed", { no: lastRevisionNo })}
                        />
                    )}
                    <AiDraftPanel
                        pluginKey={plugin.pluginKey}
                        baseRevisionId={baseRevisionId}
                        onCompleted={(result) => {
                            setLastRevisionNo(result.revisionNo)
                            reload().then(() => undefined)
                        }}
                    />
                </Space>
            )}
        </Drawer>
    )
}

export default ChatDrawer
```

**Notes:**
- A completed generation saves a revision server-side, so `reload()` refreshes the workbench (header status, Revisions tab, Scripts seed) in place.
- The drawer keeps the last successful revision number in a persistent success alert (the toast alone is transient), which is how spec §5.5's "the drawer shows the new revision number" is met.
- The selected base revision is drawer state; it survives closing and reopening until the workbench switches plugin (the provider remount resets the drawer too).

- [ ] **Step 3: Wire the drawer into `WorkbenchShell.tsx`**

Add the import:

```tsx
import ChatDrawer from "./ChatDrawer"
```

Change the context destructure from `{ plugin, loading, error, reload }` to include capabilities:

```tsx
    const { plugin, capabilities, loading, error, reload } = useWorkbench()
```

Add drawer state next to `activeTab`:

```tsx
    const [chatOpen, setChatOpen] = useState(false)
```

Add this button to the right-hand `Space` in the header, before the refresh button:

```tsx
                        {capabilities.canDevelop && (
                            <Button onClick={() => setChatOpen(true)}>
                                {t("pluginPlatform.workbench.aiAssistant")}
                            </Button>
                        )}
```

Render the drawer just before the closing `</Space>` of the outer container:

```tsx
                <ChatDrawer open={chatOpen} onClose={() => setChatOpen(false)} />
```

- [ ] **Step 4: Verify**

Run: `cd client && npx tsc --noEmit --skipLibCheck`
Expected: the four baseline errors only.

Static checks:
- The drawer is reachable only through the workbench (the provider supplies `pluginKey`).
- No new locale keys are required; the stage vocabulary (`declarations`, `compiling`, `wiring`, `retry#N`) maps onto existing `pluginPlatform.wizard.requirement.stage.*` keys.

- [ ] **Step 5: Commit**

```bash
git add client/src/pages/plugin_platform/workbench/AiDraftPanel.tsx \
  client/src/pages/plugin_platform/workbench/ChatDrawer.tsx \
  client/src/pages/plugin_platform/workbench/WorkbenchShell.tsx
git commit -m "feat(script-plugin): add the AI assistant drawer and shared draft panel

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 12: New-Plugin Wizard

**Files:**
- Create: `client/src/pages/plugin_platform/workbench/wizard/NewPluginWizard.tsx`
- Create: `client/src/pages/plugin_platform/workbench/wizard/BasicsStep.tsx`
- Create: `client/src/pages/plugin_platform/workbench/wizard/RequirementStep.tsx`
- Create: `client/src/pages/plugin_platform/workbench/wizard/ReviewStep.tsx`
- Create: `client/src/pages/plugin_platform/workbench/wizard/FinishStep.tsx`
- Modify: `client/src/pages/plugin_platform/workbench/index.tsx`

**Interfaces:**
- Consumes: `createDraft`, `getPlugin`, `getContract`, `saveRevision`, `validatePlugin`, `enablePlugin` from `../api`; `usePluginCapabilities`; `toDeclarationRows`, `fromDeclarationRows`, `validateDeclarationRows`, `toConfigRows`, `fromConfigRows`, `validateConfigRows`, `RowIssue` from `../formModels`; `EXTENSION_TYPES` from `../formModels`; `EXTENSION_TYPE_LABEL_KEYS` from `../labels`; `<AiDraftPanel />`; `<ValidationReportView />`; the shared editors.
- Produces: `<NewPluginWizard />` rendered for `?mode=new`, replacing the Task 4 placeholder.

The wizard has four steps and one server-side draft:
1. **Basics** — `createDraft` runs once; on success the plugin exists in `DRAFT` with no revision.
2. **Requirement and AI** — optional SSE generation (saves a revision and refreshes the detail) or "Continue without AI".
3. **Review** — editable scripts/declarations/config seeded from the AI result (or empty), with an explicit "Save revision" action.
4. **Finish** — run validation, then (admins only) enable, then open the workbench for the new plugin.

- [ ] **Step 1: Create `wizard/BasicsStep.tsx`**

```tsx
import React, { useState } from "react"
import { Button, Form, Input, Select, Space, message } from "antd"
import { useTranslation } from "react-i18next"
import { createDraft } from "../../api"
import { CreateDraftRequest, ScriptPlugin } from "../../types"

interface BasicsStepProps {
    onCreated: (plugin: ScriptPlugin) => void
}

interface BasicsValues {
    pluginKey: string
    name: string
    description?: string
    applySystem: string
    applyModule?: string
}

const BasicsStep: React.FC<BasicsStepProps> = ({ onCreated }) => {
    const { t } = useTranslation()
    const [submitting, setSubmitting] = useState(false)

    const submit = (values: BasicsValues) => {
        const payload: CreateDraftRequest = {
            pluginKey: values.pluginKey.trim(),
            name: values.name.trim(),
            description: (values.description || "").trim(),
            applySystem: values.applySystem,
            applyModule: (values.applyModule || "").trim()
        }
        setSubmitting(true)
        createDraft(payload)
            .then((created) => {
                message.success(t("pluginPlatform.wizard.requirement.created"))
                onCreated(created)
            })
            .catch(() => undefined)
            .then(() => setSubmitting(false))
    }

    return (
        <Form<BasicsValues>
            layout="vertical"
            initialValues={{ applySystem: "wes" }}
            onFinish={submit}
        >
            <Form.Item
                name="pluginKey"
                label={t("pluginPlatform.wizard.basics.pluginKey")}
                extra={t("pluginPlatform.wizard.basics.pluginKeyHint")}
                rules={[
                    {
                        required: true,
                        whitespace: true,
                        message: t("pluginPlatform.wizard.basics.pluginKeyRequired")
                    },
                    {
                        pattern: /^[A-Za-z0-9._-]+$/,
                        message: t("pluginPlatform.wizard.basics.pluginKeyInvalid")
                    }
                ]}
            >
                <Input maxLength={64} />
            </Form.Item>
            <Form.Item
                name="name"
                label={t("pluginPlatform.wizard.basics.name")}
                rules={[
                    {
                        required: true,
                        whitespace: true,
                        message: t("pluginPlatform.wizard.basics.nameRequired")
                    }
                ]}
            >
                <Input maxLength={128} />
            </Form.Item>
            <Form.Item name="description" label={t("pluginPlatform.wizard.basics.description")}>
                <Input.TextArea rows={3} maxLength={512} />
            </Form.Item>
            <Form.Item
                name="applySystem"
                label={t("pluginPlatform.wizard.basics.applySystem")}
                rules={[
                    {
                        required: true,
                        message: t("pluginPlatform.wizard.basics.applySystemRequired")
                    }
                ]}
            >
                <Select
                    options={[
                        { label: t("pluginPlatform.applySystem.wes"), value: "wes" },
                        { label: t("pluginPlatform.applySystem.station"), value: "station" }
                    ]}
                />
            </Form.Item>
            <Form.Item name="applyModule" label={t("pluginPlatform.wizard.basics.applyModule")}>
                <Input maxLength={64} />
            </Form.Item>
            <Space>
                <Button type="primary" htmlType="submit" loading={submitting}>
                    {t("pluginPlatform.common.next")}
                </Button>
            </Space>
        </Form>
    )
}

export default BasicsStep
```

`whitespace: true` makes the required rule reject a value made only of spaces, so the subsequent `trim()` cannot produce an empty required field.

- [ ] **Step 2: Create `wizard/RequirementStep.tsx`**

```tsx
import React, { useState } from "react"
import { Alert, Button, Select, Space, Tag, Typography } from "antd"
import { useTranslation } from "react-i18next"
import { EXTENSION_TYPES } from "../../formModels"
import { EXTENSION_TYPE_LABEL_KEYS } from "../../labels"
import { AiDraftResult, ExtensionType, PluginContract, ScriptPlugin } from "../../types"
import AiDraftPanel from "../AiDraftPanel"

interface RequirementStepProps {
    plugin: ScriptPlugin
    contract: PluginContract | null
    aiResult: AiDraftResult | null
    onResult: (result: AiDraftResult) => void
    onNext: () => void
}

const RequirementStep: React.FC<RequirementStepProps> = ({
    plugin,
    contract,
    aiResult,
    onResult,
    onNext
}) => {
    const { t } = useTranslation()
    const [hints, setHints] = useState<ExtensionType[]>([])

    const types = contract
        ? contract.archetypes.map((archetype) => archetype.type)
        : EXTENSION_TYPES
    const suffix =
        hints.length > 0 ? "Suggested extension types: " + hints.join(", ") : undefined

    return (
        <Space direction="vertical" style={{ width: "100%" }} size="middle">
            <Typography.Title level={5} style={{ margin: 0 }}>
                {t("pluginPlatform.wizard.requirement.title")}
            </Typography.Title>
            <div>
                <Typography.Text>{t("pluginPlatform.wizard.requirement.hints")}</Typography.Text>
                <Select
                    mode="multiple"
                    style={{ width: "100%", marginTop: 4 }}
                    value={hints}
                    onChange={(value: ExtensionType[]) => setHints(value)}
                    placeholder={t("pluginPlatform.wizard.requirement.hintsHint")}
                    options={types.map((type) => ({
                        label: t(EXTENSION_TYPE_LABEL_KEYS[type]),
                        value: type
                    }))}
                />
            </div>
            <AiDraftPanel
                pluginKey={plugin.pluginKey}
                baseRevisionId={null}
                requirementSuffix={suffix}
                onCompleted={onResult}
            />
            {aiResult && (
                <Alert
                    type="success"
                    showIcon
                    message={t("pluginPlatform.wizard.requirement.result")}
                    description={
                        <Space direction="vertical">
                            <span>
                                {t("pluginPlatform.wizard.requirement.resultSummary", {
                                    no: aiResult.revisionNo
                                })}
                            </span>
                            {aiResult.changeSummary && <span>{aiResult.changeSummary}</span>}
                            <Tag color={aiResult.materializationVerified ? "success" : "warning"}>
                                {aiResult.materializationVerified
                                    ? t("pluginPlatform.wizard.review.materializationVerified")
                                    : t("pluginPlatform.wizard.review.materializationUnverified")}
                            </Tag>
                        </Space>
                    }
                />
            )}
            <Space wrap>
                {aiResult ? (
                    <Button type="primary" onClick={onNext}>
                        {t("pluginPlatform.common.next")}
                    </Button>
                ) : (
                    <Button onClick={onNext}>
                        {t("pluginPlatform.wizard.requirement.skipAi")}
                    </Button>
                )}
            </Space>
        </Space>
    )
}

export default RequirementStep
```

The hint suffix is appended to the requirement by `AiDraftPanel` before sending; the enum names are the server's stable vocabulary (`OUTBOUND_WAVE_WAVE_ACTION`, …), not translated labels.

- [ ] **Step 3: Create `wizard/ReviewStep.tsx`**

```tsx
import React, { useState } from "react"
import { Alert, Button, Card, Input, Space, Typography, message } from "antd"
import { useTranslation } from "react-i18next"
import { saveRevision } from "../../api"
import {
    ConfigRow,
    DeclarationRow,
    RowIssue,
    fromConfigRows,
    fromDeclarationRows,
    toConfigRows,
    toDeclarationRows,
    validateConfigRows,
    validateDeclarationRows
} from "../../formModels"
import { AiDraftResult, PluginContract, ScriptPlugin, Scripts } from "../../types"
import ConfigSchemaTable from "../../ConfigSchemaTable"
import DeclarationTable from "../../DeclarationTable"
import ScriptFileEditor from "../../ScriptFileEditor"

interface ReviewStepProps {
    plugin: ScriptPlugin
    aiResult: AiDraftResult | null
    contract: PluginContract | null
    onSaved: () => void
    onNext: () => void
}

const ReviewStep: React.FC<ReviewStepProps> = ({ plugin, aiResult, contract, onSaved, onNext }) => {
    const { t } = useTranslation()
    const [scripts, setScripts] = useState<Scripts>(aiResult ? aiResult.scripts : {})
    const [declarations, setDeclarations] = useState<DeclarationRow[]>(
        toDeclarationRows(aiResult ? aiResult.extensions : [])
    )
    const [configRows, setConfigRows] = useState<ConfigRow[]>(
        toConfigRows(aiResult ? aiResult.configSchema : [])
    )
    const [changeSummary, setChangeSummary] = useState("")
    const [saving, setSaving] = useState(false)
    const [saved, setSaved] = useState(false)

    const hasRevision = !!plugin.currentRevisionId || !!aiResult || saved

    const issueText = (issue: RowIssue): string => t(issue.key, issue.values)

    const save = () => {
        const fileNames = Object.keys(scripts)
        if (fileNames.length === 0) {
            message.error(t("pluginPlatform.scripts.noScripts"))
            return
        }
        const declarationIssue = validateDeclarationRows(declarations, fileNames)
        if (declarationIssue) {
            message.error(issueText(declarationIssue))
            return
        }
        const configIssue = validateConfigRows(configRows)
        if (configIssue) {
            message.error(issueText(configIssue))
            return
        }
        setSaving(true)
        saveRevision(plugin.pluginKey, {
            scripts,
            extensionPoints: fromDeclarationRows(declarations),
            configSchema: fromConfigRows(configRows),
            changeSummary: changeSummary.trim()
        })
            .then(() => {
                setSaved(true)
                message.success(t("pluginPlatform.wizard.review.saved"))
                onSaved()
            })
            .catch(() => undefined)
            .then(() => setSaving(false))
    }

    return (
        <Space direction="vertical" style={{ width: "100%" }} size="middle">
            <Typography.Title level={5} style={{ margin: 0 }}>
                {t("pluginPlatform.wizard.review.title")}
            </Typography.Title>
            {aiResult ? (
                <Alert
                    type="success"
                    showIcon
                    message={t("pluginPlatform.wizard.requirement.resultSummary", {
                        no: aiResult.revisionNo
                    })}
                    description={
                        <span>
                            {aiResult.materializationVerified
                                ? t("pluginPlatform.wizard.review.materializationVerified")
                                : t("pluginPlatform.wizard.review.materializationUnverified")}
                        </span>
                    }
                />
            ) : (
                <Alert
                    type="warning"
                    showIcon
                    message={t("pluginPlatform.wizard.review.materializationUnverified")}
                />
            )}
            <Card size="small" title={t("pluginPlatform.scripts.files")}>
                <ScriptFileEditor
                    scripts={scripts}
                    onChange={setScripts}
                    readOnly={false}
                    seedKey={aiResult ? "ai:" + aiResult.revisionId : "manual"}
                />
            </Card>
            <Card size="small" title={t("pluginPlatform.scripts.declarations")}>
                <DeclarationTable
                    rows={declarations}
                    onChange={setDeclarations}
                    readOnly={false}
                    allowedTypes={contract ? contract.archetypes.map((archetype) => archetype.type) : []}
                    lifecycleEntityNames={contract ? contract.lifecycleEntityNames : []}
                />
            </Card>
            <Card size="small" title={t("pluginPlatform.scripts.configSchema")}>
                <ConfigSchemaTable rows={configRows} onChange={setConfigRows} readOnly={false} />
            </Card>
            <Card size="small">
                <Space direction="vertical" style={{ width: "100%" }}>
                    <Input.TextArea
                        rows={2}
                        maxLength={512}
                        value={changeSummary}
                        onChange={(event) => setChangeSummary(event.target.value)}
                        placeholder={t("pluginPlatform.scripts.changeSummary")}
                    />
                    <Button type="primary" onClick={save} loading={saving}>
                        {t("pluginPlatform.wizard.review.saveRevision")}
                    </Button>
                </Space>
            </Card>
            <Button type="primary" onClick={onNext} disabled={!hasRevision}>
                {t("pluginPlatform.wizard.review.next")}
            </Button>
        </Space>
    )
}

export default ReviewStep
```

**Notes:**
- State initialisers run once when the step mounts, so the AI result seeds the editors without overwriting later edits on re-render.
- Saving here is optional for the AI path (a revision already exists) and required for the manual path — `hasRevision` captures both.
- The parent's `onSaved` refetches the plugin, which updates `plugin.currentRevisionId` for the Finish step.

- [ ] **Step 4: Create `wizard/FinishStep.tsx`**

```tsx
import React, { useState } from "react"
import { Alert, Button, Card, Space, Typography, message } from "antd"
import { useTranslation } from "react-i18next"
import { enablePlugin, validatePlugin } from "../../api"
import { ScriptPlugin, ValidationReport } from "../../types"
import ValidationReportView from "../../ValidationReportView"

interface FinishStepProps {
    plugin: ScriptPlugin
    canAdminister: boolean
    onRefresh: () => void
    onClose: () => void
}

const FinishStep: React.FC<FinishStepProps> = ({ plugin, canAdminister, onRefresh, onClose }) => {
    const { t } = useTranslation()
    const [report, setReport] = useState<ValidationReport | null>(null)
    const [validating, setValidating] = useState(false)
    const [enabling, setEnabling] = useState(false)

    const runValidation = () => {
        setValidating(true)
        validatePlugin(plugin.pluginKey)
            .then((loaded) => {
                setReport(loaded)
                message.success(t("pluginPlatform.overview.validationFinished"))
                onRefresh()
            })
            .catch(() => undefined)
            .then(() => setValidating(false))
    }

    const enable = () => {
        if (!report || !report.passed) {
            message.error(t("pluginPlatform.wizard.finish.validationRequired"))
            return
        }
        setEnabling(true)
        enablePlugin(plugin.pluginKey)
            .then(() => {
                message.success(t("pluginPlatform.wizard.finish.enabled"))
                onRefresh()
                onClose()
            })
            .catch(() => undefined)
            .then(() => setEnabling(false))
    }

    return (
        <Space direction="vertical" style={{ width: "100%" }} size="middle">
            <Typography.Title level={5} style={{ margin: 0 }}>
                {t("pluginPlatform.wizard.finish.title")}
            </Typography.Title>
            {!canAdminister && (
                <Alert type="info" showIcon message={t("pluginPlatform.wizard.finish.developerHint")} />
            )}
            <Space wrap>
                <Button type="primary" onClick={runValidation} loading={validating}>
                    {t("pluginPlatform.wizard.finish.runValidation")}
                </Button>
                <Button
                    type="primary"
                    onClick={enable}
                    loading={enabling}
                    disabled={!canAdminister || !report || !report.passed}
                >
                    {t("pluginPlatform.wizard.finish.enableNow")}
                </Button>
                <Button onClick={onClose}>{t("pluginPlatform.wizard.finish.finish")}</Button>
            </Space>
            {report && (
                <Card size="small" title={t("pluginPlatform.validation.reportTitle")}>
                    <ValidationReportView report={report} />
                </Card>
            )}
        </Space>
    )
}

export default FinishStep
```

`Enable now` requires an administrator (server rule), a successful validation in this session, and therefore a current revision. Enabling navigates to the workbench for the new plugin.

- [ ] **Step 5: Create `wizard/NewPluginWizard.tsx`**

```tsx
import React, { useEffect, useState } from "react"
import { Alert, Button, Card, Modal, Spin, Steps } from "antd"
import { useTranslation } from "react-i18next"
import { useHistory } from "react-router"
import { getContract, getPlugin } from "../api"
import { AiDraftResult, PluginContract, ScriptPlugin } from "../types"
import usePluginCapabilities from "../usePluginCapabilities"
import BasicsStep from "./BasicsStep"
import RequirementStep from "./RequirementStep"
import ReviewStep from "./ReviewStep"
import FinishStep from "./FinishStep"

const NewPluginWizard: React.FC = () => {
    const { t } = useTranslation()
    const history = useHistory()
    const capabilities = usePluginCapabilities()
    const [current, setCurrent] = useState(0)
    const [plugin, setPlugin] = useState<ScriptPlugin | null>(null)
    const [aiResult, setAiResult] = useState<AiDraftResult | null>(null)
    const [contract, setContract] = useState<PluginContract | null>(null)

    const applySystem = plugin ? plugin.applySystem : null

    useEffect(() => {
        if (!applySystem) {
            return
        }
        let active = true
        getContract(applySystem)
            .then((loaded) => {
                if (active) {
                    setContract(loaded)
                }
            })
            .catch(() => {
                if (active) {
                    setContract(null)
                }
            })
        return () => {
            active = false
        }
    }, [applySystem])

    const refreshPlugin = () => {
        if (!plugin) {
            return Promise.resolve()
        }
        return getPlugin(plugin.pluginKey)
            .then((detail) => setPlugin(detail))
            .catch(() => undefined)
    }

    const cancel = () => {
        Modal.confirm({
            title: t("pluginPlatform.wizard.cancel"),
            content: t("pluginPlatform.wizard.cancelConfirm"),
            okText: t("pluginPlatform.common.confirm"),
            cancelText: t("pluginPlatform.common.cancel"),
            onOk: () => history.push("/plugin-platform/plugins")
        })
    }

    if (capabilities.loading) {
        return (
            <Card>
                <Spin />
            </Card>
        )
    }
    if (!capabilities.canDevelop) {
        return (
            <Card>
                <Alert type="warning" showIcon message={t("pluginPlatform.wizard.noPermission")} />
            </Card>
        )
    }

    const steps = [
        { title: t("pluginPlatform.wizard.step.basics") },
        { title: t("pluginPlatform.wizard.step.requirement") },
        { title: t("pluginPlatform.wizard.step.review") },
        { title: t("pluginPlatform.wizard.step.finish") }
    ]

    return (
        <Card
            title={t("pluginPlatform.wizard.title")}
            extra={<Button onClick={cancel}>{t("pluginPlatform.wizard.cancel")}</Button>}
        >
            <Steps current={current} items={steps} style={{ marginBottom: 24 }} />
            {current === 0 && (
                <BasicsStep
                    onCreated={(created) => {
                        setPlugin(created)
                        setCurrent(1)
                    }}
                />
            )}
            {current === 1 && plugin && (
                <RequirementStep
                    plugin={plugin}
                    contract={contract}
                    aiResult={aiResult}
                    onResult={(result) => {
                        setAiResult(result)
                        refreshPlugin()
                    }}
                    onNext={() => setCurrent(2)}
                />
            )}
            {current === 2 && plugin && (
                <ReviewStep
                    plugin={plugin}
                    aiResult={aiResult}
                    contract={contract}
                    onSaved={refreshPlugin}
                    onNext={() => setCurrent(3)}
                />
            )}
            {current === 3 && plugin && (
                <FinishStep
                    plugin={plugin}
                    canAdminister={capabilities.canAdminister}
                    onRefresh={refreshPlugin}
                    onClose={() =>
                        history.push(
                            "/plugin-platform/plugin-workbench?pluginKey=" +
                                encodeURIComponent(plugin.pluginKey)
                        )
                    }
                />
            )}
        </Card>
    )
}

export default NewPluginWizard
```

**Notes:**
- `createDraft` runs exactly once, from `BasicsStep`'s submit. There is no Back button after step 0, so a second draft cannot be created accidentally.
- Accepted limitation (spec §5.1): the URL stays `?mode=new` for the whole wizard and only switches to `?pluginKey=...` on Finish. Reloading mid-wizard restarts at Basics; submitting the same `pluginKey` again is rejected by the server as a duplicate. The user can cancel and open the existing plugin from the list. Switching the query to `?pluginKey=...` right after `createDraft` would unmount the wizard steps, so the four-step flow takes precedence over the literal §5.1 wording.
- The wizard does not use `WorkbenchProvider`: before a draft exists there is no `pluginKey`, and the wizard tracks the plugin itself.
- `refreshPlugin` is called after AI completion (a revision now exists) and after saving in Review, so the Review gate and Finish actions see the current revision.
- Deviation from spec §5.4: the validation report is rendered in Finish, not Review. Validation needs a saved revision, which Review only guarantees at its end; keeping a single validation surface avoids a stale report after edits. Review shows the AI's materialization-verification status instead.

- [ ] **Step 6: Replace `workbench/index.tsx` with the final version**

```tsx
import React, { useRef } from "react"
import { Button, Card, Empty, Typography } from "antd"
import { useTranslation } from "react-i18next"
import { useHistory, useLocation } from "react-router"
import { WorkbenchProvider } from "./context"
import NewPluginWizard from "./wizard/NewPluginWizard"
import WorkbenchShell from "./WorkbenchShell"

const Workbench: React.FC = () => {
    const { t } = useTranslation()
    const history = useHistory()
    const location = useLocation()
    const params = new URLSearchParams(location.search)
    const mode = params.get("mode")
    const pluginKey = params.get("pluginKey")
    // Keeps the current plugin when TabsLayout restores this tab without its query string.
    const lastPluginKeyRef = useRef<string | null>(null)
    const previousModeRef = useRef<string | null>(null)
    const wizardKeyRef = useRef(0)

    if (pluginKey) {
        lastPluginKeyRef.current = pluginKey
    }
    // Query-only navigation does not remount this component; bump the key so "New plugin"
    // entered from a workbench tab starts a fresh wizard.
    if (mode === "new" && previousModeRef.current !== "new") {
        wizardKeyRef.current += 1
    }
    previousModeRef.current = mode

    if (mode === "new") {
        return <NewPluginWizard key={wizardKeyRef.current} />
    }

    const activePluginKey = pluginKey || lastPluginKeyRef.current
    if (!activePluginKey) {
        return (
            <Card>
                <Empty description={t("pluginPlatform.workbench.noPlugin")}>
                    <Typography.Paragraph type="secondary">
                        {t("pluginPlatform.workbench.noPluginHint")}
                    </Typography.Paragraph>
                    <Button type="primary" onClick={() => history.push("/plugin-platform/plugins")}>
                        {t("pluginPlatform.workbench.backToList")}
                    </Button>
                </Empty>
            </Card>
        )
    }

    return (
        <WorkbenchProvider key={activePluginKey} pluginKey={activePluginKey}>
            <WorkbenchShell />
        </WorkbenchProvider>
    )
}

export default Workbench
```

- [ ] **Step 7: Verify**

Run: `cd client && npx tsc --noEmit --skipLibCheck`
Expected: the four baseline errors only.

Static checks:
- `createDraft` is called only from `BasicsStep`.
- Step 2's Next is disabled until `aiResult` exists; the only other path is the explicit "Continue without AI".
- Step 3's Next is disabled until a revision exists (AI, saved, or already current).
- Step 4's Enable button is disabled for non-admins and until validation passes.

- [ ] **Step 8: Commit**

```bash
git add client/src/pages/plugin_platform/workbench/wizard/NewPluginWizard.tsx \
  client/src/pages/plugin_platform/workbench/wizard/BasicsStep.tsx \
  client/src/pages/plugin_platform/workbench/wizard/RequirementStep.tsx \
  client/src/pages/plugin_platform/workbench/wizard/ReviewStep.tsx \
  client/src/pages/plugin_platform/workbench/wizard/FinishStep.tsx \
  client/src/pages/plugin_platform/workbench/index.tsx
git commit -m "feat(script-plugin): add the new-plugin wizard

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 13: Global Audit Page, Route and Final Verification

**Files:**
- Create: `client/src/pages/plugin_platform/audit/index.tsx`
- Modify: `client/src/routes/path2Compoment.tsx`
- Verify: the whole Phase 3B client change set

**Interfaces:**
- Consumes: `PluginAudit` from `../types`; the AMIS CRUD `perPageField` option; the `pluginPlatform.audit.*` and `pluginPlatform.auditAction.*` locale keys from Task 1.
- Produces: the admin-only `/plugin-platform/audit` page (the `u_menu` row seeded by Phase 3A already points at this path) and the final verification record.

- [ ] **Step 1: Create the AMIS global audit page at `audit/index.tsx`**

The endpoint `GET /plugin-platform/scriptPlugin/audit` requires an administrator and accepts only `page` and `pageSize`, so the page has no filter form and uses AMIS's `perPageField` to send the size under the server's name.

```tsx
import schema2component from "@/utils/schema2component"
import { PluginAudit } from "../types"

interface AuditPagePayload {
    items?: PluginAudit[]
    total?: number | string
}

/**
 * AMIS CRUD adaptor. The interceptor resolves business errors as HTTP 200 envelopes; returning an
 * empty page here keeps AMIS from rendering the envelope as a row and from toasting twice.
 */
async function adaptAuditPage(payload: unknown): Promise<{ items: PluginAudit[]; total: number }> {
    const envelope = payload as { code?: string; status?: string; data?: AuditPagePayload } | null
    if (!envelope || envelope.status || envelope.code !== "0" || !envelope.data) {
        return { items: [], total: 0 }
    }
    const items = Array.isArray(envelope.data.items) ? envelope.data.items : []
    return { items, total: Number(envelope.data.total || 0) }
}

const columns = [
    {
        name: "createdAt",
        label: "pluginPlatform.audit.createdAt",
        width: 170,
        tpl: "${createdAt|millis}"
    },
    {
        name: "pluginKey",
        label: "pluginPlatform.audit.plugin",
        width: 220
    },
    {
        name: "action",
        label: "pluginPlatform.audit.action",
        type: "mapping",
        width: 150,
        map: {
            CREATE_DRAFT: "pluginPlatform.auditAction.createDraft",
            SAVE_REVISION: "pluginPlatform.auditAction.saveRevision",
            VALIDATE: "pluginPlatform.auditAction.validate",
            ENABLE: "pluginPlatform.auditAction.enable",
            DISABLE: "pluginPlatform.auditAction.disable",
            ARCHIVE: "pluginPlatform.auditAction.archive",
            ROLLBACK: "pluginPlatform.auditAction.rollback",
            CONFIG_SAVE: "pluginPlatform.auditAction.configSave",
            METADATA_UPDATE: "pluginPlatform.auditAction.metadataUpdate",
            TEST_RUN: "pluginPlatform.auditAction.testRun"
        }
    },
    {
        name: "operator",
        label: "pluginPlatform.audit.operator",
        width: 160
    },
    {
        name: "success",
        label: "pluginPlatform.audit.success",
        type: "mapping",
        width: 110,
        map: {
            "true": "pluginPlatform.audit.successValue",
            "false": "pluginPlatform.audit.failureValue"
        }
    },
    {
        name: "revisionId",
        label: "pluginPlatform.audit.revision",
        width: 120,
        tpl: "${revisionId || '-'}"
    },
    {
        name: "detail",
        label: "pluginPlatform.audit.detail",
        tpl: "${detail || '-'}"
    }
]

const schema = {
    type: "page",
    title: "pluginPlatform.audit.title",
    toolbar: [],
    body: [
        {
            type: "crud",
            name: "PluginAuditTable",
            syncLocation: false,
            api: {
                method: "get",
                url: "/plugin-platform/scriptPlugin/audit",
                adaptor: adaptAuditPage
            },
            // AMIS CRUD names the page-size field perPage; the endpoint expects pageSize.
            perPageField: "pageSize",
            autoFillHeight: true,
            placeholder: "pluginPlatform.audit.empty",
            headerToolbar: ["reload"],
            footerToolbar: ["switch-per-page", "statistics", "pagination"],
            columns: columns
        }
    ]
}

export default schema2component(schema)
```

**Notes:**
- `success` is a JSON boolean; AMIS `mapping` looks it up as the string key `"true"`/`"false"`.
- `createdAt` is a Jackson string; the `|millis` filter registered in Task 1 formats it from epoch milliseconds (the server stores millis, unlike the `/1000|date` columns on older pages).
- The endpoint does not accept filters; do not add a `filter` block, because every filter field would be sent as an unsupported query parameter.

- [ ] **Step 2: Register the route in `path2Compoment.tsx`**

Insert after the `/plugin-platform/plugin-workbench` entry (from Task 3) and before the `// Monitoring` comment:

```tsx
    {
        path: "/plugin-platform/audit",
        name: (
            <Translation>{(t) => t("pluginPlatform.audit.title")}</Translation>
        ),
        component: lazy(() => import("@/pages/plugin_platform/audit"))
    },
```

The route is in `menuRouter` so `TabsLayout` registers a tab. The sidebar entry only appears for roles granted the `u_menu` row (Phase 3A seeds it for `pluginAdmin`); the server independently rejects non-admins.

- [ ] **Step 3: Typecheck**

Run: `cd client && npx tsc --noEmit --skipLibCheck`
Expected: exactly four errors, all pre-existing:
- `src/pages/wms/station/index.tsx(152,46)`
- `src/pages/wms/station/index.tsx(152,50)`
- `src/pages/wms/station/index.tsx(152,68)`
- `src/utils/polyfill.tsx(13,27)`

None may mention `plugin_platform`.

- [ ] **Step 4: Production build**

Run: `cd client && npm run build`
Expected: `EXIT:0` (the pre-change baseline). The first build takes minutes.

- [ ] **Step 5: Locale parity**

Run:

```bash
cd client && node -e "const en=require('./src/locales/en-us.json'); const zh=require('./src/locales/zh-cn.json'); const enKeys=Object.keys(en).filter(k=>k.startsWith('pluginPlatform.')); const zhKeys=Object.keys(zh).filter(k=>k.startsWith('pluginPlatform.')); console.log('missing zh:', enKeys.filter(k=>!(k in zh)).length, 'missing en:', zhKeys.filter(k=>!(k in en)).length)"
```

Expected: `missing zh: 0 missing en: 0`.

Then confirm every `pluginPlatform.*` key referenced in code exists in both locale files:

```bash
cd client && node -e "const fs=require('fs'), path=require('path'); const en=require('./src/locales/en-us.json'); const zh=require('./src/locales/zh-cn.json'); const files=[]; (function walk(dir){ for(const name of fs.readdirSync(dir)){ const full=path.join(dir,name); const stat=fs.statSync(full); if(stat.isDirectory()) walk(full); else if(/\.(ts|tsx)$/.test(name)) files.push(full); } })('src/pages/plugin_platform'); const used=new Set(); for(const file of files){ const text=fs.readFileSync(file,'utf8'); const re=/[\"'\`](pluginPlatform\.[A-Za-z0-9_.]+)[\"'\`]/g; let m; while((m=re.exec(text))) used.add(m[1]); } const missing=[]; used.forEach(k=>{ if(!(k in en) || !(k in zh)) missing.push(k); }); console.log(missing.sort().join('\n') || 'all keys present')"
```

Expected: `all keys present`.

- [ ] **Step 6: Legacy-reference sanity check**

```bash
cd client && grep -rn "pluginSystem\.\|plugin_management\|plugin_store\|plugin-platform/plugin-management\|plugin-platform/plugin-store" src || true
```

Expected: no output.

- [ ] **Step 7: Confirm the change set excludes unrelated work**

Run: `git status --short`

Expected: the only `client/` paths are those created/modified by Tasks 1–13 plus the pre-existing dashboard WIP that the human committed or stashed before Task 1. Nothing from `client/src/pages/wms/dashboard/`, `client/src/scss/dashboard.scss`, `server/` or the untracked dashboard plans may be staged in Step 8. If the dashboard WIP still appears, do not commit it.

- [ ] **Step 8: Commit**

```bash
git add client/src/pages/plugin_platform/audit/index.tsx \
  client/src/routes/path2Compoment.tsx
git commit -m "feat(script-plugin): add the global plugin audit page

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

## Implementation Complete

All thirteen tasks are done when:

- The plugin list, workbench, wizard and global audit routes render under `/plugin-platform/*`.
- `npx tsc --noEmit --skipLibCheck` reports only the four baseline errors.
- `npm run build` exits 0.
- `pluginPlatform.*` locale keys are present in both en and zh, and every key used in code exists.
- No legacy plugin-platform page, route or locale key remains.
- The dashboard WIP is untouched.

Not verified by this plan (do not claim otherwise): browser rendering, AMIS runtime behaviour, real SSE generation, and any server-side behaviour. Those require the manual acceptance steps recorded with Phase 3A.
