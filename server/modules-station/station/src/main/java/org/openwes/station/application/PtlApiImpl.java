package org.openwes.station.application;

import org.openwes.station.api.dto.PtlMessageDTO;
import org.openwes.station.api.IPtlApi;
import org.openwes.station.domain.entity.OutboundWorkStationCache;
import org.openwes.station.domain.service.WorkStationService;
import org.openwes.station.infrastructure.remote.PtlService;
import org.openwes.wes.api.basic.constants.PtlModeEnum;
import org.openwes.wes.api.basic.dto.PutWallTagConfigDTO;
import org.openwes.wes.api.basic.dto.WorkStationConfigDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PtlApiImpl implements IPtlApi {

    private final PtlService ptlService;
    private final WorkStationService<OutboundWorkStationCache> workStationService;

    @Override
    public void reminderBind(Long workStationId, String ptlTag) {
        PutWallTagConfigDTO putWallConfig = getPutWallConfig(workStationId);
        PutWallTagConfigDTO.LightConfig config = putWallConfig.getWaitingBinding();
        PtlMessageDTO ptlMessageDTO = new PtlMessageDTO().setWorkStationId(String.valueOf(workStationId))
            .setTagCode(ptlTag).setColor(config.getColor()).setMode(config.getMode()).setUpdown(config.getUpdown());
        sendMessage(ptlMessageDTO, workStationId);
    }

    @Override
    public void reminderDispatch(Long workStationId, String ptlTag, int qty, String displayText) {
        PutWallTagConfigDTO putWallConfig = getPutWallConfig(workStationId);
        PutWallTagConfigDTO.LightConfig config = putWallConfig.getDispatch();
        PtlMessageDTO ptlMessageDTO = new PtlMessageDTO().setWorkStationId(String.valueOf(workStationId))
            .setTagCode(ptlTag).setColor(config.getColor()).setMode(config.getMode()).setUpdown(config.getUpdown())
            .setNumber(qty).setDisplayText(displayText);
        sendMessage(ptlMessageDTO, workStationId);
    }

    @Override
    public void reminderSeal(Long workStationId, String ptlTag) {
        PutWallTagConfigDTO putWallConfig = getPutWallConfig(workStationId);
        PutWallTagConfigDTO.LightConfig config = putWallConfig.getWaitingSeal();
        PtlMessageDTO ptlMessageDTO = new PtlMessageDTO().setWorkStationId(String.valueOf(workStationId))
            .setTagCode(ptlTag).setColor(config.getColor()).setMode(config.getMode()).setUpdown(config.getUpdown());
        sendMessage(ptlMessageDTO, workStationId);
    }

    @Override
    public void off(Long workStationId, String ptlTag) {
        PtlMessageDTO ptlMessageDTO = new PtlMessageDTO().setWorkStationId(String.valueOf(workStationId))
            .setTagCode(ptlTag).setMode(PtlModeEnum.OFF);
        sendMessage(ptlMessageDTO, workStationId);
    }


    private void sendMessage(PtlMessageDTO ptlMessageDTO, Long workStationId) {
        ptlService.sendMessage(ptlMessageDTO, workStationId);
    }

    private PutWallTagConfigDTO getPutWallConfig(Long workStationId) {
        OutboundWorkStationCache workStationCache = workStationService.getOrThrow(workStationId);
        WorkStationConfigDTO workStationConfig = workStationCache.getWorkStationConfig();
        WorkStationConfigDTO.PickingStationConfigDTO pickingStationConfig = workStationConfig.getPickingStationConfig();
        PutWallTagConfigDTO putWallTagConfig = pickingStationConfig.getPutWallTagConfig();
        if (putWallTagConfig == null) {
            putWallTagConfig = new PutWallTagConfigDTO();
            putWallTagConfig.initialize();
        }
        return putWallTagConfig;
    }
}
