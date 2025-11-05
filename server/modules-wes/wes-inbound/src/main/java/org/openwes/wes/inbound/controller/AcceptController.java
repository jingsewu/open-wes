package org.openwes.wes.inbound.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.openwes.common.utils.constants.RedisConstants;
import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.CommonErrorDescEnum;
import org.openwes.distribute.lock.DistributeLock;
import org.openwes.wes.api.inbound.IAcceptOrderApi;
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
    private final DistributeLock distributeLock;

    @PostMapping("cancel")
    public void cancel(@RequestParam("acceptOrderId") Long acceptOrderId,
                       @RequestParam(required = false, value = "acceptOrderDetailId") Long acceptOrderDetailId) {
        acceptOrderApi.cancel(acceptOrderId, acceptOrderDetailId);
    }

    @PostMapping("completeById")
    public void completeById(@RequestParam("acceptOrderId") Long acceptOrderId) {

        boolean lock = distributeLock.acquireLock(RedisConstants.ACCEPT_ORDER_COMPLETE_LOCK + acceptOrderId, 0);
        if (!lock) {
            throw WmsException.throwWmsException(CommonErrorDescEnum.REPEAT_REQUEST);
        }
        try {
            acceptOrderApi.complete(acceptOrderId);
        } finally {
            distributeLock.releaseLock(RedisConstants.ACCEPT_ORDER_COMPLETE_LOCK + acceptOrderId);
        }
    }

    @PostMapping("completeByContainer")
    public void completeByContainer(@RequestParam("containerCode") String containerCode) {
        boolean lock = distributeLock.acquireLock(RedisConstants.ACCEPT_ORDER_COMPLETE_LOCK + containerCode, 0);
        if (!lock) {
            throw WmsException.throwWmsException(CommonErrorDescEnum.REPEAT_REQUEST);
        }
        try {
            acceptOrderApi.complete(containerCode);
        } finally {
            distributeLock.releaseLock(RedisConstants.ACCEPT_ORDER_COMPLETE_LOCK + containerCode);
        }
    }

}
