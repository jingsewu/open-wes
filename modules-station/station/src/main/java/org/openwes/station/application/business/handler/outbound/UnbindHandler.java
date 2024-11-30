package org.openwes.station.application.business.handler.outbound;

import org.openwes.station.api.IPtlApi;
import org.openwes.station.api.constants.ApiCodeEnum;
import org.openwes.station.application.business.handler.IBusinessHandler;
import org.openwes.station.application.business.handler.event.outbound.UnbindEvent;
import org.openwes.station.domain.entity.OutboundWorkStationCache;
import org.openwes.station.domain.service.WorkStationService;
import org.openwes.station.infrastructure.remote.RemoteWorkStationService;
import org.openwes.station.infrastructure.remote.TaskService;
import org.openwes.wes.api.basic.dto.PutWallSlotDTO;
import org.openwes.wes.api.task.dto.UnBindContainerDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UnbindHandler implements IBusinessHandler<UnbindEvent> {

    private final WorkStationService<OutboundWorkStationCache> workStationService;
    private final TaskService taskService;
    private final RemoteWorkStationService remoteWorkStationService;
    private final IPtlApi ptlApi;

    @Override
    public void execute(UnbindEvent body, Long workStationId) {
        OutboundWorkStationCache workStationCache = workStationService.getOrThrow(workStationId);

        body.getPutWallSlotCodes().forEach(slotCode -> {

            PutWallSlotDTO putWallSlot = remoteWorkStationService.queryPutWallSlot(workStationId, slotCode);

            taskService.unbindContainer(new UnBindContainerDTO()
                    .setPickingOrderId(putWallSlot.getPickingOrderId())
                    .setContainerCode(putWallSlot.getTransferContainerCode())
                    .setWarehouseCode(workStationCache.getWarehouseCode())
                    .setWorkStationId(workStationId)
                    .setPutWallSlotCode(slotCode));

            workStationCache.getPutWallSlot(slotCode).ifPresent(v -> ptlApi.reminderBind(workStationId, v.getPtlTag()));
        });
    }

    @Override
    public ApiCodeEnum getApiCode() {
        return ApiCodeEnum.UNBIND;
    }

    @Override
    public Class<UnbindEvent> getParameterClass() {
        return UnbindEvent.class;
    }
}
