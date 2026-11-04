package org.openwes.script.plugin.api.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ScriptPluginDisableDTO {
    @Size(max = 255)
    private String reason;
}
