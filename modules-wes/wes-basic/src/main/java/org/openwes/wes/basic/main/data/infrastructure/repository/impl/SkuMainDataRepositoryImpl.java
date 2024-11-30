package org.openwes.wes.basic.main.data.infrastructure.repository.impl;

import org.openwes.common.utils.constants.RedisConstants;
import org.openwes.wes.api.main.data.dto.BarcodeDTO;
import org.openwes.wes.basic.main.data.domain.entity.SkuBarcodeData;
import org.openwes.wes.basic.main.data.domain.entity.SkuMainData;
import org.openwes.wes.basic.main.data.domain.repository.SkuMainDataRepository;
import org.openwes.wes.basic.main.data.infrastructure.persistence.mapper.SkuBarCodeDataPORepository;
import org.openwes.wes.basic.main.data.infrastructure.persistence.mapper.SkuMainDataPORepository;
import org.openwes.wes.basic.main.data.infrastructure.persistence.po.SkuBarCodeDataPO;
import org.openwes.wes.basic.main.data.infrastructure.persistence.po.SkuMainDataPO;
import org.openwes.wes.basic.main.data.infrastructure.persistence.transfer.SkuBarcodeDataPOTransfer;
import org.openwes.wes.basic.main.data.infrastructure.persistence.transfer.SkuMainDataPOTransfer;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkuMainDataRepositoryImpl implements SkuMainDataRepository {

    private final SkuMainDataPORepository skuMainDataPORepository;
    private final SkuBarCodeDataPORepository skuBarCodeDataPORepository;
    private final SkuMainDataPOTransfer skuMainDataPOTransfer;
    private final SkuBarcodeDataPOTransfer skuBarcodeDataPOTransfer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(SkuMainData skuMainData) {
        SkuMainDataPO skuMainDataPO = skuMainDataPORepository.save(skuMainDataPOTransfer.toPO(skuMainData));
        if (skuMainData.getSkuBarcode() == null || ObjectUtils.isEmpty(skuMainData.getSkuBarcode().getBarcodes())) {
            return;
        }
        skuBarCodeDataPORepository.saveAll(skuMainData.getSkuBarcode().getBarcodes().stream()
                .map(barCode -> SkuBarCodeDataPO.builder().barCode(barCode)
                        .skuId(skuMainDataPO.getId()).skuCode(skuMainData.getSkuCode()).build()).toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAll(List<SkuMainData> skuMainDataList) {
        skuMainDataList.forEach(this::save);
    }

    @Override
    public List<SkuBarcodeData> findAllSkuBarcodeByBarcodeOrSkuCode(String barcode, String skuCode) {
        return skuBarcodeDataPOTransfer.toDOs(skuBarCodeDataPORepository.findAllByBarCodeOrSkuCode(barcode, skuCode));
    }

    @Override
    public List<SkuBarcodeData> findAllSkuBarcodeByBarcode(String barcode) {
        return skuBarcodeDataPOTransfer.toDOs(skuBarCodeDataPORepository.findAllByBarCode(barcode));
    }

    @Override
    @Cacheable(cacheNames = RedisConstants.SKU_MAIN_DATA_CACHE, key = "#ownerCode+'-' + #skuCode")
    public SkuMainData findSkuBySkuCodeAndOwnerCode(String skuCode, String ownerCode) {
        return skuMainDataPOTransfer.toDO(skuMainDataPORepository.findBySkuCodeAndOwnerCode(skuCode, ownerCode));
    }

    @Override
    public List<SkuMainData> findAllSkuBySkuCodesAndOwnerCode(Collection<String> skuCodes, String ownerCode) {
        return skuMainDataPOTransfer.toDOS(skuMainDataPORepository.findAllBySkuCodeInAndOwnerCode(skuCodes, ownerCode));
    }

    @Override
    public List<SkuMainData> findAllBySkuCodes(Collection<String> skuCodes) {
        return skuMainDataPOTransfer.toDOS(skuMainDataPORepository.findAllBySkuCodeIn(skuCodes));
    }

    @Override
    public SkuMainData findById(Long id) {
        SkuMainData skuMainData = skuMainDataPOTransfer.toDO(skuMainDataPORepository.findById(id).orElseThrow());
        List<SkuBarCodeDataPO> barCodeDataPOS = skuBarCodeDataPORepository.findAllBySkuId(id);
        if (CollectionUtils.isNotEmpty(barCodeDataPOS)) {
            List<String> barCodes = barCodeDataPOS.stream().map(SkuBarCodeDataPO::getBarCode).toList();
            skuMainData.setSkuBarcode(new BarcodeDTO(barCodes));
        }
        return skuMainData;
    }

    @Override
    public List<SkuMainData> findAllByIds(Collection<Long> skuMainDataIds) {
        List<SkuMainData> skuMainDataList = skuMainDataPOTransfer.toDOS(skuMainDataPORepository.findAllById(skuMainDataIds));

        List<SkuBarCodeDataPO> allSkuBarCodeDataPOS = skuBarCodeDataPORepository.findAllBySkuIdIn(skuMainDataIds);
        Map<Long, List<SkuBarCodeDataPO>> skuBarCodeMap = allSkuBarCodeDataPOS.stream().collect(Collectors.groupingBy(SkuBarCodeDataPO::getSkuId));
        skuMainDataList.forEach(skuMainData -> {
            List<SkuBarCodeDataPO> skuBarCodeDataPOS = skuBarCodeMap.get(skuMainData.getId());
            if (skuBarCodeDataPOS != null) {
                List<String> barCodes = skuBarCodeDataPOS.stream().map(SkuBarCodeDataPO::getBarCode).toList();
                skuMainData.setSkuBarcode(new BarcodeDTO(barCodes));
            }
        });

        return skuMainDataList;
    }

    @Override
    public SkuMainData findAnySkuMainDataBySkuCode(String skuCode) {
        return skuMainDataPOTransfer.toDO(skuMainDataPORepository.findFirstBySkuCode(skuCode));
    }

    @Override
    public Page<SkuMainData> findAllByHeatIn(Collection<String> heat, PageRequest pageable) {
        return skuMainDataPORepository.findAllByHeatIn(heat, pageable).map(skuMainDataPOTransfer::toDO);
    }

    @Override
    public List<SkuMainData> findAllBySkuCode(String skuCode) {
        return skuMainDataPOTransfer.toDOS(skuMainDataPORepository.findAllBySkuCode(skuCode));
    }

    @Override
    public void deleteAllSkuBarcodeData(List<SkuBarcodeData> skuBarcodeData) {
        skuBarCodeDataPORepository.deleteAll(skuBarcodeDataPOTransfer.toPOs(skuBarcodeData));
    }

    @Override
    public List<SkuBarcodeData> findBarcodeBySkuIds(List<Long> updatedSkuMainDataIds) {
        List<SkuBarCodeDataPO> skuBarCodeDataPOs = skuBarCodeDataPORepository.findAllBySkuIdIn(updatedSkuMainDataIds);
        return skuBarcodeDataPOTransfer.toDOs(skuBarCodeDataPOs);
    }

}
