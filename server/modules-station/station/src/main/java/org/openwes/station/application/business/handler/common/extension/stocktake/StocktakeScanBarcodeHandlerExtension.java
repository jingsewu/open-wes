package org.openwes.station.application.business.handler.common.extension.stocktake;

import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.StocktakeErrorDescEnum;
import org.openwes.station.application.business.handler.common.ScanBarcodeHandler;
import org.openwes.station.domain.entity.StocktakeWorkStationCache;
import org.openwes.station.domain.repository.WorkStationCacheRepository;
import org.openwes.station.infrastructure.remote.BarcodeService;
import org.openwes.wes.api.basic.constants.WorkStationModeEnum;
import org.openwes.wes.api.config.constants.BusinessFlowEnum;
import org.openwes.wes.api.task.dto.OperationTaskVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StocktakeScanBarcodeHandlerExtension implements ScanBarcodeHandler.Extension<StocktakeWorkStationCache> {

    private final BarcodeService barcodeService;
    private final WorkStationCacheRepository<StocktakeWorkStationCache> workStationRepository;

    @Override
    public void doScanBarcode(StocktakeWorkStationCache workStationCache) {

        List<OperationTaskVO> operateTasks = workStationCache.getOperateTasks();
        if (CollectionUtils.isEmpty(operateTasks)) {
            log.info("work station: {} operationTasks is empty", workStationCache.getId());
            throw WmsException.throwWmsException(StocktakeErrorDescEnum.STOCKTAKE_NO_OPERATION_TASK);
        }

        OperationTaskVO operationTaskVO = workStationCache.getFirstOperationTaskVO();
        if (operationTaskVO == null) {
            throw WmsException.throwWmsException(StocktakeErrorDescEnum.STOCKTAKE_NO_OPERATION_TASK);
        }
        String skuCode = parseSkuCode(operationTaskVO.getSkuMainDataDTO(), workStationCache.getScannedBarcode(), BusinessFlowEnum.STOCK_TAKE, barcodeService);
        if (StringUtils.isEmpty(skuCode)) {
            throw WmsException.throwWmsException(StocktakeErrorDescEnum.STOCKTAKE_BAR_CODE_PARSING_ERROR);
        }

        workStationCache.processTasks(skuCode);
        workStationRepository.save(workStationCache);
    }

    @Override
    public WorkStationModeEnum getWorkStationMode() {
        return WorkStationModeEnum.STOCKTAKE;
    }
}
