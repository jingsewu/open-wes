package org.openwes.wes.basic.main.data.domain.service.impl;

import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.BasicErrorDescEnum;
import org.openwes.wes.basic.main.data.domain.entity.OwnerMainData;
import org.openwes.wes.basic.main.data.domain.entity.SkuMainData;
import org.openwes.wes.basic.main.data.domain.entity.WarehouseMainData;
import org.openwes.wes.basic.main.data.domain.repository.OwnerMainDataRepository;
import org.openwes.wes.basic.main.data.domain.repository.WarehouseMainDataRepository;
import org.openwes.wes.basic.main.data.domain.service.SkuMainDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkuMainDataServiceImpl implements SkuMainDataService {

    private final WarehouseMainDataRepository warehouseMainDataRepository;
    private final OwnerMainDataRepository ownerMainDataRepository;

    @Override
    public void validate(List<SkuMainData> skuMainDataList) {

        Set<String> warehouseCodes = skuMainDataList.stream().map(SkuMainData::getWarehouseCode).
                collect(Collectors.toSet());
        Collection<WarehouseMainData> warehouseMainData = warehouseMainDataRepository.findAllByWarehouseCodes(warehouseCodes);

        if (warehouseCodes.size() != warehouseMainData.size()) {
            throw WmsException.throwWmsException(BasicErrorDescEnum.WAREHOUSE_CODE_NOT_EXIST, warehouseCodes);
        }

        skuMainDataList.stream().collect(Collectors.groupingBy(SkuMainData::getWarehouseCode)).forEach((warehouseCode, subSkuMainDataList) -> {
            Set<String> ownerCodes = subSkuMainDataList.stream().map(SkuMainData::getOwnerCode).collect(Collectors.toSet());
            Collection<OwnerMainData> ownerMainData = ownerMainDataRepository.findAllByOwnerCodes(ownerCodes, warehouseCode);

            if (ownerCodes.size() != ownerMainData.size()) {
                throw WmsException.throwWmsException(BasicErrorDescEnum.OWNER_CODE_NOT_EXIST, ownerCodes);
            }
        });
    }
}
