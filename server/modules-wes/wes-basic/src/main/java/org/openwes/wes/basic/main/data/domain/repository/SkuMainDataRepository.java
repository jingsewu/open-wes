package org.openwes.wes.basic.main.data.domain.repository;

import org.openwes.wes.basic.main.data.domain.entity.SkuBarcodeData;
import org.openwes.wes.basic.main.data.domain.entity.SkuMainData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Collection;
import java.util.List;

public interface SkuMainDataRepository {

    void save(SkuMainData skuMainData);

    void saveAll(List<SkuMainData> skuMainDataList);

    void deleteAllSkuBarcodeData(List<SkuBarcodeData> deletedSkusBarcodeData);

    SkuMainData findById(Long id);

    SkuMainData findSkuBySkuCodeAndOwnerCode(String skuCode, String ownerCode);

    SkuMainData findAnySkuMainDataBySkuCode(String skuCode);

    List<SkuMainData> findAllSkuBySkuCodesAndOwnerCode(Collection<String> skuCodes, String ownerCode);

    List<SkuMainData> findAllBySkuCodes(Collection<String> skuCodes);

    List<SkuMainData> findAllByIds(Collection<Long> skuMainDataIds);

    List<SkuMainData> findAllBySkuCode(String skuCode);

    Page<SkuMainData> findAllByHeatIn(Collection<String> heat, PageRequest pageable);

    List<SkuBarcodeData> findBarcodeBySkuIds(List<Long> updatedSkuMainDataIds);

    List<SkuBarcodeData> findAllSkuBarcodeByBarcodeOrSkuCode(String barcode, String skuCode);

    List<SkuBarcodeData> findAllSkuBarcodeByBarcode(String barcode);
}
