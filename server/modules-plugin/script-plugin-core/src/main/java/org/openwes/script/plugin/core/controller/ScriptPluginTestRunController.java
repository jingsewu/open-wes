package org.openwes.script.plugin.core.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.openwes.common.utils.http.Response;
import org.openwes.script.plugin.api.dto.TestRunRequestDTO;
import org.openwes.script.plugin.api.dto.TestRunResultDTO;
import org.openwes.script.plugin.core.domain.service.ScriptPluginTestRunService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("plugin-platform/scriptPlugin")
@Tag(name = "Script Plugin Test-Run Api")
public class ScriptPluginTestRunController {

    private final ScriptPluginTestRunService testRunService;

    @PostMapping("/{pluginKey}/test-run")
    public Response<TestRunResultDTO> run(@PathVariable("pluginKey") String pluginKey,
                                          @Valid @RequestBody TestRunRequestDTO request) {
        return Response.success(testRunService.run(pluginKey, request));
    }
}
