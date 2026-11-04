package org.openwes.script.plugin.core.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.openwes.common.utils.http.Response;
import org.openwes.script.plugin.api.dto.ScriptPluginConfigDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginConfigSaveDTO;
import org.openwes.script.plugin.core.domain.service.ScriptPluginConfigService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("plugin-platform/scriptPlugin")
@Tag(name = "Script Plugin Config Api")
public class ScriptPluginConfigController {

    private final ScriptPluginConfigService configService;

    @GetMapping("/{pluginKey}/config")
    public Response<ScriptPluginConfigDTO> getConfig(@PathVariable("pluginKey") String pluginKey) {
        return Response.success(configService.getConfig(pluginKey));
    }

    @PostMapping("/{pluginKey}/config")
    public Response<ScriptPluginConfigDTO> saveConfig(
            @PathVariable("pluginKey") String pluginKey,
            @Valid @RequestBody ScriptPluginConfigSaveDTO dto) {
        return Response.success(configService.saveConfig(pluginKey, dto));
    }
}
