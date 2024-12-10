package org.openwes.wes.basic.main.data.application;

import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.BasicErrorDescEnum;
import org.openwes.wes.api.main.data.IWarehouseMainDataApi;
import org.openwes.wes.api.main.data.dto.WarehouseMainDataDTO;
import org.openwes.wes.basic.main.data.domain.entity.WarehouseMainData;
import org.openwes.wes.basic.main.data.domain.repository.WarehouseMainDataRepository;
import org.openwes.wes.basic.main.data.domain.transfer.WarehouseMainDataTransfer;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;

@Service
@Primary
@Validated
@DubboService
@RequiredArgsConstructor
public class WarehouseMainDataApiImpl implements IWarehouseMainDataApi {

    private final WarehouseMainDataRepository warehouseRepository;
    private final WarehouseMainDataTransfer warehouseTransfer;

    @Override
    public void createWarehouse(WarehouseMainDataDTO warehouseDTO) {
        WarehouseMainData warehouse = warehouseRepository.findByWarehouseCode(warehouseDTO.getWarehouseCode());
        if (warehouseDTO.getId() == null && warehouse != null) {
            throw WmsException.throwWmsException(BasicErrorDescEnum.WAREHOUSE_CODE_EXIST, warehouse.getWarehouseCode());
        }
        warehouseRepository.save(warehouseTransfer.toDO(warehouseDTO));
    }

    @Override
    public void updateWarehouse(WarehouseMainDataDTO warehouseDTO) {
        warehouseRepository.save(warehouseTransfer.toDO(warehouseDTO));
    }

    @Override
    public WarehouseMainDataDTO getWarehouse(String warehouseCode) {
        return warehouseTransfer.toDTO(warehouseRepository.findByWarehouseCode(warehouseCode));
    }

    @Override
    public Collection<WarehouseMainDataDTO> getWarehouses(Collection<String> warehouseCodes) {
        return warehouseTransfer.toDOs(warehouseRepository.findAllByWarehouseCodes(warehouseCodes));
    }
}
