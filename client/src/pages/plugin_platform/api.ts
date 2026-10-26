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
