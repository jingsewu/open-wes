package org.openwes.station.application.business.handler.common;

import org.openwes.station.api.constants.ApiCodeEnum;
import org.openwes.station.api.dto.TapPtlEvent;
import org.openwes.station.application.business.handler.IBusinessHandler;
import org.openwes.station.application.business.handler.event.outbound.TapPutWallSlotEvent;
import org.openwes.station.application.business.handler.outbound.TapPutWallSlotHandler;
import org.openwes.station.domain.entity.WorkStationCache;
import org.openwes.station.domain.service.WorkStationService;
import org.openwes.wes.api.basic.dto.PutWallSlotDTO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TapPtlHandler<T extends WorkStationCache> implements IBusinessHandler<TapPtlEvent> {

    private final WorkStationService<T> workStationService;
    private final TapPutWallSlotHandler tapPutWallSlotHandler;

    @Override
    public void execute(TapPtlEvent tapPtlEvent, Long workStationId) {
        WorkStationCache workStationCache = workStationService.getOrThrow(workStationId);

        PutWallSlotDTO putWallSlot = workStationCache.getPutWallSlots().stream()
            .filter(v -> StringUtils.equals(v.getPtlTag(), tapPtlEvent.getPtlTag()))
            .findFirst().orElse(null);

        // not put wall tap, return now
        if (putWallSlot == null) {
            return;
        }

        tapPutWallSlotHandler.execute(new TapPutWallSlotEvent().setPutWallSlotCode(putWallSlot.getPutWallSlotCode()), workStationId);
    }

    @Override
    public ApiCodeEnum getApiCode() {
        return ApiCodeEnum.TAP_PTL;
    }

    @Override
    public Class<TapPtlEvent> getParameterClass() {
        return TapPtlEvent.class;
    }
}
