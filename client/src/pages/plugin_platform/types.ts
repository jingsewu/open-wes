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
