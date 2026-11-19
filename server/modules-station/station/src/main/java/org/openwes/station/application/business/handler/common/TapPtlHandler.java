package org.openwes.station.application.business.handler.common;

import lombok.extern.slf4j.Slf4j;
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

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class TapPtlHandler implements IBusinessHandler<TapPtlEvent> {

    private final WorkStationService workStationService;
    private final TapPutWallSlotHandler tapPutWallSlotHandler;

    @Override
    public void execute(TapPtlEvent tapPtlEvent, Long workStationId) {
        WorkStationCache workStationCache = workStationService.getOrThrow(workStationId);

        if (workStationCache.getPutWallArea() == null || workStationCache.getPutWallArea().getPutWallViews() == null) {
            log.error("work station: {} has no put wall area", workStationId);
            return;
        }

        PutWallSlotDTO putWallSlot = workStationCache.getPutWallArea().getPutWallViews().stream()
            .flatMap(pw -> (pw.getPutWallSlots() != null ? pw.getPutWallSlots() : Collections.<PutWallSlotDTO>emptyList()).stream())
            .filter(v -> StringUtils.equals(v.getPtlTag(), tapPtlEvent.getPtlTag()))
            .findFirst().orElse(null);

        // not put wall tap, return now
        if (putWallSlot == null) {
            log.error("work station: {} don't contains ptl tag: {}", workStationId, tapPtlEvent.getPtlTag());
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
