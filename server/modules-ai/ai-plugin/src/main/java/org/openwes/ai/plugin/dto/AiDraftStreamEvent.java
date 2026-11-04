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
