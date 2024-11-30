package org.openwes.api.platform.controller;

import org.openwes.api.platform.controller.param.api.ApiAddParam;
import org.openwes.api.platform.controller.param.api.ApiUpdateParam;
import org.openwes.api.platform.domain.service.ApiService;
import org.openwes.common.utils.http.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api-management")
@Tag(name = "Api Platform Module Api")
public class ApiManagementController {

    private final ApiService apiService;

    @PostMapping("/add")
    @Operation(summary = "添加接口")
    public Object add(@RequestBody @Valid ApiAddParam param) {
        apiService.addApi(param);
        return Response.success();
    }

    @PostMapping("/update")
    @Operation(summary = "修改接口")
    public Object update(@RequestBody @Valid ApiUpdateParam param) {
        apiService.updateApi(param);
        return Response.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "根据id删除接口")
    public Object deleteApiById(@Parameter(description = "接口 ID") @PathVariable("id") Long id) {
        apiService.deleteApiById(id);
        return Response.success();
    }
}
