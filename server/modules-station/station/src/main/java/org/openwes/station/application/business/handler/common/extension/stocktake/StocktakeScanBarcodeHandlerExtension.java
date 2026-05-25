package org.openwes.station.application.business.handler.common.extension.stocktake;

import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.StocktakeErrorDescEnum;
import org.openwes.station.api.model.ArrivedContainerCache;
import org.openwes.station.application.business.handler.common.ScanBarcodeHandler;
import org.openwes.station.domain.entity.WorkStationCache;
import org.openwes.station.domain.repository.WorkStationCacheRepository;
import org.openwes.station.infrastructure.remote.BarcodeService;
import org.openwes.wes.api.basic.constants.WorkStationModeEnum;
import org.openwes.wes.api.config.constants.BusinessFlowEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StocktakeScanBarcodeHandlerExtension implements ScanBarcodeHandler.Extension {

    private final BarcodeService barcodeService;
    private final WorkStationCacheRepository workStationRepository;

    @Override
    public void doScanBarcode(WorkStationCache workStationCache) {

        if (workStationCache.getSkuArea() == null || !workStationCache.getSkuArea().hasTasks()) {
            log.info("work station: {} operationTasks is empty", workStationCache.getId());
            throw WmsException.throwWmsException(StocktakeErrorDescEnum.STOCKTAKE_NO_OPERATION_TASK);
        }

        String skuCode = parseSkuCode(workStationCache.getSkuArea().getScanCode(), BusinessFlowEnum.STOCK_TAKE, barcodeService);
        if (StringUtils.isEmpty(skuCode)) {
            throw WmsException.throwWmsException(StocktakeErrorDescEnum.STOCKTAKE_BAR_CODE_PARSING_ERROR);
        }

        ArrivedContainerCache processingContainer = workStationCache.getWorkLocationArea().getProcessingContainers().stream()
                .findFirst().orElse(null);
        if (processingContainer != null) {
            workStationCache.getSkuArea().markTasksProcessing(skuCode, processingContainer.getContainerCode(), processingContainer.getFace());
        }
        workStationRepository.save(workStationCache);
    }

    @Override
    public WorkStationModeEnum getWorkStationMode() {
        return WorkStationModeEnum.STOCKTAKE;
    }
}
