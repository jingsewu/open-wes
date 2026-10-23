package org.openwes.script.plugin.core.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.openwes.common.utils.http.Response;
import org.openwes.script.plugin.api.dto.PageResultDTO;
import org.openwes.script.plugin.api.dto.PluginCapabilitiesDTO;
import org.openwes.script.plugin.api.dto.PluginContractDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginDisableDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginMetadataDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginOperationAuditDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginRevisionDTO;
import org.openwes.script.plugin.api.dto.ValidationReportDTO;
import org.openwes.script.plugin.core.domain.service.PluginWorkbenchAccessService;
import org.openwes.script.plugin.core.domain.service.ScriptPluginContractService;
import org.openwes.script.plugin.core.domain.service.ScriptPluginWorkbenchService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Management REST surface. Mapped under the reserved `plugin-platform` prefix; the gateway route
 * for /plugin-platform/** forwards without a RewritePath filter, so this prefix is stable both
 * through the gateway and on the direct service port.
 *
 * <p>All authorization lives in the service layer; these methods only bind and delegate.
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("plugin-platform/scriptPlugin")
@Tag(name = "Script Plugin Platform Api")
public class ScriptPluginController {

    private final ScriptPluginWorkbenchService workbenchService;
    private final PluginWorkbenchAccessService accessService;
    private final ScriptPluginContractService contractService;

    @GetMapping("/capabilities")
    public Response<PluginCapabilitiesDTO> capabilities() {
        return Response.success(accessService.capabilities());
    }

    @GetMapping("/contract")
    public Response<PluginContractDTO> contract(@RequestParam("applySystem") String applySystem) {
        return Response.success(contractService.contract(applySystem));
    }

    @GetMapping("/list")
    public Response<List<ScriptPluginDTO>> list() {
        return Response.success(workbenchService.list());
    }

    @GetMapping("/detail/{pluginKey}")
    public Response<ScriptPluginDTO> detail(@PathVariable("pluginKey") String pluginKey) {
        return Response.success(workbenchService.detail(pluginKey));
    }

    @PostMapping("/createDraft")
    public Response<ScriptPluginDTO> createDraft(@Valid @RequestBody ScriptPluginDTO dto) {
        return Response.success(workbenchService.createDraft(dto));
    }

    @PostMapping("/{pluginKey}/revision")
    public Response<ScriptPluginRevisionDTO> saveRevision(@PathVariable("pluginKey") String pluginKey,
                                                          @Valid @RequestBody ScriptPluginRevisionDTO dto) {
        return Response.success(workbenchService.saveRevision(pluginKey, dto));
    }

    @GetMapping("/{pluginKey}/revisions")
    public Response<List<ScriptPluginRevisionDTO>> revisions(@PathVariable("pluginKey") String pluginKey) {
        return Response.success(workbenchService.revisions(pluginKey));
    }

    @PostMapping("/{pluginKey}/validate")
    public Response<ValidationReportDTO> validate(@PathVariable("pluginKey") String pluginKey) {
        return Response.success(workbenchService.validate(pluginKey));
    }

    @PostMapping("/{pluginKey}/enable")
    public Response<ScriptPluginDTO> enable(@PathVariable("pluginKey") String pluginKey) {
        return Response.success(workbenchService.enable(pluginKey));
    }

    @PostMapping("/{pluginKey}/disable")
    public Response<ScriptPluginDTO> disable(
            @PathVariable("pluginKey") String pluginKey,
            @Valid @RequestBody(required = false) ScriptPluginDisableDTO dto) {
        return Response.success(workbenchService.disable(pluginKey,
                dto == null ? null : dto.getReason()));
    }

    @PostMapping("/{pluginKey}/archive")
    public Response<ScriptPluginDTO> archive(@PathVariable("pluginKey") String pluginKey) {
        return Response.success(workbenchService.archive(pluginKey));
    }

    @PostMapping("/{pluginKey}/rollback/{revisionId}")
    public Response<ScriptPluginDTO> rollback(@PathVariable("pluginKey") String pluginKey,
                                              @PathVariable("revisionId") long revisionId) {
        return Response.success(workbenchService.rollback(pluginKey, revisionId));
    }

    @PostMapping("/{pluginKey}/metadata")
    public Response<ScriptPluginDTO> metadata(@PathVariable("pluginKey") String pluginKey,
                                              @Valid @RequestBody ScriptPluginMetadataDTO dto) {
        return Response.success(workbenchService.updateMetadata(pluginKey, dto));
    }

    @GetMapping("/{pluginKey}/audit")
    public Response<PageResultDTO<ScriptPluginOperationAuditDTO>> audit(
            @PathVariable("pluginKey") String pluginKey,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {
        return Response.success(workbenchService.auditForPlugin(pluginKey, page, pageSize));
    }

    @GetMapping("/audit")
    public Response<PageResultDTO<ScriptPluginOperationAuditDTO>> allAudit(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {
        return Response.success(workbenchService.allAudit(page, pageSize));
    }
}
