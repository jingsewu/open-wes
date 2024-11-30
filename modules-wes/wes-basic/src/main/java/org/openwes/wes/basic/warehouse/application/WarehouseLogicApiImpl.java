package org.openwes.wes.basic.warehouse.application;

import static org.openwes.common.utils.exception.code_enum.BasicErrorDescEnum.WAREHOUSE_LOGIC_CONTAINER_LOCATION;

import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.BasicErrorDescEnum;
import org.openwes.wes.api.basic.IWarehouseLogicApi;
import org.openwes.wes.api.basic.dto.WarehouseLogicDTO;
import org.openwes.wes.basic.warehouse.domain.entity.Location;
import org.openwes.wes.basic.warehouse.domain.entity.WarehouseLogic;
import org.openwes.wes.basic.warehouse.domain.repository.LocationRepository;
import org.openwes.wes.basic.warehouse.domain.repository.WarehouseLogicRepository;
import org.openwes.wes.basic.warehouse.domain.transfer.WarehouseLogicTransfer;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated
@RequiredArgsConstructor
public class WarehouseLogicApiImpl implements IWarehouseLogicApi {

    private final WarehouseLogicRepository warehouseLogicRepository;
    private final WarehouseLogicTransfer warehouseLogicTransfer;
    private final LocationRepository locationRepository;

    @Override
    public void save(WarehouseLogicDTO warehouseLogicDTO) {
        WarehouseLogic warehouseLogic = warehouseLogicRepository.findByCode(warehouseLogicDTO.getWarehouseLogicCode());
        if (warehouseLogic != null) {
            throw WmsException.throwWmsException(BasicErrorDescEnum.WAREHOUSE_LOGIC_CODE_REPEATED, warehouseLogicDTO.getWarehouseLogicCode());
        }
        warehouseLogicRepository.save(warehouseLogicTransfer.toDO(warehouseLogicDTO));
    }

    @Override
    public void update(WarehouseLogicDTO warehouseLogicDTO) {
        warehouseLogicRepository.save(warehouseLogicTransfer.toDO(warehouseLogicDTO));
    }

    @Override
    public void enable(Long id) {
        WarehouseLogic warehouseLogic = warehouseLogicRepository.findById(id);
        List<Location> locations = locationRepository.findAllByWarehouseLogicId(warehouseLogic.getId());

        locations.forEach(Location::enable);
        locationRepository.saveAll(locations);

        warehouseLogic.enable();
        warehouseLogicRepository.save(warehouseLogic);
    }

    @Override
    public void disable(Long id) {
        WarehouseLogic warehouseLogic = warehouseLogicRepository.findById(id);
        List<Location> locations = locationRepository.findAllByWarehouseLogicId(warehouseLogic.getId());

        locations.forEach(Location::disable);
        locationRepository.saveAll(locations);

        warehouseLogic.disable();
        warehouseLogicRepository.save(warehouseLogic);
    }

    @Override
    public void delete(Long id) {
        WarehouseLogic warehouseLogic = warehouseLogicRepository.findById(id);
        List<Location> locations = locationRepository.findAllByWarehouseLogicId(warehouseLogic.getId());

        if (CollectionUtils.isNotEmpty(locations)) {
            throw WmsException.throwWmsException(WAREHOUSE_LOGIC_CONTAINER_LOCATION, warehouseLogic.getWarehouseLogicCode());
        }

        warehouseLogic.delete();
        warehouseLogicRepository.save(warehouseLogic);
    }
}
