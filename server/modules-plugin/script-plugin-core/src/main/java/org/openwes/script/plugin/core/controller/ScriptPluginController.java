package org.openwes.script.plugin.core.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.openwes.common.utils.http.Response;
import org.openwes.script.plugin.api.dto.ScriptPluginDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginRevisionDTO;
import org.openwes.script.plugin.api.dto.ValidationReportDTO;
import org.openwes.script.plugin.core.domain.service.ScriptPluginService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Management REST surface. Mapped under the reserved `plugin-platform` prefix; the gateway route
 * for /plugin-platform/** forwards without a RewritePath filter, so this prefix is stable both
 * through the gateway and on the direct service port.
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("plugin-platform/scriptPlugin")
@Tag(name = "Script Plugin Platform Api")
public class ScriptPluginController {

    private final ScriptPluginService scriptPluginService;

    @GetMapping("/list")
    public Response<List<ScriptPluginDTO>> list() {
        return Response.success(scriptPluginService.list());
    }

    @GetMapping("/detail/{pluginKey}")
    public Response<ScriptPluginDTO> detail(@PathVariable("pluginKey") String pluginKey) {
        return Response.success(scriptPluginService.detail(pluginKey));
    }

    @PostMapping("/createDraft")
    public Response<ScriptPluginDTO> createDraft(@Valid @RequestBody ScriptPluginDTO dto) {
        return Response.success(scriptPluginService.createDraft(dto));
    }

    @PostMapping("/{pluginKey}/revision")
    public Response<ScriptPluginRevisionDTO> saveRevision(@PathVariable("pluginKey") String pluginKey,
                                                          @Valid @RequestBody ScriptPluginRevisionDTO dto) {
        return Response.success(scriptPluginService.saveRevision(pluginKey, dto));
    }

    @GetMapping("/{pluginKey}/revisions")
    public Response<List<ScriptPluginRevisionDTO>> revisions(@PathVariable("pluginKey") String pluginKey) {
        return Response.success(scriptPluginService.revisions(pluginKey));
    }

    @PostMapping("/{pluginKey}/validate")
    public Response<ValidationReportDTO> validate(@PathVariable("pluginKey") String pluginKey) {
        return Response.success(scriptPluginService.validate(pluginKey));
    }

    @PostMapping("/{pluginKey}/enable")
    public Response<ScriptPluginDTO> enable(@PathVariable("pluginKey") String pluginKey) {
        return Response.success(scriptPluginService.enable(pluginKey));
    }

    @PostMapping("/{pluginKey}/disable")
    public Response<ScriptPluginDTO> disable(@PathVariable("pluginKey") String pluginKey) {
        return Response.success(scriptPluginService.disable(pluginKey, "disabled by user"));
    }

    @PostMapping("/{pluginKey}/archive")
    public Response<ScriptPluginDTO> archive(@PathVariable("pluginKey") String pluginKey) {
        return Response.success(scriptPluginService.archive(pluginKey));
    }

    @PostMapping("/{pluginKey}/rollback/{revisionId}")
    public Response<ScriptPluginDTO> rollback(@PathVariable("pluginKey") String pluginKey,
                                              @PathVariable("revisionId") long revisionId) {
        return Response.success(scriptPluginService.rollback(pluginKey, revisionId));
    }
}
