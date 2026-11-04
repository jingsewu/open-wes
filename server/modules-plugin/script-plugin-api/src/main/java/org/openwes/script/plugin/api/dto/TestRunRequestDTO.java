package org.openwes.script.plugin.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.openwes.script.plugin.api.enums.ExtensionType;

import java.util.Map;

@Data
public class TestRunRequestDTO {
    @NotNull
    private ExtensionType extensionType;
    @NotBlank
    @Size(max = 128)
    private String script;
    @NotBlank
    @Size(max = 128)
    private String method;
    private Map<String, Object> arguments;
}
