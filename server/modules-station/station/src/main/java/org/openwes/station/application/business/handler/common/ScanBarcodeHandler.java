package org.openwes.station.application.business.handler.common;

import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.OperationTaskErrorDescEnum;
import org.openwes.station.api.constants.ApiCodeEnum;
import org.openwes.station.application.business.handler.IBusinessHandler;
import org.openwes.station.application.business.handler.common.extension.ExtensionFactory;
import org.openwes.station.application.business.handler.common.extension.IExtension;
import org.openwes.station.domain.entity.WorkStationCache;
import org.openwes.station.domain.service.WorkStationService;
import org.openwes.station.infrastructure.remote.BarcodeService;
import org.openwes.wes.api.config.constants.BusinessFlowEnum;
import org.openwes.wes.api.config.constants.ExecuteTimeEnum;
import org.openwes.wes.api.config.constants.ParserObjectEnum;
import org.openwes.wes.api.config.dto.BarcodeParseRequestDTO;
import org.openwes.wes.api.config.dto.BarcodeParseResult;
import org.openwes.wes.api.main.data.dto.BarcodeDTO;
import org.openwes.wes.api.main.data.dto.SkuMainDataDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.openwes.station.api.constants.ApiCodeEnum.SCAN_BARCODE;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScanBarcodeHandler<T extends WorkStationCache> implements IBusinessHandler<String> {

    private final WorkStationService<T> workStationService;
    private final ExtensionFactory extensionFactory;

    @Override
    public void execute(String barcode, Long workStationId) {

        if (StringUtils.isEmpty(barcode)) {
            log.error("work station: {} input is empty", workStationId);
            return;
        }

        T workStationCache = workStationService.getOrThrow(workStationId);
        workStationCache.scanBarcode(barcode);

        Extension<T> tExtension = extensionFactory.getExtension(workStationCache.getWorkStationMode(), getApiCode());
        if (tExtension != null) {
            tExtension.doScanBarcode(workStationCache);
        }
    }

    @Override
    public ApiCodeEnum getApiCode() {
        return SCAN_BARCODE;
    }

    @Override
    public Class<String> getParameterClass() {
        return String.class;
    }

    public interface Extension<T extends WorkStationCache> extends IExtension {

        void doScanBarcode(T workStationCache);

        default ApiCodeEnum getApiCode() {
            return ApiCodeEnum.SCAN_BARCODE;
        }

        default String parseSkuCode(SkuMainDataDTO skuMainData, String barcode, BusinessFlowEnum businessFlowEnum,
                                    BarcodeService barcodeService) {
            BarcodeParseRequestDTO requestDTO = new BarcodeParseRequestDTO();
            requestDTO.setBarcode(barcode);
            requestDTO.setBusinessFlow(businessFlowEnum);
            requestDTO.setExecuteTime(ExecuteTimeEnum.SCAN_SKU);
            List<BarcodeParseResult> results = barcodeService.parse(requestDTO);

            if (results == null) {
                return barcode;
            }

            for (BarcodeParseResult result : results) {
                if (ParserObjectEnum.SKU_CODE.getValue().equals(result.getFieldName())) {
                    String skuCode = String.valueOf(result.getFieldValue());
                    if (skuMainData.getSkuCode().equals(skuCode)) {
                        return skuCode;
                    }
                } else if (ParserObjectEnum.BAR_CODE.getValue().equals(result.getFieldName())) {
                    BarcodeDTO skuBarcode = skuMainData.getSkuBarcode();

                    if (skuBarcode != null
                            && CollectionUtils.isNotEmpty(skuBarcode.getBarcodes())
                            && skuBarcode.getBarcodes().contains(barcode)) {

                        return skuMainData.getSkuCode();
                    }
                }
            }

            throw WmsException.throwWmsException(OperationTaskErrorDescEnum.INCORRECT_BARCODE);
        }
    }
}
