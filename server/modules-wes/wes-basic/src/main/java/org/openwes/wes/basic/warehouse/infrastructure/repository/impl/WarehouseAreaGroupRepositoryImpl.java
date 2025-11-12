package org.openwes.wes.basic.warehouse.infrastructure.repository.impl;

import org.openwes.wes.basic.warehouse.domain.entity.WarehouseAreaGroup;
import org.openwes.wes.basic.warehouse.domain.repository.WarehouseAreaGroupRepository;
import org.openwes.wes.basic.warehouse.infrastructure.persistence.mapper.WarehouseAreaGroupPORepository;
import org.openwes.wes.basic.warehouse.infrastructure.persistence.transfer.WarehouseAreaGroupPOTransfer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WarehouseAreaGroupRepositoryImpl implements WarehouseAreaGroupRepository {

    private final WarehouseAreaGroupPORepository warehouseAreaGroupPORepository;
    private final WarehouseAreaGroupPOTransfer warehouseAreaGroupPOTransfer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(WarehouseAreaGroup warehouseAreaGroup) {
        warehouseAreaGroupPORepository.save(warehouseAreaGroupPOTransfer.toPO(warehouseAreaGroup));
    }

    @Override
    public WarehouseAreaGroup findById(Long id) {
        return warehouseAreaGroupPOTransfer.toDO(warehouseAreaGroupPORepository.findById(id).orElseThrow());
    }

}
