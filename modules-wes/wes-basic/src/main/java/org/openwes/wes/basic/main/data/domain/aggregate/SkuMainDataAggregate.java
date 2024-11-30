package org.openwes.wes.basic.main.data.domain.aggregate;

import org.openwes.wes.basic.main.data.domain.entity.SkuBarcodeData;
import org.openwes.wes.basic.main.data.domain.entity.SkuMainData;
import org.openwes.wes.basic.main.data.domain.repository.SkuMainDataRepository;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkuMainDataAggregate {

    private final SkuMainDataRepository skuMainDataRepository;

    @Transactional(rollbackFor = Exception.class)
    public void create(List<SkuMainData> changedSkuMainDataList, List<SkuBarcodeData> skuBarcodeData) {

        //handle barcode
        //1. clear useless barcodes
        Map<Long, SkuMainData> changedSkuMainDataMap = changedSkuMainDataList.stream()
                .filter(v -> v.getId() != null && v.getId() > 0).collect(Collectors.toMap(SkuMainData::getId, v -> v));
        List<SkuBarcodeData> deletedSkusBarcodeData = skuBarcodeData.stream().filter(v ->
                !changedSkuMainDataMap.get(v.getSkuId()).containsBarcode(v.getBarCode())).toList();

        if (CollectionUtils.isNotEmpty(deletedSkusBarcodeData)) {
            skuMainDataRepository.deleteAllSkuBarcodeData(deletedSkusBarcodeData);
        }

        //2. add new barcodes
        changedSkuMainDataList.forEach(skuMainData -> {
            List<SkuBarcodeData> barcodeData = skuBarcodeData.stream()
                    .filter(v -> v.getSkuId().equals(skuMainData.getId())).toList();
            skuMainData.ignoreBarcodeData(barcodeData);
        });
        skuMainDataRepository.saveAll(changedSkuMainDataList);

    }
}
