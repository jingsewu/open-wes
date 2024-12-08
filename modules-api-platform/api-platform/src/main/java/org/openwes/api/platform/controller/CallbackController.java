package org.openwes.api.platform.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.common.utils.http.Response;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("callback")
@RestController
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Api Platform Module Api")
public class CallbackController {

    @PostMapping(produces = "application/json")
    @Operation(summary = "接收回调")
    public Response<Object> executeCallback(@Parameter(description = "回调内容") @RequestBody String body) {
        log.info("receive call back: {}", body);
        return Response.success();
    }
}
