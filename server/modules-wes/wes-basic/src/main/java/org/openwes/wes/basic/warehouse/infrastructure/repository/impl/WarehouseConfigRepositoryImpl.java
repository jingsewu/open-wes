package org.openwes.wes.basic.warehouse.infrastructure.repository.impl;

import org.openwes.wes.basic.warehouse.domain.entity.WarehouseConfig;
import org.openwes.wes.basic.warehouse.domain.repository.WarehouseConfigRepository;
import org.openwes.wes.basic.warehouse.infrastructure.persistence.mapper.WarehouseConfigPORepository;
import org.openwes.wes.basic.warehouse.infrastructure.persistence.transfer.WarehouseConfigPOTransfer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WarehouseConfigRepositoryImpl implements WarehouseConfigRepository {

    private final WarehouseConfigPORepository warehouseConfigPORepository;
    private final WarehouseConfigPOTransfer warehouseConfigPOTransfer;

    @Override
    public void save(WarehouseConfig warehouseConfig) {
        warehouseConfigPORepository.save(warehouseConfigPOTransfer.toPO(warehouseConfig));
    }

    @Override
    public WarehouseConfig findByWarehouseCode(String warehouseCode) {
        return warehouseConfigPOTransfer.toDO(warehouseConfigPORepository.findByWarehouseCode(warehouseCode));
    }
}
