package org.openwes.station.application.business.handler.common.extension.outbound;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.OperationTaskErrorDescEnum;
import org.openwes.station.application.business.handler.common.ScanBarcodeHandler;
import org.openwes.station.application.business.handler.outbound.helper.OutboundPtlHelper;
import org.openwes.station.domain.entity.OutboundWorkStationCache;
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
public class OutboundScanBarcodeHandlerExtension implements ScanBarcodeHandler.Extension<OutboundWorkStationCache> {

    private final BarcodeService barcodeService;
    private final WorkStationCacheRepository<OutboundWorkStationCache> workStationCacheRepository;
    private final RemoteWorkStationService remoteWorkStationService;
    private final OutboundPtlHelper outboundPtlHelper;

    @Override
    public void doScanBarcode(OutboundWorkStationCache workStationCache) {

        String skuCode = parseSkuCode(workStationCache.getScannedBarcode(), BusinessFlowEnum.OUTBOUND, barcodeService);

        List<String> putWallSlotCodes = workStationCache.getProcessingOperationTasks()
                .stream().map(v -> v.getOperationTaskDTO().getTargetLocationCode()).toList();
        List<PutWallSlotDTO> putWallSlotDTOS = remoteWorkStationService.queryPutWallSlots(workStationCache.getId(), putWallSlotCodes);
        if (putWallSlotDTOS.stream().anyMatch(v -> v.getPutWallSlotStatus() != PutWallSlotStatusEnum.BOUND)) {
            throw WmsException.throwWmsException(PUT_WALL_SLOT_NOT_BOUND);
        }

        workStationCache.processTasks(skuCode);
        if (CollectionUtils.isEmpty(workStationCache.getProcessingOperationTasks())) {
            throw WmsException.throwWmsException(OperationTaskErrorDescEnum.INCORRECT_BARCODE);
        }
        workStationCacheRepository.save(workStationCache);

        outboundPtlHelper.send(SCAN_BARCODE, workStationCache);
    }

    @Override
    public WorkStationModeEnum getWorkStationMode() {
        return WorkStationModeEnum.PICKING;
    }
}
