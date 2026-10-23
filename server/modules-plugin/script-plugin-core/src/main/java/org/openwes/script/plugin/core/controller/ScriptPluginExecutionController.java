package org.openwes.script.plugin.core.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.openwes.common.utils.http.Response;
import org.openwes.script.plugin.api.dto.ExecutionLogDTO;
import org.openwes.script.plugin.api.dto.ExecutionStatsDTO;
import org.openwes.script.plugin.api.dto.PageResultDTO;
import org.openwes.script.plugin.api.enums.ExtensionType;
import org.openwes.script.plugin.core.domain.service.ScriptPluginExecutionQueryService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("plugin-platform/scriptPlugin")
@Tag(name = "Script Plugin Execution Api")
public class ScriptPluginExecutionController {

    private final ScriptPluginExecutionQueryService queryService;

    @GetMapping("/{pluginKey}/executions")
    public Response<PageResultDTO<ExecutionLogDTO>> executions(
            @PathVariable("pluginKey") String pluginKey,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize,
            @RequestParam(value = "success", required = false) Boolean success,
            @RequestParam(value = "extensionType", required = false) ExtensionType extensionType) {
        return Response.success(queryService.page(pluginKey, page, pageSize, success, extensionType));
    }

    @GetMapping("/{pluginKey}/executions/stats")
    public Response<ExecutionStatsDTO> stats(
            @PathVariable("pluginKey") String pluginKey,
            @RequestParam(value = "hours", defaultValue = "24") int hours) {
        return Response.success(queryService.stats(pluginKey, hours));
    }
}
