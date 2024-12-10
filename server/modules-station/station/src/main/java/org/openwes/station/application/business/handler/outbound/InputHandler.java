package org.openwes.station.application.business.handler.outbound;

import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.OutboundErrorDescEnum;
import org.openwes.common.utils.exception.code_enum.StationErrorDescEnum;
import org.openwes.station.api.constants.ApiCodeEnum;
import org.openwes.station.application.business.handler.IBusinessHandler;
import org.openwes.station.application.business.handler.outbound.helper.OutboundPtlHelper;
import org.openwes.station.domain.entity.OutboundWorkStationCache;
import org.openwes.station.domain.repository.WorkStationCacheRepository;
import org.openwes.station.domain.service.WorkStationService;
import org.openwes.station.infrastructure.remote.RemoteWorkStationService;
import org.openwes.station.infrastructure.remote.TaskService;
import org.openwes.wes.api.basic.constants.PutWallSlotStatusEnum;
import org.openwes.wes.api.basic.constants.WorkStationModeEnum;
import org.openwes.wes.api.basic.dto.PutWallSlotDTO;
import org.openwes.wes.api.task.dto.BindContainerDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import static org.openwes.station.api.constants.ApiCodeEnum.INPUT;

@Service
@Slf4j
@RequiredArgsConstructor
public class InputHandler implements IBusinessHandler<String> {

    private final WorkStationService<OutboundWorkStationCache> workStationService;
    private final WorkStationCacheRepository<OutboundWorkStationCache> workStationRepository;
    private final TaskService taskService;
    private final OutboundPtlHelper outboundPtlHelper;
    private final RemoteWorkStationService remoteWorkStationService;

    @Override
    public void execute(String input, Long workStationId) {
        if (StringUtils.isEmpty(input)) {
            log.error("work station: {} input is empty", workStationId);
            return;
        }

        OutboundWorkStationCache workStationCache = workStationService.getOrThrow(workStationId);
        if (!WorkStationModeEnum.PICKING.equals(workStationCache.getWorkStationMode())) {
            return;
        }

        if (workStationCache.getPutWallSlots().stream().anyMatch(v -> StringUtils.equals(v.getPutWallSlotCode(), input))) {
            doInput(input, workStationId, workStationCache);
            return;
        }

        doBindContainer(input, workStationId, workStationCache);
    }

    private void doBindContainer(String input, Long workStationId, OutboundWorkStationCache workStationCache) {
        if (StringUtils.isEmpty(workStationCache.getInputPutWallSlot())) {
            log.error("work station: {} input: {} is not empty and not put wall slot," +
                    " but prevision input is empty.", workStationId, input);
            throw WmsException.throwWmsException(OutboundErrorDescEnum.OUTBOUND_INCRRECT_PUT_WALL_SLOT_CODE, input);
        }

        PutWallSlotDTO putWallSlot = remoteWorkStationService.queryPutWallSlot(workStationId, workStationCache.getInputPutWallSlot());
        taskService.bindContainer(new BindContainerDTO()
                .setContainerCode(input)
                .setPickingOrderId(putWallSlot.getPickingOrderId())
                .setWarehouseCode(workStationCache.getWarehouseCode())
                .setWorkStationId(workStationId)
                .setPutWallSlotCode(workStationCache.getInputPutWallSlot()));

        workStationCache.clearInput();
        workStationRepository.save(workStationCache);

        outboundPtlHelper.send(INPUT, workStationCache);
    }

    private void doInput(String input, Long workStationId, OutboundWorkStationCache workStationCache) {
        PutWallSlotDTO putWallSlot = remoteWorkStationService.queryPutWallSlot(workStationId, input);
        if (PutWallSlotStatusEnum.WAITING_BINDING != putWallSlot.getPutWallSlotStatus()) {
            throw WmsException.throwWmsException(StationErrorDescEnum.PUT_WALL_SLOT_STATUS_OCCUPANCY, input);
        }

        workStationCache.input(input);
        workStationRepository.save(workStationCache);
    }

    @Override
    public ApiCodeEnum getApiCode() {
        return INPUT;
    }

    @Override
    public Class<String> getParameterClass() {
        return String.class;
    }
}
