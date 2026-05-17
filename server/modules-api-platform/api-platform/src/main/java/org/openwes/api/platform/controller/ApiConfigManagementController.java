package org.openwes.api.platform.controller;

import org.openwes.api.platform.controller.param.apiconfig.ApiConfigTestConverterParam;
import org.openwes.api.platform.controller.param.apiconfig.ApiConfigUpdateParam;
import org.openwes.api.platform.controller.param.apiconfig.ApiConfigVO;
import org.openwes.api.platform.domain.entity.ApiConfigPO;
import org.openwes.api.platform.domain.service.ApiConfigService;
import org.openwes.api.platform.utils.ConverterHelper;
import org.openwes.common.utils.http.Response;
import org.openwes.common.utils.utils.JsonUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api-config-management")
@Tag(name = "Api Platform Module Api")
public class ApiConfigManagementController {

    private final ApiConfigService apiConfigService;

    @GetMapping("/{code}")
    @Operation(summary = "根据接口编码获取接口配置")
    public ApiConfigVO getConfigByCode(@Parameter(description = "接口编码") @PathVariable("code") String code) {
        ApiConfigPO apiConfigPO = apiConfigService.getByCode(code);
        ApiConfigVO apiConfigVO = new ApiConfigVO();
        if (apiConfigPO != null) {
            BeanUtils.copyProperties(apiConfigPO, apiConfigVO);
        } else {
            apiConfigVO.setCode(code);
        }
        return apiConfigVO;
    }

    @PostMapping("/update")
    @Operation(summary = "修改接口配置")
    public Response update(@RequestBody @Valid ApiConfigUpdateParam param) {
        apiConfigService.updateConfig(param);
        return Response.success();
    }

    @PostMapping("/test-converter")
    @Operation(summary = "测试参数转换脚本")
    public Response<String> testConverter(@RequestBody @Valid ApiConfigTestConverterParam param) {
        try {
            Object input = JsonUtils.string2MapObject(param.getInputJson());
            ApiConfigPO apiConfigPO = new ApiConfigPO();
            apiConfigPO.setParamConverterType(param.getConverterType());
            apiConfigPO.setParamConverterScript(param.getScript());
            Object result = ConverterHelper.convertParam(apiConfigPO, input);
            return Response.success(JsonUtils.obj2String(result));
        } catch (Exception e) {
            return Response.<String>builder().code("1").msg(e.getMessage()).build();
        }
    }
}
