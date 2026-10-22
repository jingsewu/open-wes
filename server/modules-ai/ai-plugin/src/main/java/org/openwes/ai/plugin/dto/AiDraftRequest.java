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
