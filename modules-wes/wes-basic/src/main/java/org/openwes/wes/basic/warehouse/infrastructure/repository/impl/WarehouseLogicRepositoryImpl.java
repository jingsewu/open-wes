package org.openwes.wes.basic.warehouse.infrastructure.repository.impl;

import org.openwes.wes.basic.warehouse.domain.entity.WarehouseLogic;
import org.openwes.wes.basic.warehouse.domain.repository.WarehouseLogicRepository;
import org.openwes.wes.basic.warehouse.infrastructure.persistence.mapper.WarehouseLogicPORepository;
import org.openwes.wes.basic.warehouse.infrastructure.persistence.transfer.WarehouseLogicPOTransfer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseLogicRepositoryImpl implements WarehouseLogicRepository {

    private final WarehouseLogicPORepository warehouseLogicPORepository;
    private final WarehouseLogicPOTransfer warehouseLogicPOTransfer;

    @Override
    public void save(WarehouseLogic warehouseLogic) {
        warehouseLogicPORepository.save(warehouseLogicPOTransfer.toPO(warehouseLogic));
    }

    @Override
    public WarehouseLogic findById(Long id) {
        return warehouseLogicPOTransfer.toDO(warehouseLogicPORepository.findById(id).orElseThrow());
    }

    @Override
    public WarehouseLogic findByCode(String warehouseLogicCode) {
        return warehouseLogicPOTransfer.toDO(warehouseLogicPORepository.findByWarehouseLogicCode(warehouseLogicCode));
    }

    @Override
    public List<WarehouseLogic> findAllByWarehouseAreaIds(List<Long> warehouseAreaIds) {
        return warehouseLogicPOTransfer.toDOs(warehouseLogicPORepository.findByWarehouseAreaIdIn(warehouseAreaIds));
    }

    @Override
    public void saveAll(List<WarehouseLogic> warehouseLogics) {
        warehouseLogicPORepository.saveAll(warehouseLogicPOTransfer.toPOs(warehouseLogics));
    }
}
