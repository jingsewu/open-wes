package org.openwes.api.platform.controller;

import org.openwes.api.platform.api.IRequestApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RequestMapping("api")
@RestController
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Api Platform Module Api")
public class ApiController {

    private final IRequestApi requestApi;

    @PostMapping(value = "execute", produces = "application/json")
    @Operation(summary = "发起客户请求")
    public Object executeRequest(
            @Parameter(description = "接口类型") @RequestParam String apiType,
            @Parameter(description = "请求内容") @RequestBody String body) {
        return requestApi.request(apiType, body);
    }
}
