package org.openwes.plugin.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.openwes.plugin.api.constants.PluginStatusEnum;

@Data
public class PluginDTO {

    private Long id;

    @NotEmpty
    @Size(max = 128)
    private String pluginUniqueKey;

    @NotEmpty
    @Size(max = 64)
    private String code;

    @NotEmpty
    @Size(max = 128)
    private String name;

    @NotEmpty
    @Size(max = 64)
    private String developer;

    @NotEmpty
    @Size(max = 64)
    private String pluginVersion;

    @Size(max = 32)
    private String applySystem = "";

    @Size(max = 32)
    private String applyModule = "";

    @Size(max = 128)
    private String dependencies = "";

    @Size(max = 256)
    private String description = "";

    @NotEmpty
    private String scanPackages;

    @Size(max = 256)
    private String jarFilePath;

    private String sourceCodeUrl;

    private String license;

    private PluginStatusEnum pluginStatus;

    private Long version;

}
