package org.openwes.wes.basic.warehouse.infrastructure.repository.impl;

import org.openwes.wes.api.basic.constants.WarehouseAreaWorkTypeEnum;
import org.openwes.wes.basic.warehouse.domain.entity.WarehouseArea;
import org.openwes.wes.basic.warehouse.domain.repository.WarehouseAreaRepository;
import org.openwes.wes.basic.warehouse.infrastructure.persistence.mapper.WarehouseAreaPORepository;
import org.openwes.wes.basic.warehouse.infrastructure.persistence.transfer.WarehouseAreaPOTransfer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseAreaRepositoryImpl implements WarehouseAreaRepository {

    private final WarehouseAreaPORepository warehouseAreaPORepository;
    private final WarehouseAreaPOTransfer warehouseAreaPOTransfer;

    @Override
    public void save(WarehouseArea warehouseArea) {
        warehouseAreaPORepository.save(warehouseAreaPOTransfer.toPO(warehouseArea));
    }

    @Override
    public WarehouseArea findById(Long warehouseAreaId) {
        return warehouseAreaPOTransfer.toDO(warehouseAreaPORepository.findById(warehouseAreaId).orElseThrow());
    }

    @Override
    public List<WarehouseArea> findAllByIds(Collection<Long> warehouseAreaIds) {
        return warehouseAreaPOTransfer.toDOs(warehouseAreaPORepository.findAllById(warehouseAreaIds));
    }

    @Override
    public WarehouseArea findByCode(String warehouseAreaCode) {
        return warehouseAreaPOTransfer.toDO(warehouseAreaPORepository.getByWarehouseAreaCode(warehouseAreaCode));
    }

    @Override
    public List<WarehouseArea> findAllByWarehouseAreaGroup(String warehouseAreaGroupCode, String warehouseCode) {
        return warehouseAreaPOTransfer
                .toDOs(warehouseAreaPORepository.findByWarehouseGroupCodeAndWarehouseCode(warehouseAreaGroupCode, warehouseCode));
    }

    @Override
    public List<WarehouseArea> findAllByWarehouseCode(String warehouseCode) {
        return warehouseAreaPOTransfer
                .toDOs(warehouseAreaPORepository.findByWarehouseCode(warehouseCode));
    }

    @Override
    public List<WarehouseArea> findAllByWarehouseCodes(Collection<String> warehouseCodes) {
        return warehouseAreaPOTransfer.toDOs(warehouseAreaPORepository.findByWarehouseCodeIn(warehouseCodes));
    }

    @Override
    public List<WarehouseArea> findAllByWarehouseAreaWorkType(WarehouseAreaWorkTypeEnum warehouseAreaWorkTypeEnum) {
        return warehouseAreaPOTransfer.toDOs(warehouseAreaPORepository.findAllByWarehouseAreaWorkType(warehouseAreaWorkTypeEnum));
    }

    @Override
    public void saveAll(List<WarehouseArea> warehouseAreas) {
        warehouseAreaPORepository.saveAll(warehouseAreaPOTransfer.toPOs(warehouseAreas));
    }
}
