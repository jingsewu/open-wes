package org.openwes.station.application.business.handler.common.extension.outbound;

import lombok.RequiredArgsConstructor;
import org.openwes.common.utils.exception.WmsException;
import org.openwes.station.application.business.handler.common.ScanBarcodeHandler;
import org.openwes.station.application.business.handler.outbound.helper.OutboundPtlHelper;
import org.openwes.station.domain.entity.OutboundWorkStationCache;
import org.openwes.station.domain.entity.WorkStationCache;
import org.openwes.station.domain.repository.WorkStationCacheRepository;
import org.openwes.station.infrastructure.remote.BarcodeService;
import org.openwes.station.infrastructure.remote.RemoteWorkStationService;
import org.openwes.wes.api.basic.constants.PutWallSlotStatusEnum;
import org.openwes.wes.api.basic.constants.WorkStationModeEnum;
import org.openwes.wes.api.basic.dto.PutWallSlotDTO;
import org.openwes.wes.api.config.constants.BusinessFlowEnum;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.openwes.common.utils.exception.code_enum.StationErrorDescEnum.PUT_WALL_SLOT_NOT_BOUND;
import static org.openwes.station.api.constants.ApiCodeEnum.SCAN_BARCODE;

@Service
@RequiredArgsConstructor
public class OutboundScanBarcodeHandlerExtension implements ScanBarcodeHandler.Extension {

    private final BarcodeService barcodeService;
    private final WorkStationCacheRepository workStationCacheRepository;
    private final RemoteWorkStationService remoteWorkStationService;
    private final OutboundPtlHelper outboundPtlHelper;

    @Override
    public void doScanBarcode(WorkStationCache workStationCache) {
        if (!(workStationCache instanceof OutboundWorkStationCache outboundCache)) {
            return;
        }

        String skuCode = parseSkuCode(outboundCache.getSkuArea().getScanCode(), BusinessFlowEnum.OUTBOUND, barcodeService);

        List<String> putWallSlotCodes = outboundCache.getProcessingOperationTasks()
                .stream().map(v -> v.getTargetLocationCode()).toList();
        List<PutWallSlotDTO> putWallSlotDTOS = remoteWorkStationService.queryPutWallSlots(outboundCache.getId(), putWallSlotCodes);
        if (putWallSlotDTOS.stream().anyMatch(v -> v.getPutWallSlotStatus() != PutWallSlotStatusEnum.BOUND)) {
            throw WmsException.throwWmsException(PUT_WALL_SLOT_NOT_BOUND);
        }

        outboundCache.processTasks(skuCode);
        workStationCacheRepository.save(outboundCache);

        outboundPtlHelper.send(SCAN_BARCODE, outboundCache);
    }

    @Override
    public WorkStationModeEnum getWorkStationMode() {
        return WorkStationModeEnum.PICKING;
    }
}
