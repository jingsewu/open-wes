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
