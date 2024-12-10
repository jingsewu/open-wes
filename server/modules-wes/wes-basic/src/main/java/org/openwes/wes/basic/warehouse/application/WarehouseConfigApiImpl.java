package org.openwes.wes.basic.warehouse.application;

import org.openwes.wes.api.basic.IWarehouseConfigApi;
import org.openwes.wes.api.basic.dto.WarehouseConfigDTO;
import org.openwes.wes.basic.warehouse.domain.entity.WarehouseConfig;
import org.openwes.wes.basic.warehouse.domain.repository.WarehouseConfigRepository;
import org.openwes.wes.basic.warehouse.domain.transfer.WarehouseConfigTransfer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@RequiredArgsConstructor
public class WarehouseConfigApiImpl implements IWarehouseConfigApi {

    private final WarehouseConfigRepository warehouseConfigRepository;
    private final WarehouseConfigTransfer warehouseConfigTransfer;

    @Override
    @Transactional
    public void save(WarehouseConfigDTO warehouseConfigDTO) {
        warehouseConfigRepository.save(warehouseConfigTransfer.toDO(warehouseConfigDTO));
    }

    @Override
    public void update(WarehouseConfigDTO warehouseConfigDTO) {
        warehouseConfigRepository.save(warehouseConfigTransfer.toDO(warehouseConfigDTO));
    }

    @Override
    public WarehouseConfigDTO getWarehouseConfig(String warehouseCode) {
        WarehouseConfig warehouseConfig = warehouseConfigRepository.findByWarehouseCode(warehouseCode);
        return warehouseConfigTransfer.toDTO(warehouseConfig);
    }
}
