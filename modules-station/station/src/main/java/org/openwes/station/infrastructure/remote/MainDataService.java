package org.openwes.station.infrastructure.remote;

import org.openwes.wes.api.main.data.ISkuMainDataApi;
import org.openwes.wes.api.main.data.dto.SkuBarcodeDataDTO;
import org.openwes.wes.api.main.data.dto.SkuMainDataDTO;
import org.openwes.wes.api.stock.ISkuBatchAttributeApi;
import org.openwes.wes.api.stock.dto.SkuBatchAttributeDTO;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Component
public class MainDataService {

    @DubboReference
    private ISkuMainDataApi skuMainDataApi;
    @DubboReference
    private ISkuBatchAttributeApi skuBatchAttributeApi;

    public SkuMainDataDTO querySkuMainData(String skuCode, String ownerCode) {
        return skuMainDataApi.getSkuMainData(skuCode, ownerCode);
    }

    public List<SkuBarcodeDataDTO> querySkuCodeByBarcodeOrSkuCode(String barCode, String skuCode) {
        return skuMainDataApi.querySkuBarcodeData(barCode, skuCode);
    }

    public List<SkuBarcodeDataDTO> querySkuByBarcode(String barCode) {
        return skuMainDataApi.querySkuByBarcode(barCode);
    }

    public List<SkuMainDataDTO> querySkuMainData(Collection<String> skuCodes, String ownerCode) {
        return skuMainDataApi.getSkuMainData(skuCodes, ownerCode);
    }

    public SkuBatchAttributeDTO queryOrCreateSkuBatchAttribute(Long skuId, Map<String, Object> batchAttributes) {
        return skuBatchAttributeApi.getOrCreateSkuBatchAttribute(skuId, batchAttributes);
    }

    public List<SkuBatchAttributeDTO> getSkuBatchAttributeBySkuIds(Collection<Long> skuIds) {
        return skuBatchAttributeApi.getBySkuIds(skuIds);
    }

    public List<SkuMainDataDTO> querySkuMainData(Collection<Long> skuIds) {
        return skuMainDataApi.getByIds(skuIds);
    }

    public SkuMainDataDTO querySkuMainData(Long skuId) {
        return skuMainDataApi.getById(skuId);
    }

    public SkuMainDataDTO queryAnySkuMainDataBySkuCode(String skuCode) {
        return skuMainDataApi.getAnySkuMainDataBySkuCode(skuCode);
    }

    public List<SkuMainDataDTO> findSkuMainDataBySkuCode(String skuCode) {
        return skuMainDataApi.findSkuMainDataBySkuCode(skuCode);
    }

}
