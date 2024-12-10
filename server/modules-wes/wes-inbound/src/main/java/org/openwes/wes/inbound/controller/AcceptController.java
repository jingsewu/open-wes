package org.openwes.wes.inbound.controller;

import org.openwes.wes.api.inbound.IAcceptOrderApi;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("inbound/accept")
@RequiredArgsConstructor
@Schema(description = "验收相关接口")
@Tag(name = "Wms Module Api")
public class AcceptController {

    private final IAcceptOrderApi acceptOrderApi;

    @PostMapping("cancel")
    public void cancel(@RequestParam("acceptOrderId") Long acceptOrderId,
                       @RequestParam(required = false, value = "acceptOrderDetailId") Long acceptOrderDetailId) {
        acceptOrderApi.cancel(acceptOrderId, acceptOrderDetailId);
    }

    @PostMapping("completeById")
    public void completeById(@RequestParam("acceptOrderId") Long acceptOrderId) {
        acceptOrderApi.complete(acceptOrderId);
    }

    @PostMapping("completeByContainer")
    public void completeByContainer(@RequestParam("containerCode") String containerCode) {
        acceptOrderApi.complete(containerCode);
    }

}
